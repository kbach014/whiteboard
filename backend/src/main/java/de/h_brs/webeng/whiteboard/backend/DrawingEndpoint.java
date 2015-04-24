package de.h_brs.webeng.whiteboard.backend;

import java.io.IOException;
import java.util.Collection;
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
import akka.actor.ActorSystem;
import de.h_brs.webeng.whiteboard.backend.domain.DrawEvent;

@ServerEndpoint(value = "/drawings/{id}", decoders = { DrawEventsDecoder.class })
public class DrawingEndpoint {

	private static final Logger LOG = LoggerFactory.getLogger(DrawingEndpoint.class);

	private final ActorSystem system = ActorSystem.create("DrawingSystem");

	@OnOpen
	public void onOpen(Session session, @PathParam("id") String whiteboardId) {
		LOG.info("Connected session " + session.getId() + " for user " + session.getUserPrincipal().getName() + " to whiteboard " + whiteboardId);
		// TODO: is session.getUserPrincipal() authorized to draw on this whiteboard?
		// TODO: add session.getAsyncRemote() to actor responsible for sending responses.
	}

	@OnMessage
	public void processDrawEvent(Collection<DrawEvent> events, Session session, @PathParam("id") String whiteboardId) throws IOException {
		// TODO: pass events to dispatching actor for whiteboard #whiteboardId
		for (DrawEvent event : events) {
			LOG.info("Received event: " + event);
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
