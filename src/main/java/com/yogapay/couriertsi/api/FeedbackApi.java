package com.yogapay.couriertsi.api;

import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.yogapay.couriertsi.domain.Feedback;
import com.yogapay.couriertsi.services.FeedbackService;
import com.yogapay.couriertsi.utils.CommonResponse;
import com.yogapay.couriertsi.utils.ValidateUtil;
/**
 * 意见反馈接口类
 * @author hhh
 *
 */
@Controller
@RequestMapping(value = "/feedback",method=RequestMethod.POST)
@Scope("prototype")
public class FeedbackApi extends BaseApi{
	
	@Resource
	private FeedbackService feedbackService ;
	
	//提交反馈意见接口
	@RequestMapping(value = "/add")
	public void add(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(new String[] {"type","info"}), true, userSessionService,false,null,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
            Feedback feedback = new Feedback() ;
            feedback.setType(params.get("type"));
            feedback.setInfo(params.get("info"));
            feedback.setName(params.get("name"));
            feedback.setPhone(params.get("phone"));
            feedback.setUserNo(ret.get("userNo"));
            feedback.setAppVersion(params.get("appVersion"));
            feedback.setUserType(2);
            feedback.setCreateTime(new Date());
            feedbackService.save(feedback) ;
			render(JSON_TYPE, CommonResponse.respSuccessJson("","提交成功", params.get("reqNo")), response);     
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
