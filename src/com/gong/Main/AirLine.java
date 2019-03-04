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
	//定义一个存储链表，用于存放读好的信息
	private LinkedList<String> airMessage = new LinkedList<>();
	//用于存放航班类型信息
	private static HashMap<String, String> flinMap = new HashMap<>();
	//用于判断链表中航显信息的状况
	private volatile boolean stop = false;
	
	/**
	 * 定义一个读文件的线程类，以行为单位读取航班信息
	 * @author jpgong
	 */
	class Reader extends Thread{
		@Override
		public void run() {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("fds_data.txt")));
				String msg = null;
				while((msg = br.readLine()) !=null){
					//在临界区上对临界资源加锁，在这里链表空间是临界资源，要注意加锁的粒度化
					synchronized (airMessage) {
						airMessage.addFirst(msg);
						//在这里读文件进程相当于生产者，当链表中（临界资源）有消息时要通知消费者
						//是对临界资源进行wait()和notify()操作
						airMessage.notify();
					}
				}
				//文件已读完，将标志位设置为true
				synchronized (airMessage) {
					stop = true;
					//读线程在读完全部文件内容之后，要通知分析线程
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
	 * 定义一个分析消息进程类，对读到的信息按要求进行分析
	 * @author jpgong
	 */
	class Analyser extends Thread{
		
		//用于存放航班ID,判断是否该航班有更新数据
		private  LinkedList<String> flidList = new LinkedList<>();
		
		@Override
		public void run() {
			//定义一个转换机场和航空公司信息的工具类
			CodeToName airAndPlane = new CodeToName("AirLine.txt");
			
			while(true) {
				//因为判断链表是否为空这个操作需要一口气做完，所以需要对临界资源加锁
				synchronized (airMessage) {
					/*
					 * 判断链表是否为空,若为空，有两种情况:
					 * 1、读消息进程还没开始运行，链表中没有内容
					 * 2、链表中的内容已经被分析完，需要结束该进程
				   	 * 所以需要设置一个标志位stop来判断是否已分析完所有消息
					 * 当stop=true时表示已分析完毕，stop=false时表示文件还没有读完，需要等待读进程的运行
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
					//对航班类型添加键值对
					flinMap.put("D", "国内航班");
					flinMap.put("I", "国际航班");
					flinMap.put("M", "混合航班");
					
					//当链表不为空时，这时分析进程类需要对链表中的一条消息进行分析
					String line = airMessage.removeLast();
					
					//判断航显消息类型
					if (line.substring(20, 29).equals("flop_stnd")) {
						//提取每条信息的实际产生时间
						String dttm = "dttm=(\\d+)";
						Pattern dttmPattern = Pattern.compile(dttm);
						Matcher dttmMatcher = dttmPattern.matcher(line);
						
						//提取字符串中只包含数字的信息，信息内容是航班号
						String flid = "flid=(\\d+)";
						Pattern flidPattern = Pattern.compile(flid);
						Matcher flidMatcher = flidPattern.matcher(line);
						
						//提取字符串中包含大写字母、数字和“-”的信息，信息内容是航班主要信息
						String ffid = "ffid=([A-Z0-9\\-]+)";
						Pattern ffidPattern = Pattern.compile(ffid);
						Matcher ffidMatcher = ffidPattern.matcher(line);
						
						//提取出的信息是航班机位序号
						String psno = "psno=(\\d+)";
						Pattern psnoPattern = Pattern.compile(psno);
						Matcher psnoMatcher = psnoPattern.matcher(line);

						//提取的信息是航班实际分配的机位
						String psst = "PSST=(\\d+)";
						Pattern psstPattern = Pattern.compile(psst);
						Matcher psstMatcher = psstPattern.matcher(line);

						//提取由字母和数字组成的字符串，信息内容是机位分配的的开始时间和结束时间
						String stst = "STST=([A-Z0-9]+)";
						Pattern ststPattern = Pattern.compile(stst);
						Matcher ststMatcher = ststPattern.matcher(line);

						String stet = "STET=([A-Z0-9]+)";
						Pattern stetPattern = Pattern.compile(stet);
						Matcher stetMatcher = stetPattern.matcher(line);
						
						while(flidMatcher.find()){
							//用于判断链表中是否已经存在该航班信息
							synchronized (flidList) {
								//如果数组中不存该航班ID，则将该航班添加到数组中
								//如果存在该航班ID，说明这条消息是更新消息
								if (flidList.contains(flidMatcher.group(1))) {
									System.out.println("更新后的信息是：");
								}else {
									flidList.add(flidMatcher.group(1));
								}
								
								while(dttmMatcher.find()){
									System.out.println("信息产生时间: " + dttmMatcher.group(1)+ "; ");
								}
								System.out.println("航班ID: " + flidMatcher.group(1)+ "; ");
								while(ffidMatcher.find()){
									System.out.println("可读航班ID: " + ffidMatcher.group(1)+ ": ");
									String message = ffidMatcher.group(1);
									//将可读航班ID中的信息再次进行拆分
									//对航班号信息进行提取
									String aString = message.substring(0, 2) + "-([A-Z0-9]+)";
									Pattern aPattern = Pattern.compile(aString);
									Matcher aMatcher = aPattern.matcher(message);
									String airNum = null;
									while(aMatcher.find()){ airNum = aMatcher.group(1);}
									//对航向指示信息进行提取
									String bString = airNum + "-([A-Z]+)";
									Pattern bPattern = Pattern.compile(bString);
									Matcher bMatcher = bPattern.matcher(message);
									String airLead = null;
									while(bMatcher.find()){ airLead = bMatcher.group(1);}
									//对航班时间信息进行提取
									String cString = airLead + "-([A-Z0-9]+)";
									Pattern cPattern = Pattern.compile(cString);
									Matcher cMatcher = cPattern.matcher(message);
									String airTime = null;
									while (cMatcher.find()) { airTime = cMatcher.group(1);}
									//对航班指示器信息进行提取
									String dString = airTime + "-([A-Z]+)";
									Pattern dPattern = Pattern.compile(dString);
									Matcher dMatcher = dPattern.matcher(message);
									String airPoint = null;
									while(dMatcher.find()){ airPoint = dMatcher.group(1);}
				
									System.out.print("该航空公司是：" + airAndPlane.getNameFromCode(message.substring(0, 2)) + "; 航班号是：" + airNum + 
											"; 即将" + (airLead.equals("A")? "到达":"离港") + "的时间是：" 
											+ getTime(airTime));
									
								    switch (airPoint) {
									case "D":
										System.out.println("; 该航班是：" + flinMap.get("D") + "。");
										break;
									case "I":
										System.out.println("; 该航班是：" + flinMap.get("I") + "。");
										break;
									case "M":
										System.out.println("; 该航班是：" + flinMap.get("M") + "。");
										break;
									}
								}
								
								//因为航班的停机位有时候不止一个，所以这里的处理是为了将一个停机位的信息完整输出
								while(psnoMatcher.find()&&psstMatcher.find()&&ststMatcher.find()&&stetMatcher.find()) {
									System.out.println("该机位序号是: " + psnoMatcher.group(1));
									System.out.println("实际分配的机位是: " + psstMatcher.group(1) );
									System.out.print("该机位分配的开始时间是: " + getTime(ststMatcher.group(1))+ "; ");
									System.out.println("结束时间是: " + getTime(stetMatcher.group(1)));
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
	 * 把航显系统的时间转换为正常的时间
	 * @param airTime 提取出来的航班时间信息
	 * @return
	 */
	public static String getTime(String airTime){
		String month = "1";
		CodeToName date = new CodeToName("Date.txt");
		month = date.getNameFromCode(airTime.substring(2,5));
//		return airTime.substring(7,9) + ":" + airTime.substring(9, 11);
		return "20" + airTime.substring(5,7) + "年" + month + "月" + airTime.substring(0,2) + "日 " +
		       airTime.substring(7,9) + ":" + airTime.substring(9, 11);
	}
}
