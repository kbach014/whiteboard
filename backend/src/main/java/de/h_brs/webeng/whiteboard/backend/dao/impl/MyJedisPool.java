package de.h_brs.webeng.whiteboard.backend.dao.impl;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class MyJedisPool {
	private static JedisPool pool;
	
	
	public static JedisPool getPool(String host) {
		if(pool == null) {
			pool = new JedisPool(new JedisPoolConfig(), host);
		}
		
		return pool;
	}

}
