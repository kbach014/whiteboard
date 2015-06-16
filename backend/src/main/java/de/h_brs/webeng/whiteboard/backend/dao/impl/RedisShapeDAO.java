package de.h_brs.webeng.whiteboard.backend.dao.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import de.h_brs.webeng.whiteboard.backend.dao.*;
import de.h_brs.webeng.whiteboard.backend.domain.*;
import de.h_brs.webeng.whiteboard.backend.dao.exception.*;

public class RedisShapeDAO implements ShapeDAO {
	public static final String FIELD_POS_X1 = "x1";
	public static final String FIELD_POS_Y1 = "y1";
	public static final String FIELD_POS_X2 = "x2";
	public static final String FIELD_POS_Y2 = "y2";
	public static final String FIELD_PATH_POINTS 	= "points";
	public static final String FIELD_CIRCLE_RADIUS 	= "r";
	public static final String FIELD_SHAPE_TYPE 	= "type";
	public static final String FIELD_CREATOR 		= "creator";

	/**
	 * Inserts a new Rectangle-Instance to the database. NOTE: A shape in general can only be inserted if it belongs to a Whiteboard and an Artist (User). Therefore Shape-Instance must provide these information,
	 * otherwise an IllegalShapeException will be thrown.
	 */
	public void insertRect(Rectangle rect) throws UserNotFoundException, WhiteboardNotFoundException, IllegalShapeException {
		if (rect != null && rect.getUsername() != null && rect.getWbID() != null) {
			try (Jedis jedis = MyJedisPool.getPool("localhost").getResource()) {
				UserDAO userDAO = new RedisUserDAO();
				WhiteboardDAO wbDAO = new RedisWhiteboardDAO();

				if (!userDAO.userExists(rect.getUsername()))
					throw new UserNotFoundException();
				if (!wbDAO.whiteboardExists(rect.getWbID()))
					throw new WhiteboardNotFoundException();

				Transaction tx = jedis.multi();
				Map<String, String> rectProperties = new HashMap<String, String>();
				rectProperties.put(FIELD_POS_X1, String.valueOf(rect.getP1().getX()));
				rectProperties.put(FIELD_POS_Y1, String.valueOf(rect.getP1().getY()));
				rectProperties.put(FIELD_POS_X2, String.valueOf(rect.getP2().getX()));
				rectProperties.put(FIELD_POS_Y2, String.valueOf(rect.getP2().getY()));

				rectProperties.put(FIELD_SHAPE_TYPE, "r");

				tx.hmset(rect.getShapeKey(), rectProperties);
				tx.sadd("whiteboard:" + rect.getWbID() + ":user:" + rect.getUsername() + ":shapes", rect.getUuid().toString());
				tx.exec();
			}
		} else {
			throw new IllegalShapeException();
		}
	}

	/**
	 * Inserts a new Circle-Instance to the database. NOTE: A shape in general can only be inserted if it belongs to a Whiteboard and an Artist (User). Therefore Shape-Instance must provide these information, otherwise
	 * an IllegalShapeException will be thrown.
	 */
	public void insertCircle(Circle circle) throws UserNotFoundException, WhiteboardNotFoundException, IllegalShapeException {
		if (circle != null && circle.getUsername() != null && circle.getWbID() != null) {
			try (Jedis jedis = MyJedisPool.getPool("localhost").getResource()) {
				UserDAO userDAO = new RedisUserDAO();
				WhiteboardDAO wbDAO = new RedisWhiteboardDAO();

				if (!userDAO.userExists(circle.getUsername()))
					throw new UserNotFoundException();
				if (!wbDAO.whiteboardExists(circle.getWbID()))
					throw new WhiteboardNotFoundException();

				Transaction tx = jedis.multi();
				Map<String, String> rectProperties = new HashMap<String, String>();
				rectProperties.put(FIELD_POS_X1, String.valueOf(circle.getCoordinate().getX()));
				rectProperties.put(FIELD_POS_Y1, String.valueOf(circle.getCoordinate().getY()));
				rectProperties.put(FIELD_CIRCLE_RADIUS, String.valueOf(circle.getRadius()));
				rectProperties.put(FIELD_SHAPE_TYPE, "c");

				tx.hmset(circle.getShapeKey(), rectProperties);
				tx.sadd("whiteboard:" + circle.getWbID() + ":user:" + circle.getUsername() + ":shapes", circle.getUuid().toString());
				tx.exec();
			}
		} else {
			throw new IllegalShapeException();
		}
	}
	
