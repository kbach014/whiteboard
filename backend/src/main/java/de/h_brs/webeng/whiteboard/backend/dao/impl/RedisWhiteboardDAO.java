package de.h_brs.webeng.whiteboard.backend.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Transaction;
import de.h_brs.webeng.whiteboard.backend.dao.*;
import de.h_brs.webeng.whiteboard.backend.dao.exception.*;
import de.h_brs.webeng.whiteboard.backend.domain.*;

public class RedisWhiteboardDAO implements WhiteboardDAO {
	public static final String FIELD_ACCESS_TYPE 	= "accessType";
	public static final String FIELD_WHITEBOARD_ID 	= "wbid";
	public static final String FIELD_CREATOR		= "creator";
	
	public static final String ACCESS_PUBLIC		= "public";
	public static final String ACCESS_PRIVATE		= "private";
	/**
	 * Inserts a new Whiteboard-Instance in the database. The instance will be saved as a Redis-Hash and the generated wb-ID will be added to the Redis-List which indicates all Whiteboards
	 */
	@Override
	public Whiteboard insertWhiteboard(String creatorName) throws UserNotFoundException {
		try (Jedis jedis = MyJedisPool.getPool("localhost").getResource()) {
			if (creatorName != null) {
				UserDAO userDAO = new RedisUserDAO();

				if (userDAO.userExists(creatorName)) {
					Long wbid = jedis.incr("wbid");
					
					// Generate a new ID (atomic operation)
					Transaction tx = jedis.multi();
					// Store singleWhiteboard Instance as a Redis Hash
					String wbKey = "whiteboard:" + wbid;
					tx.hset(wbKey, FIELD_WHITEBOARD_ID, String.valueOf(wbid));
					tx.hset(wbKey, FIELD_CREATOR, creatorName);
					tx.hset(wbKey, FIELD_ACCESS_TYPE, ACCESS_PRIVATE);

					// Put Whiteboard Key in a Redis Set
					tx.sadd("whiteboards:private", String.valueOf(wbid));

					// Register creator to his own whiteboard:
					String creatorWbsKey = "creator:" + creatorName + ":whiteboards";
					tx.sadd(creatorWbsKey, String.valueOf(wbid));

					tx.exec();

					return new Whiteboard(wbid, creatorName, AccessType.PRIVATE);
				} else {
					throw new UserNotFoundException();
				}
			} else {
				throw new UserNotFoundException();
			}
		}
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
	/**
	 * Fetches a single Whiteboard from the database if the parameter wbid can be found, otherwise a WhiteboardNotFoundException will be thrown. NOTE: This method fetches Whiteboards lazy. Only base information (wbid,
	 * crator-name) about the Whiteboard gets fetched. There won't be any information about Shapes or any other User of the Whiteboard.
	 */
	@Override
	public Whiteboard findWhiteboardByID(long wbid) throws WhiteboardNotFoundException {
		// System.out.println("Trying to retrive Whiteboard#"+wbid+"\n");
		try (Jedis jedis = MyJedisPool.getPool("localhost").getResource()) {
			Map<String, String> properties = jedis.hgetAll("whiteboard:" + wbid);
			
			if(properties.size() > 0) {
				String creatorUserName = properties.get(FIELD_CREATOR);
				String accessType = properties.get(FIELD_ACCESS_TYPE);
				
				if(accessType != null)
					return new Whiteboard(wbid, creatorUserName, AccessTypeMap.getInstance().get(accessType));
				else
					throw new WhiteboardNotFoundException(wbid);
			} else 
				throw new WhiteboardNotFoundException(wbid);
			
		}
	}

	public Whiteboard findWhiteboardByID(long wbid, Jedis jedis) throws WhiteboardNotFoundException {
		// System.out.println("Trying to retrive Whiteboard#"+wbid+"\n");
		Map<String, String> properties = jedis.hgetAll("whiteboard:" + wbid);

		if (properties.size() > 0) {
			String creatorUserName = properties.get(FIELD_CREATOR);
			String accessType = properties.get(FIELD_ACCESS_TYPE);

			if (accessType != null)
				return new Whiteboard(wbid, creatorUserName, AccessTypeMap.getInstance().get(accessType));
			else
				throw new WhiteboardNotFoundException(wbid);
		} else
			throw new WhiteboardNotFoundException(wbid);
	}

	/**
	 * Fetches a limited amount of Whiteboards NOTE: This method fetches Whiteboards lazy. Only base information (wbid, crator-name) about the Whiteboard gets fetched. There won't be any information about Shapes or any
	 * other User of the Whiteboard.
	 */
	@Override
	public List<Whiteboard> findWhiteboards(int start, int count) {
		try (Jedis jedis = MyJedisPool.getPool("localhost").getResource()) {
			List<String> lst = jedis.sort("whiteboards:public", new SortingParams().limit(start, count).get("whiteboard:*->wbid", "whiteboard:*->creator"));
			List<Whiteboard> allWhiteboards = new ArrayList<Whiteboard>();

			if (!lst.isEmpty()) {
				allWhiteboards = new ArrayList<Whiteboard>();
				for (int x = 0; x < lst.size(); x += 2) {
					long wbid = Long.parseLong(lst.get(x));
					String creatorUserName = lst.get(x + 1);

					allWhiteboards.add(new Whiteboard(wbid, creatorUserName, AccessType.PUBLIC));
				}
			}

			return allWhiteboards;
		}
	}

	/**
	 * Fetches all Whiteboards which a User has registered to. NOTE: This method fetches Whiteboards lazy. Only base information (wbid, crator-name) about the Whiteboard gets fetched. There won't be any information about
	 * Shapes or any other User of the Whiteboard.
	 */
	@Override
	public List<Whiteboard> findRegisteredWhiteboards(String username) throws UserNotFoundException {
		try (Jedis jedis = MyJedisPool.getPool("localhost").getResource()) {
			List<Whiteboard> userWhiteboards = new ArrayList<Whiteboard>();

			UserDAO userDAO = new RedisUserDAO();
			if (userDAO.userExists(username)) {
				List<String> lst = jedis.sort("user:" + username + ":whiteboards",
						new SortingParams().nosort().get("whiteboard:*->" + FIELD_WHITEBOARD_ID, "whiteboard:*->" + FIELD_CREATOR , "whiteboard:*->" + FIELD_ACCESS_TYPE));

				if (!lst.isEmpty()) {
					userWhiteboards = new ArrayList<Whiteboard>();
					for (int x = 0; x < lst.size(); x += 3) {
						long wbid = Long.parseLong(lst.get(x));
						String creatorUserName = lst.get(x + 1);
						String accessType = lst.get(x + 2);
						
						userWhiteboards.add(new Whiteboard(wbid, creatorUserName, AccessTypeMap.getInstance().get(accessType)));
					}
				}
			} else {
				throw new UserNotFoundException();
			}

			return userWhiteboards;
		}
	}
	
	public List<Whiteboard> findCreatedWhiteboards(String username) throws UserNotFoundException {
		try (Jedis jedis = MyJedisPool.getPool("localhost").getResource()) {
			List<Whiteboard> userWhiteboards = new ArrayList<Whiteboard>();

			UserDAO userDAO = new RedisUserDAO();
			if (userDAO.userExists(username)) {
				List<String> lst = jedis.sort("creator:" + username + ":whiteboards",
						new SortingParams().nosort().get("whiteboard:*->" + FIELD_WHITEBOARD_ID, "whiteboard:*->" + FIELD_CREATOR , "whiteboard:*->" + FIELD_ACCESS_TYPE));
				
				if (!lst.isEmpty()) {
					userWhiteboards = new ArrayList<Whiteboard>();
					for (int x = 0; x < lst.size(); x += 3) {
						long wbid = Long.parseLong(lst.get(x));
						String creatorUserName = lst.get(x + 1);
						String accessType = lst.get(x + 2);
						
						userWhiteboards.add(new Whiteboard(wbid, creatorUserName, AccessTypeMap.getInstance().get(accessType)));
					}
				}
			} else {
				throw new UserNotFoundException();
			}

			return userWhiteboards;
		}
	}

	/**
	 * Fetch all whiteboards which are not visited by the user so far. For performance reasons, this method is parameterized with a starting point and an offset so that we can limit the fetched data. For example:
	 * Retrieve the latest 50 whiteboards (start = 0, count=50) which are not visited by the user so far
	 * 
	 * NOTE: This method fetches Whiteboards lazy. Only base information (wbid, crator-name) about the Whiteboard gets fetched. There won't be any information about Shapes or any other User of the Whiteboard.
	 */
	@SuppressWarnings("unchecked")
	public List<Whiteboard> findUnregisteredWhiteboards(String username, int start, int count) throws UserNotFoundException {
		try (Jedis jedis = MyJedisPool.getPool("localhost").getResource()) {
			UserDAO userDAO = new RedisUserDAO();
			if (!userDAO.userExists(username)) {
				throw new UserNotFoundException();
			}
			// Fetch the latest X whiteboards
			List<String> latestWBs = jedis.sort("whiteboards:public", new SortingParams().limit(start, count).desc().get("whiteboard:*->wbid", "whiteboard:*->creator"));
			List<Whiteboard> unregisteredWhiteboards = new ArrayList<Whiteboard>();

			String userWBkeys = "user:" + username + ":whiteboards";

			// Make use of pipelining for performance reasons
			Pipeline p = jedis.pipelined();
			Response<Boolean>[] response = new Response[count];

			for (int x = 0; x < latestWBs.size(); x += 2) {
				// check if the current registered whiteboard is part of the latest
				// x whiteboards
				response[x] = p.sismember(userWBkeys, latestWBs.get(x));
			}

			p.sync();

			for (int x = 0; x < latestWBs.size(); x += 2) {
				if (!response[x].get()) {
					// candidate for our unregistered WB List
					Long wbID = Long.parseLong(latestWBs.get(x));
					String creator = latestWBs.get(x + 1);
					if(!creator.equals(username)) {
						Whiteboard wb = new Whiteboard(wbID, creator, AccessType.PUBLIC);
						unregisteredWhiteboards.add(wb);
					}
				}
			}

			// TODO Find the rest

			return unregisteredWhiteboards;
		}
	}

	@Override
	public void setAccessType(Long wbid, AccessType accessType) throws WhiteboardNotFoundException {
		try (Jedis jedis = MyJedisPool.getPool("localhost").getResource()) {
			String accessTypeOld = jedis.hget("whiteboard:"+wbid, FIELD_ACCESS_TYPE);
			String wbidS = String.valueOf(wbid);
			
			if(accessTypeOld == null || accessTypeOld.equals("")) {
				throw new WhiteboardNotFoundException(wbid);
			}
			
			// Remove the old AT first, then add the new one
			Transaction tx = jedis.multi();
			tx.srem("whiteboards:"+accessTypeOld, wbidS);
			
			if(accessType.equals(AccessType.PUBLIC)) {
				if(accessTypeOld.equals(ACCESS_PUBLIC))
					return;
				tx.sadd("whiteboards:"+ACCESS_PUBLIC, wbidS);
				tx.hset("whiteboard:"+wbidS, FIELD_ACCESS_TYPE, ACCESS_PUBLIC);
			} else if(accessType.equals(AccessType.PRIVATE)) {
				if(accessTypeOld.equals(ACCESS_PRIVATE))
					return;
				tx.sadd("whiteboards:"+ACCESS_PRIVATE, wbidS);
				tx.hset("whiteboard:"+wbidS, FIELD_ACCESS_TYPE, ACCESS_PRIVATE);
			}
			
			tx.exec();
		}
	}

	public boolean whiteboardExists(Long wbid) {
		try (Jedis jedis = MyJedisPool.getPool("localhost").getResource()) {
			return whiteboardExists(wbid, jedis);
		}
	}

	public boolean whiteboardExists(Whiteboard whiteboard) {
		return whiteboardExists(whiteboard.getWbid());
	}

	public boolean whiteboardExists(Long wbid, Jedis jedis) {
		Map<String, String> result = jedis.hgetAll("whiteboard:"+String.valueOf(wbid));
		
		if(result.size() > 0)
			return true;
		else
			return false;
	}

	public boolean whiteboardExists(Whiteboard whiteboard, Jedis jedis) {
		return whiteboardExists(whiteboard.getWbid(), jedis);
	}
}
