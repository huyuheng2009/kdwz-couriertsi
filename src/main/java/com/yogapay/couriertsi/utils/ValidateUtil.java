package com.yogapay.couriertsi.utils;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.yogapay.couriertsi.SessionManager;
import com.yogapay.couriertsi.api2.CourierSession;
import com.yogapay.couriertsi.dataSource.DynamicDataSourceHolder;
import com.yogapay.couriertsi.domain.User;
import com.yogapay.couriertsi.domain.UserSession;
import com.yogapay.couriertsi.services.AppVersionService;
import com.yogapay.couriertsi.services.UserSessionService;
import net.sf.ehcache.Element;

/**
 * 接口验证辅助类
 * 
 * @author hhh
 * 
 */
public class ValidateUtil {

	public static boolean checkSubstation(UserSessionService userSessionService, Map<String, String> retMap, String courierNo) throws SQLException {
		if (!userSessionService.substationShut(courierNo)) {
			retMap.put("isSuccess", "FALSE");
			retMap.put("respCode", "9066");
			retMap.put("respMsg", "当前预付款余额已经低于系统关闭金额，请及时充值");
			return false;
		}
		return true;
	}

	public static boolean checkManager(UserSessionService userSessionService, Map<String, String> retMap, String userNo) throws SQLException {
		if (!userSessionService.managerStaffShut(userNo)) {
			retMap.put("isSuccess", "FALSE");
			retMap.put("respCode", "9066");
			retMap.put("respMsg", "当前预付款余额已经低于系统关闭金额，请及时充值");
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param request
	 *            http请求类
	 * @param params
	 *            接口必传参数集合
	 * @param login
	 *            是否需要验证登陆
	 * @param terminalSessionService
	 *            验证登陆用到的service
	 * @param chackVersion
	 *            是否需要检查版本
	 * @param appVersionService
	 *            验证版本用到的service
	 * @return
	 * @throws SQLException
	 */
	public static Map<String, String> validateRequest(
			HttpServletRequest request, String[] params, boolean login,
			UserSessionService terminalSessionService, boolean chackVersion,
			AppVersionService appVersionService,
			DynamicDataSourceHolder dynamicDataSourceHolder)
			throws SQLException {
		Map<String, String> retMap = new HashMap<String, String>();
		System.out.println("************getParameterMap****************");
		print(request.getParameterMap());
		System.out.println(request.getRequestURL());
		System.out.println("************getParameterMap****************");
		String reqVersion = request.getParameter("appVersion");
		if (StringUtils.isEmptyWithTrim(reqVersion)) {
			retMap.put("isSuccess", "FALSE");
			retMap.put("respCode", "9001");
			retMap.put("respMsg", "缺少参数：appVersion");
			return retMap;
		}
		String uid = request.getParameter("uid");
		if (StringUtils.isEmptyWithTrim(uid)) {
			retMap.put("isSuccess", "FALSE");
			retMap.put("respCode", "9001");
			retMap.put("respMsg", "缺少参数：uid");
			return retMap;
		}
		if (uid.contains("_")) {
			uid = uid.substring(0, uid.indexOf("_"));
		}
		if (!dynamicDataSourceHolder.isExitKey(uid)) {
			retMap.put("isSuccess", "FALSE");
			retMap.put("respCode", "9001");
			retMap.put("respMsg", "uid error！");
			return retMap;
		} else {
			dynamicDataSourceHolder.setDataSource(uid);
			dynamicDataSourceHolder.getCompanyDataSourceService().setCurrentDataSourceByKey(uid);
		}
		System.out.println("--------------------------" + uid
				+ "-----------------------------");
		// 检查软件版本信息
		if (chackVersion) {
			String[] ver = reqVersion.split("\\.");
			if (ver.length < 3) {
				System.out.println(ver.length);
				System.out.println(reqVersion);
				retMap.put("isSuccess", "FALSE");
				retMap.put("respCode", "9027");
				retMap.put("respMsg", "参数错误：appVersion");
				return retMap;
			}
			Map<String, String> p = new HashMap<String, String>();
			p.put("platform", ver[0]);
			p.put("bname", ver[1]);
			String v = reqVersion.substring(reqVersion.indexOf(".",
															   reqVersion.indexOf(".") + 1) + 1);
			p.put("version", v);
			Map<String, Object> curVersion = appVersionService.getVersion(p);
			Map<String, Object> lastVersion = null;
			if (curVersion != null) {
				lastVersion = appVersionService.lastVersion(p);
			}
			if (curVersion == null || lastVersion == null) {
				retMap.put("isSuccess", "FALSE");
				retMap.put("respCode", "9028");
				retMap.put("respMsg", "版本信息错误");
				return retMap;
			} else {
				if (!reqVersion.equals(ver[0] + "." + ver[1] + "."
						+ lastVersion.get("version"))) { // 当前版本不是最新版本
					List<Map<String, Object>> vlist = appVersionService
							.getUpdateList(curVersion);
					if (vlist != null) {
						boolean mupdate = false;
						for (int i = 0; i < vlist.size(); i++) {
							if (Integer.parseInt(vlist.get(i).get("mupdate")
									.toString()) == 1) {
								mupdate = true;
								break;
							}
						}
						if (mupdate) {
							retMap.put("isSuccess", "FALSE");
							retMap.put("respCode", "9029");
							Map<String, Object> jsonMap = new HashMap<String, Object>();
							jsonMap.put("msg", "版本过低，请更新后再进行操作！");
							jsonMap.put("version", lastVersion.get("version"));
							jsonMap.put("address", lastVersion.get("address"));
							retMap.put("respMsg", JsonUtil.toJson(jsonMap));
							return retMap;
						}
					} else {
						retMap.put("isSuccess", "FALSE");
						retMap.put("respCode", "9028");
						retMap.put("respMsg", "版本信息错误");
						return retMap;
					}

				}
			}
		}

		String param = "p";
		String loginRet = "";
		if (login) {
			param = request.getHeader("sessionNO");
			if (StringUtils.isEmptyWithTrim(param)) {
				retMap.put("isSuccess", "FALSE");
				retMap.put("respCode", "9001");
				retMap.put("respMsg", "缺少参数：sessionNO");
				return retMap;
			}
			loginRet = userLogin(request, terminalSessionService);
			if ("none".equals(loginRet)) {
				retMap.put("isSuccess", "FALSE");
				retMap.put("respCode", "9009");
				retMap.put("respMsg", "当前用户已在别处登录！");
				return retMap;
			}
//			Element cache;
//			if ("timeout".equals(loginRet) || (cache = terminalSessionService.getSessionCache().get(param)) == null) {
//				retMap.put("isSuccess", "FALSE");
//				retMap.put("respCode", "9004");
//				retMap.put("respMsg", "登陆超时！");
//				return retMap;
//			}
			Element cache = terminalSessionService.getSessionCache().get(param);
			if(cache==null){
				try {
					SessionManager sessionManager =terminalSessionService.getSessionManager();
					Map<String,Object> m = terminalSessionService.getUserByCourierNo(loginRet);
					sessionManager.setCurrent(request, request.getHeader("sessionNO"), new CourierSession((User)m.get("user"), m.get("lgcNo").toString()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if ("timeout".equals(loginRet)) {
				retMap.put("isSuccess", "FALSE");
				retMap.put("respCode", "9004");
				retMap.put("respMsg", "登陆超时！");
				return retMap;
			}
		}
		if (login) {
			if (!checkSubstation(terminalSessionService, retMap, loginRet)) {
				return retMap;
			}
		}

		if (params != null) {
			String lastParam = "";
			for (int i = 0; i < params.length; i++) {
				lastParam = params[i];
				if (StringUtils.isEmptyWithTrim(params[i])) {
					continue;
				} else {
					param = request.getParameter(params[i]);
					if (StringUtils.isEmptyWithTrim(param)) {
						break;
					}
				}
			}
			if (StringUtils.isEmptyWithTrim(param)) {
				retMap.put("isSuccess", "FALSE");
				retMap.put("respCode", "9001");
				retMap.put("respMsg", "缺少参数：" + lastParam.replace("[]", ""));
				return retMap;
			}
		}
		retMap.put("isSuccess", "TRUE");
		if (login) {
			retMap.put("userNo", loginRet);
		}
		return retMap;
	}

	public static Map<String, String> managerValidateRequest(
			HttpServletRequest request, String[] params, boolean login,
			UserSessionService terminalSessionService, boolean chackVersion,
			AppVersionService appVersionService,
			DynamicDataSourceHolder dynamicDataSourceHolder)
			throws SQLException {
		Map<String, String> retMap = new HashMap<String, String>();
		System.out.println("************getParameterMap****************");
		print(request.getParameterMap());
		System.out.println(request.getRequestURL());
		System.out.println("************getParameterMap****************");
		String reqVersion = request.getParameter("appVersion");
		if (StringUtils.isEmptyWithTrim(reqVersion)) {
			retMap.put("isSuccess", "FALSE");
			retMap.put("respCode", "9001");
			retMap.put("respMsg", "缺少参数：appVersion");
			return retMap;
		}
		String uid = request.getParameter("uid");
		ApplicationContext applicationContext = WebApplicationContextUtils
				.getRequiredWebApplicationContext(request.getSession()
						.getServletContext());
		if (StringUtils.isEmptyWithTrim(uid)) {
			retMap.put("isSuccess", "FALSE");
			retMap.put("respCode", "9001");
			retMap.put("respMsg", "缺少参数：uid");
			return retMap;
		}
		if (uid.contains("_")) {
			uid = uid.substring(0, uid.indexOf("_"));
			if (!dynamicDataSourceHolder.isExitKey(uid)) {
				retMap.put("isSuccess", "FALSE");
				retMap.put("respCode", "9001");
				retMap.put("respMsg", "uid error！");
				return retMap;
			} else {
				dynamicDataSourceHolder.setDataSource(uid);
			}
		}
		// 检查软件版本信息
		if (chackVersion) {
			String[] ver = reqVersion.split("\\.");
			if (ver.length < 3) {
				System.out.println(ver.length);
				System.out.println(reqVersion);
				retMap.put("isSuccess", "FALSE");
				retMap.put("respCode", "9027");
				retMap.put("respMsg", "参数错误：appVersion");
				return retMap;
			}
			Map<String, String> p = new HashMap<String, String>();
			p.put("platform", ver[0]);
			p.put("bname", ver[1]);
			String v = reqVersion.substring(reqVersion.indexOf(".",
															   reqVersion.indexOf(".") + 1) + 1);
			p.put("version", v);
			Map<String, Object> curVersion = appVersionService.getVersion(p);
			Map<String, Object> lastVersion = null;
			if (curVersion != null) {
				lastVersion = appVersionService.lastVersion(p);
			}
			if (curVersion == null || lastVersion == null) {
				retMap.put("isSuccess", "FALSE");
				retMap.put("respCode", "9028");
				retMap.put("respMsg", "版本信息错误");
				return retMap;
			} else {
				if (!reqVersion.equals(ver[0] + "." + ver[1] + "."
						+ lastVersion.get("version"))) { // 当前版本不是最新版本
					List<Map<String, Object>> vlist = appVersionService
							.getUpdateList(curVersion);
					if (vlist != null) {
						boolean mupdate = false;
						for (int i = 0; i < vlist.size(); i++) {
							if (Integer.parseInt(vlist.get(i).get("mupdate")
									.toString()) == 1) {
								mupdate = true;
								break;
							}
						}
						if (mupdate) {
							retMap.put("isSuccess", "FALSE");
							retMap.put("respCode", "9029");
							Map<String, Object> jsonMap = new HashMap<String, Object>();
							jsonMap.put("msg", "版本过低，请更新后再进行操作！");
							jsonMap.put("version", lastVersion.get("version"));
							jsonMap.put("address", lastVersion.get("address"));
							retMap.put("respMsg", JsonUtil.toJson(jsonMap));
							return retMap;
						}
					} else {
						retMap.put("isSuccess", "FALSE");
						retMap.put("respCode", "9028");
						retMap.put("respMsg", "版本信息错误");
						return retMap;
					}

				}
			}
		}

		String param = "p";
		String loginRet = "";
		if (login) {
			param = request.getHeader("sessionNO");
			if (StringUtils.isEmptyWithTrim(param)) {
				retMap.put("isSuccess", "FALSE");
				retMap.put("respCode", "9001");
				retMap.put("respMsg", "缺少参数：sessionNO");
				return retMap;
			}
			loginRet = managerLogin(request, terminalSessionService);
			if ("none".equals(loginRet)) {
				retMap.put("isSuccess", "FALSE");
				retMap.put("respCode", "9009");
				retMap.put("respMsg", "当前用户已在别处登录！");
				return retMap;
			}
			if ("timeout".equals(loginRet)) {
				retMap.put("isSuccess", "FALSE");
				retMap.put("respCode", "9004");
				retMap.put("respMsg", "登陆超时！");
				return retMap;
			}
		}
		if (login) {
			if (!checkManager(terminalSessionService, retMap, loginRet)) {
				return retMap;
			}
		}

		if (params != null) {
			String lastParam = "";
			for (int i = 0; i < params.length; i++) {
				lastParam = params[i];
				if (StringUtils.isEmptyWithTrim(params[i])) {
					continue;
				} else {
					param = request.getParameter(params[i]);
					if (StringUtils.isEmptyWithTrim(param)) {
						break;
					}
				}
			}
			if (StringUtils.isEmptyWithTrim(param)) {
				retMap.put("isSuccess", "FALSE");
				retMap.put("respCode", "9001");
				retMap.put("respMsg", "缺少参数：" + lastParam.replace("[]", ""));
				return retMap;
			}
		}
		retMap.put("isSuccess", "TRUE");
		if (login) {
			retMap.put("userNo", loginRet);
		}
		return retMap;
	}

	/**
	 * 快递员信息登录
	 * 
	 * @param request
	 * @param userSessionService
	 * @return
	 * @throws SQLException
	 */
	public static String userLogin(HttpServletRequest request,
			UserSessionService userSessionService) throws SQLException {
		String sessionNO = "";
		String appVersion = request.getParameter("appVersion");
		sessionNO = request.getHeader("sessionNO").split(",")[0];
		if (StringUtils.isEmptyWithTrim(sessionNO)) {
			return "none";
		}
		String ip = getClientIP(request);
		UserSession userSession = userSessionService.getUserSession(sessionNO,
																	appVersion);
		if (userSession == null) {
			return "none";
		} else {
			if (!DateUtils.bofore(userSession.getExpiryTime())) {
				return "timeout";
			} else {
				Date nowDate = new Date();
				userSession.setLastUpdateTime(nowDate);
				userSession.setExpiryTime(DateUtils.addDate(nowDate, 0, 0, 30));
				userSessionService.updateSession(userSession);
				return userSession.getUserNo();
			}
		}
	}

	/**
	 * 获取仓管员登录信息
	 * 
	 * @param request
	 * @param userSessionService
	 * @return
	 * @throws SQLException
	 */
	public static String managerLogin(HttpServletRequest request,
			UserSessionService userSessionService) throws SQLException {
		String sessionNO = "";
		String appVersion = request.getParameter("appVersion");
		sessionNO = request.getHeader("sessionNO");
		if (StringUtils.isEmptyWithTrim(sessionNO)) {
			return "none";
		}
		String ip = getClientIP(request);
		UserSession userSession = userSessionService.getManagerSession(
				sessionNO, appVersion);
		if (userSession == null) {
			return "none";
		} else {
			if (!DateUtils.bofore(userSession.getExpiryTime())) {
				return "timeout";
			} else {
				Date nowDate = new Date();
				userSession.setLastUpdateTime(nowDate);
				userSession.setExpiryTime(DateUtils.addDate(nowDate, 0, 0, 30));
				userSessionService.updateSession(userSession);
				return userSession.getUserNo();
			}
		}
	}

	public static String getClientIP(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("http_client_ip");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		// 如果是多级代理，那么取第一个ip为客户ip
		if (ip != null && ip.indexOf(",") != -1) {
			ip = ip.substring(ip.lastIndexOf(",") + 1, ip.length()).trim();
		}
		if ("0:0:0:0:0:0:0:1".equals(ip)) {
			ip = "127.0.0.1";
		}
		return ip;
	}

	public static void print(Map map) {
		Iterator<Map.Entry> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = iterator.next();
			System.out.print("**" + entry.getKey() + "------->");
			String[] tmp = (String[]) entry.getValue();
			for (int i = 0; i < tmp.length; i++) {
				System.out.print(tmp[i]);
			}
			System.out.println();

		}
	}

	/**
	 * 前置登录效验
	 * 
	 * @param request
	 *            http请求类
	 * @param params
	 *            接口必传参数集合
	 * @param terminalSessionService
	 *            验证登陆用到的service
	 * @param chackVersion
	 *            是否需要检查版本
	 * @param appVersionService
	 *            验证版本用到的service
	 * @return
	 * @throws SQLException
	 */
	public static Map<String, String> loginValidateRequest(
			HttpServletRequest request, String[] params, boolean chackVersion,
			AppVersionService appVersionService) throws SQLException {
		Map<String, String> retMap = new HashMap<String, String>();
		System.out.println("************getParameterMap****************");
		print(request.getParameterMap());
		System.out.println(request.getRequestURL());
		System.out.println("************getParameterMap****************");
		String reqVersion = request.getParameter("appVersion");
		if (StringUtils.isEmptyWithTrim(reqVersion)) {
			retMap.put("isSuccess", "FALSE");
			retMap.put("respCode", "9001");
			retMap.put("respMsg", "缺少参数：appVersion");
			return retMap;
		}

		// 检查软件版本信息
		if (chackVersion) {
			String[] ver = reqVersion.split("\\.");
			if (ver.length < 3) {
				System.out.println(ver.length);
				System.out.println(reqVersion);
				retMap.put("isSuccess", "FALSE");
				retMap.put("respCode", "9027");
				retMap.put("respMsg", "参数错误：appVersion");
				return retMap;
			}
			Map<String, String> p = new HashMap<String, String>();
			p.put("platform", ver[0]);
			p.put("bname", ver[1]);
			String v = reqVersion.substring(reqVersion.indexOf(".",
															   reqVersion.indexOf(".") + 1) + 1);
			p.put("version", v);
			Map<String, Object> curVersion = appVersionService.getVersion(p);
			Map<String, Object> lastVersion = null;
			if (curVersion != null) {
				lastVersion = appVersionService.lastVersion(p);
			}
			if (curVersion == null || lastVersion == null) {
				retMap.put("isSuccess", "FALSE");
				retMap.put("respCode", "9028");
				retMap.put("respMsg", "版本信息错误");
				return retMap;
			} else {
				if (!reqVersion.equals(ver[0] + "." + ver[1] + "."
						+ lastVersion.get("version"))) { // 当前版本不是最新版本
					List<Map<String, Object>> vlist = appVersionService
							.getUpdateList(curVersion);
					if (vlist != null) {
						boolean mupdate = false;
						for (int i = 0; i < vlist.size(); i++) {
							if (Integer.parseInt(vlist.get(i).get("mupdate")
									.toString()) == 1) {
								mupdate = true;
								break;
							}
						}
						if (mupdate) {
							retMap.put("isSuccess", "FALSE");
							retMap.put("respCode", "9029");
							Map<String, Object> jsonMap = new HashMap<String, Object>();
							jsonMap.put("msg", "版本过低，请更新后再进行操作！");
							jsonMap.put("version", lastVersion.get("version"));
							jsonMap.put("address", lastVersion.get("address"));
							retMap.put("respMsg", JsonUtil.toJson(jsonMap));
							return retMap;
						}
					} else {
						retMap.put("isSuccess", "FALSE");
						retMap.put("respCode", "9028");
						retMap.put("respMsg", "版本信息错误");
						return retMap;
					}

				}
			}
		}

		String param = "p";

		if (params != null) {
			String lastParam = "";
			for (int i = 0; i < params.length; i++) {
				lastParam = params[i];
				if (StringUtils.isEmptyWithTrim(params[i])) {
					continue;
				} else {
					param = request.getParameter(params[i]);
					if (StringUtils.isEmptyWithTrim(param)) {
						break;
					}
				}
			}
			if (StringUtils.isEmptyWithTrim(param)) {
				retMap.put("isSuccess", "FALSE");
				retMap.put("respCode", "9001");
				retMap.put("respMsg", "缺少参数：" + lastParam.replace("[]", ""));
				return retMap;
			}
		}
		retMap.put("isSuccess", "TRUE");

		return retMap;
	}
}
