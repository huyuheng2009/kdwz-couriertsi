package com.yogapay.couriertsi.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.yogapay.couriertsi.SessionManager;
import com.yogapay.couriertsi.domain.User;
import com.yogapay.couriertsi.domain.UserSession;
import com.yogapay.couriertsi.utils.Dao;
import com.yogapay.couriertsi.utils.DateUtils;
import com.yogapay.couriertsi.utils.StringUtils;
import net.sf.ehcache.Cache;

/**
 * 用户登陆session service
 * @author 
 *
 */
@Service
public class UserSessionService {

	@Resource
	private Dao dao ;
	@Resource
	private Cache sessionCache;
	@Resource
	private SessionManager sessionManager;



	/**
	 * 保存快递员登录状态
	 * @param userSession
	 * @return
	 * @throws SQLException
	 */
	public int saveCourier(UserSession userSession) throws SQLException {
		String sql = "insert into courier_session(user_no,session_no,app_version,android_id,create_time,last_update_time,expiry_time,ip) values(?,?,?,?,?,?,?,?)";
		List<Object> list = new ArrayList<Object>();
		list.add(userSession.getUserNo()) ;
		list.add(userSession.getSessionNo()) ;
		list.add(userSession.getAppVersion());
		list.add(userSession.getAndroidId()) ;
		list.add(DateUtils.formatDate(userSession.getCreateTime()));
		list.add(DateUtils.formatDate(userSession.getLastUpdateTime()));
		list.add(DateUtils.formatDate(userSession.getExpiryTime()));
		list.add(userSession.getIp()) ;
		return dao.update(sql, list.toArray()); 
	}

