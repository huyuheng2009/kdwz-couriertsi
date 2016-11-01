package com.yogapay.couriertsi.api;

import com.yogapay.boss.company.service.COrderService;
import com.yogapay.core.ErrorCodeException;
import com.yogapay.core.ResultResourceBundle;
import com.yogapay.couriertsi.utils.CommonResponse;
import com.yogapay.couriertsi.utils.ValidateUtil;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value = "/order")
public class OrderExtApi extends BaseApi {

	@Autowired
	private COrderService cOrderService;
	@Resource(name = "commonResultResourceBundle")
	private ResultResourceBundle bundle;

	@RequestMapping(value = "/nextNo")
	public void nextNo(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, String> ret = ValidateUtil.validateRequest(request, new String[]{}, true, userSessionService, checkVersion, appVersionService, dynamicDataSourceHolder);
		if ("TRUE".equals(ret.get("isSuccess"))) {
			try {
				String lgcOrderNo = cOrderService.next_no();
				model = new HashMap<String, Object>();
				model.put("nextNo", lgcOrderNo);
				render(JSON_TYPE, CommonResponse.respSuccessJson("", model, params.get("reqNo")), response);
			} catch (ErrorCodeException ex) {
				int errorCode = ex.getErrorCode();
				render(JSON_TYPE, CommonResponse.respFailJson(String.valueOf(errorCode), bundle.message(errorCode), params.get("reqNo")),
					   response);
			}
		} else {
			log.info("validate false!!!!");
			render(JSON_TYPE, CommonResponse.respFailJson(ret.get("respCode"), ret.get("respMsg"), params.get("reqNo")), response);
		}
	}

}
