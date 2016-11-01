package com.yogapay.couriertsi.api2.controller;

import com.yogapay.core.Result;
import com.yogapay.core.ResultResourceBundle;
import com.yogapay.couriertsi.domain.User;
import com.yogapay.couriertsi.services2.FreightRuleService2;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("order/take")
public class OrderTakeController {

	@Autowired
	private FreightRuleService2 freightRuleService2 ;
	@Autowired
	private ResultResourceBundle bundle;


	@RequestMapping("freight_calculate")
	public Result transfer_order(
			@RequestParam String time_type,
			@RequestParam String item_type,
			@RequestParam double item_weight,
			@RequestParam double distance,
			User user) {
		try {
            double freight = freightRuleService2.freightCalculate(time_type,item_type,item_weight,distance) ;
            Map<String, Object> retMap = new HashMap<String, Object>() ;
            retMap.put("freight", freight) ;
			return new Result(0, null, retMap);
		} catch (Exception ex) {
			ex.printStackTrace();
			return bundle.create(9000);
		}
	}
}
