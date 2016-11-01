package com.yogapay.couriertsi.domain;

import java.sql.Timestamp;
import java.util.Date;

public class YouxinCallLog {
	private int id;
	private Timestamp startTime;
	private Timestamp endTime;
	private int callType;//1直拨，2回拨，3ott
	private String calleePhone;//被叫号码
	private String callerPhone;
	private String callStatus;
	private int callTime;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Date getStartTime() {
		return startTime;
	}

	public Timestamp getEndTime() {
		return endTime;
	}
	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}
	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}
	public int getCallType() {
		return callType;
	}
	public void setCallType(int callType) {
		this.callType = callType;
	}
	public String getCalleePhone() {
		return calleePhone;
	}
	public void setCalleePhone(String calleePhone) {
		this.calleePhone = calleePhone;
	}
	public String getCallerPhone() {
		return callerPhone;
	}
	public void setCallerPhone(String callerPhone) {
		this.callerPhone = callerPhone;
	}
	public String getCallStatus() {
		return callStatus;
	}
	public void setCallStatus(String callStatus) {
		this.callStatus = callStatus;
	}
	public int getCallTime() {
		return callTime;
	}
	public void setCallTime(int callTime) {
		this.callTime = callTime;
	}

}
