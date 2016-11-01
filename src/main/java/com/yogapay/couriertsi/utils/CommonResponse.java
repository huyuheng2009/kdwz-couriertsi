package com.yogapay.couriertsi.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CommonResponse {

	
	
	@SuppressWarnings("unchecked")
	public static String respSuccessJson(String respMsg,Object model,String respNo) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("respTime", new Date());
		retMap.put("isSuccess", true);
		retMap.put("respNo", respNo);
		retMap.put("respCode", "0010");
		if (model!=null) {
			if (model instanceof Map) {
				retMap.put("respMsg", respMsg) ;
				retMap.putAll((Map<String, Object>) model);
			}
			if (model instanceof String) {
				retMap.put("respMsg", model.toString()) ;
			}
		}
		System.out.println("**************************");
		System.out.println(JsonUtil.toJson(retMap));
		System.out.println("**************************");
		return JsonUtil.toJson(retMap) ;
	}
	@SuppressWarnings("unchecked")
	public static String respSuccessJsonTO(String respCode ,String respMsg,Object model,String respNo) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("respTime", new Date());
		retMap.put("isSuccess", true);
		retMap.put("respNo", respNo);
		retMap.put("respCode", respCode);
		if (model!=null) {
			if (model instanceof Map) {
				retMap.put("respMsg", respMsg) ;
				retMap.putAll((Map<String, Object>) model);
			}
			if (model instanceof String) {
				retMap.put("respMsg", model.toString()) ;
			}
		}
		System.out.println("**************************");
		System.out.println(JsonUtil.toJson(retMap));
		System.out.println("**************************");
		return JsonUtil.toJson(retMap) ;
	}
	

	public static String respFailJson(String respCode,String respMsg,String respNo) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("respTime", new Date());
		retMap.put("isSuccess", false);
		retMap.put("respCode", respCode);
		retMap.put("respMsg", respMsg);
		retMap.put("respNo", respNo);
		System.out.println("**************************");
		System.out.println("respCode:"+respCode);
		System.out.println("respMsg:"+respMsg);
		System.out.println("**************************");
		return JsonUtil.toJson(retMap) ;
	}
	
	
}
