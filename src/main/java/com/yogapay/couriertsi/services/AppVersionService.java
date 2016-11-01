package com.yogapay.couriertsi.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.yogapay.couriertsi.utils.Dao;
import com.yogapay.couriertsi.utils.ManagerDao;
import com.yogapay.couriertsi.utils.StringUtils;

/**
 * 软件版本service
 * @author 
 *
 */
@Service
public class AppVersionService {
	
	@Resource
	private ManagerDao managerDao ;
	@Resource
	private Dao dao ;
	
	public Map<String, Object> lastVersion(Map<String, String> params) throws SQLException {
		 StringBuffer sql = new StringBuffer() ;
		 List<Object> list = new ArrayList<Object>();
		 sql.append( "select v.* from app_version v left join app_product p on v.app_code=p.app_code where 1=1 ") ;
		 if (StringUtils.isNotEmptyWithTrim(params.get("bname"))) {
				sql.append(" and p.bname=? ") ;
				list.add(params.get("bname")) ;
			 }
		 if (StringUtils.isNotEmptyWithTrim(params.get("appCode"))) {
			sql.append(" and v.app_code=? ") ;
			list.add(params.get("appCode")) ;
		 }
		 if (StringUtils.isNotEmptyWithTrim(params.get("platform"))) {
				sql.append(" and v.platform=? ") ;
				list.add(params.get("platform")) ;
		}
		 sql.append(" order by v.id desc ") ;
		return managerDao.findFirst(sql.toString(), list.toArray()) ;
	}

	public List<Map<String, Object>> getUpdateList(Map<String, Object> curVersion) throws SQLException {
		 StringBuffer sql = new StringBuffer() ;
		 List<Object> list = new ArrayList<Object>();
		 sql.append( "select v.* from app_version v left join app_product p on v.app_code=p.app_code where 1=1 ")
		    .append(" and p.bname=? and v.platform=? and v.id>? ");
				list.add(curVersion.get("bname")) ;
				list.add(curVersion.get("platform")) ;
				list.add(curVersion.get("id")) ;
		return managerDao.find(sql.toString(), list.toArray()) ;
	
	}

	public Map<String, Object> getVersion(Map<String, String> params) throws SQLException {
		 StringBuffer sql = new StringBuffer() ;
		 List<Object> list = new ArrayList<Object>();
		 sql.append( "select v.*,p.bname from app_version v left join app_product p on v.app_code=p.app_code where 1=1 ") ;
		 if (StringUtils.isNotEmptyWithTrim(params.get("bname"))) {
				sql.append(" and p.bname=? ") ;
				list.add(params.get("bname")) ;
			 }
		 if (StringUtils.isNotEmptyWithTrim(params.get("appCode"))) {
			sql.append(" and v.app_code=? ") ;
			list.add(params.get("appCode")) ;
		 }
		 if (StringUtils.isNotEmptyWithTrim(params.get("platform"))) {
				sql.append(" and v.platform=? ") ;
				list.add(params.get("platform")) ;
		}
		 if (StringUtils.isNotEmptyWithTrim(params.get("version"))) {
				sql.append(" and v.version=? ") ;
				list.add(params.get("version")) ;
		}
		 sql.append(" order by v.id desc ") ;
		return managerDao.findFirst(sql.toString(), list.toArray()) ;
	}
	
	//获取版本支持的支付方式
	public List<Map<String, Object>> getPayType(String lgcNo) throws SQLException {
	String sql = "select pt.pay_code payCode ,pt.pay_name  payName    from "
			+ "   lgc_pay_type lpt ,pay_type pt where lpt.pay_id=pt.id AND  lpt.lgc_no =? AND  pt.`status`=1 ";
	 List<Object> list = new ArrayList<Object>();
	 list.add(lgcNo);
	 return managerDao.find(sql,list.toArray());
	}
}
