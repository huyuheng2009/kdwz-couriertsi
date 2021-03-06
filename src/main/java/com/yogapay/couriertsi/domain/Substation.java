package com.yogapay.couriertsi.domain;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class Substation {

	private int id;
	private String substationNo;
	private String substationName;
	private String substationAddr;
	private String phone;
	private String location;
	private String lgcNo;
	private String exchange;
	private String nextCno;
	private String innerNo;
	private String sarea;
	private String substationType;

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

	public String getSubstationName() {
		return substationName;
	}

	public void setSubstationName(String substationName) {
		this.substationName = substationName;
	}

	public String getSubstationAddr() {
		return substationAddr;
	}

	public void setSubstationAddr(String substationAddr) {
		this.substationAddr = substationAddr;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getLgcNo() {
		return lgcNo;
	}

	public void setLgcNo(String lgcNo) {
		this.lgcNo = lgcNo;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public String getNextCno() {
		return nextCno;
	}

	public void setNextCno(String nextCno) {
		this.nextCno = nextCno;
	}

	public String getInnerNo() {
		return innerNo;
	}

	public void setInnerNo(String innerNo) {
		this.innerNo = innerNo;
	}

	public String getSarea() {
		return sarea;
	}

	public void setSarea(String sarea) {
		this.sarea = sarea;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

	public String getSubstationType() {
		return substationType;
	}

	public void setSubstationType(String substationType) {
		this.substationType = substationType;
	}

}
