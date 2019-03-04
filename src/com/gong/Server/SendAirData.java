package com.gong.Server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.*;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * 使用非阻塞通信技术，开发航显系统的服务器端程序
 * 
 * @author jpgong
 *
 */
public class SendAirData {
	private Selector selector = null;
	private ServerSocketChannel ServerSocketChannel = null;
	private int port = 9999;
	private Charset charset = Charset.forName("GBK");

	/**
	 * 对服务器进行初始配置，绑定地址和端口
	 */
	@SuppressWarnings("static-access")
	public SendAirData() {
		try {
			selector = Selector.open();
			ServerSocketChannel = ServerSocketChannel.open();
			// 使得在同一主机上关闭了该服务器程序，紧接着启动该服务器时可以绑定到相同的端口
			ServerSocketChannel.socket().setReuseAddress(true);
			// 让该服务器处于非阻塞状态
			ServerSocketChannel.configureBlocking(false);
			// 将服务器进程与一个本地端口绑定
			ServerSocketChannel.socket().bind(new InetSocketAddress(port));
			System.out.println("服务器连接成功！");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 在该方法中处理客户端发过来的请求（如接收连接就绪事件，写就绪事件）
	 * 
	 * @throws Exception
	 */
	public void server() throws IOException {
		// ServerSocketChannel通过register()方法向selector注册接收连接就绪事件
		ServerSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		/*
		 * 返回相关事件已发生的SelectionKey对象数目，如果一个也没有，就进入阻塞状态，直到出现以下情况之一，才从select中返回
		 * 1、至少有一个SelectionKey的相关事件已发生
		 * 2、其他线程调用了Selector的wakeup()方法，导致执行select()方法的线程立即从select()中返回
		 * 3、当前执行select()方法的线程被其他线程中断 4、超出等待时间
		 */
		while (selector.select() > 0) {
			// 将所有相关事件被Selector捕获的SelectionKey对象用set集合装起来
			Set<SelectionKey> readyKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = readyKeys.iterator();

			// 遍历set集合中所有捕获的SelectionKey对象
			while (iterator.hasNext()) {
				SelectionKey selectionKey = null;
				try {
					// 取出一个SelectionKey事件，用来判断他的注册事件类型
					selectionKey = iterator.next();
					// 将取出的注册事件剔除，因为待会就要去处理他了
					iterator.remove();

					/*
					 * 处理接收连接就绪事件
					 */
					if (selectionKey.isAcceptable()) {
						// 连接就绪事件发生，需要创建SocketChannel对象来处理读、写事件
						// 通过channel()方法返回一个与该SelectionKey对象关联的SelectableChannel对象
						ServerSocketChannel ssc = (ServerSocketChannel) selectionKey.channel();
						SocketChannel socketChannel = ssc.accept();
						Socket socket = socketChannel.socket();
						System.out.println("接收到客户端连接，来自：" + socket.getInetAddress() + ":" + socket.getPort());
						
						socketChannel.configureBlocking(false);
						// 指向隐含数组byte的索引，即文件指针
						long point = -1;
						// socketChannel向selector注册读就绪和写就绪注册事件，并为SelectionKey关联一个附件
						socketChannel.register(selector, SelectionKey.OP_WRITE, point);
					}
					/*
					 * 处理写就绪事件
					 */
					if (selectionKey.isWritable()) {
						send(selectionKey);
					}
				} catch (IOException e) {
					e.printStackTrace();
					if (selectionKey != null) {
						selectionKey.cancel();
						selectionKey.channel().close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 将文本中的航显系统信息发给客服端
	 * 
	 * @param selectionKey
	 * @throws Exception
	 */
	private void send(SelectionKey selectionKey) throws Exception {

		// 通过SelectionKey得到他的附件和SocketChannel
		long point = (long) selectionKey.attachment();
		SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

		// 已只读模式处理fds_data(lab4).txt文件中的航显信息
		RandomAccessFile raf = new RandomAccessFile("fds_data(lab4).txt", "r");
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("fds_data(lab4).txt")));
		// 用来存放从文件中读取的字节
	    byte[] buffer = new byte[1024];
		
		if (point == -1) {
			int count = 0;
			while (br.readLine() != null) {
				count++;
			}
			ByteBuffer countBuff = encode("共有" + count + "条航班数据待处理\r\n");
			socketChannel.write(countBuff);
			br.close();
			point = 0;
			selectionKey.attach(point);
		}

		// 用来统计一次读操作读了多少大小的数据
		int hasRead = 0;
		// 通过文件指针来确定读取文件的位置
		raf.seek(point);
		String msg = null;

		// 能读多少读多少，只要读取的字节数大于0就进行写操作
		if ((hasRead = raf.read(buffer)) > 0) {
			Thread.sleep(100);
			msg = new String(buffer, 0, hasRead);
			ByteBuffer outputBuffer = encode(msg);

			// 判断是否还有未处理的字节，确保一次发送一行
			// 在非阻塞模式下确保发送一行数据，outputBuffer中存放了要发送的数据编码
			while (outputBuffer.hasRemaining())
				socketChannel.write(outputBuffer);
			// 记录读取完后的位置，便于下次读取
			point += hasRead;
			selectionKey.attach(point);
		} else {
			selectionKey.cancel();
			socketChannel.close();
			System.out.println("关闭与客户的连接");
			raf.close();
		}

	}

	/**
	 * 将字符串转换为字节序列，进行编码
	 * 
	 * @param outputData
	 *            需要转换的字符串
	 * @return
	 */
	private ByteBuffer encode(String outputData) {
		return charset.encode(outputData);
	}

	public static void main(String[] args) throws IOException {
		SendAirData sendAirData = new SendAirData();
		sendAirData.server();

	}
}