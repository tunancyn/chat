package com.services;

import org.apache.ibatis.session.SqlSession;

import com.bean.User;
import com.dao.IUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mybatis.MyBatisUtils;
import com.server.io.EnMsgType;

public class ForgetPwd {
private ObjectNode node;
	
	public ForgetPwd(ObjectNode node){
		this.node = node;
	}
	
	public ObjectNode process(){
		String name = node.get("name").asText();
		String email = node.get("email").asText();
		
		//与数据库中的数据进行对比
		User user = null;
		SqlSession session = MyBatisUtils.getSession();
		try{
			IUserService service = session.getMapper(IUserService.class);
			user = service.queryUser(name);
		}finally{
			session.close();
		}
		
		//组装响应用的json字符串
		ObjectMapper jsonMapper = new ObjectMapper();
		ObjectNode node = jsonMapper.createObjectNode();
		node.put("msgtype", EnMsgType.EN_MSG_ACK.toString());
		node.put("srcmsg", EnMsgType.EN_MSG_FORGET_PWD.toString());
		if(user != null && user.getEmail().equals(email)){
			node.put("code", "success");
			SendEmail testemail = new SendEmail();
			String yourpwd = user.getPassword();
			testemail.setPwd(yourpwd);
			testemail.setMail(email);
			testemail.testEmail();		
		}else{
			node.put("code", "fail");
		}
		return node;
	}
}
