package com.server.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
	private Selector ss;
	private ServerSocketChannel ssc;
	private LinkedList<Worker> queue; 
	private static final int CPU_NUM = 3; 
	private static ExecutorService pool = null;
	
	static{
		pool = Executors.newFixedThreadPool(CPU_NUM);
	}
	
	public Server() throws IOException{
		ss = Selector.open();
		ssc = ServerSocketChannel.open();
		ssc.bind(new InetSocketAddress("127.0.0.1", 6000));
		ssc.configureBlocking(false);
		ssc.register(ss, SelectionKey.OP_ACCEPT);
		
		//开启线程池，提交worker子任务
		queue = new LinkedList<Worker>();
		for(int i=0; i<CPU_NUM; ++i){
			Worker task = new Worker();
			queue.offer(task);
			pool.submit(task);
		}
	}
	
	public void startServer() throws IOException{
		System.out.println("server is running...");
		
		while(!Thread.currentThread().isInterrupted()){
			int num = ss.select();
			if(num <= 0){
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
				
				if(key.isAcceptable()){
					SocketChannel sc = ssc.accept();
					sc.configureBlocking(false);
					
					//按照轮询的方式给每个worker子线程派发channel
					Worker worker = queue.poll();
					worker.addSocketChannel(sc);
					queue.offer(worker);
					worker.getSelector().wakeup();
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Server s = new Server();
		s.startServer();
	}
}
