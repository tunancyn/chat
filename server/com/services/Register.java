package com.services;

import org.apache.ibatis.session.SqlSession;

import com.bean.User;
import com.dao.IUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mybatis.MyBatisUtils;
import com.server.io.EnMsgType;

public class Register {
	private ObjectNode node;
	
	public Register(ObjectNode node){
		this.node = node;
	}
	
	public ObjectNode process(){
		String name = node.get("name").asText();
		String pwd = node.get("pwd").asText();
		String email = node.get("email").asText();
	
		//与数据库中的数据进行对比
		User user = new User();
		user.setUsername(name);
		user.setPassword(pwd);
		user.setEmail(email);
		System.out.println(user.getUsername()+" "+ user.getPassword() +" "+ user.getEmail());
		SqlSession session = MyBatisUtils.getSession();
		try{
			IUserService service = session.getMapper(IUserService.class);
			service.insertUser(user);
		}finally{
			session.close();
		}
		
		//组装响应用的json字符串
		ObjectMapper jsonMapper = new ObjectMapper();
		ObjectNode node = jsonMapper.createObjectNode();
		node.put("msgtype", EnMsgType.EN_MSG_ACK.toString());
		node.put("srcmsg", EnMsgType.EN_MSG_REGISTER.toString());
		if(user.getUsername() == name){
			node.put("code", "success");
		}else{
			node.put("code", "fail");
		}

		return node;

	}
}
