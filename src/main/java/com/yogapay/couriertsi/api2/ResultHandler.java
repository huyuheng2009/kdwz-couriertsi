package com.yogapay.couriertsi.api2;

import com.yogapay.core.Result;
import com.yogapay.core.ResultResourceBundle;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

public class ResultHandler implements HandlerMethodReturnValueHandler {

	public static final String RET_MAP_KEY = ResultHandler.class.getName() + "#retMap";
	private final MappingJacksonJsonView view;
	@Autowired
	private ResultResourceBundle bundle;

	public ResultHandler() {
		view = new MappingJacksonJsonView();
		view.getObjectMapper().setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		view.setExtractValueFromSingleKeyModel(true);
		view.setModelKey("retMap");
	}

	@Override
	public boolean supportsReturnType(MethodParameter returnType) {
		return Result.class.isAssignableFrom(returnType.getMethod().getReturnType());
	}

	private static Map<String, Object> retMap(HttpServletRequest req, Result ret) {
		HashMap<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("respTime", new Date());
		retMap.put("isSuccess", ret.getErrorCode() == 0);
		retMap.put("respCode", String.valueOf(ret.getErrorCode()));
		retMap.put("respMsg", ret.getMessage());
		retMap.put("respNo", req.getParameter("respNo"));
		retMap.put("value", ret.getValue());
		return retMap;
	}

	@Override
	public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
		Map<String, Object> retMap = null;
		HttpServletRequest req = (HttpServletRequest) webRequest.getNativeRequest();
		if (retMap == null) {
			retMap = (Map<String, Object>) req.getAttribute(RET_MAP_KEY);
		}
		if (retMap == null && returnValue != null) {
			Result ret = (Result) returnValue;
			retMap = retMap(req, ret);
		}
		if (retMap == null) {
			retMap = retMap(req, bundle.create(-9000));
		}
		mavContainer.addAttribute("retMap", retMap);
		mavContainer.setView(view);
	}

}
