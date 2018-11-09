package com.services;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.server.io.WorkerTask;

public class AllChat {
	private ObjectNode node;
	
	public AllChat(ObjectNode node){
		this.node = node;
	}
	
	public void process() throws IOException{
		
		Set<Entry<String, SocketChannel>> set = WorkerTask.map.entrySet();
		Iterator<Entry<String, SocketChannel>> it = set.iterator();
		while(it.hasNext()){
			Entry<String, SocketChannel> r = it.next();
			SocketChannel sk = r.getValue();
			sk.write(ByteBuffer.wrap((node+"\n").getBytes()));
		}	
	}
}
