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
 * ʹ�÷�����ͨ�ż�������������ϵͳ�ķ������˳���
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
	 * �Է��������г�ʼ���ã��󶨵�ַ�Ͷ˿�
	 */
	@SuppressWarnings("static-access")
	public SendAirData() {
		try {
			selector = Selector.open();
			ServerSocketChannel = ServerSocketChannel.open();
			// ʹ����ͬһ�����Ϲر��˸÷��������򣬽����������÷�����ʱ���԰󶨵���ͬ�Ķ˿�
			ServerSocketChannel.socket().setReuseAddress(true);
			// �ø÷��������ڷ�����״̬
			ServerSocketChannel.configureBlocking(false);
			// ��������������һ�����ض˿ڰ�
			ServerSocketChannel.socket().bind(new InetSocketAddress(port));
			System.out.println("���������ӳɹ���");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * �ڸ÷����д���ͻ��˷�������������������Ӿ����¼���д�����¼���
	 * 
	 * @throws Exception
	 */
	public void server() throws IOException {
		// ServerSocketChannelͨ��register()������selectorע��������Ӿ����¼�
		ServerSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		/*
		 * ��������¼��ѷ�����SelectionKey������Ŀ�����һ��Ҳû�У��ͽ�������״̬��ֱ�������������֮һ���Ŵ�select�з���
		 * 1��������һ��SelectionKey������¼��ѷ���
		 * 2�������̵߳�����Selector��wakeup()����������ִ��select()�������߳�������select()�з���
		 * 3����ǰִ��select()�������̱߳������߳��ж� 4�������ȴ�ʱ��
		 */
		while (selector.select() > 0) {
			// ����������¼���Selector�����SelectionKey������set����װ����
			Set<SelectionKey> readyKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = readyKeys.iterator();

			// ����set���������в����SelectionKey����
			while (iterator.hasNext()) {
				SelectionKey selectionKey = null;
				try {
					// ȡ��һ��SelectionKey�¼��������ж�����ע���¼�����
					selectionKey = iterator.next();
					// ��ȡ����ע���¼��޳�����Ϊ�����Ҫȥ��������
					iterator.remove();

					/*
					 * ����������Ӿ����¼�
					 */
					if (selectionKey.isAcceptable()) {
						// ���Ӿ����¼���������Ҫ����SocketChannel�������������д�¼�
						// ͨ��channel()��������һ�����SelectionKey���������SelectableChannel����
						ServerSocketChannel ssc = (ServerSocketChannel) selectionKey.channel();
						SocketChannel socketChannel = ssc.accept();
						Socket socket = socketChannel.socket();
						System.out.println("���յ��ͻ������ӣ����ԣ�" + socket.getInetAddress() + ":" + socket.getPort());
						
						socketChannel.configureBlocking(false);
						// ָ����������byte�����������ļ�ָ��
						long point = -1;
						// socketChannel��selectorע���������д����ע���¼�����ΪSelectionKey����һ������
						socketChannel.register(selector, SelectionKey.OP_WRITE, point);
					}
					/*
					 * ����д�����¼�
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
	 * ���ı��еĺ���ϵͳ��Ϣ�����ͷ���
	 * 
	 * @param selectionKey
	 * @throws Exception
	 */
	private void send(SelectionKey selectionKey) throws Exception {

		// ͨ��SelectionKey�õ����ĸ�����SocketChannel
		long point = (long) selectionKey.attachment();
		SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

		// ��ֻ��ģʽ����fds_data(lab4).txt�ļ��еĺ�����Ϣ
		RandomAccessFile raf = new RandomAccessFile("fds_data(lab4).txt", "r");
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("fds_data(lab4).txt")));
		// ������Ŵ��ļ��ж�ȡ���ֽ�
	    byte[] buffer = new byte[1024];
		
		if (point == -1) {
			int count = 0;
			while (br.readLine() != null) {
				count++;
			}
			ByteBuffer countBuff = encode("����" + count + "���������ݴ�����\r\n");
			socketChannel.write(countBuff);
			br.close();
			point = 0;
			selectionKey.attach(point);
		}

		// ����ͳ��һ�ζ��������˶��ٴ�С������
		int hasRead = 0;
		// ͨ���ļ�ָ����ȷ����ȡ�ļ���λ��
		raf.seek(point);
		String msg = null;

		// �ܶ����ٶ����٣�ֻҪ��ȡ���ֽ�������0�ͽ���д����
		if ((hasRead = raf.read(buffer)) > 0) {
			Thread.sleep(100);
			msg = new String(buffer, 0, hasRead);
			ByteBuffer outputBuffer = encode(msg);

			// �ж��Ƿ���δ������ֽڣ�ȷ��һ�η���һ��
			// �ڷ�����ģʽ��ȷ������һ�����ݣ�outputBuffer�д����Ҫ���͵����ݱ���
			while (outputBuffer.hasRemaining())
				socketChannel.write(outputBuffer);
			// ��¼��ȡ����λ�ã������´ζ�ȡ
			point += hasRead;
			selectionKey.attach(point);
		} else {
			selectionKey.cancel();
			socketChannel.close();
			System.out.println("�ر���ͻ�������");
			raf.close();
		}

	}

	/**
	 * ���ַ���ת��Ϊ�ֽ����У����б���
	 * 
	 * @param outputData
	 *            ��Ҫת�����ַ���
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