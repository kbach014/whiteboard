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

	@Override
	public boolean updateUser(User user) throws UserNotFoundException {
		try (Jedis jedis = MyJedisPool.getPool("localhost").getResource()) {
			if (userExists(user.getUsername(), jedis)) {
				String userKey = "user:" + user.getUsername();

				Transaction tx = jedis.multi();
				tx.hset(userKey, FIELD_FIRSTNAME, user.getFirstname());
				tx.hset(userKey, FIELD_LASTNAME, user.getLastname());
				if (!user.getPassword().equals(""))
					tx.hset(userKey, FIELD_PASSWORD, user.getPassword());

				tx.exec();
				return true;
			} else {
				throw new UserNotFoundException();
			}
		}
	}

	@Override
	public boolean deleteUser(User user) {
		// TODO Not supported at the moment
		return false;
	}

	@Override
	public User findUserByUsername(String username) throws UserNotFoundException {
		// System.out.println("Trying to retrive User \""+username+"\n");
		try (Jedis jedis = MyJedisPool.getPool("localhost").getResource()) {
			if (userExists(username, jedis)) {
				Map<String, String> properties = jedis.hgetAll("user:" + username);
				// System.out.println("User \""+username+"\" was found!");
				return new User(username, properties.get("firstname"), properties.get("lastname"));
			} else {
				throw new UserNotFoundException(username);
			}
		}
	}
	
	public User findUserByUsername(String username, Jedis jedis) throws UserNotFoundException {
		if (userExists(username, jedis)) {
			Map<String, String> properties = jedis.hgetAll("user:" + username);
			// System.out.println("User \""+username+"\" was found!");
			return new User(username, properties.get("firstname"), properties.get("lastname"));
		} else {
			throw new UserNotFoundException(username);
		}
	}

	@Override
	public List<User> findAllUsersFromWB(Whiteboard whiteboard) throws WhiteboardNotFoundException {
		try (Jedis jedis = MyJedisPool.getPool("localhost").getResource()) {
			RedisWhiteboardDAO wbDAO = new RedisWhiteboardDAO();
			List<User> wbUsers = new ArrayList<User>();
			
			if (!wbDAO.whiteboardExists(whiteboard, jedis)) {
				throw new WhiteboardNotFoundException();
			}
			
			if(whiteboard.getCreator() == null || whiteboard.getCreator().equals("")) {
				whiteboard = wbDAO.findWhiteboardByID(whiteboard.getWbid(), jedis);
			}
			
			RedisUserDAO userDAO = new RedisUserDAO();
			User creator = null;
			try {
				creator = userDAO.findUserByUsername(whiteboard.getCreator(), jedis);
			} catch(UserNotFoundException e) {
				e.printStackTrace();
			}
			wbUsers.add(creator);
			
			String wbUsernames = "whiteboard:" + whiteboard.getWbid() + ":users";

			List<String> lst = jedis.sort(wbUsernames,
					new SortingParams().nosort().get("user:*->" + RedisUserDAO.FIELD_USERNAME, "user:*->" + RedisUserDAO.FIELD_FIRSTNAME, "user:*->" + RedisUserDAO.FIELD_LASTNAME));

			if (!lst.isEmpty()) {
				for (int x = 0; x < lst.size(); x += 3) {
					String username = lst.get(x);
					String firstname = lst.get(x + 1);
					String lastname = lst.get(x + 2);

					wbUsers.add(new User(username, firstname, lastname));
				}
			}
			
			
			
			return wbUsers;
		}
	}

	@Override
	public void register(User user) throws UserAlreadyRegisteredException {
		try (Jedis jedis = MyJedisPool.getPool("localhost").getResource()) {
			if (!userExists(user, jedis)) {
				Transaction tx = jedis.multi();

				Map<String, String> userProperties = new HashMap<String, String>();
				userProperties.put(FIELD_USERNAME, user.getUsername());
				userProperties.put(FIELD_FIRSTNAME, user.getFirstname());
				userProperties.put(FIELD_LASTNAME, user.getLastname());
				userProperties.put(FIELD_PASSWORD, user.getPassword());

				tx.hmset("user:" + user.getUsername(), userProperties);
				tx.sadd(ALL_USERS, user.getUsername());
				tx.exec();
			} else {
				throw new UserAlreadyRegisteredException(user.getUsername());
			}
		}
	}

	@Override
	public User login(String username, String password) throws UserNotFoundException, PasswordIncorrectException {
		try (Jedis jedis = MyJedisPool.getPool("localhost").getResource()) {
			if (userExists(username, jedis)) {
				String pw = jedis.hget("user:" + username, FIELD_PASSWORD);

				if (pw.equals(password)) {
					String firstname = jedis.hget("user:" + username, FIELD_FIRSTNAME);
					String lastname = jedis.hget("user:" + username, FIELD_LASTNAME);

					return new User(username, firstname, lastname);
				} else {
					throw new PasswordIncorrectException();
				}
			} else {
				jedis.close();
				throw new UserNotFoundException();
			}
		}
	}

	@Override
	public void registerToWhiteboard(String username, Long whiteboardId) throws UserNotFoundException, WhiteboardNotFoundException, UserWhiteboardException {
		try (Jedis jedis = MyJedisPool.getPool("localhost").getResource()) {
			if (!userExists(username, jedis)) {
				throw new UserNotFoundException(username);
			}

			RedisWhiteboardDAO wbDAO = new RedisWhiteboardDAO();
			if (!wbDAO.whiteboardExists(whiteboardId, jedis)) {
				throw new WhiteboardNotFoundException();
			}

			// Key for Redis Set which contains all registered users for the whiteboard
			String wbUsersKey = "whiteboard:" + whiteboardId + ":users";
			// Key for Redis Set which contains all whiteboards which a user is working on
			String userWbsKey = "user:" + username + ":whiteboards";

			// Is User already registered to Whiteboard?
			if (!jedis.sismember(wbUsersKey, username)) {
				Transaction tx = jedis.multi();
				tx.sadd(wbUsersKey, username);
				tx.sadd(userWbsKey, String.valueOf(whiteboardId));
				tx.exec();

				// System.out.println(user.getUsername() + " was sucessfully registered for " + "whiteboad#" + whiteboard.getWbid());
			} else {
				throw new UserWhiteboardException(username, whiteboardId);
			}
		}
	}

	public boolean userHasWhiteboard(User user, Whiteboard whiteboard) throws UserNotFoundException, WhiteboardNotFoundException {
		try (Jedis jedis = MyJedisPool.getPool("localhost").getResource()) {
			RedisWhiteboardDAO wbDAO = new RedisWhiteboardDAO();
			if (!userExists(user, jedis)) {
				throw new UserNotFoundException();
			}
			if (!wbDAO.whiteboardExists(whiteboard, jedis)) {
				throw new WhiteboardNotFoundException();
			}
			
			if(jedis.sismember("user:" + user.getUsername() + ":whiteboards", String.valueOf(whiteboard.getWbid())))
				return true;
			else if(jedis.sismember("creator:" + user.getUsername() + ":whiteboards", String.valueOf(whiteboard.getWbid())))
				return true;
			else
				return false;
		}
	}

	public boolean userHasWhiteboard(String username, Long wbID) throws UserNotFoundException, WhiteboardNotFoundException {
		User usr = new User(username);
		Whiteboard wb = new Whiteboard(wbID);
		
		return userHasWhiteboard(usr, wb);
	}

	public boolean userExists(String username) {
		try (Jedis jedis = MyJedisPool.getPool("localhost").getResource()) {
			return userExists(username, jedis);
		}
	}

	public boolean userExists(User user) {
		return userExists(user.getUsername());
	}

	public boolean userExists(String username, Jedis jedis) {
		return jedis.sismember(ALL_USERS, username);
	}

	public boolean userExists(User user, Jedis jedis) {
		return userExists(user.getUsername(), jedis);
	}

}
