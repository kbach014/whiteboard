package de.h_brs.webeng.whiteboard.backend;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import de.h_brs.webeng.whiteboard.backend.actors.GoodbyeMessage;
import de.h_brs.webeng.whiteboard.backend.actors.HelloMessage;
import de.h_brs.webeng.whiteboard.backend.actors.RemoteEndpointSender;
import de.h_brs.webeng.whiteboard.backend.actors.WhiteboardHandler;
import de.h_brs.webeng.whiteboard.backend.dto.DrawEventDto;

@ServerEndpoint(value = "/drawings/{id}", decoders = { DrawEventsDecoder.class }, encoders = { DrawEventsEncoder.class })
public class DrawingEndpoint {

	private static final Logger LOG = LoggerFactory.getLogger(DrawingEndpoint.class);

	private static final ActorSystem SYSTEM = ActorSystem.create("DrawingSystem");
	private static final ConcurrentMap<Long, ActorRef> WHITEBOARD_HANDLERS = new ConcurrentHashMap<>();

	@OnOpen
	public void onOpen(Session session, @PathParam("id") Long whiteboardId) {
		LOG.info("Connected session " + session.getId() + " for user " + session.getUserPrincipal().getName() + " to whiteboard " + whiteboardId);

		final ActorRef ref = SYSTEM.actorOf(Props.create(RemoteEndpointSender.class, session.getAsyncRemote()), "upstream_" + session.getId());
		getHandlerForWhiteboard(whiteboardId).tell(new HelloMessage(session.getUserPrincipal().getName()), ref);
		session.getUserProperties().put("sender", ref);
	}

	@OnMessage
	public void processDrawEvent(Collection<DrawEventDto> events, Session session, @PathParam("id") Long whiteboardId) throws IOException {
		final ActorRef handler = getHandlerForWhiteboard(whiteboardId);
		events.forEach(event -> {
			event.setUsername(session.getUserPrincipal().getName());
			handler.tell(event, ActorRef.noSender());
		});
	}

	@OnError
	public void onError(Throwable t, Session session) {
		try {
			if (session.isOpen()) {
				try {
					session.close(new CloseReason(CloseCodes.UNEXPECTED_CONDITION, t.getMessage()));
				} catch (IllegalStateException e) {
					LOG.warn("Closing session failed: " + e.getMessage());
				}
			}
		} catch (IOException e) {
			LOG.error("Error during exception handling.", e);
		}
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason, @PathParam("id") Long whiteboardId) {
		final ActorRef ref = (ActorRef) session.getUserProperties().remove("sender");
		if (ref != null) {
			getHandlerForWhiteboard(whiteboardId).tell(new GoodbyeMessage(session.getUserPrincipal().getName()), ref);
		}
		LOG.debug(String.format("Session %s closed because of %s", session.getId(), closeReason));
	}

	/**
	 * Atomically gets the actor in charge of handling messages for a given whiteboard.
	 * 
	 * @param whiteboardId
	 *            Which whiteboard
	 * @return Non-null actor (creates new actor, if not yet existing).
	 */
	private ActorRef getHandlerForWhiteboard(Long whiteboardId) {
		return WHITEBOARD_HANDLERS.computeIfAbsent(whiteboardId, this::createWhiteboardHandler);
	}

	/**
	 * Guaranteed to be called only once per whiteboardId (see {@link ConcurrentHashMap#computeIfAbsent(Object, java.util.function.Function)} ).
	 * 
	 * @param whiteboardId
	 *            For which whiteboard to create an adapter
	 * @return new adapter for the given whiteboardId
	 */
	private ActorRef createWhiteboardHandler(Long whiteboardId) {
		return SYSTEM.actorOf(Props.create(WhiteboardHandler.class, whiteboardId), "whiteboard_" + String.valueOf(whiteboardId));
	}

}
