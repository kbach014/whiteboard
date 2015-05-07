package de.h_brs.webeng.whiteboard.backend;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import de.h_brs.webeng.whiteboard.backend.actors.WhiteboardEventDispatcher;
import de.h_brs.webeng.whiteboard.backend.domain.DrawEvent;

@ServerEndpoint(value = "/drawings/{id}", decoders = { DrawEventsDecoder.class })
public class DrawingEndpoint {

	private static final Logger LOG = LoggerFactory.getLogger(DrawingEndpoint.class);

	private final ActorSystem system = ActorSystem.create("DrawingSystem");
	private final ConcurrentMap<Long, ActorRef> workers = new ConcurrentHashMap<>();

	@OnOpen
	public void onOpen(Session session, @PathParam("id") Long whiteboardId) {
		LOG.info("Connected session " + session.getId() + " for user " + session.getUserPrincipal().getName() + " to whiteboard " + whiteboardId);
		// TODO: is session.getUserPrincipal() authorized to draw on this whiteboard?
		// TODO: add session.getAsyncRemote() to actor responsible for sending responses.
	}

	@OnMessage
	public void processDrawEvent(Collection<DrawEvent> events, Session session, @PathParam("id") Long whiteboardId) throws IOException {
		// TODO: use router to decide, which whiteboard to use
		final ActorRef ref = getOrCreateWorkerForShape(whiteboardId);
		events.forEach(event -> ref.tell(event, ActorRef.noSender()));
	}
	
	// TODO: use Props.create(...).withRouter to decide, which whiteboard to use instead of "workers" Map.
	private ActorRef getOrCreateWorkerForShape(Long whiteboardId) {
		if (workers.containsKey(whiteboardId)) {
			return workers.get(whiteboardId);
		} else {
			final ActorRef ref = system.actorOf(Props.create(WhiteboardEventDispatcher.class, whiteboardId));
			workers.putIfAbsent(whiteboardId, ref);
			return ref;
		}
	}

	@OnError
    public void onError(Throwable t, Session session) {
    	try {
    		if (session.isOpen()) {
    			session.close(new CloseReason(CloseCodes.UNEXPECTED_CONDITION , t.getMessage()));
    		}
		} catch (IOException e) {
			LOG.error("Error during exception handling.", e);
		}
    }

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		// TODO: remove asyncRemote from actor
		LOG.info(String.format("Session %s closed because of %s", session.getId(), closeReason));
	}

	@PreDestroy
	private void shutdown() {
		system.shutdown();
		system.awaitTermination(Duration.create(15, TimeUnit.SECONDS));
	}

}