	/**
	 * 保存仓管员登录状态
	 * @param userSession
	 * @return
	 * @throws SQLException
	 */
	public int saveManager(UserSession userSession) throws SQLException {
		String sql = "insert into boss_user_session(user_no,session_no,app_version,token,create_time,last_update_time,expiry_time,ip) values(?,?,?,?,?,?,?,?)";
		List<Object> list = new ArrayList<Object>();
		list.add(userSession.getUserNo()) ;
		list.add(userSession.getSessionNo()) ;
		list.add(userSession.getAppVersion());
		list.add(userSession.getAndroidId()) ;
		list.add(DateUtils.formatDate(userSession.getCreateTime()));
		list.add(DateUtils.formatDate(userSession.getLastUpdateTime()));
		list.add(DateUtils.formatDate(userSession.getExpiryTime()));
		list.add(userSession.getIp()) ;
		return dao.update(sql, list.toArray()); 
	}
	//查询最新的签到信息
	public Map<String,Object> signInfo(Map<String, String> params) throws SQLException {
		StringBuffer sql = new StringBuffer() ;
		List<Object> list = new ArrayList<Object>();   
		sql.append(" select * from yx_courier_sign_status  where user_no=? AND substr(create_time,1,10)=?")
		.append(" order by  sign_time  desc ");		
		list.add(params.get("userNo"));
		list.add(params.get("createTime"));		
		Map<String,Object> mao = dao.findFirst(sql.toString(), list.toArray());
		return  mao;
	}
	//查询 所有签到信息
	public List<Map<String,Object>> signInfoAll(Map<String, String> params) throws SQLException {
		StringBuffer sql = new StringBuffer() ;
		List<Object> list = new ArrayList<Object>();   
		sql.append(" select * from yx_courier_sign_status  where user_no=? AND substr(create_time,1,10)=?")
		.append(" order by  sign_time  desc ");		
		list.add(params.get("userNo"));
		list.add(params.get("createTime"));		
		return  	dao.find(sql.toString(), list.toArray());

	}
	//查询最新的签退信息
	public Map<String,Object> signOutInfo(Map<String, String> params) throws SQLException {
		StringBuffer sql = new StringBuffer() ;
		List<Object> list = new ArrayList<Object>();   
		sql.append(" select * from yx_courier_sign_status  where user_no=? AND substr(create_time,1,10)=?")
		.append(" order by  sign_time  desc ");		
		list.add(params.get("userNo"));
		list.add(params.get("createTime"));		
		Map<String,Object> mao = dao.findFirst(sql.toString(), list.toArray());
		return  mao;
	}
	//查询一天的所有的签退信息
	public List<Map<String,Object>> signOutInfoAll(Map<String, String> params) throws SQLException {
		StringBuffer sql = new StringBuffer() ;
		List<Object> list = new ArrayList<Object>();   
		sql.append(" select * from yx_courier_sign_out_status  where user_no=? AND substr(create_time,1,10)=?")
		.append(" order by  sign_out_time  desc ");		
		list.add(params.get("userNo"));
		list.add(params.get("createTime"));		
		return dao.find(sql.toString(), list.toArray());
	}
	//查询当月每天的工作时间
	public Page<Map<String,Object>> monthWorkTime(Map<String, String> params,PageRequest page) throws SQLException {
		StringBuffer sql = new StringBuffer() ;
		List<Object> list = new ArrayList<Object>();   
	    sql.append(	"select   date_format(signTime,'%Y-%m-%d')  workDate, date_format(signTime,'%H:%i') signTime, date_format(signOutTime,'%H:%i') signOutTime from ");
	    sql.append(" ((select min(sign_time) signTime from");
	    sql.append(	"	yx_courier_sign_status o  where o.user_no =? ");
	    sql.append(" group by substr(o.sign_time,1,10) ) a ");
		sql.append("left join (select  max(sign_out_time) signOutTime from yx_courier_sign_out_status o ");		
		sql.append(" where o.user_no = ?  group by substr(o.sign_out_time,1,10)) b  ");		
		sql.append(" on  substr(a.signTime,1,10)  = substr(b.signOutTime,1,10)) where 1=1 ");		
		list.add(params.get("userNo"));			
		list.add(params.get("userNo"));		
		if(StringUtils.isNotEmptyWithTrim(params.get("beginTime"))){
			sql.append("AND SUBSTR(signTime,1,10) >= ? ");
			list.add(params.get("beginTime"));	
		}
		if(StringUtils.isNotEmptyWithTrim(params.get("endTime"))){
			sql.append("AND SUBSTR(signTime,1,10) <= ? ");
			list.add(params.get("endTime"));	
		}				
		return dao.find(sql.toString(), list.toArray(),page);
	}
	//查询一天内所有签到签退时间
	public List<Map<String,Object>> signAllTimeCheck(Map<String, String> params) throws SQLException {
		StringBuffer sql = new StringBuffer() ;
		List<Object> list = new ArrayList<Object>();   
		sql.append("select signTime from (")
		.append(" select a.sign_time signTime FROM yx_courier_sign_status a WHERE a.user_no=? AND SUBSTR(a.create_time,1,10)=? ")
		.append(" union all ")
		.append("select b.sign_out_time signTime from yx_courier_sign_out_status b WHERE b.user_no=? AND SUBSTR(b.create_time,1,10)=? ")
		.append(") c order by signTime asc");
		list.add(params.get("userNo"));
		list.add(params.get("createTime"));		
		list.add(params.get("userNo"));
		list.add(params.get("createTime"));	
		return dao.find(sql.toString(), list.toArray());
	}


