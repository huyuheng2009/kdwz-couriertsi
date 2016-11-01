package com.yogapay.couriertsi.api2;

import java.io.PrintWriter;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.yogapay.boss.manager.service.CompanyDataSourceService;
import com.yogapay.core.ExPatternParser;
import com.yogapay.core.Result;
import com.yogapay.core.ResultResourceBundle;
import com.yogapay.couriertsi.SessionManager;
import com.yogapay.couriertsi.dataSource.DynamicDataSourceHolder;
import com.yogapay.couriertsi.domain.User;
import com.yogapay.couriertsi.domain.UserSession;
import com.yogapay.couriertsi.services.AppVersionService;
import com.yogapay.couriertsi.services.LgcService;
import com.yogapay.couriertsi.services.UserService;
import com.yogapay.couriertsi.services.UserSessionService;
import com.yogapay.couriertsi.services.WarehouseService;
import com.yogapay.couriertsi.utils.CommonResponse;

public class UserStatusInceptor implements HandlerInterceptor{
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
	@Autowired
	private UserService userService;
	@Autowired
	private  LgcService lgcService;
	@Autowired
	private WarehouseService warehouseService;
	@Resource
	private ResultResourceBundle bundle;
	@Value("#{config['api2.debug']}")
	private boolean debug;
	private static final String[] IGNORE_URI = {"/user/loginBegin","/login/index", "/user/userLogin","/user/managerLogin","/user/regist","/user/layout","/user/managerLayout","/pay/weixinzf"};
	@Override
	public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object arg2) throws Exception {
		System.out.println(">>>>>>>>>>>>>>>>>>");
		String url = request.getRequestURL().toString();
        System.out.println(">>>: " + url);
        for (String s : IGNORE_URI) {
            if (url.contains(s)) {
                return true;
            }
        }


        String uid = request.getParameter("uid");
        if (uid==null||"".equals(uid)) {
        	 return true;
		}
		if (uid.contains("_")) {
			uid = uid.substring(0, uid.indexOf("_"));
		}
		if (!dynamicDataSourceHolder.isExitKey(uid)) {
			
		} else {
			dynamicDataSourceHolder.setDataSource(uid);
			dynamicDataSourceHolder.getCompanyDataSourceService().setCurrentDataSourceByKey(uid);
		}
		
		PrintWriter pw = response.getWriter();
		
        CourierSession session = sessionManager.getCurrent(request, request.getHeader("sessionNO"));
        if (session==null) {
			return true ;
		}
        User user =null;
        if(session!=null){
        	user=session.getUser();
        }
		if(user!=null){
			user =userService.getUserByNo(user.getCourierNo());
			if(user==null){
				user=warehouseService.getManagerInfo(user.getUserName());
			}
			System.out.println(">>>>>"+"0".equals(user.getStatus().toString()));
			System.out.println(">>>>>"+(0==Integer.parseInt(user.getStatus().toString())));
			if("0".equals(user.getStatus().toString())){
				System.out.println("已经被停用");
				pw.write(CommonResponse.respFailJson("9002", "账号已被停用",  request.getHeader("sessionNO")));
				pw.flush();
				pw.close();
				return false;
			}
		}else{
			String sessionNo = request.getHeader("sessionNO");
			UserSession us = userSessionService.getUserSession(sessionNo);
			if(us==null){
				us=userSessionService.getManagerSessionNo(sessionNo);
			}
			
			if(us!=null){
				user =userService.getUserByNo(us.getUserNo());
				if(user==null){
					user=warehouseService.getManagerInfo(us.getUserNo());
				}
				if(session==null){
					String substation_no =user.getSubstationNo();
					Map<String, Object> subTmp = lgcService.getSubstationInfo(substation_no);
					session =new CourierSession(user, subTmp.get("lgc_no").toString());
				}
				System.out.println(">>>>>"+"0".equals(user.getStatus().toString()));
				System.out.println(">>>>>"+(0==Integer.parseInt(user.getStatus().toString())));
				if("0".equals(user.getStatus().toString())){
					pw.write(CommonResponse.respFailJson("9002", "账号已被停用",  request.getHeader("sessionNO")));
					pw.flush();
					pw.close();
					return false;
				}
			}
		}
		ExPatternParser.setCurrentValue("lgcNo", session.lgcNo);
		companyDataSourceService.setCurrentDataSource(session.lgcNo);
		
		return true;
	}

}
