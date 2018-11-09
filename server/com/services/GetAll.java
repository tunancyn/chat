package com.services;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.server.io.EnMsgType;
import com.server.io.WorkerTask;

public class GetAll {
	
	public GetAll(){
	}
	
	public ObjectNode process() throws IOException{	
		//组装响应用的json字符串
		ObjectMapper jsonMapper = new ObjectMapper();
		ObjectNode node1 = jsonMapper.createObjectNode();
		node1.put("msgtype", EnMsgType.EN_MSG_ACK.toString());
		node1.put("srcmsg", EnMsgType.EN_MSG_GET_ALL_USERS.toString());
		
		Set<Entry<String, SocketChannel>> set = WorkerTask.map.entrySet();
		Iterator<Entry<String, SocketChannel>> it = set.iterator();
		String[] names = new String[WorkerTask.map.size()];
		int i=0;
		while(it.hasNext()){
			Entry<String, SocketChannel> r = it.next();
			String name = r.getKey();
			names[i] = name;
			i++;
		}
		node1.put("users",names.toString());//String[]类型 ???-->String
		System.out.println(node1.get("users").asText());
		return node1;
	}
}
