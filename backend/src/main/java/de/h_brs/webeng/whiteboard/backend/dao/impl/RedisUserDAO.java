package de.h_brs.webeng.whiteboard.backend.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redis.clients.jedis.Jedis;
import de.h_brs.webeng.whiteboard.backend.dao.UserDAO;
import de.h_brs.webeng.whiteboard.backend.domain.User;
import de.h_brs.webeng.whiteboard.backend.domain.Whiteboard;

public class RedisUserDAO implements UserDAO {
	private static final String ALL_USERS = "users";
	
	private Jedis jedis = MyJedisPool.getPool("localhost").getResource();
	
	@Override
	public boolean updateUser(User user) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteUser(User user) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public User findUserByUsername(String username) {
		System.out.println("Trying to retrive User \""+username+"\n");
		
		if(jedis.sismember(ALL_USERS, username)) {
			Map<String, String> properties = jedis.hgetAll("user:" + username);
			System.out.println("User \""+username+"\" was found!");
			User user = new User(username, properties.get("firstname"), properties.get("lastname"));
			return user;
		} else {
			// TODO Ausgabe / Exception
			System.out.println("Could not find user \""+username+"\" !");
			return null;
		}
	}

	@Override
	public boolean register(User user) {
		if(!jedis.sismember(ALL_USERS, user.getUsername())) {
			Map<String, String> userProperties = new HashMap<String, String>();
			userProperties.put("username", user.getUsername());
			userProperties.put("firstname", user.getFirstname());
			userProperties.put("lastname", user.getLastname());
			userProperties.put("password", user.getPassword());
			jedis.hmset("user:" + user.getUsername(), userProperties);
			return true;
		} else {
			// TODO Ausgabe
			System.out.println("Could not find user \""+user.getUsername()+"\" !");
			return false;
		}
	}

	@Override
	public boolean login(User user) {
		// TODO Validate Login
		return false;
	}

	@Override
	public List<Whiteboard> findRegisteredWhiteboards(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean registerToWhiteboard(User user, Whiteboard whiteboard) {
		// Key for Redis Set which contains all registered users for the whiteboard
		String wbUsersKey = "whiteboard:" + whiteboard.getWbid() + ":users";
		// Key for Redis Set which contains all whiteboards which a user is working on
		String userWbsKey = "user:" + user.getUsername() + ":whiteboards";

		// Is User already registered to Whiteboard?
		if (!jedis.sismember(wbUsersKey, user.getUsername())) {
			jedis.sadd(wbUsersKey, user.getUsername());
			jedis.sadd(userWbsKey, String.valueOf(whiteboard.getWbid()));

			System.out.println(user.getUsername() + " was sucessfully registered for " + "whiteboad#" + whiteboard.getWbid());
			return true;
		} else {
			System.out.println(user.getUsername() + " is already registered for " + "whiteboad#" + whiteboard.getWbid());
			return false;
		}
	}
	
}
