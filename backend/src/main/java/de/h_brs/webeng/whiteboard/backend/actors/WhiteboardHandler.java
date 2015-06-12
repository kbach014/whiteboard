package de.h_brs.webeng.whiteboard.backend.actors;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Router;
import de.h_brs.webeng.whiteboard.backend.dao.ShapeDAO;
import de.h_brs.webeng.whiteboard.backend.dao.impl.RedisShapeDAO;
import de.h_brs.webeng.whiteboard.backend.domain.Color;
import de.h_brs.webeng.whiteboard.backend.domain.Rectangle;
import de.h_brs.webeng.whiteboard.backend.domain.ShapeType;
import de.h_brs.webeng.whiteboard.backend.dto.DrawEventDto;
import de.h_brs.webeng.whiteboard.backend.dto.DrawEventDto.EventType;
import de.h_brs.webeng.whiteboard.backend.dto.ShapeDto;

public class WhiteboardHandler extends UntypedActor {
	
	private static final Logger LOG = LoggerFactory.getLogger(WhiteboardHandler.class);
		
	private final Long whiteboardId;
	private Router upstreamRouter = new Router(BroadcastRoutingLogic.apply());
	private Map<String, Color> sessionColors = new HashMap<>();
	
	public WhiteboardHandler(Long whiteboardId) {
		this.whiteboardId = whiteboardId;
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof DrawEventDto) {
			final DrawEventDto event = (DrawEventDto) message;
			this.onReceiveDrawEvent(event);
		} else if (message instanceof HelloMessage) {
			final HelloMessage hello = (HelloMessage) message;
			final Color myColor = Color.values()[sessionColors.size() % Color.values().length];
			sessionColors.put(hello.getSessionId(), myColor);
			upstreamRouter = upstreamRouter.addRoutee(getSender());
			LOG.debug("added receiver to whiteboard {}", whiteboardId);
		} else if (message instanceof GoodbyeMessage) {
			final GoodbyeMessage goodbye = (GoodbyeMessage) message;
			sessionColors.remove(goodbye.getSessionId());
			upstreamRouter = upstreamRouter.removeRoutee(getSender());
			LOG.debug("removed receiver to whiteboard {}", whiteboardId);
		} else {
			unhandled(message);
		}
	}
	
	private void onReceiveDrawEvent(DrawEventDto event) throws Exception {
		event.getShape().setColor(sessionColors.get(event.getSessionId()));
		if (event.getType().equals(EventType.FINISH)) {
			if(event.getShape().getType() == ShapeType.RECT) {
				ShapeDto shapeDto = event.getShape();
				
				// TODO Kacke, an dieser Stelle brauchen wir doch den User, denn sonst weiß das Shape 
				// nicht von dem es gezeichnet wrude
				// Am besten durch das DrawEventDto gekapselt übergeben..
				Rectangle rect = new Rectangle("", String.valueOf(whiteboardId), shapeDto.getP1(), shapeDto.getP2());
				ShapeDAO shapeDAO = new RedisShapeDAO(); 
				//shapeDAO.insertRect(rect);
			}
			// TODO restliche Shapes überprüfen
		}
		upstreamRouter.route(event, getSelf());
	}

}
