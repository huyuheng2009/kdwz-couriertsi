package com.yogapay.couriertsi.api;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.yogapay.couriertsi.domain.User;
import com.yogapay.couriertsi.services.LgcService;
import com.yogapay.couriertsi.services.MuserService;
import com.yogapay.couriertsi.services.UserService;
import com.yogapay.couriertsi.utils.CommonResponse;
import com.yogapay.couriertsi.utils.ValidateUtil;

/**
 * 月结客户接口类
 * @author hhh
 *
 */
@Controller
@RequestMapping(value = "/muser",method=RequestMethod.POST)
public class MonthUserApi  extends BaseApi{
	
    @Resource 
    private MuserService muserService ;
    @Resource 
    private UserService userService ;
	
	//
	@RequestMapping(value = "/getbyno")
	public void getbyno(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(new String[] { "muserNo"}), true, userSessionService,checkVersion,appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
		    User loginUser = userService.getUserByNo(ret.get("userNo")) ;
            Map<String, Object> map = muserService.selectMonthByFive(params.get("muserNo")) ;
            if (map==null) {
            	render(JSON_TYPE, CommonResponse.respFailJson("9031","不存在的月结号", params.get("reqNo")), response);
            	return ;
			}
			model = new HashMap<String, Object>() ;
            model.putAll(map);
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
