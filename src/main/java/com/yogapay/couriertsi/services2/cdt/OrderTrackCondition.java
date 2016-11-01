package com.yogapay.couriertsi.services2.cdt;

import com.yogapay.sql.QueryCondition;

public class OrderTrackCondition extends QueryCondition {

	public String orderNo;
	public String lgcOrderNo;

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getLgcOrderNo() {
		return lgcOrderNo;
	}

	public void setLgcOrderNo(String lgcOrderNo) {
		this.lgcOrderNo = lgcOrderNo;
	}

}
