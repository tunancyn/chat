package com.redis;

import redis.clients.jedis.Jedis;

public enum RedisClient {
	REDIS_CLIENT;
	
	private Jedis jedis;
	private RedisClient(){
		jedis = new Jedis("127.0.0.1");
	}
	
	public void set(String key, String value){
		jedis.set(key, value);
	}
	
	public String get(String key){
		return jedis.get(key);
	}
}
