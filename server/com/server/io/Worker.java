package com.server.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.services.Chat;
import com.services.Login;
import com.services.Register;

public class Worker implements Runnable {
	private Selector ss;
	private List<SocketChannel> queue;
	private ByteBuffer buffer = ByteBuffer.allocate(1024);
	
	public Worker() throws IOException{
		ss = Selector.open();
		queue = Collections.synchronizedList(new LinkedList<SocketChannel>());
	}
	
	public Selector getSelector(){
		return ss;
	}
	
	public void addSocketChannel(SocketChannel sc){
		queue.add(sc);
	}
	
	public ObjectNode parseJson(String json, SocketChannel sc){
		try {
			ObjectMapper jsonMapper = new ObjectMapper();
			ObjectNode node = jsonMapper.readValue(json, ObjectNode.class);
			String msgtype = node.get("msgtype").asText();
			EnMsgType type = EnMsgType.valueOf(msgtype);
			switch(type){
			case EN_MSG_LOGIN:   //鐧诲綍涓氬姟澶勭悊
				//瀛樺偍鐢ㄦ埛鐨勫鍚嶄俊鎭拰瀵瑰簲鐨剆ocketchannel淇℃伅
				String name = node.get("name").asText();
				UserInfo.addUserSc(name, sc);
				Login login = new Login(sc,node);
				return login.process();
			case EN_MSG_REGISTER:   //娉ㄥ唽涓氬姟澶勭悊
				Register re = new Register(node);
				return re.process();
			case EN_MSG_CHAT:
				Chat ct = new Chat(node);
				ct.process(json);
			default:
				break;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void startWorker() throws IOException{
		while(!Thread.currentThread().isInterrupted()){
			int num = ss.select();
			if(num <= 0){
				for(int i=0; i<queue.size(); ++i){
					queue.get(i).register(ss, SelectionKey.OP_READ);
				}
				queue.clear();
				continue;
			}
			
			Iterator<SelectionKey> it = ss.selectedKeys().iterator();
			while(it.hasNext()){
				SelectionKey key = it.next();
				it.remove();
				
				if(!key.isValid()){
					key.channel().close();
					key.cancel();
					continue;
				}
				
				if(key.isReadable()){
					SocketChannel sc = (SocketChannel)key.channel();
					sc.configureBlocking(false);
					
					try{
						int readbytes = 0;
						StringBuilder jsonstr = new StringBuilder();
						while((readbytes = sc.read(buffer)) >= 0){
							if(readbytes == 0){
								break;
							}
							buffer.flip();
							jsonstr.append(new String(buffer.array(), 0, buffer.remaining()));
							buffer.clear();
						}
						if(readbytes == -1){
							//杩滅鍏抽棴浜唖ocket
							sc.close();
							key.cancel();
							continue;
						}
						
						//瑙ｆ瀽json瀛楃涓�
						System.out.println(jsonstr);
						String responseJson = parseJson(jsonstr.toString(), sc).toString();
						System.out.println("2:" + responseJson);
						if(responseJson != null){
							responseJson+="\n";
							sc.write(ByteBuffer.wrap(responseJson.getBytes()));
						}
					}catch(IOException e){
						//鐢ㄦ埛寮傚父涓嬬嚎
						sc.close();
						key.cancel();
						continue;
					}
				}
			}
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			startWorker();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
