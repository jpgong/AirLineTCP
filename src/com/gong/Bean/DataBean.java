package com.gong.Bean;

/**
 * 定义一个显示数据的选项基类，用来封装数据项
 * 
 * @author jpgong
 *
 */
public class DataBean {
	// 航班号
	private String airNum;
	// 航班类型
	private String airType;
	// 航空公司
	private String AirComp;
	// 对航班是到达还是离港进行判断
	private String AirLead;
	// 给航班分配的机位
	private String psst;
	// 分配的机位开始时间
	private String startTime;
	// 分配的机位结束时间
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
