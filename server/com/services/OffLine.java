package com.services;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.server.io.EnMsgType;
import com.server.io.WorkerTask;

public class OffLine {
	private ObjectNode node;
	SocketChannel _sc;
	
	public OffLine(SocketChannel _sc,ObjectNode node){
		this.node = node;
	}
	
	public static void offLines(String name1) throws IOException{	
		ObjectMapper jsonMapper = new ObjectMapper();
		ObjectNode node1 = jsonMapper.createObjectNode();
		node1.put("msgtype", EnMsgType.EN_MSG_NOTIFY_OFFLINE.toString());
		node1.put("name", name1);
		
		Set<Entry<String, SocketChannel>> set = WorkerTask.map.entrySet();
		Iterator<Entry<String, SocketChannel>> it = set.iterator();
		while(it.hasNext()){
			Entry<String, SocketChannel> r = it.next();
			SocketChannel sk = r.getValue();
			sk.write(ByteBuffer.wrap((node1+"\n").getBytes()));
		}
	}
	
	public ObjectNode process() throws IOException{
		String name = node.get("name").asText();
		
		//组装响应用的json字符串
		ObjectMapper jsonMapper = new ObjectMapper();
		ObjectNode node2 = jsonMapper.createObjectNode();
		node2.put("msgtype", EnMsgType.EN_MSG_ACK.toString());
		node2.put("srcmsg", EnMsgType.EN_MSG_OFFLINE.toString());
		if(name != null){
			node2.put("code", "success");
			WorkerTask.map.remove(name);		
			offLines(name); //通知在线成员您已下线	
		}else{
			node2.put("code", "fail");
		}
		return node2;
	}
}
