package com.yogapay.couriertsi.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yogapay.couriertsi.utils.http.HttpUtils;

public class KuaiDiYiBaiUtil {

	@SuppressWarnings("unchecked")
	public static List<Map<String, String>> track(String lgc,String orderNo) throws Exception {
		List<Map<String, String>> trackList = new ArrayList<Map<String,String>>() ;
		 Map<String, String> params = new HashMap<String, String>() ;
		 params.put("type", lgc) ;
		 params.put("postid", orderNo) ;
		 HttpUtils httpUtils = new HttpUtils() ; 
		 String jsonString = httpUtils.get("http://www.kuaidi100.com/query", params) ;
		Map<String, Object> ret = JsonUtil.getMapFromJson(jsonString);
		if (ret==null||!"200".equals(ret.get("status").toString())) {
			Map<String, String> map = new HashMap<String, String>() ;
			map.put("time", "") ;	
			map.put("location", "") ;
			map.put("context", "无此物流单号信息！") ;
			map.put("ftime", "") ;
			trackList.add(map);
		}else {
			trackList.addAll((Collection<? extends Map<String, String>>) ret.get("data")) ;
		}
		return trackList;
	}

}
