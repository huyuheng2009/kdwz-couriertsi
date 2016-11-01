package com.yogapay.couriertsi;

import com.yogapay.couriertsi.api2.CourierSession;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

public class SessionManager {

	public static final String KEY = SessionManager.class.getName() + "#KEY";
	@Resource
	private Cache sessionCache;

	public CourierSession getCurrent(HttpServletRequest req, String session_no) {
		if (session_no != null) {
			Element e = sessionCache.get(session_no);
			if (e != null) {
				return (CourierSession) e.getObjectValue();
			}
		}
		HttpSession session = req.getSession(false);
		if (session == null) {
			return null;
		}
		return (CourierSession) session.getAttribute(KEY);
	}

	public void setCurrent(HttpServletRequest req, String session_no, CourierSession obj) {
		sessionCache.put(new Element(session_no, obj));
		HttpSession session = req.getSession(false);
		if (session != null) {
			session.setAttribute(KEY, obj);
		}
	}
}
