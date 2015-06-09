package de.h_brs.webeng.whiteboard.backend.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import de.h_brs.webeng.whiteboard.backend.dao.*;
import de.h_brs.webeng.whiteboard.backend.domain.*;
import de.h_brs.webeng.whiteboard.backend.dao.exception.*;



public class RedisShapeDAO implements ShapeDAO {

	private Jedis jedis = MyJedisPool.getPool("localhost").getResource();
	
	public static final String FIELD_POS_X 			= "x";
	public static final String FIELD_POS_Y 			= "y";
	public static final String FIELD_RECT_WIDTH 	= "w";
	public static final String FIELD_RECT_HEIGHT	= "h";
	public static final String FIELD_CIRCLE_RADIUS 	= "r";
	public static final String FIELD_SHAPE_TYPE 	= "type";
	
	
	/**
	 * Inserts a new Rectangle-Instance to the database. 
	 * NOTE: A shape in general can only be inserted if it belongs to a Whiteboard and
	 * an Artist (User). Therefore Shape-Instance must provide these information,
	 * otherwise an IllegalShapeException will be thrown.
	 */
	public void insertRect(Rectangle rect) 
			throws UserNotFoundException, WhiteboardNotFoundException, IllegalShapeException 
	{
		if(rect != null && rect.getUsername() != null && rect.getWbID() != null) {
			UserDAO userDAO = new RedisUserDAO();
			WhiteboardDAO wbDAO = new RedisWhiteboardDAO();
			
			if(!userDAO.userExists(rect.getUsername()))
				throw new UserNotFoundException();
			if(!wbDAO.whiteboardExists(rect.getWbID()))
				throw new WhiteboardNotFoundException();
			
			Transaction tx = jedis.multi();
			Map<String, String> rectProperties = new HashMap<String, String>();
			rectProperties.put(FIELD_POS_X, String.valueOf(rect.getCoordinate().getX()));
			rectProperties.put(FIELD_POS_Y, String.valueOf(rect.getCoordinate().getY()));
			rectProperties.put(FIELD_RECT_WIDTH, String.valueOf(rect.getWidth()));
			rectProperties.put(FIELD_RECT_HEIGHT, String.valueOf(rect.getHeight()));
			rectProperties.put(FIELD_SHAPE_TYPE, String.valueOf("r"));
			
			tx.hmset(rect.getShapeKey(), rectProperties);
			tx.sadd("whiteboard:"+rect.getWbID()+":user:"+rect.getUsername()+":shapes", rect.getUuid().toString());
			tx.exec();
		}
		else
			throw new IllegalShapeException();
	}
	
	/**
	 * Inserts a new Circle-Instance to the database. 
	 * NOTE: A shape in general can only be inserted if it belongs to a Whiteboard and
	 * an Artist (User). Therefore Shape-Instance must provide these information,
	 * otherwise an IllegalShapeException will be thrown.
	 */
	public void insertCircle(Circle circle) 
			throws UserNotFoundException, WhiteboardNotFoundException, IllegalShapeException 
	{
		if(circle != null && circle.getUsername() != null && circle.getWbID() != null) {
			UserDAO userDAO = new RedisUserDAO();
			WhiteboardDAO wbDAO = new RedisWhiteboardDAO();
			
			if(!userDAO.userExists(circle.getUsername()))
				throw new UserNotFoundException();
			if(!wbDAO.whiteboardExists(circle.getWbID()))
				throw new WhiteboardNotFoundException();
			
			Transaction tx = jedis.multi();
			Map<String, String> rectProperties = new HashMap<String, String>();
			rectProperties.put(FIELD_POS_X, String.valueOf(circle.getCoordinate().getX()));
			rectProperties.put(FIELD_POS_Y, String.valueOf(circle.getCoordinate().getY()));
			rectProperties.put(FIELD_CIRCLE_RADIUS, String.valueOf(circle.getRadius()));
			rectProperties.put(FIELD_SHAPE_TYPE, String.valueOf("c"));
			
			tx.hmset(circle.getShapeKey(), rectProperties);
			tx.sadd("whiteboard:"+circle.getWbID()+":user:"+circle.getUsername()+":shapes", circle.getUuid().toString());
			tx.exec();
		}
		else
			throw new IllegalShapeException();
	}

