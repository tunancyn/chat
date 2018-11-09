package com.services;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.ibatis.session.SqlSession;

import com.bean.User;
import com.dao.IUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mybatis.MyBatisUtils;
import com.server.io.EnMsgType;
import com.server.io.WorkerTask;

public class ChangePwd {
	private ObjectNode node;
	
	public ChangePwd(ObjectNode node){
		this.node = node;
	}
	
	public ObjectNode process() throws IOException{
		String name = node.get("name").asText();
		String newpwd = node.get("newpwd").asText();
		String renewpwd = node.get("renewpwd").asText();
		
		ObjectMapper jsonMapper = new ObjectMapper();
		ObjectNode node2 = jsonMapper.createObjectNode();

		if(newpwd == renewpwd){
			SqlSession session = MyBatisUtils.getSession();
			try{
				IUserService service = session.getMapper(IUserService.class);
				service.changePwd(newpwd,name);
			}finally{
				session.close();
			}
		
			node2.put("msgtype", EnMsgType.EN_MSG_ACK.toString());
			node2.put("srcmsg", EnMsgType.EN_MSG_MODIFY_PWD.toString());
			node2.put("code", "success");
		}else{
			node2.put("code", "fail");
		}
		return node2;
	}
}
