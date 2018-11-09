package com.redis;

import redis.clients.jedis.Jedis;

public class Redis {
	private Jedis redis;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Jedis redis = new Jedis("127.0.0.1");
		System.out.println("Jedis连接成功！");
		
		redis.mset("username", "caiyanan","password","111111","email","123@123.com");
		System.out.println(redis.get("username") +" "+ redis.get("password") +" "+ redis.get("email"));
	}

}
