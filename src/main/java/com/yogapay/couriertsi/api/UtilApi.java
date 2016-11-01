package com.yogapay.couriertsi.api;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yogapay.couriertsi.utils.CommonResponse;
import com.yogapay.couriertsi.utils.PushUtil;
import com.yogapay.couriertsi.utils.ValidateUtil;


@Controller
@RequestMapping(value = "/test")
public class UtilApi  extends BaseApi{
	
	
	@RequestMapping(value = "/push")
	public void list(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, String> ret = ValidateUtil.validateRequest(request,new String[] { "msgId","userType"}, false, null,false,null,dynamicDataSourceHolder);
		if ("TRUE".equals(ret.get("isSuccess"))) {
			   PushUtil.pushById(configInfo,String.valueOf(Integer.parseInt(params.get("msgId"))),Integer.parseInt(params.get("userType")),params.get("uid"));
		} else {
			log.info("validate false!!!!");
			render(JSON_TYPE, CommonResponse.respFailJson(ret.get("respCode"), ret.get("respMsg"), params.get("reqNo")),response);
		}
	}
		
	@RequestMapping(value = "/test")
	public void test(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
	System.out.println(params);
		
		
	}
		
	


}
