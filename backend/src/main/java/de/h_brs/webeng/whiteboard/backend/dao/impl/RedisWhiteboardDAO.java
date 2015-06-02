package de.h_brs.webeng.whiteboard.backend.dao.impl;

import java.util.List;
import java.util.Map;

import redis.clients.jedis.Jedis;
import de.h_brs.webeng.whiteboard.backend.dao.WhiteboardDAO;
import de.h_brs.webeng.whiteboard.backend.domain.Shape;
import de.h_brs.webeng.whiteboard.backend.domain.User;
import de.h_brs.webeng.whiteboard.backend.domain.Whiteboard;

public class RedisWhiteboardDAO implements WhiteboardDAO {
	private Jedis jedis = MyJedisPool.getPool("localhost").getResource();
	
	private final String ALL_WHITEBOARDS = "whiteboards";
	
	@Override
	public long insertWhiteboard(Whiteboard whiteboard) {
		User creator = whiteboard.getCreator();
		if(creator != null) {
			// Generate a new ID
			long wbid = jedis.incr("wbid");
					
			//Store singleWhiteboard Instance as a Redis Hash
			String wbKey = "whiteboard:"+wbid;
			jedis.hset(wbKey, "wbid", String.valueOf(wbid));
			jedis.hset(wbKey, "creator", creator.getUsername());
					
			// Put Whiteboard Key in a Redis Set
			jedis.sadd("whiteboards", String.valueOf(wbid));
			return wbid;
		}
		else 
			return -1;
	}

	@Override
	public boolean updateWhiteboard(Whiteboard whiteboard) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteWhiteboard(Whiteboard whiteboard) {
		// TODO Auto-generated method stub
		return false;
	}

	// TODO LAZY oder EAGER Loading?! Option?!
	@Override
	public Whiteboard findWhiteboardByID(long wbid) {
		System.out.println("Trying to retrive Whiteboard#"+wbid+"\n");
		
		if(jedis.sismember(ALL_WHITEBOARDS, String.valueOf(wbid))) {
			Map<String, String> properties = jedis.hgetAll("whiteboard:"+wbid);
			System.out.println("Whiteboard#"+wbid+" was found!");
			
			String creatorUserName = properties.get("creator");
			RedisUserDAO userDAO = new RedisUserDAO();
			User creator = userDAO.findUserByUsername(creatorUserName);
			
			Whiteboard wb = new Whiteboard(wbid, creator);
			
			return wb;
		} else {
			// TODO Ausgabe
			System.out.println("Could not find Whiteboard#"+wbid+" !");
			return null;
		}
	}

	@Override
	public List<Whiteboard> findAllWhiteboards() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addShapeToWB(Shape shape, Whiteboard whiteboard) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Shape> getAllShapesFromWB(Whiteboard whiteboard) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeShapeFromWB(Shape shape, Whiteboard whiteboard) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addUserToWB(User user, Whiteboard whiteboard) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeUserFromWB(User user, Whiteboard whiteboard, String p3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<User> getAllUsersFromWB(Whiteboard whiteboard) {
		// TODO Auto-generated method stub
		return null;
	}

}
