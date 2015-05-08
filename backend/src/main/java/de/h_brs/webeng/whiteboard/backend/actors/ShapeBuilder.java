package de.h_brs.webeng.whiteboard.backend.actors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;
import de.h_brs.webeng.whiteboard.backend.domain.DrawEvent;
import de.h_brs.webeng.whiteboard.backend.domain.FinishedShape;

class ShapeBuilder extends UntypedActor {
	
	private static final Logger LOG = LoggerFactory.getLogger(ShapeBuilder.class);
	
	private final Long whiteboardId;
	
	public ShapeBuilder(Long whiteboardId) {
		this.whiteboardId = whiteboardId;
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof DrawEvent) {
			final DrawEvent event = (DrawEvent) message;
			switch (event.getType()) {
			case START:
				LOG.info("started drawing on whiteboard {}: {}", whiteboardId, event.getShape().name());
				getSender().tell(new FinishedShape(), getSelf());
				break;
			case UPDATE:
				LOG.info("updated drawing on whiteboard {}: {}", whiteboardId, event.getShape().name());
				break;
			case FINISH:
				LOG.info("finished drawing on whiteboard {}: {}", whiteboardId, event.getShape().name());
				// TODO calculate final Shape and pass it to persisting actor
				getSender().tell(new FinishedShape(), getSelf());
				break;
			case CANCEL:
				LOG.info("canceled drawing on whiteboard {}: {}", whiteboardId, event.getShape().name());
				break;
			}
		} else {
			unhandled(message);
		}
	}

}
