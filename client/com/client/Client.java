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

//ר�Ÿ�����շ��������ص���Ӧ��Ϣ
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
		 * ר�Ÿ�����շ��������ص���Ϣ���ȴ�buffer����õ�json�ַ�����
		 * Ȼ�����msgtype�� ���ݲ�ͬ��msgtype������ͬ������
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
				System.out.println(offname + "�����ߣ�");
				break;
			case EN_MSG_NOTIFY_ONLINE:
				String loginname = jsonNode.get("name").asText();
				System.out.println(loginname + "�����ߣ�");
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
//				System.out.println(excname + "���쳣���ߣ�");
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
		System.out.println("1.��¼");
		System.out.println("2.ע��");
		System.out.println("3.��������");
		System.out.println("4.�˳�ϵͳ");
		System.out.println("==============");
	}
	
	public static void showMainMenu(){
		System.out.println("==============================================");
		System.out.println("1.����modifypwd:username��ʾ���û�Ҫ�޸�����");
		System.out.println("2.����getallusers:username ��ʾ���û�Ҫ��ѯ����������Ա��Ϣ");
		System.out.println("3.����username:xxx ��ʾһ��һ����");
		System.out.println("4.����all��xxx ��ʾ����Ⱥ����Ϣ");
		System.out.println("5.���� sendfile��username��xxx ��ʾ�����ļ�����");
		System.out.println("6.����quit��username ��ʾ���û����ߣ��˳�ϵͳ");
		System.out.println("===========================");
	}
	
	public static void startSystem(String username){
		String input = "";
		
		//�����������߳�
		new Thread(new Reader(sc)).start();
		
		while(true){
			showMainMenu();
			input = in.nextLine();
			//�����û������input���ݣ���װ��ͬ��json�ַ��������͵�������
			while(true){
				String[] inputArray = input.split(":");  //�ã��ֿ�����
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
	
	//��ȡ�������߳�Ա��Ϣ
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
			System.out.println("������Ա��Ϣ��" + name);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Ⱥ��
	public static void allChat(String name,String allmsg){
		ObjectNode jsonNode = jsonMapper.createObjectNode();
		jsonNode.put("msgtype", EnMsgType.EN_MSG_CHAT_ALL.toString());
		jsonNode.put("from", name);
		jsonNode.put("msg", allmsg);
		String json = jsonNode.toString();
		
		System.out.println(json);
		
		try {
			//���͵�¼��Ϣ
			sc.write(ByteBuffer.wrap(json.getBytes()));
					
			//�����ȴ����������ص���Ϣ
//			sc.read(buffer);  //?????
			
			System.out.println("���ͳɹ�");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//�޸�����
	public static void modify_pwd(String name){
		System.out.println("�����������:");
		String oldpwd = in.nextLine();
		System.out.println("������������:");
		String newpwd = in.nextLine();
		System.out.println("��ȷ��������:");
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
			//���͵�¼��Ϣ
			sc.write(ByteBuffer.wrap(json.getBytes()));
					
			//�����ȴ����������ص���Ϣ
			sc.read(buffer);
					
			//������¼��Ӧ��code�ֶΡ�success/fail    position limit cap
			buffer.flip();
			byte[] buf = new byte[buffer.remaining()];
			buffer.get(buf, 0, buffer.remaining());
			json = new String(buf);
			buffer.clear();
			jsonNode = jsonMapper.readValue(json, ObjectNode.class);
			String code = jsonNode.get("code").asText();
			if(code.compareTo("success") == 0){
				//���ͳɹ� 
				System.out.println("�����޸ĳɹ���");
			}else{
				//ʧ��
				System.out.println("�����޸�ʧ�ܣ�������");
				modify_pwd(name);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//һ��һ����
	public static void one_oneChat(String username,String action,String msg){
		
		ObjectNode jsonNode = jsonMapper.createObjectNode();
		jsonNode.put("msgtype", EnMsgType.EN_MSG_CHAT.toString());
		jsonNode.put("from", username);
		jsonNode.put("to", action);
		jsonNode.put("msg", msg);
		String json = jsonNode.toString();
		
		System.out.println(json);
		
		try {
			//���͵�¼��Ϣ
			sc.write(ByteBuffer.wrap(json.getBytes()));
					
			//�����ȴ����������ص���Ϣ
			sc.read(buffer);
					
			//������¼��Ӧ��code�ֶΡ�success/fail    position limit cap
			buffer.flip();
			byte[] buf = new byte[buffer.remaining()];
			buffer.get(buf, 0, buffer.remaining());
			json = new String(buf);
//			System.out.println(json);
			buffer.clear();
			jsonNode = jsonMapper.readValue(json, ObjectNode.class);
			String code = jsonNode.get("code").asText();
			if(code.compareTo("success") == 0){
				//���ͳɹ� 
				System.out.println("���ͳɹ�");
			}else{
				//ʧ��
				System.out.println("����ʧ�ܣ���Ϣδ����");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//��¼
	public static void doLogin(){
		
		System.out.print("�û���:");
		String username = in.nextLine();
		System.out.print("����:");
		String password = in.nextLine();
		
		//��װ��¼json�ַ���
		ObjectNode jsonNode = jsonMapper.createObjectNode();
		jsonNode.put("msgtype", EnMsgType.EN_MSG_LOGIN.toString());
		jsonNode.put("name", username);
		jsonNode.put("pwd", password);
		String json = jsonNode.toString();
		
		System.out.println(json);
		
		try {
			//���͵�¼��Ϣ
			sc.write(ByteBuffer.wrap(json.getBytes()));
					
			//�����ȴ����������ص���Ϣ
			sc.read(buffer);
					
			//������¼��Ӧ��code�ֶΡ�success/fail    position limit cap
			buffer.flip();
			byte[] buf = new byte[buffer.remaining()];
			buffer.get(buf, 0, buffer.remaining());
			json = new String(buf);
			buffer.clear();
			jsonNode = jsonMapper.readValue(json, ObjectNode.class);
			String code = jsonNode.get("code").asText();
			if(code.compareTo("success") == 0){
				//��¼�ɹ� ����ϵͳ
				startSystem(username);
			}else{
				//��¼ʧ��
				System.out.println("��¼ʧ�ܣ������µ�¼");
				doLogin();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//ע��
	public static void registered(){
		System.out.println("�û���:");
		String username = in.nextLine();
		System.out.println("����:");
		String password = in.nextLine();
		System.out.println("����:");
		String email = in.nextLine();
		
		//��װ��¼json�ַ���
		ObjectNode jsonNode = jsonMapper.createObjectNode();
		jsonNode.put("msgtype", EnMsgType.EN_MSG_REGISTER.toString());
		jsonNode.put("name", username);
		jsonNode.put("pwd", password);
		jsonNode.put("email", email);
		String json = jsonNode.toString();
		
		System.out.println(json);
		try {
			//����ע����Ϣ
			sc.write(ByteBuffer.wrap(json.getBytes()));
			
			//�����ȴ����������ص���Ϣ
			sc.read(buffer);
			
			//����ע����Ӧ��code�ֶΡ�success/fail    position limit cap
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
				//ע��ɹ� ����ϵͳ
				System.out.println("ע��ɹ�����ѡ��");
				return;
			}else{
				//ע��ʧ��
				System.out.println("ע��ʧ�ܣ����û�����ע�ᣬ������ע��");
				registered();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//��������
	public static void forgetPwd(){
		System.out.println("�û���:");
		String username = in.nextLine();
		System.out.println("����:");
		String email = in.nextLine();
		
		//��װ��¼json�ַ���
		ObjectNode jsonNode = jsonMapper.createObjectNode();
		jsonNode.put("msgtype", EnMsgType.EN_MSG_FORGET_PWD.toString());
		jsonNode.put("name", username);
		jsonNode.put("email", email);
		String json = jsonNode.toString();
		
		System.out.println(json);
		try {
			//�����������������Ϣ
			sc.write(ByteBuffer.wrap(json.getBytes()));
			
			//�����ȴ����������ص���Ϣ
			sc.read(buffer);
			
			//������Ӧ��code�ֶΡ�success/fail    position limit cap
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
				System.out.println("�һ�����ɹ����뵽����鿴����");	
				return;
			}else{
				//
				System.out.println("�һ�����ʧ��!�����������û���������");
				forgetPwd();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//��������
	public static void offLine(String action,String name){
		ObjectNode jsonNode = jsonMapper.createObjectNode();
		jsonNode.put("name", name);
		jsonNode.put("msgtype", EnMsgType.EN_MSG_OFFLINE.toString());
		String json = jsonNode.toString();
		
		System.out.println(json);
		try {
			//���������Ϣ
			sc.write(ByteBuffer.wrap(json.getBytes()));
			
			//�����ȴ����������ص���Ϣ
			sc.read(buffer);
			
//			System.exit(0);
			
			//������Ӧ��code�ֶΡ�success/fail    position limit cap
			buffer.flip();
			byte[] buf = new byte[buffer.remaining()];
			buffer.get(buf, 0, buffer.remaining());
			json = new String(buf);
			buffer.clear();
			jsonNode = jsonMapper.readValue(json, ObjectNode.class);
			String code = jsonNode.get("code").asText();
			if(code.compareTo("success") == 0){
				System.out.println("���ѳɹ�����!");	
				System.exit(0);
			}else{		
				System.out.println("����ʧ��!");
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
			System.out.print("��ѡ��:");
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
				System.out.println("������Ч�����룬����������!");
				break;
			}
		}
	}
}
