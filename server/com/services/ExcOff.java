package com.services;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.server.io.EnMsgType;
import com.server.io.WorkerTask;

public class ExcOff {
	public ExcOff(){
		
	}
	public void failOff(String name) throws IOException{
		ObjectMapper jsonMapper = new ObjectMapper();
		ObjectNode node1 = jsonMapper.createObjectNode();
		node1.put("name", name);
		
		Set<Entry<String, SocketChannel>> set = WorkerTask.map.entrySet();
		Iterator<Entry<String, SocketChannel>> it = set.iterator();
		while(it.hasNext()){
			Entry<String, SocketChannel> r = it.next();
			SocketChannel sk = r.getValue();
			sk.write(ByteBuffer.wrap((node1+"\n").getBytes()));
		}
	}
}
