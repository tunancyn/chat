package com.services;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.server.io.UserInfo;

public class Chat {
	private ObjectNode jsonNode;
	public Chat(ObjectNode node) {
		// TODO Auto-generated constructor stub
		this.jsonNode = node;
	}
	
	public String process(String json){
		
		String toname = jsonNode.get("to").asText();
		SocketChannel sc = UserInfo.getUserSc(toname);
		if(sc != null){
			//对方在线，服务器直接转发消息到to端
			try {
				sc.write(ByteBuffer.wrap(json.getBytes()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			//对方不在线，把离线消息缓存起来，并给from端回复ack code:fail
		}
		
		return null;
	}
}
