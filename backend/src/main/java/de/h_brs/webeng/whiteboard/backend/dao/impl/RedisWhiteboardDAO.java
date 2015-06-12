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
	private Jedis jedis = MyJedisPool.getPool("localhost").getResource();
	
	private final String ALL_WHITEBOARDS = "whiteboards";
	
	public final int PUBLIC  = 0;
	public final int PRIVATE = 1;
	
	/**
	 * Inserts a new Whiteboard-Instance in the database.
	 * The instance will be saved as a Redis-Hash and the 
	 * generated wb-ID will be added to the Redis-List which 
	 * indicates all Whiteboards 
	 */
	@Override
	public Whiteboard insertWhiteboard(User creator) throws UserNotFoundException {
		if(creator != null) {
			UserDAO userDAO = new RedisUserDAO();
			
			if(userDAO.userExists(creator)) {
				Transaction tx = jedis.multi();
				
				// Generate a new ID (atomic operation)
				long wbid = jedis.incr("wbid");
						
				//Store singleWhiteboard Instance as a Redis Hash
				String wbKey = "whiteboard:"+wbid;
				tx.hset(wbKey, "wbid", String.valueOf(wbid));
				tx.hset(wbKey, "creator", creator.getUsername());
						
				// Put Whiteboard Key in a Redis Set
				tx.sadd("whiteboards", String.valueOf(wbid));
				
				tx.exec();
				
				Whiteboard wb = new Whiteboard(wbid, creator.getUsername());
				
				return wb;
			}
			else
				throw new UserNotFoundException();
		}
		else
			throw new UserNotFoundException();
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
	 * Fetches a single Whiteboard from the database if the parameter wbid can be found,
	 * otherwise a WhiteboardNotFoundException will be thrown.
	 * NOTE: This method fetches Whiteboards lazy. Only base information (wbid, crator-name)
	 * about the Whiteboard gets fetched. There won't be any information about Shapes 
	 * or any other User of the Whiteboard.
	 */
	@Override
	public Whiteboard findWhiteboardByID(long wbid) throws WhiteboardNotFoundException {
		//System.out.println("Trying to retrive Whiteboard#"+wbid+"\n");
		
		if(jedis.sismember(ALL_WHITEBOARDS, String.valueOf(wbid))) {
			Map<String, String> properties = jedis.hgetAll("whiteboard:"+wbid);
			//System.out.println("Whiteboard#"+wbid+" was found!");
			
			String creatorUserName = properties.get("creator");
			
			Whiteboard wb = new Whiteboard(wbid, creatorUserName);
			
			return wb;
		} 
		else
			throw new WhiteboardNotFoundException(wbid);
	}
	
	/**
	 * Fetches a limited amount of Whiteboards
	 * NOTE: This method fetches Whiteboards lazy. Only base information (wbid, crator-name)
	 * about the Whiteboard gets fetched. There won't be any information about Shapes 
	 * or any other User of the Whiteboard.
	 */
	@Override
	public List<Whiteboard> findWhiteboards(int start, int count) {
		List<String> lst = jedis.sort("whiteboards", 
				new SortingParams().limit(start, count).get("whiteboard:*->wbid", "whiteboard:*->creator"));
		List<Whiteboard> allWhiteboards = new ArrayList<Whiteboard>();
		
		if(!lst.isEmpty()) {
			allWhiteboards = new ArrayList<Whiteboard>();
			for(int x=0 ; x<lst.size(); x+=2) {
				long wbid = Long.parseLong(lst.get(x));
				String creatorUserName = lst.get(x+1);
				
				allWhiteboards.add(new Whiteboard(wbid, creatorUserName));
			}
		}

		return allWhiteboards;
	}
	
	/**
	 * Fetches all Whiteboards which a User has registered to.
	 * NOTE: This method fetches Whiteboards lazy. Only base information (wbid, crator-name)
	 * about the Whiteboard gets fetched. There won't be any information about Shapes 
	 * or any other User of the Whiteboard.
	 */
	@Override
	public List<Whiteboard> findRegisteredWhiteboards(User user) throws UserNotFoundException {
		List<Whiteboard> userWhiteboards = new ArrayList<Whiteboard>();
		
		UserDAO userDAO = new RedisUserDAO();
		if(userDAO.userExists(user)) {
			List<String> lst = jedis.sort("user:"+user.getUsername()+":whiteboards", 
					new SortingParams().nosort().get("whiteboard:*->wbid", "whiteboard:*->creator"));
			
			if(!lst.isEmpty()) {
				userWhiteboards = new ArrayList<Whiteboard>();
				for(int x=0 ; x<lst.size(); x+=2) {
					long wbid = Long.parseLong(lst.get(x));
					String creatorUserName = lst.get(x+1);
					
					userWhiteboards.add(new Whiteboard(wbid, creatorUserName));
				}
			}
		} 
		else {
			throw new UserNotFoundException();
		}
		return userWhiteboards;
	}
	
	/**
	 * Fetch all whiteboards which are not visited by the user so far.
	 * For performance reasons, this method is parameterized with a 
	 * starting point and an offset so that we can limit the fetched data.
	 * For example: Retrieve the latest 50 whiteboards (start = 0, count=50) 
	 * which are not visited by the user so far
	 * 
	 * NOTE: This method fetches Whiteboards lazy. Only base information (wbid, crator-name)
	 * about the Whiteboard gets fetched. There won't be any information about Shapes 
	 * or any other User of the Whiteboard.
	 */
	@SuppressWarnings("unchecked")
	public List<Whiteboard> findUnregisteredWhiteboards(User user, int start, int count) throws UserNotFoundException {
		UserDAO userDAO = new RedisUserDAO();
		if(!userDAO.userExists(user)) {
			throw new UserNotFoundException();
		}
		// Fetch the latest X whiteboards
		List<String> latestWBs = jedis.sort(
				"whiteboards", 
				new SortingParams().limit(start, count).desc().get("whiteboard:*->wbid", "whiteboard:*->creator"));
		List<Whiteboard> unregisteredWhiteboards = new ArrayList<Whiteboard>();
		
		String userWBkeys = "user:"+user.getUsername()+":whiteboards";
		
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
				Whiteboard wb = new Whiteboard(wbID, creator);
				unregisteredWhiteboards.add(wb);
			}
		}
		
		// TODO Find the rest
		return unregisteredWhiteboards;
	}
	
	@Override
	public void setPublicity(Whiteboard wb, int mode) {
		switch(mode) {
			case PUBLIC:
				if(!wb.isShared())
					wb.setShared(true);
				break;
			case PRIVATE:
				if(wb.isShared())
					wb.setShared(false);
				break;
		}
	}
	
	public boolean whiteboardExists(String wbid) {
		if(jedis.sismember(ALL_WHITEBOARDS, wbid))
			return true;
		else
			return false;
	}
	
	public boolean whiteboardExists(Whiteboard whiteboard) {
		return whiteboardExists(String.valueOf(whiteboard.getWbid()));
	}
}
