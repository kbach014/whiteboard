package de.h_brs.webeng.whiteboard.backend.actors;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Router;
import de.h_brs.webeng.whiteboard.backend.domain.DrawEvent;
import de.h_brs.webeng.whiteboard.backend.domain.FinishedShape;

public class WhiteboardHandler extends UntypedActor {
	
	private static final Logger LOG = LoggerFactory.getLogger(ShapeBuilder.class);
		
	private final Long whiteboardId;
	private final Map<UUID, ActorRef> workers = new HashMap<>();
	private Router upstreamRouter = new Router(BroadcastRoutingLogic.apply());
	
	public WhiteboardHandler(Long whiteboardId) {
		this.whiteboardId = whiteboardId;
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof DrawEvent) {
			final DrawEvent event = (DrawEvent) message;
			final ActorRef worker = getWorkerForShape(event.getShapeUuid());
			worker.tell(message, getSelf());
		} else if (message instanceof FinishedShape) {
			upstreamRouter.route(message, getSelf());
		} else if (Message.HELLO_WHITEBOARD.equals(message)) {
			upstreamRouter = upstreamRouter.addRoutee(getSender());
			LOG.debug("added receiver of whiteboard updates");
		} else if (Message.GOODBYE_WHITEBOARD.equals(message)) {
			upstreamRouter = upstreamRouter.removeRoutee(getSender());
			LOG.debug("removed receiver of whiteboard updates");
		} else {
			unhandled(message);
		}
	}
	
	private ActorRef getWorkerForShape(UUID shapeUuid) {
		// no synchronization needed here, has actors handles one message after another:
		if (workers.containsKey(shapeUuid)) {
			return workers.get(shapeUuid);
		} else {
			final ActorRef ref = getContext().actorOf(Props.create(ShapeBuilder.class, this::createWorker), shapeUuid.toString());
			workers.put(shapeUuid, ref);
			return ref;
		}
	}
	
	public ShapeBuilder createWorker() throws Exception {
		return new ShapeBuilder(whiteboardId);
	}

}
