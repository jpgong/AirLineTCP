package com.gong.Jframe;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.JButton;
import java.awt.Font;
import javax.swing.table.DefaultTableModel;

import com.gong.Bean.DataBean;
import com.gong.Util.CodeToName;

import java.net.Socket;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.Toolkit;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class AirLineShow extends JFrame {
	private JPanel contentPane;
	private JTable table;
	private RecieveAndAnalyse recieveAndAnalyse;
	private DefaultTableModel tm;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				AirLineShow frame = new AirLineShow();
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public AirLineShow() {
		setForeground(new Color(255, 0, 0));
		setIconImage(Toolkit.getDefaultToolkit()
				.getImage("G:\\Cryptography\\AirLine\\b2de9c82d158ccbf9bd9298f10d8bc3eb135411a.jpg"));
		setFont(new Font("华文行楷", Font.BOLD, 17));
		setTitle("\u505C\u673A\u4F4D\u822A\u663E\u7CFB\u7EDF");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 608, 512);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, BorderLayout.CENTER);

		table = new JTable();
		tm = new DefaultTableModel(new String[] { "\u822A\u73ED\u53F7", "\u822A\u73ED\u7C7B\u578B",
				"\u822A\u7A7A\u516C\u53F8", "\u8FDB/\u51FA\u6E2F", "\u5206\u914D\u673A\u4F4D",
				"\u5F00\u59CB\u65F6\u95F4", "\u7ED3\u675F\u65F6\u95F4", "\u6570\u636E\u884C\u53F7" }, 0);
		table.setModel(tm);
		scrollPane.setViewportView(table);

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.SOUTH);

		JButton btnStart = new JButton("\u5F00\u59CB");
		btnStart.addActionListener((e) -> {
			if (recieveAndAnalyse == null) {
				recieveAndAnalyse = new RecieveAndAnalyse();
				// 调度此 SwingWorker以便在 worker 线程上执行。
				recieveAndAnalyse.execute();
			}
			btnStart.setEnabled(false);
		});
		btnStart.setFont(new Font("楷体", Font.BOLD, 14));
		panel.add(btnStart);

		JButton btnStop = new JButton("\u7ED3\u675F");
		btnStop.addActionListener((e) -> {
			System.exit(0);
		});
		btnStop.setFont(new Font("楷体", Font.BOLD, 14));
		panel.add(btnStop);
		
		JLabel label = new JLabel("\u505C\u673A\u4F4D\u4FE1\u606F\u663E\u793A\u7A97\u53E3");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setForeground(Color.RED);
		label.setFont(new Font("华文楷体", Font.BOLD, 18));
		contentPane.add(label, BorderLayout.NORTH);

		pack();
		setLocationRelativeTo(null);
	}

	/**
	 * 继承SwingWorker的子类，可以使数据更新更方便
	 * 
	 * @author jpgong
	 *
	 */
	class RecieveAndAnalyse extends SwingWorker<String, DataBean> {

		// 用来记录航显信息在文件的位置
		public int numberOfline = 0;
		// 定义一个转换机场和航空公司信息的工具类
		private CodeToName airAndPlane = new CodeToName("AirLine.txt");
		// 用于存放航班类型信息
		private HashMap<String, String> flinMap = new HashMap<>();
		// 用于存放航班ID和该航班消息的所在行数,判断是否该航班信息需要更新
		Hashtable<String, Integer> numTable = new Hashtable<>();

		/*
		 * 覆盖这一方法并执行后台任务最终返回这一工作的结果 读取航显信息文件，每次读取一行。 每读取一行，然后调用publish()方法将分析好的数据
		 * 传给process()方法，用于界面的显示
		 * publish()方法是传递中间进度数据到事件分配线程，在doInBackground()中调用该方法 (non-Javadoc)
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		protected String doInBackground() throws Exception {

			String StartLines = null;
			// 对航班类型添加键值对
			flinMap.put("D", "国内航班");
			flinMap.put("I", "国际航班");
			flinMap.put("M", "混合航班");

			try (// 使用Socket连接航显信息所在的服务器
					Socket socket = new Socket("10.8.48.192", 9999);
					Scanner scanner = new Scanner(socket.getInputStream())) {
				String line = null;
				StartLines = scanner.nextLine();
				System.out.println(StartLines);
				JOptionPane.showMessageDialog(null, "连接服务器成功\r\n" + StartLines);
//				int count = 0;
				while (scanner.hasNext()) {
					if (!(line = scanner.nextLine()).equals("no data!")) {
//						System.out.println(count++ + " : " + line);
						// 判断航显消息类型
						// 对所需的消息进行提取和分析
						if (line.substring(20, 29).equals("flop_stnd")) {
							// 用来存放需要显示数据的封装类
							DataBean daBean = new DataBean();
							// 提取每条信息的实际产生时间
							String dttm = "dttm=(\\d+)";
							Pattern dttmPattern = Pattern.compile(dttm);
							Matcher dttmMatcher = dttmPattern.matcher(line);
							// 使用新的日期时间类对当前时间进行封装
							String timeAndDate = null;
							int year = 1;
							int month = 1;
							int day = 1;
							int hour = 0;
							int minute = 0;
							while (dttmMatcher.find()) {
								timeAndDate = dttmMatcher.group(1);
								year = Integer.parseInt(timeAndDate.substring(0, 4));
								month = Integer.parseInt(timeAndDate.substring(4, 6));
								day = Integer.parseInt(timeAndDate.substring(6, 8));
								hour = Integer.parseInt(timeAndDate.substring(8, 10));
								if (hour == 24) {
									hour = 0;
								}
								minute = Integer.parseInt(timeAndDate.substring(10, 12));
							}
							LocalDateTime nowDateTime = LocalDateTime.of(year, month, day, hour, minute);

							// 提取字符串中只包含数字的信息，信息内容航班唯一标识
							// String flid = "flid=(\\d+)";
							// Pattern flidPattern = Pattern.compile(flid);
							// Matcher flidMatcher = flidPattern.matcher(line);

							// 提取字符串中包含大写字母、数字和“-”的信息，信息内容是航班主要信息
							String ffid = "ffid=([A-Z0-9\\-]+)";
							Pattern ffidPattern = Pattern.compile(ffid);
							Matcher ffidMatcher = ffidPattern.matcher(line);

							// System.out.println("航班ID: " + flidMatcher.group(1) + "; ");
							while (ffidMatcher.find()) {
								// System.out.println("可读航班ID: " + ffidMatcher.group(1) + ": ");
								String message = ffidMatcher.group(1);
								// 将可读航班ID中的信息再次进行拆分
								// 对航班号信息进行提取
								String aString = message.substring(0, 2) + "-([A-Z0-9]+)";
								Pattern aPattern = Pattern.compile(aString);
								Matcher aMatcher = aPattern.matcher(message);
								String airNum = null;
								while (aMatcher.find()) {
									airNum = aMatcher.group(1);
									daBean.setAirNum(airNum);
								}
								// 对航向指示信息进行提取
								String bString = airNum + "-([A-Z]+)";
								Pattern bPattern = Pattern.compile(bString);
								Matcher bMatcher = bPattern.matcher(message);
								String airLead = null;
								while (bMatcher.find()) {
									airLead = bMatcher.group(1);
								}
								// 对航班时间信息进行提取
								String cString = airLead + "-([A-Z0-9]+)";
								Pattern cPattern = Pattern.compile(cString);
								Matcher cMatcher = cPattern.matcher(message);
								String airTime = null;
								while (cMatcher.find()) {
									airTime = cMatcher.group(1);
								}
								// 对航班指示器信息进行提取
								String dString = airTime + "-([A-Z]+)";
								Pattern dPattern = Pattern.compile(dString);
								Matcher dMatcher = dPattern.matcher(message);
								String airPoint = null;
								while (dMatcher.find()) {
									airPoint = dMatcher.group(1);
								}
								//
								// System.out.print("该航空公司是：" + airAndPlane.getNameFromCode(message.substring(0,
								// 2))
								// + "; 航班号是：" + airNum + "; 即将" + (airLead.equals("A") ? "到达" : "离港")
								// + "的时间是：" + getTime(airTime));

								// 对航班是到达还是离港进行判断
								if (airLead.equals("A")) {
									daBean.setAirLead("到达");
								} else {
									daBean.setAirLead("离港");
								}
								// 将航班信息添加到定义的数据类中
								daBean.setAirComp(airAndPlane.getNameFromCode(message.substring(0, 2)));

								switch (airPoint) {
								case "D":
									// System.out.println("; 该航班是：" + flinMap.get("D") + "。");
									daBean.setAirType(flinMap.get("D"));
									break;
								case "I":
									// System.out.println("; 该航班是：" + flinMap.get("I") + "。");
									daBean.setAirType(flinMap.get("I"));
									break;
								case "M":
									// System.out.println("; 该航班是：" + flinMap.get("M") + "。");
									daBean.setAirType(flinMap.get("M"));
									break;
								}
							}

							// 提取出的信息是航班机位序号
							String psno = "psno=(\\d+)";
							Pattern psnoPattern = Pattern.compile(psno);
							Matcher psnoMatcher = psnoPattern.matcher(line);

							// 提取的信息是航班实际分配的机位
							String psst = "PSST=(\\d+)";
							Pattern psstPattern = Pattern.compile(psst);
							Matcher psstMatcher = psstPattern.matcher(line);

							// 提取由字母和数字组成的字符串，信息内容是机位分配的的开始时间和结束时间
							String stst = "STST=([A-Z0-9]+)";
							Pattern ststPattern = Pattern.compile(stst);
							Matcher ststMatcher = ststPattern.matcher(line);

							String stet = "STET=([A-Z0-9]+)";
							Pattern stetPattern = Pattern.compile(stet);
							Matcher stetMatcher = stetPattern.matcher(line);

							// 因为航班的停机位有时候不止一个，所以这里的处理是为了将一个停机位的信息完整输出
							while (psnoMatcher.find() && ststMatcher.find() && stetMatcher.find()) {
								// System.out.println("该机位序号是: " + psnoMatcher.group(1));
								// System.out.println("实际分配的机位是: " + psstMatcher.group(1));
								// System.out.print("该机位分配的开始时间是: " + getTime(ststMatcher.group(1)) + "; ");
								// System.out.println("结束时间是: " + getTime(stetMatcher.group(1)));
								// 利用动态时间随机给航班分配机位
								// LocalDateTime currentStartDateTime = getTime(ststMatcher.group(1));
								// LocalDateTime currentEndDateTime = getTime(stetMatcher.group(1));
								if (!psstMatcher.find()) {
									daBean.setPsst("机位为空");
								} else {
									daBean.setPsst(psstMatcher.group(1));
								}

								daBean.setStartTime(getTime(ststMatcher.group(1)));
								daBean.setEndTime(getTime(stetMatcher.group(1)));
								// 将分析好的数据传给process()方法
								publish(daBean);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return StartLines;
		}

		/*
		 * 覆盖这一方法来处理事件分配进程中的传过来的中间进度数据 
		 * 将传过来的数据显示到界面上，用来更新界面(non-Javadoc)
		 * @see javax.swing.SwingWorker#process(java.util.List)
		 */
		protected void process(List<DataBean> datas) {
			//用来判断事件是否处理完毕
			if (isCancelled()) {
				return;
			}
			// 用来记录航班消息的记录行数
			int row;
			for (DataBean data : datas) {

				/*
				 * 在hashtable集合中检查是否已存在该航班号 如果存在说明这条消息是更新消息，则将前面一条的消息给替换了
				 * 如果集合中不存在该航班号，说明这个航班是第一次出现在这个系统中，只需要要输出就可以了
				 */
				if (numTable.containsKey(data.getAirNum())) {
					row = numTable.get(data.getAirNum());
					// 用该更新消息覆盖前面出现的消息
					tm.setValueAt(data.getAirNum(), row, 0);
					tm.setValueAt(data.getAirType(), row, 1);
					tm.setValueAt(data.getAirComp(), row, 2);
					tm.setValueAt(data.getAirLead() + "(更新)", row, 3);
					tm.setValueAt(data.getPsst() + "(更新)", row, 4);
					tm.setValueAt(data.getStartTime() + "(更新)", row, 5);
					tm.setValueAt(data.getEndTime() + "(更新)", row, 6);
					break;

				}

				row = numberOfline++;
				// 将第一次出现的航班号添加到数组集合中
				numTable.put(data.getAirNum(), row);
				// 如果不是更新消息，则直接将消息显示在图形界面即可
				tm.addRow(new String[] { data.getAirNum(), data.getAirType(), data.getAirComp(), data.getAirLead(),
						data.getPsst(), data.getStartTime(), data.getEndTime(), "" + row });
			}
		}

	}

	/**
	 * 把航显系统的时间转换为正常的时间 使用新的时间日期类对机位的分配进行合理安排
	 * 
	 * @param airTime
	 *            提取出来的航班时间信息
	 * @return
	 */
	public static String getTime(String airTime) {
		// LocalDateTime localDateTime = null;
		// int year = 1;
		// int month = 1;
		// int day = 1;
		// int hour = 0;
		// int minute = 0;
		// CodeToName date = new CodeToName("Date.txt");
		String year = airTime.substring(5, 7);
		if (year.equals("00")) {
			return "null";
		} else {
			return airTime.substring(7, 9) + ":" + airTime.substring(9, 11);
		}
		// month = Integer.parseInt(date.getNameFromCode(airTime.substring(2,5)));
		// day = Integer.parseInt(airTime.substring(0,2));
		// hour = Integer.parseInt(airTime.substring(7,9));
		// if (hour == 24) {
		// hour = 0;
		// }
		// minute = Integer.parseInt(airTime.substring(9, 11));
		// localDateTime = LocalDateTime.of(year, month, day, hour, minute);
		// return airTime.substring(7, 9) + ":" + airTime.substring(9, 11);
		// return "20" + airTime.substring(5,7) + "年" + month + "月" +
		// airTime.substring(0,2) + "日 " +
		// airTime.substring(7,9) + ":" + airTime.substring(9, 11);
	}

}
