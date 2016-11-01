package com.yogapay.couriertsi.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.yogapay.couriertsi.utils.Dao;
import com.yogapay.couriertsi.utils.ManagerDao;
/**
 * 快�?�公司service
 * @author 
 *
 */
@Service
public class LgcService {
	
	@Resource
	private Dao dao ;
	@Resource
	private ManagerDao managerDao ;
	
	public Page<Map<String, Object>> list(Map<String, String> params,PageRequest pageRequest) throws SQLException {
		 StringBuffer sql = new StringBuffer() ;
		 sql.append("select lgc_no lgcNo,name,valuation_rate valuationRate  from lgc where 1=1 ") ;
		 List<Object> list = new ArrayList<Object>();
		return dao.find(sql.toString(), list.toArray(), pageRequest) ;
	}
	
	public String getLgcOrderNo(String orderNo) throws SQLException {
		 String sql = "select lgc_order_no lgcOrderNo from order_info where order_no=? " ;
		 List<Object> list = new ArrayList<Object>();
		 list.add(orderNo) ;
		 Map<String, Object> ret = dao.findFirst(sql, list.toArray()) ;
		 if (ret==null) {
			return "" ;
		}
		return ret.get("lgcOrderNo").toString() ; 
	}

    public Map<String, Object> getLgcInfo(String orderNo) throws SQLException {
    	 String sql = "select s.lgc_no lgcNo ,l.name lgcName,l.pingyin pingyin from order_info o,substation s,lgc l where o.sub_station_no = s.substation_no and s.lgc_no=l.lgc_no and o.order_no=? " ;
		 List<Object> list = new ArrayList<Object>();
		 list.add(orderNo) ;
		return dao.findFirst(sql, list.toArray()) ;
	}
    
    public Map<String, Object> getLgc(String lgcNo) throws SQLException {
   	 String sql = "select * from lgc where lgc_no=?" ;
		 List<Object> list = new ArrayList<Object>();
		 list.add(lgcNo) ;
		return dao.findFirst(sql, list.toArray()) ;
	}
    
    
    public String getDefaultSubstation(String lgcNo) throws SQLException {
    	String sql = "select substation_no from substation where lgc_no=?" ;
    	List<Object> list = new ArrayList<Object>();
		 list.add(lgcNo) ;
		Map<String, Object> ret = dao.findFirst(sql, list.toArray());
    	if (ret!=null) {
			return ret.get("substation_no").toString() ;
		}
    	return "10000";
	}
    
    public Map<String, Object> getSubstationInfo(String sno) throws SQLException {
    	String sql = "select * from substation where substation_no=?" ;
    	List<Object> list = new ArrayList<Object>();
		 list.add(sno) ;
		Map<String, Object> ret = dao.findFirst(sql, list.toArray());
    	return ret;
	}

	public Map<String, Object> getLgcVrate() throws SQLException {
		String sql = "select v.rate_type rateType,v.rate rate,v.latter,v.top,v.minv,v.maxv from valuation_rule v  where status = 1 order by id desc" ; 
		Map<String, Object> ret = dao.findFirst(sql);
    	return ret;
	}

	public Map<String, Object> getLgcCodrate() throws SQLException {
		String sql = "select v.rate_type rateType,v.rate rate,v.latter,v.top,v.minv,v.maxv from cod_rule v where status = 1 order by id desc" ;
		Map<String, Object> ret = dao.findFirst(sql);
    	return ret;
	}

	public Map<String, Object> getLgcByUid(String uid) throws SQLException {
		String sql = "select * from `manager_lgc`.project_ds p where p.key=?";
		Map<String,Object> ret = dao.findFirst(sql, uid);
		return ret;
	}
	
	public List<Map<String, Object>> listPunish() throws SQLException {
		 StringBuffer sql = new StringBuffer() ;
		 sql.append("select p.punish_text AS punishText,p.rule_text AS ruleText from system_punish p order by id desc limit 0,200 ") ;
		return dao.find(sql.toString()) ;
	}
    
	public String getLgcConfig(String config_type,String config_name,String def) throws SQLException {
		 String r = def ;
		 String sql = "select * from lgc_config where config_type=?  and config_name=? " ;
		 List<Object> list = new ArrayList<Object>();
		 list.add(config_type) ;
		 list.add(config_name) ;
		 Map<String, Object> ret = dao.findFirst(sql,list.toArray());
		 if (ret!=null&&ret.get("config_value")!=null) {
			r = ret.get("config_value").toString() ;
		  }
		 return r ;
	}
	
	public void updateByTypeName(String	type,String name,String value) throws SQLException {
		 String sql = "update lgc_config set config_value=? where config_type=?  and config_name=? " ;
		 List<Object> list = new ArrayList<Object>();
		 list.add(value) ;
		 list.add(type) ;
		 list.add(name) ;
		 dao.update(sql, list.toArray()) ;
	}
	
	  public int msgCount(String lgcNo) throws SQLException {        
		  String sql = "select c.message_count as messagecount from manager_lgc.message_count c where c.lgc_no=? " ;
			 List<Object> list = new ArrayList<Object>();
			 list.add(lgcNo) ;
			 Map<String, Object> retMap = managerDao.findFirst(sql,list.toArray());
	        int c = 0 ;
	        if (retMap!=null&&retMap.get("messagecount")!=null) {
				c=Integer.valueOf(retMap.get("messagecount").toString()) ;
			}
	        return c ;
	    }
}
