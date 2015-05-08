package de.h_brs.webeng.whiteboard.backend.actors;

import javax.websocket.RemoteEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.h_brs.webeng.whiteboard.backend.domain.FinishedShape;
import akka.actor.UntypedActor;

public class RemoteEndpointSender extends UntypedActor {
	
	private static final Logger LOG = LoggerFactory.getLogger(RemoteEndpointSender.class);
	
	private final RemoteEndpoint.Async endpoint;
	
	public RemoteEndpointSender(RemoteEndpoint.Async endpoint) {
		LOG.debug("adding endpoint.");
		this.endpoint = endpoint;
	}

	@Override
	public void onReceive(Object message) throws Exception {
		LOG.debug("sending to client finishedShape");
		if (message instanceof FinishedShape) {
			// TODO: buffer and send batch?
			endpoint.sendObject(message);
		}
	}

}
