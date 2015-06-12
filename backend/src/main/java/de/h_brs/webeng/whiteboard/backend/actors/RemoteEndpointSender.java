package de.h_brs.webeng.whiteboard.backend.actors;

import javax.websocket.RemoteEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;
import de.h_brs.webeng.whiteboard.backend.dto.DrawEventDto;

public class RemoteEndpointSender extends UntypedActor {
	
	private static final Logger LOG = LoggerFactory.getLogger(RemoteEndpointSender.class);
	
	private final RemoteEndpoint.Async endpoint;
	
	public RemoteEndpointSender(RemoteEndpoint.Async endpoint) {
		this.endpoint = endpoint;
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof DrawEventDto) {
			// TODO: buffer and send batch?
			endpoint.sendObject(message);
		}
	}

}
