package com.services;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.ibatis.session.SqlSession;
import com.bean.User;
import com.dao.IUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mybatis.MyBatisUtils;
import com.server.io.EnMsgType;
import com.server.io.WorkerTask;

public class Login {
	private ObjectNode node;
	private SocketChannel sc;
	
	public Login(SocketChannel sc,ObjectNode node){
		this.node = node;
		this.sc = sc;
	}
	
	public static void allLogin(String name1) throws IOException{		
		ObjectMapper jsonMapper = new ObjectMapper();
		ObjectNode node1 = jsonMapper.createObjectNode();
		node1.put("msgtype", EnMsgType.EN_MSG_NOTIFY_ONLINE.toString());
		node1.put("name", name1);
		
		Set<Entry<String, SocketChannel>> set = WorkerTask.map.entrySet();
		Iterator<Entry<String, SocketChannel>> it = set.iterator();
		while(it.hasNext()){
			Entry<String, SocketChannel> r = it.next();
			SocketChannel s = r.getValue();
			s.write(ByteBuffer.wrap((node1+"\n").getBytes()));
		}
	}
	
	public ObjectNode process() throws IOException{
		String name = node.get("name").asText();
		String pwd = node.get("pwd").asText();

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
		ObjectNode node2 = jsonMapper.createObjectNode();
		node2.put("msgtype", EnMsgType.EN_MSG_ACK.toString());
		node2.put("srcmsg", EnMsgType.EN_MSG_LOGIN.toString());
		if(user != null && user.getPassword().equals(pwd)){
			node2.put("code", "success");
			allLogin(name);
			WorkerTask.map.put(name, sc);
		}else{
			node2.put("code", "fail");
		}
		return node2;
	}

}
