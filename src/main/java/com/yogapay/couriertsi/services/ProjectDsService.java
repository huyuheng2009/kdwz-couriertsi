package com.yogapay.couriertsi.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.yogapay.couriertsi.dataSource.MDataSource;
import com.yogapay.couriertsi.utils.ManagerDao;

@Service
public class ProjectDsService {
	
	@Resource
	private ManagerDao managerDao ;
	
	
	public List<MDataSource> getByProKey(String proKey) throws SQLException {
		 StringBuffer sql = new StringBuffer() ;
		 sql.append("select * from project_ds where pro_key=? ") ;
		 List<Object> list = new ArrayList<Object>();
         list.add(proKey) ;
		return managerDao.find(MDataSource.class, sql.toString(), list.toArray());
	}
	public String getLgcNo(String code) throws SQLException {
		 String sql ="select * from lgc_config where lgc_key=?  AND status = 1" ;
		 List<Object> list = new ArrayList<Object>();
        list.add(code) ;
        Map<String,Object> map= 	managerDao.findFirst(sql.toString(), list.toArray());
        if(map != null){
        	return (String)map.get("lgc_no");
        }
		return "";		
	}
	public List<Map<String,Object>>  logoUrl(String lgcNo) throws SQLException {
		String sql ="select res_name resName , res_url resUrl from lgc_resource where lgc_no = ?" ;
		List<Object> list = new ArrayList<Object>();
		list.add(lgcNo) ;
		return managerDao.find(sql.toString(), list.toArray());
	}
	public String  getUid(String lgcNo) throws SQLException {
		String sql ="select `key` from project_ds where lgc_no = ?" ;
		List<Object> list = new ArrayList<Object>();
		list.add(lgcNo) ;
		Map<String,Object> map = managerDao.findFirst(sql.toString(), list.toArray());
		if(map!=null){
			return (String)map.get("key");
		}
		return "";
	}
}
