package com.yogapay.couriertsi.api;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.yogapay.couriertsi.services.AppVersionService;
import com.yogapay.couriertsi.utils.CommonResponse;
import com.yogapay.couriertsi.utils.ValidateUtil;


@Controller
@RequestMapping(value = "/version",method=RequestMethod.POST)
public class AppVersionApi extends BaseApi {
	
	@Resource
	private AppVersionService appVersionService ;
	
	@RequestMapping(value = "/update")
	public void update(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(null), false, null,checkVersion,appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				params.put("bname", "") ;
				Map<String, Object> version = appVersionService.lastVersion(params) ;
				if (version!=null) {
					model.putAll(version);
				}
			    render(JSON_TYPE, CommonResponse.respSuccessJson("",model, params.get("reqNo")), response);     
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE, CommonResponse.respFailJson(ret.get("respCode"), ret.get("respMsg"), params.get("reqNo")),response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE, CommonResponse.respFailJson("9000","服务器异常", params.get("reqNo")), response);
		}
	}
	

}
