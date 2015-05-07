package de.h_brs.webeng.whiteboard.backend.actors;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import de.h_brs.webeng.whiteboard.backend.domain.DrawEvent;

public class WhiteboardEventDispatcher extends UntypedActor {
	
	private static final Logger LOG = LoggerFactory.getLogger(ShapeBuilder.class);
		
	private final Long whiteboardId;
	private final Map<UUID, ActorRef> workers = new HashMap<>();
	
	public WhiteboardEventDispatcher(Long whiteboardId) {
		this.whiteboardId = whiteboardId;
		LOG.debug("init WhiteboardEventDispatcher");
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof DrawEvent) {
			final DrawEvent event = (DrawEvent) message;
			final ActorRef worker = getOrCreateWorkerForShape(event.getShapeUuid());
			worker.forward(message, getContext());
		} else {
			unhandled(message);
		}
	}
	
	private ActorRef getOrCreateWorkerForShape(UUID shapeUuid) {
		// no synchronization needed here, has actors handles one message after another:
		if (workers.containsKey(shapeUuid)) {
			return workers.get(shapeUuid);
		} else {
			final ActorRef ref = getContext().actorOf(Props.create(ShapeBuilder.class, this::createWorker));
			workers.put(shapeUuid, ref);
			return ref;
		}
	}
	
	public ShapeBuilder createWorker() throws Exception {
		return new ShapeBuilder(whiteboardId);
	}

}