	@Override
	public boolean updateShape(Shape shape) {
		// TODO Not so important at the moment
		return false;
	}

	@Override
	public boolean deleteShape(Shape shape) {
		// TODO Not so important at the moment
		return false;
	}

	@Override
	public Shape findShapeByUUID(UUID uuid) {
		// TODO Not so important at the moment
		return null;
	}

	/**
	 * Fetches all shapes and paths from the whiteboard which users have drawn in the past.
	 * @param whiteboard The whiteboard - only a valid WB-ID is needed
	 * @return The method returns a Hashmap which gives access to all the shapes from the
	 * whiteboard. You get access to the shapes via username-keys. 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public HashMap<String, List<Shape>> findAllShapesFromWB(Whiteboard whiteboard) 
			throws WhiteboardNotFoundException 
	{
		// TODO right now only shapes (Rectangles and Circles) are fetchable! Next step: fetching path!! 
		
		UserDAO userDAO = new RedisUserDAO();
		WhiteboardDAO wbDAO = new RedisWhiteboardDAO();
		
		// Check if Whiteboard can be found in DB
		if(!wbDAO.whiteboardExists(whiteboard))
			throw new WhiteboardNotFoundException();
		
		// Fetch all registered useres from the whiteboard 
		List<User> registeredUseres = userDAO.findAllUsersFromWB(whiteboard);
		Long wbID = whiteboard.getWbid();
		
		// Hashmap contains the usernames as keys which refer to all the shapes they have drawn
		// in this Whiteboard
		HashMap<String, List<String>> hashMap = new HashMap<String, List<String>>();
		
		// Fetch all shape-UUIDs from every registered user of the whiteboard
		for(int x=0; x<registeredUseres.size(); ++x) {
			String shapeKeys = "whiteboard:" + wbID + ":user:" 
					+registeredUseres.get(x).getUsername()+ ":shapes";
			
			List<String> shapeUUIDs = new ArrayList<String>(jedis.smembers(shapeKeys));
			hashMap.put(registeredUseres.get(x).getUsername(), shapeUUIDs);
		}

		Pipeline p = jedis.pipelined();
		
		// PIPELINED: Fetch all Shapes with out previously fetched UUIDs
		Response<Map<String, String>>[][] res = new Response[registeredUseres.size()][];
		for(int x = 0; x < registeredUseres.size(); ++x) {
			String username = registeredUseres.get(x).getUsername();
			List<String> shapeUUIDs = hashMap.get(username);
			
			res[x] = new Response[shapeUUIDs.size()];
			
			for(int y = 0; y < res[x].length; ++y) {
				res[x][y] = p.hgetAll("whiteboard:" + wbID  + ":shape:" + shapeUUIDs.get(y));
			}
		}

		p.sync();
		
		// When piplined fetching is ready, store the Shape-Objects in the Hashmap
		HashMap<String, List<Shape>> userShapes = new HashMap<String, List<Shape>>();
		for(int x = 0; x < registeredUseres.size(); ++x) {
			List<Shape> shapes = new ArrayList<Shape>();
			for(int y = 0; y < res[x].length; ++y) {
				Map<String, String> properties = res[x][y].get();
				
				if(properties.get(FIELD_SHAPE_TYPE).equals("c")) {
					int radius = Integer.parseInt(properties.get(FIELD_CIRCLE_RADIUS));
					int posX = Integer.parseInt(properties.get(FIELD_POS_X));
					int posY = Integer.parseInt(properties.get(FIELD_POS_Y));
					
					Shape c = new Circle(registeredUseres.get(x), whiteboard, 
							new Point(posX, posY), radius);
					
					shapes.add(c);
				} 
				else if(properties.get(FIELD_SHAPE_TYPE).equals("r")) {
					int width = Integer.parseInt(properties.get(FIELD_RECT_WIDTH));
					int height = Integer.parseInt(properties.get(FIELD_RECT_HEIGHT));
					int posX = Integer.parseInt(properties.get(FIELD_POS_X));
					int posY = Integer.parseInt(properties.get(FIELD_POS_Y));
					
					Shape r = new Rectangle(registeredUseres.get(x), whiteboard, 
							new Point(posX, posY), width, height);
					
					shapes.add(r);
				}
			}
			userShapes.put(registeredUseres.get(x).getUsername(), shapes);
		}
		
		return userShapes;
	}

}
