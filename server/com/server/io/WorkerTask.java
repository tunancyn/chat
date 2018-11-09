package com.server.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.client.EnMsgType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.services.AllChat;
import com.services.ChangePwd;
import com.services.ForgetPwd;
import com.services.GetAll;
import com.services.Login;
import com.services.OffLine;
import com.services.OneChat;
import com.services.Register;

public class WorkerTask implements Runnable{
	private Selector ss;
	private ByteBuffer buffer;
	private List<SocketChannel> list;
//	public static String name;
	public static ConcurrentHashMap<String, SocketChannel> map = new ConcurrentHashMap<String, SocketChannel>();
	
	public WorkerTask(){
		list = Collections.synchronizedList(new ArrayList<SocketChannel>());
	}
	
	public void showInfo(long id, String info){
		System.out.println(id + " " + info);
	}
	
	public Selector getSelector(){
		return ss;
	}
	
	public void setNewChannel(SocketChannel sc){
		list.add(sc);
	}
	
	public ObjectNode passuser(SocketChannel sc,String json){
		try{			
			ObjectMapper jsonMapper = new ObjectMapper();
			ObjectNode node = jsonMapper.readValue(json, ObjectNode.class);
			String msgtype = node.get("msgtype").asText();
			EnMsgType type = EnMsgType.valueOf(msgtype);
			switch(type){
			case EN_MSG_LOGIN:
				Login login = new Login(sc,node);
				return login.process();
			case EN_MSG_OFFLINE:
				OffLine offline = new OffLine(sc,node);
				return offline.process();
			case EN_MSG_REGISTER:
				Register register = new Register(node);
				return register.process();
			case EN_MSG_FORGET_PWD:
				ForgetPwd forgetpwd = new ForgetPwd(node);
				return forgetpwd.process();
			case EN_MSG_CHAT:
				OneChat onechat = new OneChat(node);
				return onechat.process();
			case EN_MSG_GET_ALL_USERS:
				GetAll getall = new GetAll();
				return getall.process();
			case EN_MSG_CHAT_ALL:
				AllChat allchat = new AllChat(node);
				allchat.process();
				break;
			case EN_MSG_MODIFY_PWD:
				ChangePwd change = new ChangePwd(node);
				return change.process();
			default:
				break;
			}
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;	
	}
	
//	public void excpOffLine(SocketChannel sc){
//		StringBuilder readContent = new StringBuilder();
//		int readbytes = 0;
//		try {
//			while((readbytes=sc.read(buffer)) != -1){
//				if(readbytes == 0){
//					break;
//				}
//				buffer.flip();
//				byte[] buf = new byte[buffer.remaining()];
//				buffer.get(buf, 0, buffer.limit());
//				readContent.append(new String(buf));
//				buffer.clear();
//			}
//			
//			String msgs = readContent.toString();
//			ObjectMapper jsonMapper = new ObjectMapper();
//			ObjectNode node = jsonMapper.readValue(msgs, ObjectNode.class);
//			name = node.get("name").asText();
//			
//			ExcOff off = new ExcOff();
//			off.failOff(name);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	public void run() {
		// TODO Auto-generated method stub

		buffer = ByteBuffer.allocate(1024);
		
		try {
			ss = Selector.open();
			
			while(!Thread.currentThread().isInterrupted()){
				/**
				 * 这里阻塞，主线程向子线程这里的selector注册新用户的channel，
				 * 此时，当前进程死锁了!
				 */
				int num = ss.select();  //不用selectNow() 解决死锁问题
				if(num <= 0){
					for(int i=0;i<list.size();++i){
						//list的Channel添加到Selector里   主线程添加到list里
						list.get(i).register(ss, SelectionKey.OP_READ);  
					}
					list.clear();
					continue;
				}
				
				Iterator<SelectionKey> it = ss.selectedKeys().iterator();
				while(it.hasNext()){
					SelectionKey key = it.next();
					it.remove();
					
					if(!key.isValid()){
						key.cancel();
						continue;
					}
					
					if(key.isReadable()){
						SocketChannel _sc = (SocketChannel)key.channel();	
						//收发数据
						try{
							StringBuilder readContent = new StringBuilder();
							int readbytes = 0;
							while((readbytes=_sc.read(buffer)) != -1){
								if(readbytes == 0){
									break;
								}
								buffer.flip();
								byte[] buf = new byte[buffer.remaining()];
								buffer.get(buf, 0, buffer.limit());
								readContent.append(new String(buf));
								buffer.clear();
							}
							if(readbytes == -1){
//								excpOffLine(_sc);
								_sc.close();
								key.cancel();
								continue;
							}else{
							ObjectNode res = passuser(_sc,readContent.toString());
//							System.out.println(res.toString());
							_sc.write(ByteBuffer.wrap((res+"\n").getBytes())); //fail success
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
//							excpOffLine(_sc);
							_sc.close();
							key.cancel();
							continue;
						}						
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
