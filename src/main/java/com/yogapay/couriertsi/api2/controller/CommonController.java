package com.yogapay.couriertsi.api2.controller;

import com.yogapay.core.Result;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CommonController {

	@RequestMapping("render")
	public Result render(HttpServletRequest req) {
		Result ret = (Result) req.getAttribute("result");
		return ret;
	}
}
