package de.h_brs.webeng.whiteboard.backend.actors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Router;
import de.h_brs.webeng.whiteboard.backend.dao.ShapeDAO;
import de.h_brs.webeng.whiteboard.backend.dao.impl.RedisShapeDAO;
import de.h_brs.webeng.whiteboard.backend.domain.Color;
import de.h_brs.webeng.whiteboard.backend.domain.Path;
import de.h_brs.webeng.whiteboard.backend.domain.Rectangle;
import de.h_brs.webeng.whiteboard.backend.domain.Shape;
import de.h_brs.webeng.whiteboard.backend.domain.ShapeType;
import de.h_brs.webeng.whiteboard.backend.domain.Whiteboard;
import de.h_brs.webeng.whiteboard.backend.dto.DrawEventDto;
import de.h_brs.webeng.whiteboard.backend.dto.DrawEventDto.EventType;
import de.h_brs.webeng.whiteboard.backend.dto.ShapeDto;

public class WhiteboardHandler extends UntypedActor {
	
	private static final Logger LOG = LoggerFactory.getLogger(WhiteboardHandler.class);
		
	private final Long whiteboardId;
	private Router upstreamRouter = new Router(BroadcastRoutingLogic.apply());
	private Map<String, Color> sessionColors = new HashMap<>();
	private Integer connectedClients = 0;
	
	public WhiteboardHandler(Long whiteboardId) {
		LOG.info("initialized whiteboard handler #{}: ", whiteboardId, this);
		this.whiteboardId = whiteboardId;
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof DrawEventDto) {
			final DrawEventDto event = (DrawEventDto) message;
			this.onReceiveDrawEvent(event);
		} else if (message instanceof HelloMessage) {
			final HelloMessage hello = (HelloMessage) message;
			this.onReceiveHello(hello);
		} else if (message instanceof GoodbyeMessage) {
			final GoodbyeMessage goodbye = (GoodbyeMessage) message;
			this.onReceiveGoodbye(goodbye);
		} else {
			unhandled(message);
		}
	}
	
	private void onReceiveHello(HelloMessage hello) throws Exception {
		connectedClients++;
		upstreamRouter = upstreamRouter.addRoutee(getSender());
		
		ShapeDAO shapeDAO = new RedisShapeDAO();
		
		List<Shape> allShapes = shapeDAO.findAllShapesFromWB(new Whiteboard(whiteboardId));
		
		for(Shape shape : allShapes) {
			final DrawEventDto event = new DrawEventDto();
			
			if(shape.getType().equals(ShapeType.RECT)) {
				Rectangle rect = (Rectangle) shape;
				
				event.setType(EventType.FINISH);
				event.setShape(new ShapeDto());
				event.getShape().setUuid(rect.getUuid());
				event.getShape().setType(ShapeType.RECT);
				event.getShape().setFinished(true);
				event.getShape().setColor(getColorForUser(rect.getUsername()));
				event.getShape().setP1(rect.getP1());
				event.getShape().setP2(rect.getP2());
				getSender().tell(event, getSelf());
			} else if(shape.getType().equals(ShapeType.PATH)) {
				Path path = (Path) shape;
				
				event.setType(EventType.FINISH);
				event.setShape(new ShapeDto());
				event.getShape().setUuid(path.getUuid());
				event.getShape().setType(ShapeType.PATH);
				event.getShape().setFinished(true);
				event.getShape().setColor(getColorForUser(path.getUsername()));
				event.getShape().setPoints(path.getPoints());
				getSender().tell(event, getSelf());
			}
			
		}
		
		LOG.debug("added receiver {} to whiteboard {}", hello.getUsername(), whiteboardId);
	}
	
	private void onReceiveGoodbye(GoodbyeMessage goodbye) throws Exception {
		connectedClients--;
		upstreamRouter = upstreamRouter.removeRoutee(getSender());
		LOG.debug("removed receiver {} from whiteboard {}", goodbye.getUsername(), whiteboardId);
	}
	
	private void onReceiveDrawEvent(DrawEventDto event) throws Exception {
		event.getShape().setColor(getColorForUser(event.getUsername()));
		if (event.getType().equals(EventType.FINISH)) {
			if(event.getShape().getType().equals(ShapeType.RECT)) {
				ShapeDto shapeDto = event.getShape();
				
				Rectangle rect = new Rectangle(event.getUsername(), String.valueOf(whiteboardId), shapeDto.getP1(), shapeDto.getP2());
				ShapeDAO shapeDAO = new RedisShapeDAO(); 
				shapeDAO.insertRect(rect);
			}
			else if(event.getShape().getType().equals(ShapeType.PATH)) {
				ShapeDto shapeDto = event.getShape();
				
				Path path = new Path(event.getUsername(), String.valueOf(whiteboardId), shapeDto.getPoints());
				ShapeDAO shapeDAO = new RedisShapeDAO(); 
				shapeDAO.insertPath(path);
			}
			// TODO restliche Shapes überprüfen
		}
		LOG.debug("sending event on whiteboard {} to {} receivers", whiteboardId, connectedClients);
		upstreamRouter.route(event, getSelf());
	}
	
	private Color getColorForUser(String username) {
		return sessionColors.computeIfAbsent(username, name -> {
			return Color.values()[sessionColors.size() % Color.values().length];
		});
	}

}
