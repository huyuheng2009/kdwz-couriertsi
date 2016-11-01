package com.yogapay.couriertsi.api;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.yogapay.couriertsi.services.CnapsNoService;
import com.yogapay.couriertsi.utils.CommonResponse;
import com.yogapay.couriertsi.utils.ValidateUtil;

/**
 * 银行联行号查询接口
 * @author hhh
 *
 */

@Controller
@RequestMapping(value = "/bank",method=RequestMethod.POST)
@Scope("prototype")
public class BankApi extends BaseApi{
	
	@Resource
	private CnapsNoService cnapsNoService ;
	
    //银行联行号查询接口
	@RequestMapping(value = "/search")
	public void add(@RequestParam Map<String, String> params, 
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(new String[] {"keyWord"}), false, null,checkVersion,appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
			setPageInfo(params);
			String keyWords[] = params.get("keyWord").split(" ");
			Page<Map<String, Object>> cnapsNoList = cnapsNoService.search(keyWords, pageRequest);
			
			System.out.println(cnapsNoList);
			System.out.println(cnapsNoList.getTotalElements());
			
			model = new HashMap<String, Object>() ;
			model.put("cnapsNoList", cnapsNoList.getContent()) ;
			model.put("totalCount", cnapsNoList.getTotalElements()) ;
			model.put("cp", cnapsNoList.getNumber()+1) ;
			model.put("isLastPage", cnapsNoList.isLastPage()) ;
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
