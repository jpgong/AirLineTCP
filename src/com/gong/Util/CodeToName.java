package com.gong.Util;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * �Ժ�����Ϣ��ת����
 * ͨ��properties�Լ�ֵ�Խ��й���
 * @author jpgong
 *
 */
public class CodeToName {
	private Properties properties = new Properties();
	public CodeToName(String fileName) {
		try {
			properties.load(new FileReader(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public String getNameFromCode(String code){
		return properties.getProperty(code);
	}
//	public static void main(String[] args) {
//		CodeToName codeToName = new CodeToName("AirLine.txt");
//		System.out.println(codeToName.getNameFromCode("MU"));
//	}
}
