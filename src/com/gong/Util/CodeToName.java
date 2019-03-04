package com.gong.Util;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * 对航显消息的转换类
 * 通过properties对键值对进行管理
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
