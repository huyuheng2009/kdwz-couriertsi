package com.yogapay.couriertsi.filter;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.yogapay.couriertsi.exception.ParamsUncheckException;
import com.yogapay.couriertsi.utils.StringUtil;

public class ParamsInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object o) throws Exception {
		System.out.println("Header===========" + request.getHeader("Content-Type"));
		String contentType = request.getHeader("Content-Type");
		if (contentType == null || !contentType.toLowerCase().contains("multipart/form-data")) {
			Map paramsMap = request.getParameterMap();
			System.out.println("****************params*******************");
			System.out.println(paramsMap);
			System.out.println("****************params*******************");
			for (Iterator<Map.Entry> it = paramsMap.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = it.next();
				Object[] values = (Object[]) entry.getValue();
				String value = "";
				for (Object obj : values) {
					String oldStr = obj.toString();
					String newStr = StringUtil.StringFilter(oldStr);
					if (!oldStr.equals(newStr)) {
						throw new ParamsUncheckException();
					}
					value += oldStr;
				}
			}
		}

		return super.preHandle(request, response, o);
	}

	@Override
	public void postHandle(HttpServletRequest hsr, HttpServletResponse hsr1,
			Object o, ModelAndView mav) throws Exception {
	}

	@Override
	public void afterCompletion(HttpServletRequest hsr,
			HttpServletResponse hsr1, Object o, Exception excptn)
			throws Exception {
	}
}