	//签到
	public int  sign(Map<String, Object> params) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "insert into yx_courier_sign_status(user_no,app_version,android_id,create_time,expiry_time,ip,sign_time)values(?,?,?,?,?,?,?)";		
		list.add(params.get("user_no"));//用户号
		list.add(params.get("app_version"));//
		list.add(params.get("android_id"));//最后更改时间
		list.add(params.get("create_time"));//签到时间
		list.add(params.get("expiry_time"));//签到15分钟后才能签退
		list.add(params.get("ip"));	//签到为当天IP
		list.add(params.get("sign_time"));	//签到为当天登录时间
		return dao.update(sql, list.toArray()); 
	}
	//签退
	public int  signOut(Map<String, Object> params) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "insert into yx_courier_sign_out_status(user_no,app_version,android_id,create_time,ip,sign_out_time,sign_work_time,by_sign_id)values(?,?,?,?,?,?,?,?)";		
		list.add(params.get("user_no"));//签到15分钟后才能签退
		list.add(params.get("app_version"));//签到时间
		list.add(params.get("android_id"));//最后更改时间
		list.add(params.get("create_date"));//签到次数
		list.add(params.get("ip"));//用户号
		list.add(params.get("sign_out_time"));	//签到为当天登录时间
		list.add(params.get("sign_work_time"));	//签到为当天登录时间
		list.add(params.get("id"));	//与签到表向关联
		return dao.update(sql, list.toArray()); 
	}
	//	//查询当月签到次数
	public List<Map<String, Object>>  signDetailTimes(String userNo) throws SQLException {
		StringBuffer sql = new StringBuffer() ;
		List<Object> list = new ArrayList<Object>();   
		sql.append( "select * ")
		.append(" from yx_courier_sign_status  where user_no=? AND substr(create_time,1,7)=?  group by user_no,substr(sign_time,1,10) ");		
		list.add(userNo);
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM"));
		return dao.find(sql.toString(), list.toArray());	
	}
	//	/查询一次完整的签到签退时间
	public List<Map<String, Object>>  signTimeCheck(String userNo) throws SQLException {
		StringBuffer sql = new StringBuffer() ;
		List<Object> list = new ArrayList<Object>();   
		sql.append( "select a.sign_time,b.sign_out_time FORM yx_courier_sign_status a JOIN yx_courier_sign_out_status b")
		.append("ON a.id = b.by_sign_id where a.user_no=? and substr(a.create_time,1,10)=? ");		
		list.add(userNo);
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM"));
		return dao.find(sql.toString(), list.toArray());	
	}
	/**
	 * 删除快递员旧的登录状态
	 * @param userNO
	 * @return
	 * @throws SQLException
	 */
	public int deleteByUserNo(String userNO) throws SQLException {
		String sql = "delete from courier_session where user_no='"+userNO+"'" ;
		return dao.update(sql) ;
	}
	/**
	 * 删除快递员当前登录状态
	 * @param sessionNO
	 * @return
	 * @throws SQLException
	 */
	public int deleteBySessionNO(String sessionNO) throws SQLException {
		String sql = "delete from courier_session where session_no='"+sessionNO+"'" ;
		return dao.update(sql) ;
	}
	/**
	 * 删除仓管员旧的登录状态
	 * @param userNO
	 * @return
	 * @throws SQLException
	 */
	public int deleteByManagerNo(String userNO) throws SQLException {
		String sql = "delete from boss_user_session where user_no='"+userNO+"'" ;
		return dao.update(sql) ;
	}
	/**
	 * 删除仓管员当前登录状态
	 * @param sessionNO
	 * @return
	 * @throws SQLException
	 */
	public int deleteByManagerSessionNO(String sessionNO) throws SQLException {
		String sql = "delete from boss_user_session where session_no='"+sessionNO+"'" ;
		return dao.update(sql) ;
	}

	/**
	 * 获取快递员登录状态
	 * @param sessionNO
	 * @param appVersion
	 * @return
	 * @throws SQLException
	 */
	public UserSession getUserSession(String sessionNO,String appVersion) throws SQLException {
		String sql = "select * from courier_session where session_no=? and app_version=?" ;
		List<Object> list = new ArrayList<Object>();
		list.add(sessionNO);
		list.add(appVersion) ;
		UserSession userSession = dao.findFirst(UserSession.class, sql, list.toArray()) ;
		return userSession ; 
	}
	
	public UserSession getUserSession(String sessionNO) throws SQLException {
		String sql = "select id,user_no userNo,session_no sessionNo,app_version appVersion,android_id androidId,create_time createTime,last_update_time lastUpdateTime,expiry_time expiryTime,ip from courier_session where session_no=? " ;
		List<Object> list = new ArrayList<Object>();
		list.add(sessionNO);
		UserSession userSession = dao.findFirst(UserSession.class, sql, list.toArray()) ;
		return userSession ; 
	}
	/**
	 * 获取仓管员登录状态
	 * @param sessionNO
	 * @param appVersion
	 * @return
	 * @throws SQLException
	 */
	public UserSession getManagerSession(String sessionNO,String appVersion) throws SQLException {
		String sql = "select * from boss_user_session where session_no=? and app_version=?" ;
		List<Object> list = new ArrayList<Object>();
		list.add(sessionNO);
		list.add(appVersion) ;
		UserSession userSession = dao.findFirst(UserSession.class, sql, list.toArray()) ;
		return userSession ; 
	}
	
	public UserSession getManagerSessionNo(String sessionNO) throws SQLException {
		String sql = "select * from boss_user_session where session_no=? " ;
		List<Object> list = new ArrayList<Object>();
		list.add(sessionNO);
		UserSession userSession = dao.findFirst(UserSession.class, sql, list.toArray()) ;
		return userSession ; 
	}
	
	/**
	 * 更新快递员登录状态
	 * @param userSession
	 * @return
	 * @throws SQLException
	 */
	public boolean updateSession(UserSession userSession) throws SQLException {
		String sql = "update courier_session set last_update_time=?,expiry_time=? where id=?";
		List<Object> list = new ArrayList<Object>();
		list.add(DateUtils.formatDate(userSession.getLastUpdateTime()));
		list.add(DateUtils.formatDate(userSession.getExpiryTime()));
		list.add(userSession.getId());
		dao.updateGetID(sql, list);
		return true ;
	}
	/**
	 * 更新仓管员登录状态
	 * @param userSession
	 * @return
	 * @throws SQLException
	 */
	public boolean updateManagerSession(UserSession userSession) throws SQLException {
		String sql = "update boss_user_session set last_update_time=?,expiry_time=? where id=?";
		List<Object> list = new ArrayList<Object>();
		list.add(DateUtils.formatDate(userSession.getLastUpdateTime()));
		list.add(DateUtils.formatDate(userSession.getExpiryTime()));
		list.add(userSession.getId());
		dao.updateGetID(sql, list);
		return true ;
	}
