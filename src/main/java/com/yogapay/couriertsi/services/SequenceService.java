package com.yogapay.couriertsi.services;

import java.sql.SQLException;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.yogapay.couriertsi.utils.Dao;

@Service
public class SequenceService {

	@Resource private Dao dao ;
	
	public String getNextVal(String seqName) throws SQLException {
		String sql = "select nextval('" + seqName+ "') as t" ;
		Map<String, Object> map = dao.findFirst(sql);
		String val = "" ;
		if (map!=null&&map.get("t")!=null) {
			val = map.get("t").toString() ;
		}
      return val ;
	}
	
	public String getCurrVal(String seqName) throws SQLException {
		String sql = "select currval('" + seqName+ "') as t" ;
		Map<String, Object> map = dao.findFirst(sql);
		String val = "" ;
		if (map!=null&&map.get("t")!=null) {
			val = map.get("t").toString() ;
		}
      return val ;
	}
	
	
	public String setCurrVal(String seqName,String value) throws SQLException {
		String sql = "select setval('" + seqName+ "','"+value+"') as t" ;
		Map<String, Object> map = dao.findFirst(sql);
		String val = "" ;
		if (map!=null&&map.get("t")!=null) {
			val = map.get("t").toString() ;
		}
      return val ;
	}

}
