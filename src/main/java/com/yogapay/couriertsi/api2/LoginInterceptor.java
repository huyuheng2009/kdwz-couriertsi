package com.yogapay.couriertsi.api2;

import com.yogapay.boss.manager.service.CompanyDataSourceService;
import com.yogapay.core.ExPatternParser;
import com.yogapay.core.Result;
import com.yogapay.core.ResultResourceBundle;
import com.yogapay.couriertsi.SessionManager;
import com.yogapay.couriertsi.api2.controller.CommonController;
import com.yogapay.couriertsi.dataSource.DynamicDataSourceHolder;
import com.yogapay.couriertsi.domain.User;
import com.yogapay.couriertsi.domain.UserSession;
import com.yogapay.couriertsi.services.AppVersionService;
import com.yogapay.couriertsi.services.UserService;
import com.yogapay.couriertsi.services.UserSessionService;
import com.yogapay.couriertsi.utils.ValidateUtil;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class LoginInterceptor implements HandlerInterceptor {

	@Resource
	public UserSessionService userSessionService;
	@Resource
	public AppVersionService appVersionService;
	@Resource
	public DynamicDataSourceHolder dynamicDataSourceHolder;
	@Autowired
	private CompanyDataSourceService companyDataSourceService;
	@Autowired
	private SessionManager sessionManager;
	@Resource
	private ResultResourceBundle bundle;
	@Value("#{config['api2.debug']}")
	private boolean debug;
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		ExPatternParser.setCurrentValue("lgcNo", "--");
		HandlerMethod m = (HandlerMethod) handler;
		Object bean = m.getBean();
		
		if (bean instanceof CommonController) {
			return true;
		}
		if (!debug) {
			Map<String, String> ret = ValidateUtil.validateRequest(request, null, true, userSessionService, true, appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
			} else {
				request.setAttribute(ResultHandler.RET_MAP_KEY, ret);
				request.getRequestDispatcher(request.getServletPath() + "/render").forward(request, response);
				return false;
			}
		}
		CourierSession session = sessionManager.getCurrent(request, request.getHeader("sessionNO"));
		if (session == null) {
			Result ret = bundle.create(-9001);
			request.setAttribute("result", ret);
			request.getRequestDispatcher(request.getServletPath() + "/render").forward(request, response);
			return false;
		}
		
		ExPatternParser.setCurrentValue("lgcNo", session.lgcNo);
		companyDataSourceService.setCurrentDataSource(session.lgcNo);
		
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
	}

}
