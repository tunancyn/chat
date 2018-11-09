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

class NIOServer{
	private Selector ss;
	private ServerSocketChannel ssc;
	private LinkedList<WorkerTask> queue;
	
	public NIOServer(String ip, int port, WorkerTask[] tasks)throws Exception{
		ss = Selector.open();
		ssc = ServerSocketChannel.open();
		ssc.bind(new InetSocketAddress(ip, port));
		ssc.configureBlocking(false);
		ssc.register(ss, SelectionKey.OP_ACCEPT);
		
		queue = new LinkedList<WorkerTask>();
		for(WorkerTask t : tasks){
			queue.offer(t);
		}
	}
	
	public void showInfo(long id, String info){
		System.out.println(id + " " + info);
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
					key.cancel();
					continue;
				}
				
				if(key.isAcceptable()){
					SocketChannel _sc = ssc.accept();
					_sc.configureBlocking(false);
					showInfo(Thread.currentThread().getId(), " recv new client:" + _sc.getRemoteAddress());
					//采用轮询给每个子线程注册新用户
					WorkerTask task = queue.poll();
					//把连接到的通道SocketChannel添加到list里
					task.setNewChannel(_sc);
					//wakeup是一定会把阻塞的任务唤醒一次的，不管是在阻塞还是在运行(下一次阻塞的时候)
					task.getSelector().wakeup();
					_sc.register(task.getSelector(), SelectionKey.OP_READ);
					queue.offer(task);
				}
			}
		}
	}
}

public class ServerIO {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		final int CPU_NUM = 3;
		ExecutorService pool = Executors.newFixedThreadPool(CPU_NUM);
		WorkerTask[] tasks = new WorkerTask[CPU_NUM];
		for(int i=0; i<CPU_NUM; ++i){
			tasks[i] = new WorkerTask();
			pool.submit(tasks[i]);
		}
		
		NIOServer server = new NIOServer("127.0.0.1", 6000, tasks);
		server.startServer();
		
		pool.shutdown();
	}
}
