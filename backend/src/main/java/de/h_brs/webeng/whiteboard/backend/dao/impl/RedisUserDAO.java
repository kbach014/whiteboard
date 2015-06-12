package de.h_brs.webeng.whiteboard.backend.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Transaction;
import de.h_brs.webeng.whiteboard.backend.dao.*;
import de.h_brs.webeng.whiteboard.backend.dao.exception.*;
import de.h_brs.webeng.whiteboard.backend.domain.*;

public class RedisUserDAO implements UserDAO {
	private static final String ALL_USERS = "users";
	
	public static final String FIELD_USERNAME = "username";
	public static final String FIELD_FIRSTNAME = "firstname";
	public static final String FIELD_LASTNAME = "lastname";
	public static final String FIELD_PASSWORD = "password";
	
	private Jedis jedis = MyJedisPool.getPool("localhost").getResource();

	@Override
	public boolean updateUser(User user) throws UserNotFoundException {
		if(userExists(user.getUsername())) {
			String userKey = "user:"+user.getUsername();
			
			Transaction tx = jedis.multi();
			tx.hset(userKey, FIELD_FIRSTNAME, user.getFirstname());
			tx.hset(userKey, FIELD_LASTNAME, user.getLastname());
			if(!user.getPassword().equals(""))
				tx.hset(userKey, FIELD_PASSWORD, user.getPassword());
			
			tx.exec();
			return true;
		} 
		else
			throw new UserNotFoundException();
	}

	@Override
	public boolean deleteUser(User user) {
		// TODO Not supported at the moment
		return false;
	}
	
	@Override
	public User findUserByUsername(String username) throws UserNotFoundException {
		//System.out.println("Trying to retrive User \""+username+"\n");
		
		if(userExists(username)) {
			Map<String, String> properties = jedis.hgetAll("user:" + username);
			//System.out.println("User \""+username+"\" was found!");
			User user = new User(username, properties.get("firstname"), properties.get("lastname"));
			return user;
		} 
		else {
			throw new UserNotFoundException(username);
		}
	}

	
	
	
	@Override
	public List<User> findAllUsersFromWB(Whiteboard whiteboard) 
			throws WhiteboardNotFoundException 
	{
		WhiteboardDAO wbDAO = new RedisWhiteboardDAO();
		List<User> wbUsers = new ArrayList<User>();
		
		if(!wbDAO.whiteboardExists(whiteboard))
			throw new WhiteboardNotFoundException();
		
		String wbUsernames = "whiteboard:"+whiteboard.getWbid()+":users";
		
		List<String> lst = jedis.sort(wbUsernames, 
				new SortingParams().nosort().get("user:*->"+RedisUserDAO.FIELD_USERNAME, 
						"user:*->"+RedisUserDAO.FIELD_FIRSTNAME,
						"user:*->"+RedisUserDAO.FIELD_LASTNAME));
		
		if(!lst.isEmpty()) {
			for(int x=0; x<lst.size(); x+=3) {
				String username = lst.get(x);
				String firstname = lst.get(x+1);
				String lastname = lst.get(x+2);
				
				wbUsers.add(new User(username, firstname, lastname));
			}
		}
		
		return wbUsers;
	}
	

	@Override
	public void register(User user) throws UserAlreadyRegisteredException {
		if(!userExists(user)) {
			Transaction tx = jedis.multi();
			
			Map<String, String> userProperties = new HashMap<String, String>();
			userProperties.put(FIELD_USERNAME, user.getUsername());
			userProperties.put(FIELD_FIRSTNAME, user.getFirstname());
			userProperties.put(FIELD_LASTNAME, user.getLastname());
			userProperties.put(FIELD_PASSWORD, user.getPassword());
			
			tx.hmset("user:" + user.getUsername(), userProperties);
			tx.sadd(ALL_USERS, user.getUsername());
			tx.exec();
		} 
		else {
			throw new UserAlreadyRegisteredException(user.getUsername());
		}
	}

	@Override
	public User login(String username, String password) 
			throws UserNotFoundException, PasswordIncorrectException 
	{
		if(userExists(username)) {
			String pw = jedis.hget("user:"+username, FIELD_PASSWORD);
			
			if(pw.equals(password)) {
				String firstname = jedis.hget("user:"+username, FIELD_FIRSTNAME);
				String lastname = jedis.hget("user:"+username, FIELD_LASTNAME);
				
				User validUser = new User(username, firstname, lastname);
				
				return validUser;
			} 
			else
				throw new PasswordIncorrectException();
		}
		else
			throw new UserNotFoundException();
	}

	@Override
	public void registerToWhiteboard(User user, Whiteboard whiteboard) 
			throws UserNotFoundException, WhiteboardNotFoundException, UserWhiteboardException 
	{
		if(!userExists(user)) 
			throw new UserNotFoundException(user);
		
		WhiteboardDAO wbDAO = new RedisWhiteboardDAO();
		if(!wbDAO.whiteboardExists(whiteboard))
			throw new WhiteboardNotFoundException();
		
		// Key for Redis Set which contains all registered users for the whiteboard
		String wbUsersKey = "whiteboard:" + whiteboard.getWbid() + ":users";
		// Key for Redis Set which contains all whiteboards which a user is working on
		String userWbsKey = "user:" + user.getUsername() + ":whiteboards";

		// Is User already registered to Whiteboard?
		if (!jedis.sismember(wbUsersKey, user.getUsername())) {
			Transaction tx = jedis.multi();
			tx.sadd(wbUsersKey, user.getUsername());
			tx.sadd(userWbsKey, String.valueOf(whiteboard.getWbid()));
			tx.exec();

			//System.out.println(user.getUsername() + " was sucessfully registered for " + "whiteboad#" + whiteboard.getWbid());
		} 
		else {
			throw new UserWhiteboardException(user, whiteboard);
		}
	}
	

	public boolean userHasWhiteboard(User user, Whiteboard whiteboard) 
			throws UserNotFoundException, WhiteboardNotFoundException 
	{
		WhiteboardDAO wbDAO = new RedisWhiteboardDAO();
		if(!userExists(user))
			throw new UserNotFoundException();
		if(!wbDAO.whiteboardExists(whiteboard))
			throw new WhiteboardNotFoundException();
		
		
		if(jedis.sismember("user:"+user.getUsername()+":whiteboards", String.valueOf(whiteboard.getWbid())))
			return true;
		else 
			return false;
	}
	
	public boolean userHasWhiteboard(String username, Long wbID) 
			throws UserNotFoundException, WhiteboardNotFoundException 
	{
		WhiteboardDAO wbDAO = new RedisWhiteboardDAO();
		if(!userExists(username))
			throw new UserNotFoundException();
		if(!wbDAO.whiteboardExists(String.valueOf(wbID)))
			throw new WhiteboardNotFoundException();
		
		
		if(jedis.sismember("user:"+username+":whiteboards", String.valueOf(wbID)))
			return true;
		else 
			return false;
	}
	
	public boolean userExists(String username) {
		if(jedis.sismember(ALL_USERS, username)) {
			return true;
		} 
		else {
			System.out.println("Could not find user \""+username+"\" !");
			return false;
		}
	}
	
	public boolean userExists(User user) {
		return userExists(user.getUsername());
	}
	
	

}
