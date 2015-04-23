package de.h_brs.webeng.whiteboard.backend;

import java.io.IOException;
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
import de.h_brs.webeng.whiteboard.backend.domain.DrawEvent;
import akka.actor.ActorSystem;

@ServerEndpoint(value = "/drawings/{id}", decoders = { DrawEventDecoder.class })
public class DrawingEndpoint {

	private static final Logger LOG = LoggerFactory.getLogger(DrawingEndpoint.class);

	private final ActorSystem system = ActorSystem.create("DrawingSystem");

	@OnOpen
	public void onOpen(Session session) {
		LOG.info("Connected ... " + session.getId());
		// TODO: add session.getAsyncRemote() to actor responsible for sending responses.
	}

	@OnMessage
	public void processDrawEvent(DrawEvent event, Session session, @PathParam("id") String whiteboardId) throws IOException {
		// TODO: pass event to dispatching actor for whiteboard #whiteboardId
	}

	@OnError
    public void onError(Throwable t, Session session) {
    	try {
			session.close(new CloseReason(CloseCodes.UNEXPECTED_CONDITION , t.getMessage()));
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
