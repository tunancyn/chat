package com.services;

import java.io.IOException;
import java.nio.ByteBuffer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.server.io.EnMsgType;
import com.server.io.WorkerTask;

public class OneChat {
	private ObjectNode node;
	
	public OneChat(ObjectNode node){
		this.node = node;
	}
	
	public ObjectNode process() throws IOException{
		
		if(WorkerTask.map.containsKey(node.get("to").asText())){
			WorkerTask.map.get(node.get("to").asText()).write(ByteBuffer.wrap((node+"\n").getBytes()));
		}
		
		//组装响应用的json字符串
		ObjectMapper jsonMapper = new ObjectMapper();
		ObjectNode node = jsonMapper.createObjectNode();
		node.put("msgtype", EnMsgType.EN_MSG_ACK.toString());
		node.put("srcmsg", EnMsgType.EN_MSG_CHAT.toString());
		//(node.get(mesege) != null)
		if(WorkerTask.map.containsKey(node.get("to").asText())){
			node.put("code", "success");
		}else{
			node.put("code", "fail");
		}
		return node;
	}
}
