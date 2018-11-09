package com.test.unit;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.junit.Test;

import com.client.EnMsgType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestServerUnit {
	//@Test
	public void testLogin1() throws IOException{
		
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		ObjectMapper jsonMapper = new ObjectMapper();
		
		
		
		long begin = System.currentTimeMillis();
		int count = 20000;
		try {
			while(count-- > 0){
				
				SocketChannel sc = SocketChannel.open();
				sc.connect(new InetSocketAddress("127.0.0.1", 6000));
				
				ObjectNode jsonNode = jsonMapper.createObjectNode();
				jsonNode.put("msgtype", EnMsgType.EN_MSG_LOGIN.toString());
				jsonNode.put("name", "zhang san");
				jsonNode.put("pwd", "111111");
				String json = jsonNode.toString();
				
				//发送登录消息
				sc.write(ByteBuffer.wrap(json.getBytes()));
				
				//阻塞等待服务器返回的信息
				sc.read(buffer);
				buffer.clear();
			}
		}finally{
			System.out.println("count:" + count);
		}
		long end = System.currentTimeMillis();
		System.out.println("spend time:" + (end-begin)*1.0/1000 + "s");
	}
	
	/**
	 * 100 :  1.2s      0.2s       0.08s
	 * 10000 :11s       5s         3.4-4s
	 * 50000 :23s/22s              11-14s
	 * 
	 * 数据库I/O（磁盘I/O）          网络I/O    Json解析和打包
	 * @throws IOException
	 */
	@Test
	public void testLogin2() throws IOException{
		
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		ObjectMapper jsonMapper = new ObjectMapper();
		
		SocketChannel sc = SocketChannel.open();
		sc.connect(new InetSocketAddress("127.0.0.1", 6000));
		
		ObjectNode jsonNode = jsonMapper.createObjectNode();
		jsonNode.put("msgtype", EnMsgType.EN_MSG_LOGIN.toString());
		jsonNode.put("name", "zhang san");
		jsonNode.put("pwd", "111111");
		String json = jsonNode.toString();
		
		long begin = System.currentTimeMillis();
		try {
			int count = 1;
			while(count-- > 0){
				//发送登录消息
				sc.write(ByteBuffer.wrap(json.getBytes()));
				
				//阻塞等待服务器返回的信息
				sc.read(buffer);
				buffer.clear();
			}
		}finally{
			sc.close();
		}
		long end = System.currentTimeMillis();
		System.out.println("spend time:" + (end-begin)*1.0/1000 + "s");
	}
}
