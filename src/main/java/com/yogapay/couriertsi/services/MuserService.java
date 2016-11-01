package com.yogapay.couriertsi.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.yogapay.couriertsi.utils.Dao;
/**
 * 月结客户service
 * @author 
 *
 */
@Service
public class MuserService {
	
	@Resource
	private Dao dao ;
//	
////	/**
////	 * 
////	 * @param sno  分站编号
////	 * @param settleNo
////	 * @return
////	 * @throws SQLException
////	 */
//	public Map<String, Object> getbyAllSettleNo(String settleNo) throws SQLException {
//		 String sql = "select m.month_no monthId ,m.month_sname monthName,m.contact_name contactName,m.contact_phone contactPhone from month_settle_user m where m.month_settle_no=?  " ;
//		 List<Object> list = new ArrayList<Object>();
//		 list.add(settleNo) ;
//		return dao.findFirst(sql, list.toArray()) ; 
//	}
//
//	public Map<String, Object> getbySettleNo(String settleNo) throws SQLException {
//		 String sql = "select m.month_settle_no monthSettleNo,m.month_no monthId ,m.month_name monthName,m.contact_name contactName,m.contact_phone contactPhone from month_settle_user m where substr(m.month_settle_no,-5) =? " ;
//		 List<Object> list = new ArrayList<Object>();
//		 list.add(settleNo) ;
//		return dao.findFirst(sql, list.toArray()) ; 
//	}
	/**
	 * 查询月结用户
	 * @param user
	 * @return
	 * @throws SQLException
	 */
	public Map<String,Object> selectMonthBy(String monthSettleNo) throws SQLException {
		String sql = "select m.month_settle_no monthSettleNo,m.month_sname monthSname,s.discount,m.contact_name contactName,m.contact_phone contactPhone "+
				"from  month_settle_user m ,month_settle_type s where m.mstype=s.id and m.month_settle_no=? ";		
		List<Object> list = new ArrayList<Object>();
		list.add(monthSettleNo);
		return	dao.findFirst(sql,list.toArray());

	}
	/**
	 * 查询月结用户后五位
	 * @param user
	 * @return
	 * @throws SQLException
	 */
	public Map<String,Object> selectMonthByFive(String monthSettleNo) throws SQLException {
		String sql = "select m.month_settle_no monthSettleNo,m.month_sname monthSname,s.discount,m.contact_name contactName,m.contact_phone contactPhone "+
				"from  month_settle_user m ,month_settle_type s where m.mstype=s.id and substr(m.month_settle_no,-5)=? ";		
		List<Object> list = new ArrayList<Object>();
		list.add(monthSettleNo);
		return	dao.findFirst(sql,list.toArray());
		
	}
    
}