/**
 * 快递员退出登录
 * @param sessionNO
 * @return
 * @throws SQLException
 */
	public boolean expirySession(String sessionNO) throws SQLException {
		String sql = "update courier_session set expiry_time=create_time where session_no=?";
		List<Object> list = new ArrayList<Object>();
		list.add(sessionNO);
		dao.updateGetID(sql, list);
		return true ;
	}
/**
 * 仓管员退出登录
 * @param sessionNO
 * @return
 * @throws SQLException
 */
	public boolean managerExpirySession(String sessionNO) throws SQLException {
		String sql = "update boss_user_session  set expiry_time=create_time where session_no=?";
		List<Object> list = new ArrayList<Object>();
		list.add(sessionNO);
		dao.updateGetID(sql, list);
		return true ;
	}
	
	/**
	 * 根据快递员编号判断所属网点是否为加盟网点，并检查余额
	 * @param courierNo
	 * @return  非加盟或者加盟网点余额充足，或者加盟网点账户停用返回true，其余返回false
	 * @throws SQLException
	 */
	public boolean substationShut(String courierNo) throws SQLException {
		String sql = "select c.courier_no,s.substation_no,s.substation_type from courier c left join substation s on c.substation_no=s.substation_no where c.courier_no=?";
		List<Object> list = new ArrayList<Object>();
		list.add(courierNo);
		Map<String, Object> map = dao.findFirst(sql, list.toArray()) ;
		if (map==null) {
			return false ;
		}
		if (!"J".equals(map.get("substation_type"))) {
			return true ;
		}
		String sno = "" ;
		if (map.get("substation_no")!=null) {
			sno = map.get("substation_no").toString() ;
		}else {
			return false ;
		}
		sql = "select * from  join_substation_acount  where substation_no=?";
		list.clear();
		list.add(sno) ;
		map = dao.findFirst(sql, list.toArray()) ; 
		if (map==null) {   //||"0".equals(map.get("status"))
			return true ;
		}
		sql = "select * from  join_substation_acount  where cur_balance>=shut_balance and substation_no=?";
		map = dao.findFirst(sql, list.toArray()) ;
		if (map==null) {
			return false ;
		}
		return true ;
	}
  /**
   * 根据仓管员编号判断所属网点是否为加盟网点，并检查余额
   * @param userNo
   * @return 非加盟或者加盟网点余额充足，或者加盟网点账户停用返回true，其余返回false
   * @throws SQLException
   */
	public boolean managerShut(String userNo) throws SQLException {
		String sql = "select u.user_name,s.substation_no,sub.substation_no,sub.substation_name,sub.substation_type from boss_user u "
                     +" left join substation_user s on u.id=s.user_id "
                     +" left join substation sub on s.substation_no=sub.substation_no "
                     +" where sub.id is not null and sub.`status`=1 and u.user_name=? ";
		List<Object> list = new ArrayList<Object>();
		list.add(userNo);
		Map<String, Object> map = dao.findFirst(sql, list.toArray()) ;
		if (map==null) {
			return false ;
		}
		if (!"J".equals(map.get("substation_type"))) {
			return true ;
		}
		String sno = "" ;
		if (map.get("substation_no")!=null) {
			sno = map.get("substation_no").toString() ;
		}else {
			return false ;
		}
		sql = "select * from  join_substation_acount  where substation_no=?";
		list.clear();
		list.add(sno) ;
		map = dao.findFirst(sql, list.toArray()) ;
		if (map==null) {   //||"0".equals(map.get("status"))
			return true ;
		}
		sql = "select * from  join_substation_acount  where cur_balance>=shut_balance and substation_no=?";
		map = dao.findFirst(sql, list.toArray()) ;
		if (map==null) {
			return false ;
		}
		return true ;
	}
	
	/**
	   * 根据仓管员编号判断所属网点是否为加盟网点，并检查余额
	   * @param userNo
	   * @return 非加盟或者加盟网点余额充足，或者加盟网点账户停用返回true，其余返回false
	   * @throws SQLException
	   */
		public boolean managerStaffShut(String userNo) throws SQLException {
			String sql1="select u.user_name,u.substation_no,sub.substation_no,sub.substation_name,sub.substation_type from warehouse_staff u "
					 +" left join substation sub on u.substation_no=sub.substation_no "
                     +" where sub.id is not null and sub.`status`=1 and u.user_name=? ";
			
			String sql = "select u.user_name,s.substation_no,sub.substation_no,sub.substation_name,sub.substation_type from boss_user u "
	                     +" left join substation_user s on u.id=s.user_id "
	                     +" left join substation sub on s.substation_no=sub.substation_no "
	                     +" where sub.id is not null and sub.`status`=1 and u.user_name=? ";
			List<Object> list = new ArrayList<Object>();
			list.add(userNo);
			Map<String, Object> map = dao.findFirst(sql1, list.toArray()) ;
			if(map==null || map.isEmpty()){
				map = dao.findFirst(sql, list.toArray()) ;
			}
			if (map==null) {
				return false ;
			}
			if (!"J".equals(map.get("substation_type"))) {
				return true ;
			}
			String sno = "" ;
			if (map.get("substation_no")!=null) {
				sno = map.get("substation_no").toString() ;
			}else {
				return false ;
			}
			sql = "select * from  join_substation_acount  where substation_no=?";
			list.clear();
			list.add(sno) ;
			map = dao.findFirst(sql, list.toArray()) ;
			if (map==null||"0".equals(map.get("status"))) {
				return true ;
			}
			sql = "select * from  join_substation_acount  where cur_balance>=shut_balance and substation_no=?";
			map = dao.findFirst(sql, list.toArray()) ;
			if (map==null) {
				return false ;
			}
			return true ;
		}

	public Map getUserByCourierNo(String courierNo) throws Exception{
		String sql = "select * from courier where courier_no=? " ;
		List<Object> list = new ArrayList<Object>();
		list.add(courierNo) ;
		User user = dao.findFirst(User.class, sql,list.toArray() ) ;
		Map m =new HashMap<String, Object>();
		m.put("user", user);
		sql=" select DISTINCT lgc_no from substation where substation_no in(  select substation_no from courier where courier_no=?)";
		Map m1 = dao.findFirst(sql, courierNo);
		if(m1!=null){
			m.put("lgcNo", m1.get("lgc_no"));
		}else{
			m.put("lgcNo","");
		}
		return m;
		
	}
	
	public Cache getSessionCache() {
		return sessionCache;
	}

	public SessionManager getSessionManager() {
		return sessionManager;
	}

	
}
