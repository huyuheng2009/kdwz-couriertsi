package com.yogapay.couriertsi.api;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yogapay.couriertsi.domain.YouxinCallLog;
import com.yogapay.couriertsi.services.UserService;
import com.yogapay.couriertsi.utils.CommonResponse;
import com.yogapay.couriertsi.utils.Commons;
import com.yogapay.couriertsi.utils.SHA;
import com.yogapay.couriertsi.utils.ValidateUtil;

@Controller
@RequestMapping(value = "/youxin")
@Scope("prototype")
public class YouXinApi extends BaseApi {
	Logger log = LoggerFactory.getLogger(YouXinApi.class);
	@Resource
	private UserService userService;

	// 获取token
	@RequestMapping(value = "/getToken")
	public void getToken(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "uid", "phone", "pv" }), false,
					null, checkVersion, appVersionService,
					dynamicDataSourceHolder);
			String sn = "" + System.currentTimeMillis()
					+ (int) ((Math.random() * 9 + 1) * 100000);
			log.info("sn" + sn);
			params.put("sn", sn);
			params.put("apiv", params.get("appVersion"));
			params.put("accountid", params.get("phone"));
			params.put("phone", params.get("phone"));
			params.put("appv", params.get("appVersion"));
			params.put("appid", "zhifujie");
			params.remove("appVersion");
			params.remove("reqNo");
			params.remove("reqTime");
			log.info("params="+params);
			String tmp = Commons.createLinkString(params);
			String sign = getSign("2c65775c38d441c5dcd7b0955468cb34d9dd683a", params);
			params.put("sign", sign);
			tmp = tmp + "&sign=" + sign;
			String url = "http://sdk.uxin.com/ams/reg?";
			System.out.println(url+tmp);
			String res = Commons.get(url+tmp);
			String token = null;
			Map<String, Object> map = (Map<String, Object>) JSONArray.parse(res);
			if("success".equals(map.get("msg"))){
				JSONObject info = (JSONObject) map.get("info");
				Map<String, String> infoMap = (Map<String, String>)JSONArray.parse(info.toJSONString());
				token = infoMap.get("token");
			}
			model = new HashMap<String, Object>();
			model.put("token", token);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		render(JSON_TYPE,CommonResponse.respSuccessJson("", model,params.get("reqNo")), response);
	}

	public static String getSign(String appkey, Map<String, String> map) {

		if (map == null || map.size() == 0) {
			return "";
		}
		List<String> list = new ArrayList<String>();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (!"sign".equals(key) && value != null && !"".equals(value)
					&& !"true".equalsIgnoreCase(value)) {
				list.add(value);
			}
		}

		Collections.sort(list, new Comparator<String>() {
			@Override
			public int compare(String v1, String v2) {
				return v1.toLowerCase().compareTo(v2.toLowerCase());
			}
		});

		StringBuilder signSrc = new StringBuilder();
		for (String value : list) {
			signSrc.append(value);
		}
		signSrc.append(appkey);

		String sha1Sign = SHA.SHA1Encode(signSrc.toString());
		return sha1Sign;
	}
	
	@RequestMapping(value = "/callback")
	public void callback(@RequestParam Map<String, String> params, YouxinCallLog callLog,
			HttpServletRequest request, HttpServletResponse response){
		log.info("==========YouXin callback============");
		log.info("params="+params);
		
		outText("success", response);
		
	}
}
