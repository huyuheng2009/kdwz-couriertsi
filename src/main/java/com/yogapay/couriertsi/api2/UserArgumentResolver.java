package com.yogapay.couriertsi.api2;

import com.yogapay.couriertsi.SessionManager;
import com.yogapay.couriertsi.domain.User;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class UserArgumentResolver implements HandlerMethodArgumentResolver {

	@Autowired
	private SessionManager sessionManager;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		Class<?> type = parameter.getParameterType();
		return type == CourierSession.class || type == User.class;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
		CourierSession session = sessionManager.getCurrent(request, request.getHeader("sessionNO"));
		if (session == null) {
			return null;
		}
		Class<?> type = parameter.getParameterType();
		if (type == CourierSession.class) {
			return session;
		}
		return session.getUser();
	}

}
