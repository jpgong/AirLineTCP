package com.gong.Bean;

/**
 * ����һ����ʾ���ݵ�ѡ����࣬������װ������
 * 
 * @author jpgong
 *
 */
public class DataBean {
	// �����
	private String airNum;
	// ��������
	private String airType;
	// ���չ�˾
	private String AirComp;
	// �Ժ����ǵ��ﻹ����۽����ж�
	private String AirLead;
	// ���������Ļ�λ
	private String psst;
	// ����Ļ�λ��ʼʱ��
	private String startTime;
	// ����Ļ�λ����ʱ��
	private String endTime;
	
	public String getAirNum() {
		return airNum;
	}

	public void setAirNum(String airNum) {
		this.airNum = airNum;
	}

	public String getAirType() {
		return airType;
	}

	public void setAirType(String airType) {
		this.airType = airType;
	}

	public String getAirComp() {
		return AirComp;
	}

	public void setAirComp(String airComp) {
		AirComp = airComp;
	}

	public String getAirLead() {
		return AirLead;
	}

	public void setAirLead(String airLead) {
		AirLead = airLead;
	}

	public String getPsst() {
		return psst;
	}

	public void setPsst(String psst) {
		this.psst = psst;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
}
