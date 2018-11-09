package com.server.io;

import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UserInfo {
	private static Map<String, SocketChannel> userMap;
	
	static{
		userMap = Collections.synchronizedMap(new HashMap<String, SocketChannel>());
	}

	public static void addUserSc(String name, SocketChannel sc){
		userMap.put(name, sc);
	}
	
	public static SocketChannel getUserSc(String name){
		return userMap.get(name);
	}
}
