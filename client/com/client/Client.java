package com.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

//专门负责接收服务器返回的相应消息
class Reader implements Runnable{
	private SocketChannel sc;
	private ByteBuffer buffer = ByteBuffer.allocate(1024);
	
	public Reader(SocketChannel sc) {
		super();
		this.sc = sc;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		/**
		 * 专门负责接收服务器返回的消息，先从buffer里面得到json字符串，
		 * 然后解析msgtype， 根据不同的msgtype，做不同的事情
		 */
		while(!Thread.currentThread().isInterrupted()){
			String json = "";
			int readbytes = 0;
			try {
				readbytes = sc.read(buffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			buffer.flip();
			json = new String(buffer.array(), 0, buffer.remaining());
			buffer.clear();
			
			System.out.println(json);
			
			ObjectMapper jsonMapper = new ObjectMapper();
			ObjectNode jsonNode = null;
			try {
				jsonNode = jsonMapper.readValue(json, ObjectNode.class);
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			EnMsgType msgtype = EnMsgType.valueOf(jsonNode.get("msgtype").asText());
			switch(msgtype){
			case EN_MSG_NOTIFY_OFFLINE:
				String offname = jsonNode.get("name").asText();
				System.out.println(offname + "已下线！");
				break;
			case EN_MSG_NOTIFY_ONLINE:
				String loginname = jsonNode.get("name").asText();
				System.out.println(loginname + "已上线！");
				break;
			case EN_MSG_CHAT:
				String from = jsonNode.get("from").asText();
				String msg = jsonNode.get("msg").asText();
				System.out.println("from:"+from+" "+"msg:"+msg);
				break;
			case EN_MSG_CHAT_ALL:
				String theMsg = jsonNode.get("msg").asText();
				String fromWho = jsonNode.get("from").asText();
				System.out.println(fromWho +":"+ theMsg);
				break;
			default:
//				String excname = jsonNode.get("name").asText();
//				System.out.println(excname + "已异常下线！");
				break;
				
			}
		}
	}
}

class Write implements Runnable{

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}

public class Client {
	//private static Log log = LogFactory.getLog(Client.class);
	private static SocketChannel sc = null;
	private static Scanner in = new Scanner(System.in);
	private static ByteBuffer buffer = ByteBuffer.allocate(1024);
	private static ObjectMapper jsonMapper = new ObjectMapper();
	
	public static void showBeginMenu(){
		System.out.println("==============");
		System.out.println("1.登录");
		System.out.println("2.注册");
		System.out.println("3.忘记密码");
		System.out.println("4.退出系统");
		System.out.println("==============");
	}
	
	public static void showMainMenu(){
		System.out.println("==============================================");
		System.out.println("1.输入modifypwd:username表示该用户要修改密码");
		System.out.println("2.输入getallusers:username 表示该用户要查询所有在线人员信息");
		System.out.println("3.输入username:xxx 表示一对一聊天");
		System.out.println("4.输入all：xxx 表示发送群聊消息");
		System.out.println("5.输入 sendfile：username：xxx 表示发送文件请求");
		System.out.println("6.输入quit：username 表示该用户下线，退出系统");
		System.out.println("===========================");
	}
	
	public static void startSystem(String username){
		String input = "";
		
		//启动接收子线程
		new Thread(new Reader(sc)).start();
		
		while(true){
			showMainMenu();
			input = in.nextLine();
			//根据用户输入的input内容，组装不同的json字符串，发送到服务器
			while(true){
				String[] inputArray = input.split(":");  //用：分开数组
				String action = inputArray[0].trim();	
				switch(action){
				case "modifypwd":
					modify_pwd(inputArray[1].trim());
					break;
				case "getallusers":
					getAllUsers();
					break;
				case "all":
					allChat(username,inputArray[1].trim());
					break;
				case "sendfile":
					break;
				case "quit":
					offLine(action,inputArray[1].trim());
					break;
				default:
					one_oneChat(username,action,inputArray[1].trim());
					break;
				}
			}
		}
	}
	
	//获取所有在线成员信息
	public static void getAllUsers(){
		ObjectNode jsonNode = jsonMapper.createObjectNode();
		jsonNode.put("msgtype", EnMsgType.EN_MSG_GET_ALL_USERS.toString());
		String json = jsonNode.toString();
		
		System.out.println(json);
		
		try {
			sc.write(ByteBuffer.wrap(json.getBytes()));
			
			sc.read(buffer);
			
			buffer.flip();
			byte[] buf = new byte[buffer.remaining()];
			buffer.get(buf, 0, buffer.remaining());
			json = new String(buf);
			buffer.clear();
			jsonNode = jsonMapper.readValue(json,ObjectNode.class);
			String name = jsonNode.get("users").asText();
			System.out.println("在线人员信息：" + name);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//群聊
	public static void allChat(String name,String allmsg){
		ObjectNode jsonNode = jsonMapper.createObjectNode();
		jsonNode.put("msgtype", EnMsgType.EN_MSG_CHAT_ALL.toString());
		jsonNode.put("from", name);
		jsonNode.put("msg", allmsg);
		String json = jsonNode.toString();
		
		System.out.println(json);
		
		try {
			//发送登录消息
			sc.write(ByteBuffer.wrap(json.getBytes()));
					
			//阻塞等待服务器返回的信息
//			sc.read(buffer);  //?????
			
			System.out.println("发送成功");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//修改密码
	public static void modify_pwd(String name){
		System.out.println("请输入旧密码:");
		String oldpwd = in.nextLine();
		System.out.println("请输入新密码:");
		String newpwd = in.nextLine();
		System.out.println("请确认新密码:");
		String renewpwd = in.nextLine();
		ObjectNode jsonNode = jsonMapper.createObjectNode();
		jsonNode.put("msgtype", EnMsgType.EN_MSG_MODIFY_PWD.toString());
		jsonNode.put("name", name);
		jsonNode.put("oldpwd", oldpwd);
		jsonNode.put("newpwd", newpwd);
		jsonNode.put("renewpwd", renewpwd);
		String json = jsonNode.toString();
		System.out.println(json);
		
		try {
			//发送登录消息
			sc.write(ByteBuffer.wrap(json.getBytes()));
					
			//阻塞等待服务器返回的信息
			sc.read(buffer);
					
			//解析登录相应的code字段。success/fail    position limit cap
			buffer.flip();
			byte[] buf = new byte[buffer.remaining()];
			buffer.get(buf, 0, buffer.remaining());
			json = new String(buf);
			buffer.clear();
			jsonNode = jsonMapper.readValue(json, ObjectNode.class);
			String code = jsonNode.get("code").asText();
			if(code.compareTo("success") == 0){
				//发送成功 
				System.out.println("密码修改成功！");
			}else{
				//失败
				System.out.println("密码修改失败，请重试");
				modify_pwd(name);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//一对一聊天
	public static void one_oneChat(String username,String action,String msg){
		
		ObjectNode jsonNode = jsonMapper.createObjectNode();
		jsonNode.put("msgtype", EnMsgType.EN_MSG_CHAT.toString());
		jsonNode.put("from", username);
		jsonNode.put("to", action);
		jsonNode.put("msg", msg);
		String json = jsonNode.toString();
		
		System.out.println(json);
		
		try {
			//发送登录消息
			sc.write(ByteBuffer.wrap(json.getBytes()));
					
			//阻塞等待服务器返回的信息
			sc.read(buffer);
					
			//解析登录相应的code字段。success/fail    position limit cap
			buffer.flip();
			byte[] buf = new byte[buffer.remaining()];
			buffer.get(buf, 0, buffer.remaining());
			json = new String(buf);
//			System.out.println(json);
			buffer.clear();
			jsonNode = jsonMapper.readValue(json, ObjectNode.class);
			String code = jsonNode.get("code").asText();
			if(code.compareTo("success") == 0){
				//发送成功 
				System.out.println("发送成功");
			}else{
				//失败
				System.out.println("聊天失败，消息未发出");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//登录
	public static void doLogin(){
		
		System.out.print("用户名:");
		String username = in.nextLine();
		System.out.print("密码:");
		String password = in.nextLine();
		
		//组装登录json字符串
		ObjectNode jsonNode = jsonMapper.createObjectNode();
		jsonNode.put("msgtype", EnMsgType.EN_MSG_LOGIN.toString());
		jsonNode.put("name", username);
		jsonNode.put("pwd", password);
		String json = jsonNode.toString();
		
		System.out.println(json);
		
		try {
			//发送登录消息
			sc.write(ByteBuffer.wrap(json.getBytes()));
					
			//阻塞等待服务器返回的信息
			sc.read(buffer);
					
			//解析登录相应的code字段。success/fail    position limit cap
			buffer.flip();
			byte[] buf = new byte[buffer.remaining()];
			buffer.get(buf, 0, buffer.remaining());
			json = new String(buf);
			buffer.clear();
			jsonNode = jsonMapper.readValue(json, ObjectNode.class);
			String code = jsonNode.get("code").asText();
			if(code.compareTo("success") == 0){
				//登录成功 进入系统
				startSystem(username);
			}else{
				//登录失败
				System.out.println("登录失败，请重新登录");
				doLogin();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//注册
	public static void registered(){
		System.out.println("用户名:");
		String username = in.nextLine();
		System.out.println("密码:");
		String password = in.nextLine();
		System.out.println("邮箱:");
		String email = in.nextLine();
		
		//组装登录json字符串
		ObjectNode jsonNode = jsonMapper.createObjectNode();
		jsonNode.put("msgtype", EnMsgType.EN_MSG_REGISTER.toString());
		jsonNode.put("name", username);
		jsonNode.put("pwd", password);
		jsonNode.put("email", email);
		String json = jsonNode.toString();
		
		System.out.println(json);
		try {
			//发送注册消息
			sc.write(ByteBuffer.wrap(json.getBytes()));
			
			//阻塞等待服务器返回的信息
			sc.read(buffer);
			
			//解析注册相应的code字段。success/fail    position limit cap
			buffer.flip();
			byte[] buf = new byte[buffer.remaining()];
			buffer.get(buf, 0, buffer.remaining());
			json = new String(buf);
//			System.out.println(json);
			buffer.clear();
			jsonNode = jsonMapper.readValue(json, ObjectNode.class);
			String code = jsonNode.get("code").asText();
			System.out.println("code:" + jsonNode.get("code").asText());
			if(code.compareTo("success") == 0){
				//注册成功 进入系统
				System.out.println("注册成功，请选择：");
				return;
			}else{
				//注册失败
				System.out.println("注册失败，该用户名已注册，请重新注册");
				registered();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//忘记密码
	public static void forgetPwd(){
		System.out.println("用户名:");
		String username = in.nextLine();
		System.out.println("邮箱:");
		String email = in.nextLine();
		
		//组装登录json字符串
		ObjectNode jsonNode = jsonMapper.createObjectNode();
		jsonNode.put("msgtype", EnMsgType.EN_MSG_FORGET_PWD.toString());
		jsonNode.put("name", username);
		jsonNode.put("email", email);
		String json = jsonNode.toString();
		
		System.out.println(json);
		try {
			//发送忘记密码相关消息
			sc.write(ByteBuffer.wrap(json.getBytes()));
			
			//阻塞等待服务器返回的信息
			sc.read(buffer);
			
			//解析相应的code字段。success/fail    position limit cap
			buffer.flip();
			byte[] buf = new byte[buffer.remaining()];
			buffer.get(buf, 0, buffer.remaining());
			json = new String(buf);
			System.out.println(json);
			buffer.clear();
			jsonNode = jsonMapper.readValue(json, ObjectNode.class);
			String code = jsonNode.get("code").asText();
			if(code.compareTo("success") == 0){
				//tunancyn@163.com
				System.out.println("找回密码成功！请到邮箱查看密码");	
				return;
			}else{
				//
				System.out.println("找回密码失败!请重新输入用户名和邮箱");
				forgetPwd();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//正常下线
	public static void offLine(String action,String name){
		ObjectNode jsonNode = jsonMapper.createObjectNode();
		jsonNode.put("name", name);
		jsonNode.put("msgtype", EnMsgType.EN_MSG_OFFLINE.toString());
		String json = jsonNode.toString();
		
		System.out.println(json);
		try {
			//发送相关消息
			sc.write(ByteBuffer.wrap(json.getBytes()));
			
			//阻塞等待服务器返回的信息
			sc.read(buffer);
			
//			System.exit(0);
			
			//解析相应的code字段。success/fail    position limit cap
			buffer.flip();
			byte[] buf = new byte[buffer.remaining()];
			buffer.get(buf, 0, buffer.remaining());
			json = new String(buf);
			buffer.clear();
			jsonNode = jsonMapper.readValue(json, ObjectNode.class);
			String code = jsonNode.get("code").asText();
			if(code.compareTo("success") == 0){
				System.out.println("您已成功下线!");	
				System.exit(0);
			}else{		
				System.out.println("下线失败!");
				offLine(action,name);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.exit(0);
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		sc = SocketChannel.open();
		sc.connect(new InetSocketAddress("127.0.0.1", 6000));

		int choice = 0;
		while(true){
			showBeginMenu();
			System.out.print("请选择:");
			choice = Integer.parseInt(in.nextLine());
			
			switch(choice){
			case 1:
				doLogin();
				break;
			case 2:
				registered();
				break;
			case 3:
				forgetPwd();
				break;
			case 4:
				System.exit(0);
				break;
			default:
				System.out.println("这是无效的输入，请重新输入!");
				break;
			}
		}
	}
}
