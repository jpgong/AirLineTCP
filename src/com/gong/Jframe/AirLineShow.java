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
		setFont(new Font("�����п�", Font.BOLD, 17));
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
				// ���ȴ� SwingWorker�Ա��� worker �߳���ִ�С�
				recieveAndAnalyse.execute();
			}
			btnStart.setEnabled(false);
		});
		btnStart.setFont(new Font("����", Font.BOLD, 14));
		panel.add(btnStart);

		JButton btnStop = new JButton("\u7ED3\u675F");
		btnStop.addActionListener((e) -> {
			System.exit(0);
		});
		btnStop.setFont(new Font("����", Font.BOLD, 14));
		panel.add(btnStop);
		
		JLabel label = new JLabel("\u505C\u673A\u4F4D\u4FE1\u606F\u663E\u793A\u7A97\u53E3");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setForeground(Color.RED);
		label.setFont(new Font("���Ŀ���", Font.BOLD, 18));
		contentPane.add(label, BorderLayout.NORTH);

		pack();
		setLocationRelativeTo(null);
	}

	/**
	 * �̳�SwingWorker�����࣬����ʹ���ݸ��¸�����
	 * 
	 * @author jpgong
	 *
	 */
	class RecieveAndAnalyse extends SwingWorker<String, DataBean> {

		// ������¼������Ϣ���ļ���λ��
		public int numberOfline = 0;
		// ����һ��ת�������ͺ��չ�˾��Ϣ�Ĺ�����
		private CodeToName airAndPlane = new CodeToName("AirLine.txt");
		// ���ڴ�ź���������Ϣ
		private HashMap<String, String> flinMap = new HashMap<>();
		// ���ڴ�ź���ID�͸ú�����Ϣ����������,�ж��Ƿ�ú�����Ϣ��Ҫ����
		Hashtable<String, Integer> numTable = new Hashtable<>();

		/*
		 * ������һ������ִ�к�̨�������շ�����һ�����Ľ�� ��ȡ������Ϣ�ļ���ÿ�ζ�ȡһ�С� ÿ��ȡһ�У�Ȼ�����publish()�����������õ�����
		 * ����process()���������ڽ������ʾ
		 * publish()�����Ǵ����м�������ݵ��¼������̣߳���doInBackground()�е��ø÷��� (non-Javadoc)
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		protected String doInBackground() throws Exception {

			String StartLines = null;
			// �Ժ���������Ӽ�ֵ��
			flinMap.put("D", "���ں���");
			flinMap.put("I", "���ʺ���");
			flinMap.put("M", "��Ϻ���");

			try (// ʹ��Socket���Ӻ�����Ϣ���ڵķ�����
					Socket socket = new Socket("10.8.48.192", 9999);
					Scanner scanner = new Scanner(socket.getInputStream())) {
				String line = null;
				StartLines = scanner.nextLine();
				System.out.println(StartLines);
				JOptionPane.showMessageDialog(null, "���ӷ������ɹ�\r\n" + StartLines);
//				int count = 0;
				while (scanner.hasNext()) {
					if (!(line = scanner.nextLine()).equals("no data!")) {
//						System.out.println(count++ + " : " + line);
						// �жϺ�����Ϣ����
						// ���������Ϣ������ȡ�ͷ���
						if (line.substring(20, 29).equals("flop_stnd")) {
							// ���������Ҫ��ʾ���ݵķ�װ��
							DataBean daBean = new DataBean();
							// ��ȡÿ����Ϣ��ʵ�ʲ���ʱ��
							String dttm = "dttm=(\\d+)";
							Pattern dttmPattern = Pattern.compile(dttm);
							Matcher dttmMatcher = dttmPattern.matcher(line);
							// ʹ���µ�����ʱ����Ե�ǰʱ����з�װ
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

							// ��ȡ�ַ�����ֻ�������ֵ���Ϣ����Ϣ���ݺ���Ψһ��ʶ
							// String flid = "flid=(\\d+)";
							// Pattern flidPattern = Pattern.compile(flid);
							// Matcher flidMatcher = flidPattern.matcher(line);

							// ��ȡ�ַ����а�����д��ĸ�����ֺ͡�-������Ϣ����Ϣ�����Ǻ�����Ҫ��Ϣ
							String ffid = "ffid=([A-Z0-9\\-]+)";
							Pattern ffidPattern = Pattern.compile(ffid);
							Matcher ffidMatcher = ffidPattern.matcher(line);

							// System.out.println("����ID: " + flidMatcher.group(1) + "; ");
							while (ffidMatcher.find()) {
								// System.out.println("�ɶ�����ID: " + ffidMatcher.group(1) + ": ");
								String message = ffidMatcher.group(1);
								// ���ɶ�����ID�е���Ϣ�ٴν��в��
								// �Ժ������Ϣ������ȡ
								String aString = message.substring(0, 2) + "-([A-Z0-9]+)";
								Pattern aPattern = Pattern.compile(aString);
								Matcher aMatcher = aPattern.matcher(message);
								String airNum = null;
								while (aMatcher.find()) {
									airNum = aMatcher.group(1);
									daBean.setAirNum(airNum);
								}
								// �Ժ���ָʾ��Ϣ������ȡ
								String bString = airNum + "-([A-Z]+)";
								Pattern bPattern = Pattern.compile(bString);
								Matcher bMatcher = bPattern.matcher(message);
								String airLead = null;
								while (bMatcher.find()) {
									airLead = bMatcher.group(1);
								}
								// �Ժ���ʱ����Ϣ������ȡ
								String cString = airLead + "-([A-Z0-9]+)";
								Pattern cPattern = Pattern.compile(cString);
								Matcher cMatcher = cPattern.matcher(message);
								String airTime = null;
								while (cMatcher.find()) {
									airTime = cMatcher.group(1);
								}
								// �Ժ���ָʾ����Ϣ������ȡ
								String dString = airTime + "-([A-Z]+)";
								Pattern dPattern = Pattern.compile(dString);
								Matcher dMatcher = dPattern.matcher(message);
								String airPoint = null;
								while (dMatcher.find()) {
									airPoint = dMatcher.group(1);
								}
								//
								// System.out.print("�ú��չ�˾�ǣ�" + airAndPlane.getNameFromCode(message.substring(0,
								// 2))
								// + "; ������ǣ�" + airNum + "; ����" + (airLead.equals("A") ? "����" : "���")
								// + "��ʱ���ǣ�" + getTime(airTime));

								// �Ժ����ǵ��ﻹ����۽����ж�
								if (airLead.equals("A")) {
									daBean.setAirLead("����");
								} else {
									daBean.setAirLead("���");
								}
								// ��������Ϣ��ӵ��������������
								daBean.setAirComp(airAndPlane.getNameFromCode(message.substring(0, 2)));

								switch (airPoint) {
								case "D":
									// System.out.println("; �ú����ǣ�" + flinMap.get("D") + "��");
									daBean.setAirType(flinMap.get("D"));
									break;
								case "I":
									// System.out.println("; �ú����ǣ�" + flinMap.get("I") + "��");
									daBean.setAirType(flinMap.get("I"));
									break;
								case "M":
									// System.out.println("; �ú����ǣ�" + flinMap.get("M") + "��");
									daBean.setAirType(flinMap.get("M"));
									break;
								}
							}

							// ��ȡ������Ϣ�Ǻ����λ���
							String psno = "psno=(\\d+)";
							Pattern psnoPattern = Pattern.compile(psno);
							Matcher psnoMatcher = psnoPattern.matcher(line);

							// ��ȡ����Ϣ�Ǻ���ʵ�ʷ���Ļ�λ
							String psst = "PSST=(\\d+)";
							Pattern psstPattern = Pattern.compile(psst);
							Matcher psstMatcher = psstPattern.matcher(line);

							// ��ȡ����ĸ��������ɵ��ַ�������Ϣ�����ǻ�λ����ĵĿ�ʼʱ��ͽ���ʱ��
							String stst = "STST=([A-Z0-9]+)";
							Pattern ststPattern = Pattern.compile(stst);
							Matcher ststMatcher = ststPattern.matcher(line);

							String stet = "STET=([A-Z0-9]+)";
							Pattern stetPattern = Pattern.compile(stet);
							Matcher stetMatcher = stetPattern.matcher(line);

							// ��Ϊ�����ͣ��λ��ʱ��ֹһ������������Ĵ�����Ϊ�˽�һ��ͣ��λ����Ϣ�������
							while (psnoMatcher.find() && ststMatcher.find() && stetMatcher.find()) {
								// System.out.println("�û�λ�����: " + psnoMatcher.group(1));
								// System.out.println("ʵ�ʷ���Ļ�λ��: " + psstMatcher.group(1));
								// System.out.print("�û�λ����Ŀ�ʼʱ����: " + getTime(ststMatcher.group(1)) + "; ");
								// System.out.println("����ʱ����: " + getTime(stetMatcher.group(1)));
								// ���ö�̬ʱ���������������λ
								// LocalDateTime currentStartDateTime = getTime(ststMatcher.group(1));
								// LocalDateTime currentEndDateTime = getTime(stetMatcher.group(1));
								if (!psstMatcher.find()) {
									daBean.setPsst("��λΪ��");
								} else {
									daBean.setPsst(psstMatcher.group(1));
								}

								daBean.setStartTime(getTime(ststMatcher.group(1)));
								daBean.setEndTime(getTime(stetMatcher.group(1)));
								// �������õ����ݴ���process()����
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
		 * ������һ�����������¼���������еĴ��������м�������� 
		 * ����������������ʾ�������ϣ��������½���(non-Javadoc)
		 * @see javax.swing.SwingWorker#process(java.util.List)
		 */
		protected void process(List<DataBean> datas) {
			//�����ж��¼��Ƿ������
			if (isCancelled()) {
				return;
			}
			// ������¼������Ϣ�ļ�¼����
			int row;
			for (DataBean data : datas) {

				/*
				 * ��hashtable�����м���Ƿ��Ѵ��ڸú���� �������˵��������Ϣ�Ǹ�����Ϣ����ǰ��һ������Ϣ���滻��
				 * ��������в����ڸú���ţ�˵����������ǵ�һ�γ��������ϵͳ�У�ֻ��ҪҪ����Ϳ�����
				 */
				if (numTable.containsKey(data.getAirNum())) {
					row = numTable.get(data.getAirNum());
					// �øø�����Ϣ����ǰ����ֵ���Ϣ
					tm.setValueAt(data.getAirNum(), row, 0);
					tm.setValueAt(data.getAirType(), row, 1);
					tm.setValueAt(data.getAirComp(), row, 2);
					tm.setValueAt(data.getAirLead() + "(����)", row, 3);
					tm.setValueAt(data.getPsst() + "(����)", row, 4);
					tm.setValueAt(data.getStartTime() + "(����)", row, 5);
					tm.setValueAt(data.getEndTime() + "(����)", row, 6);
					break;

				}

				row = numberOfline++;
				// ����һ�γ��ֵĺ������ӵ����鼯����
				numTable.put(data.getAirNum(), row);
				// ������Ǹ�����Ϣ����ֱ�ӽ���Ϣ��ʾ��ͼ�ν��漴��
				tm.addRow(new String[] { data.getAirNum(), data.getAirType(), data.getAirComp(), data.getAirLead(),
						data.getPsst(), data.getStartTime(), data.getEndTime(), "" + row });
			}
		}

	}

	/**
	 * �Ѻ���ϵͳ��ʱ��ת��Ϊ������ʱ�� ʹ���µ�ʱ��������Ի�λ�ķ�����к�����
	 * 
	 * @param airTime
	 *            ��ȡ�����ĺ���ʱ����Ϣ
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
		// return "20" + airTime.substring(5,7) + "��" + month + "��" +
		// airTime.substring(0,2) + "�� " +
		// airTime.substring(7,9) + ":" + airTime.substring(9, 11);
	}

}
