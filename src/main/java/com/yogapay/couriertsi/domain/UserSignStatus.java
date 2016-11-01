package com.yogapay.couriertsi.domain;

import java.util.Date;

public class UserSignStatus {
      
	private int id ;
	
	private String userNo ;
	
	private String sessionNo ;
	 
	private String appVersion ;
	
	private Date createTime ;
	
	private String  androidId ;
	
	private Date lastUpdateTime ;
	
	private Date expiryTime ;
	
	private String ip ;
	
	private Date signTime;
	
	private Date signOutTime;
	
	private String signTimes;
	
	private String signOutTimes;
	
	public String getSignOutTimes() {
		return signOutTimes;
	}

	public void setSignOutTimes(String signOutTimes) {
		this.signOutTimes = signOutTimes;
	}

	public String getSignTimes() {
		return signTimes;
	}

	public void setSignTimes(String signTimes) {
		this.signTimes = signTimes;
	}

	public Date getSignTime() {
		return signTime;
	}

	public void setSignTime(Date signTime) {
		this.signTime = signTime;
	}

	public Date getSignOutTime() {
		return signOutTime;
	}

	public void setSignOutTime(Date signOutTime) {
		this.signOutTime = signOutTime;
	}

	public String getSignStatus() {
		return signStatus;
	}

	public void setSignStatus(String signStatus) {
		this.signStatus = signStatus;
	}

	public String getSignOutStatus() {
		return signOutStatus;
	}

	public void setSignOutStatus(String signOutStatus) {
		this.signOutStatus = signOutStatus;
	}

	private String signStatus;
	
	private String signOutStatus;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUserNo() {
		return userNo;
	}

	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}

	public String getSessionNo() {
		return sessionNo;
	}

	public void setSessionNo(String sessionNo) {
		this.sessionNo = sessionNo;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	
	public String getAndroidId() {
		return androidId;
	}

	public void setAndroidId(String androidId) {
		this.androidId = androidId;
	}

	public Date getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(Date lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public Date getExpiryTime() {
		return expiryTime;
	}

	public void setExpiryTime(Date expiryTime) {
		this.expiryTime = expiryTime;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

}
