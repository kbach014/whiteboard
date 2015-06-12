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
import de.h_brs.webeng.whiteboard.backend.actors.Message;
import de.h_brs.webeng.whiteboard.backend.actors.RemoteEndpointSender;
import de.h_brs.webeng.whiteboard.backend.actors.WhiteboardHandler;
import de.h_brs.webeng.whiteboard.backend.dao.UserDAO;
import de.h_brs.webeng.whiteboard.backend.dao.exception.*;
import de.h_brs.webeng.whiteboard.backend.dao.impl.RedisUserDAO;
import de.h_brs.webeng.whiteboard.backend.domain.DrawEvent;

@ServerEndpoint(value = "/drawings/{id}", decoders = { DrawEventsDecoder.class }, encoders = {FinishedShapeEncoder.class})
public class DrawingEndpoint {

	private static final Logger LOG = LoggerFactory.getLogger(DrawingEndpoint.class);

	private final ActorSystem system = ActorSystem.create("DrawingSystem");
	private final ConcurrentMap<Long, ActorRef> whiteboardHandlers = new ConcurrentHashMap<>();

	@OnOpen
	public void onOpen(Session session, @PathParam("id") Long whiteboardId) {
		LOG.info("Connected session " + session.getId() + " for user " + session.getUserPrincipal().getName() + " to whiteboard " + whiteboardId);	
		UserDAO userDAO = new RedisUserDAO();
		
		try {
			if(userDAO.userHasWhiteboard(session.getUserPrincipal().getName(), whiteboardId)) {
				final ActorRef ref = system.actorOf(Props.create(RemoteEndpointSender.class, session.getAsyncRemote()), session.getId());
				getHandlerForWhiteboard(whiteboardId).tell(Message.HELLO_WHITEBOARD, ref);
				session.getUserProperties().put("sender", ref);
			}
		} 
		catch(UserNotFoundException e) {
			// TODO Handle Exception
		}
		catch(WhiteboardNotFoundException e) {
			// TODO Handle Exception
		}
	}

	@OnMessage
	public void processDrawEvent(Collection<DrawEvent> events, Session session, @PathParam("id") Long whiteboardId) throws IOException {
		final ActorRef handler = getHandlerForWhiteboard(whiteboardId);
		events.forEach(event -> handler.tell(event, ActorRef.noSender()));
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
	public void onClose(Session session, CloseReason closeReason, @PathParam("id") Long whiteboardId) {
		final ActorRef ref = (ActorRef) session.getUserProperties().remove("sender");
		if (ref != null) {
			getHandlerForWhiteboard(whiteboardId).tell(Message.GOODBYE_WHITEBOARD, ref);
		}
		LOG.info(String.format("Session %s closed because of %s", session.getId(), closeReason));
	}

	@PreDestroy
	private void shutdown() {
		system.shutdown();
		system.awaitTermination(Duration.create(15, TimeUnit.SECONDS));
	}
	
	/**
	 * Atomically gets the actor in charge of handling messages for a given whiteboard.
	 * @param whiteboardId Which whiteboard
	 * @return Non-null actor (creates new actor, if not yet existing).
	 */
	private ActorRef getHandlerForWhiteboard(Long whiteboardId) {
		whiteboardHandlers.computeIfAbsent(whiteboardId, this::createWhiteboardHandler);
		return whiteboardHandlers.get(whiteboardId);
	}
	
	/**
	 * Guaranteed to be called only once per whiteboardId (see {@link ConcurrentHashMap#computeIfAbsent(Object, java.util.function.Function)}).
	 * @param whiteboardId For which whiteboard to create an adapter
	 * @return new adapter for the given whiteboardId
	 */
	private ActorRef createWhiteboardHandler(Long whiteboardId) {
		return system.actorOf(Props.create(WhiteboardHandler.class, whiteboardId), String.valueOf(whiteboardId));
	}
	

}
