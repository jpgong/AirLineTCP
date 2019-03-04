package com.gong.Main;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gong.Util.CodeToName;


public class AirLine {
	//����һ���洢�������ڴ�Ŷ��õ���Ϣ
	private LinkedList<String> airMessage = new LinkedList<>();
	//���ڴ�ź���������Ϣ
	private static HashMap<String, String> flinMap = new HashMap<>();
	//�����ж������к�����Ϣ��״��
	private volatile boolean stop = false;
	
	/**
	 * ����һ�����ļ����߳��࣬����Ϊ��λ��ȡ������Ϣ
	 * @author jpgong
	 */
	class Reader extends Thread{
		@Override
		public void run() {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("fds_data.txt")));
				String msg = null;
				while((msg = br.readLine()) !=null){
					//���ٽ����϶��ٽ���Դ����������������ռ����ٽ���Դ��Ҫע����������Ȼ�
					synchronized (airMessage) {
						airMessage.addFirst(msg);
						//��������ļ������൱�������ߣ��������У��ٽ���Դ������ϢʱҪ֪ͨ������
						//�Ƕ��ٽ���Դ����wait()��notify()����
						airMessage.notify();
					}
				}
				//�ļ��Ѷ��꣬����־λ����Ϊtrue
				synchronized (airMessage) {
					stop = true;
					//���߳��ڶ���ȫ���ļ�����֮��Ҫ֪ͨ�����߳�
					airMessage.notify();
					System.err.println("Read finished");
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * ����һ��������Ϣ�����࣬�Զ�������Ϣ��Ҫ����з���
	 * @author jpgong
	 */
	class Analyser extends Thread{
		
		//���ڴ�ź���ID,�ж��Ƿ�ú����и�������
		private  LinkedList<String> flidList = new LinkedList<>();
		
		@Override
		public void run() {
			//����һ��ת�������ͺ��չ�˾��Ϣ�Ĺ�����
			CodeToName airAndPlane = new CodeToName("AirLine.txt");
			
			while(true) {
				//��Ϊ�ж������Ƿ�Ϊ�����������Ҫһ�������꣬������Ҫ���ٽ���Դ����
				synchronized (airMessage) {
					/*
					 * �ж������Ƿ�Ϊ��,��Ϊ�գ����������:
					 * 1������Ϣ���̻�û��ʼ���У�������û������
					 * 2�������е������Ѿ��������꣬��Ҫ�����ý���
				   	 * ������Ҫ����һ����־λstop���ж��Ƿ��ѷ�����������Ϣ
					 * ��stop=trueʱ��ʾ�ѷ�����ϣ�stop=falseʱ��ʾ�ļ���û�ж��꣬��Ҫ�ȴ������̵�����
					 */
					if (airMessage.isEmpty()) {
						if (stop) {
							System.out.println("analyse finished!");
							return;
						} else {
							try {
								airMessage.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
					//�Ժ���������Ӽ�ֵ��
					flinMap.put("D", "���ں���");
					flinMap.put("I", "���ʺ���");
					flinMap.put("M", "��Ϻ���");
					
					//������Ϊ��ʱ����ʱ������������Ҫ�������е�һ����Ϣ���з���
					String line = airMessage.removeLast();
					
					//�жϺ�����Ϣ����
					if (line.substring(20, 29).equals("flop_stnd")) {
						//��ȡÿ����Ϣ��ʵ�ʲ���ʱ��
						String dttm = "dttm=(\\d+)";
						Pattern dttmPattern = Pattern.compile(dttm);
						Matcher dttmMatcher = dttmPattern.matcher(line);
						
						//��ȡ�ַ�����ֻ�������ֵ���Ϣ����Ϣ�����Ǻ����
						String flid = "flid=(\\d+)";
						Pattern flidPattern = Pattern.compile(flid);
						Matcher flidMatcher = flidPattern.matcher(line);
						
						//��ȡ�ַ����а�����д��ĸ�����ֺ͡�-������Ϣ����Ϣ�����Ǻ�����Ҫ��Ϣ
						String ffid = "ffid=([A-Z0-9\\-]+)";
						Pattern ffidPattern = Pattern.compile(ffid);
						Matcher ffidMatcher = ffidPattern.matcher(line);
						
						//��ȡ������Ϣ�Ǻ����λ���
						String psno = "psno=(\\d+)";
						Pattern psnoPattern = Pattern.compile(psno);
						Matcher psnoMatcher = psnoPattern.matcher(line);

						//��ȡ����Ϣ�Ǻ���ʵ�ʷ���Ļ�λ
						String psst = "PSST=(\\d+)";
						Pattern psstPattern = Pattern.compile(psst);
						Matcher psstMatcher = psstPattern.matcher(line);

						//��ȡ����ĸ��������ɵ��ַ�������Ϣ�����ǻ�λ����ĵĿ�ʼʱ��ͽ���ʱ��
						String stst = "STST=([A-Z0-9]+)";
						Pattern ststPattern = Pattern.compile(stst);
						Matcher ststMatcher = ststPattern.matcher(line);

						String stet = "STET=([A-Z0-9]+)";
						Pattern stetPattern = Pattern.compile(stet);
						Matcher stetMatcher = stetPattern.matcher(line);
						
						while(flidMatcher.find()){
							//�����ж��������Ƿ��Ѿ����ڸú�����Ϣ
							synchronized (flidList) {
								//��������в���ú���ID���򽫸ú�����ӵ�������
								//������ڸú���ID��˵��������Ϣ�Ǹ�����Ϣ
								if (flidList.contains(flidMatcher.group(1))) {
									System.out.println("���º����Ϣ�ǣ�");
								}else {
									flidList.add(flidMatcher.group(1));
								}
								
								while(dttmMatcher.find()){
									System.out.println("��Ϣ����ʱ��: " + dttmMatcher.group(1)+ "; ");
								}
								System.out.println("����ID: " + flidMatcher.group(1)+ "; ");
								while(ffidMatcher.find()){
									System.out.println("�ɶ�����ID: " + ffidMatcher.group(1)+ ": ");
									String message = ffidMatcher.group(1);
									//���ɶ�����ID�е���Ϣ�ٴν��в��
									//�Ժ������Ϣ������ȡ
									String aString = message.substring(0, 2) + "-([A-Z0-9]+)";
									Pattern aPattern = Pattern.compile(aString);
									Matcher aMatcher = aPattern.matcher(message);
									String airNum = null;
									while(aMatcher.find()){ airNum = aMatcher.group(1);}
									//�Ժ���ָʾ��Ϣ������ȡ
									String bString = airNum + "-([A-Z]+)";
									Pattern bPattern = Pattern.compile(bString);
									Matcher bMatcher = bPattern.matcher(message);
									String airLead = null;
									while(bMatcher.find()){ airLead = bMatcher.group(1);}
									//�Ժ���ʱ����Ϣ������ȡ
									String cString = airLead + "-([A-Z0-9]+)";
									Pattern cPattern = Pattern.compile(cString);
									Matcher cMatcher = cPattern.matcher(message);
									String airTime = null;
									while (cMatcher.find()) { airTime = cMatcher.group(1);}
									//�Ժ���ָʾ����Ϣ������ȡ
									String dString = airTime + "-([A-Z]+)";
									Pattern dPattern = Pattern.compile(dString);
									Matcher dMatcher = dPattern.matcher(message);
									String airPoint = null;
									while(dMatcher.find()){ airPoint = dMatcher.group(1);}
				
									System.out.print("�ú��չ�˾�ǣ�" + airAndPlane.getNameFromCode(message.substring(0, 2)) + "; ������ǣ�" + airNum + 
											"; ����" + (airLead.equals("A")? "����":"���") + "��ʱ���ǣ�" 
											+ getTime(airTime));
									
								    switch (airPoint) {
									case "D":
										System.out.println("; �ú����ǣ�" + flinMap.get("D") + "��");
										break;
									case "I":
										System.out.println("; �ú����ǣ�" + flinMap.get("I") + "��");
										break;
									case "M":
										System.out.println("; �ú����ǣ�" + flinMap.get("M") + "��");
										break;
									}
								}
								
								//��Ϊ�����ͣ��λ��ʱ��ֹһ������������Ĵ�����Ϊ�˽�һ��ͣ��λ����Ϣ�������
								while(psnoMatcher.find()&&psstMatcher.find()&&ststMatcher.find()&&stetMatcher.find()) {
									System.out.println("�û�λ�����: " + psnoMatcher.group(1));
									System.out.println("ʵ�ʷ���Ļ�λ��: " + psstMatcher.group(1) );
									System.out.print("�û�λ����Ŀ�ʼʱ����: " + getTime(ststMatcher.group(1))+ "; ");
									System.out.println("����ʱ����: " + getTime(stetMatcher.group(1)));
								}
								System.out.println("-------------------------------------------------------------------------");
							}			
						}
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		AirLine airLine = new AirLine();
		airLine.new Reader().start();
		airLine.new Analyser().start();
	}
	
	/**
	 * �Ѻ���ϵͳ��ʱ��ת��Ϊ������ʱ��
	 * @param airTime ��ȡ�����ĺ���ʱ����Ϣ
	 * @return
	 */
	public static String getTime(String airTime){
		String month = "1";
		CodeToName date = new CodeToName("Date.txt");
		month = date.getNameFromCode(airTime.substring(2,5));
//		return airTime.substring(7,9) + ":" + airTime.substring(9, 11);
		return "20" + airTime.substring(5,7) + "��" + month + "��" + airTime.substring(0,2) + "�� " +
		       airTime.substring(7,9) + ":" + airTime.substring(9, 11);
	}
}
