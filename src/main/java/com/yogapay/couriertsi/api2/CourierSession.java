package com.yogapay.couriertsi.api2;

import com.yogapay.couriertsi.domain.User;
import java.io.Serializable;

public class CourierSession implements Serializable {

	private static final long serialVersionUID = 20160624L;
	private final User user;
	public final String lgcNo;

	public CourierSession(User user, String lgcNo) {
		this.user = user;
		this.lgcNo = lgcNo;
	}

	public User getUser() {
		return user;
	}

}
