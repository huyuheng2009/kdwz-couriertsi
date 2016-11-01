package com.yogapay.couriertsi.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.yogapay.couriertsi.services.LgcService;
import com.yogapay.couriertsi.utils.CommonResponse;
import com.yogapay.couriertsi.utils.ValidateUtil;

/**
 * 快递公司接口类
 * @author hhh
 *
 */
@Controller
@RequestMapping(value = "/lgc",method=RequestMethod.POST)
public class LgcApi  extends BaseApi{
	
	@Resource
	private LgcService lgcService ;
	
	//获取快递公司列表
	@RequestMapping(value = "/list")
	public void list(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(null), false, null,checkVersion,appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
			setPageInfo(params);	
			Page<Map<String, Object>> lgcList = lgcService.list(params, pageRequest) ;
			model = new HashMap<String, Object>() ;
			model.put("lgcList", lgcList.getContent()) ;
			model.put("totalCount", lgcList.getTotalElements()) ;
			model.put("cp", lgcList.getNumber()+1) ;
			model.put("isLastPage", lgcList.isLastPage()) ;
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
	
	@RequestMapping(value = "/punish")
	public void punish(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(null), false, null,checkVersion,appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
			setPageInfo(params);	
			List<Map<String, Object>> list = lgcService.listPunish() ;
			model = new HashMap<String, Object>() ;
			model.put("list", list) ;
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