	public void insertPath(Path path) throws UserNotFoundException, WhiteboardNotFoundException, IllegalShapeException {
		if (path != null && path.getUsername() != null && path.getWbID() != null) {
			try (Jedis jedis = MyJedisPool.getPool("localhost").getResource()) {
				UserDAO userDAO = new RedisUserDAO();
				WhiteboardDAO wbDAO = new RedisWhiteboardDAO();

				if (!userDAO.userExists(path.getUsername()))
					throw new UserNotFoundException();
				if (!wbDAO.whiteboardExists(path.getWbID()))
					throw new WhiteboardNotFoundException();
				
				try {
					List<Point> pathCoords = path.getPoints();
					ObjectMapper om = new ObjectMapper();
					String json = om.writeValueAsString(pathCoords);

					Transaction tx = jedis.multi();
					Map<String, String> pathProperties = new HashMap<String, String>();

					pathProperties.put(FIELD_PATH_POINTS, json);
					pathProperties.put(FIELD_SHAPE_TYPE, "p");
					tx.hmset(path.getShapeKey(), pathProperties);

					tx.sadd("whiteboard:" + path.getWbID() + ":user:" + path.getUsername() + ":shapes", path.getUuid().toString());
					tx.exec();
				} catch (JsonProcessingException je) {
					je.printStackTrace();
				}
			}
		}
		else {
			throw new IllegalShapeException();
		}
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
	 * 
	 * @param whiteboard
	 *            The whiteboard - only a valid WB-ID is needed
	 * @return The method returns a Hashmap which gives access to all the shapes from the whiteboard. You get access to the shapes via username-keys.
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Shape> findAllShapesFromWB(Whiteboard whiteboard) 
			throws WhiteboardNotFoundException, JsonParseException, JsonMappingException, IOException 
	{
		try (Jedis jedis = MyJedisPool.getPool("localhost").getResource()) {
			UserDAO userDAO = new RedisUserDAO();

			// Fetch all registered useres from the whiteboard
			List<User> registeredUseres = userDAO.findAllUsersFromWB(whiteboard);
			
			List<String> allShapeUUIDs = new ArrayList<String>();

			// Fetch all shape-UUIDs from every registered user of the whiteboard
			for (int x = 0; x < registeredUseres.size(); ++x) {
				String shapeKeys = "whiteboard:" + whiteboard.getWbid() + ":user:" + registeredUseres.get(x).getUsername() + ":shapes";

				allShapeUUIDs.addAll(jedis.smembers(shapeKeys));
			}

			Pipeline p = jedis.pipelined();

			// PIPELINED: Fetch all Shapes without previously fetched UUIDs
			/*Response<Map<String, String>>[][] res = new Response[registeredUseres.size()][];
			for (int x = 0; x < registeredUseres.size(); ++x) {
				String username = registeredUseres.get(x).getUsername();
				List<String> shapeUUIDs = hashMap.get(username);

				res[x] = new Response[shapeUUIDs.size()];

				for (int y = 0; y < res[x].length; ++y) {
					res[x][y] = p.hgetAll("whiteboard:" + whiteboardID + ":shape:" + shapeUUIDs.get(y));
				}
			}*/
			
			Response<Map<String, String>>[] result = new Response[allShapeUUIDs.size()];
			for(int x = 0; x < allShapeUUIDs.size(); ++x) {
				result[x] = p.hgetAll("whiteboard:" + whiteboard.getWbid() + ":shape:" + allShapeUUIDs.get(x));
			}

			p.sync();

			List<Shape> allShapes = new ArrayList<Shape>();
			
			ObjectMapper om = new ObjectMapper();
			for (int x = 0; x < result.length; ++x) {
				Map<String, String> properties = result[x].get();

				if (properties.get(FIELD_SHAPE_TYPE).equals("p")) {
					String json = properties.get(FIELD_PATH_POINTS);
					String creator = properties.get(FIELD_CREATOR);
					List<Point> points = om.readValue(json, om.getTypeFactory().constructCollectionType(List.class, Point.class));
					
					Shape path = new Path(creator, whiteboard.getWbid(), points);
					
					allShapes.add(path);
				} else if (properties.get(FIELD_SHAPE_TYPE).equals("r")) {
					int x1 = Integer.parseInt(properties.get(FIELD_POS_X1));
					int y1 = Integer.parseInt(properties.get(FIELD_POS_Y1));
					int x2 = Integer.parseInt(properties.get(FIELD_POS_X2));
					int y2 = Integer.parseInt(properties.get(FIELD_POS_Y2));
					String creator = properties.get(FIELD_CREATOR);
					
					Shape rect = new Rectangle(creator, whiteboard.getWbid(), new Point(x1, y1), new Point(x2, y2));

					allShapes.add(rect);
				} else if (properties.get(FIELD_SHAPE_TYPE).equals("c")) {
					int radius = Integer.parseInt(properties.get(FIELD_CIRCLE_RADIUS));
					int posX = Integer.parseInt(properties.get(FIELD_POS_X1));
					int posY = Integer.parseInt(properties.get(FIELD_POS_Y1));

					Shape circle = new Circle(registeredUseres.get(x), whiteboard, new Point(posX, posY), radius);

					allShapes.add(circle);
				}
			}
			
			// When piplined fetching is ready, store the Shape-Objects in the Hashmap
			/*HashMap<String, List<Shape>> userShapes = new HashMap<String, List<Shape>>();
			for (int x = 0; x < registeredUseres.size(); ++x) {
				List<Shape> shapes = new ArrayList<Shape>();
				for (int y = 0; y < res[x].length; ++y) {
					Map<String, String> properties = res[x][y].get();

					if (properties.get(FIELD_SHAPE_TYPE).equals("c")) {
						int radius = Integer.parseInt(properties.get(FIELD_CIRCLE_RADIUS));
						int posX = Integer.parseInt(properties.get(FIELD_POS_X1));
						int posY = Integer.parseInt(properties.get(FIELD_POS_Y1));

						Shape c = new Circle(registeredUseres.get(x), whiteboard, new Point(posX, posY), radius);

						shapes.add(c);
					} else if (properties.get(FIELD_SHAPE_TYPE).equals("r")) {
						int x1 = Integer.parseInt(properties.get(FIELD_POS_X1));
						int y1 = Integer.parseInt(properties.get(FIELD_POS_Y1));
						int x2 = Integer.parseInt(properties.get(FIELD_POS_X2));
						int y2 = Integer.parseInt(properties.get(FIELD_POS_Y2));

						Shape r = new Rectangle(registeredUseres.get(x), whiteboard, new Point(x1, y1), new Point(x2, y2));

						shapes.add(r);
					}
				}
				userShapes.put(registeredUseres.get(x).getUsername(), shapes);
			}*/

			return allShapes;
		}
	}

}
