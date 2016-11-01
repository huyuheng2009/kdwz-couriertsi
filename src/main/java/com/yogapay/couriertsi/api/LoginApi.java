package com.yogapay.couriertsi.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.yogapay.couriertsi.services.ProjectDsService;
import com.yogapay.couriertsi.utils.CommonResponse;
import com.yogapay.couriertsi.utils.StringUtils;
import com.yogapay.couriertsi.utils.ValidateUtil;


@Controller
@RequestMapping(value="/login",method=RequestMethod.POST)
@Scope("prototype")
public class LoginApi extends BaseApi{
	@Resource
	private 	ProjectDsService projectDsService;

	@RequestMapping(value = "/index")	
	public void login(@RequestParam Map<String,String> params,HttpServletRequest request,HttpServletResponse response){
		try {
			Map<String, String> ret = ValidateUtil.loginValidateRequest(request,reqParams(new String[] { "verCode"}),true,appVersionService);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String verCode = params.get("verCode");
				String lgcNo = 	projectDsService.getLgcNo(verCode);
				if(StringUtils.isEmptyWithTrim(lgcNo)){
					render(JSON_TYPE, CommonResponse.respFailJson(ret.get("respCode"), "验证码有误", params.get("reqNo")),response);
					return;
				}
				List<Map<String,Object>> urlList = projectDsService.logoUrl(lgcNo);
				if(urlList.size()<1){
				 urlList = projectDsService.logoUrl("0000");	
				}
				Map<Object,Object> map1 = new HashMap<Object,Object>();
				for(Map<String,Object> map : urlList ){			
					map1.put(map.get("resName"), map.get("resUrl"));		
				}
				String uid = projectDsService.getUid(lgcNo);
				model = new HashMap<String,Object>();
				model.put("uid",uid+"_courier");
				model.put("urlList",map1 );		
				render(JSON_TYPE, CommonResponse.respSuccessJson("",model, params.get("reqNo")), response);   			
			}else {
				log.info("validate false!!!!");
				render(JSON_TYPE, CommonResponse.respFailJson(ret.get("respCode"), ret.get("respMsg"), params.get("reqNo")),response);
			}		
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE, CommonResponse.respFailJson("9000","服务器异常", params.get("reqNo")), response);
		}
	}






}


