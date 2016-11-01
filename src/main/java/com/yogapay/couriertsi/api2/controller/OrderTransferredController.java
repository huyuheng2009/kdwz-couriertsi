package com.yogapay.couriertsi.api2.controller;

import com.yogapay.core.ErrorCodeException;
import com.yogapay.core.Result;
import com.yogapay.core.ResultResourceBundle;
import com.yogapay.couriertsi.domain.User;
import com.yogapay.couriertsi.services2.OrderInfoService2;
import com.yogapay.couriertsi.services2.OthersService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("order/transfer")
public class OrderTransferredController {

	@Autowired
	private OrderInfoService2 orderInfoService2;
	@Autowired
	private OthersService othersService;
	@Autowired
	private ResultResourceBundle bundle;

	@RequestMapping("get_cpn")
	public Result query_cpn() {
		List<Map<String, Object>> list = othersService.get_cpn();
		return new Result(0, null, list);
	}

	@RequestMapping("transfer_order")
	public Result transfer_order(
			@RequestParam String c_order_no,
			@RequestParam int cpn_id,
			@RequestParam String cpn_order_no,
			User user) {
		try {
			orderInfoService2.transfer_order_tx(user, c_order_no, cpn_id, cpn_order_no);
			return new Result();
		} catch (ErrorCodeException ex) {
			return bundle.create(ex.getErrorCode());
		}
	}
}
