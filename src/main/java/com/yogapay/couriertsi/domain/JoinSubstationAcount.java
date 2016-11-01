package com.yogapay.couriertsi.domain;

public class JoinSubstationAcount {

	private int id;
	private String substationNo;
	private double curBalance;
	private double shutBalance;

	public boolean isCourierAllowLogin() {
		return curBalance >= shutBalance;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSubstationNo() {
		return substationNo;
	}

	public void setSubstationNo(String substationNo) {
		this.substationNo = substationNo;
	}

	public double getCurBalance() {
		return curBalance;
	}

	public void setCurBalance(double curBalance) {
		this.curBalance = curBalance;
	}

	public double getShutBalance() {
		return shutBalance;
	}

	public void setShutBalance(double shutBalance) {
		this.shutBalance = shutBalance;
	}

}
