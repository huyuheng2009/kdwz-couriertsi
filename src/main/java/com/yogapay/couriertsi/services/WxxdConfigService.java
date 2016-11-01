package com.yogapay.couriertsi.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.yogapay.couriertsi.domain.WxxdConfig;
import com.yogapay.couriertsi.utils.ManagerDao;

@Service
public class WxxdConfigService {
	
	@Resource
	private ManagerDao managerDao ;
	
	
	public WxxdConfig getByDskey(String dskey) throws SQLException {
		 StringBuffer sql = new StringBuffer() ;
		 sql.append("select * from wxxd_config where dskey=? ") ;
		 List<Object> list = new ArrayList<Object>();
         list.add(dskey) ;
		return managerDao.findFirst(WxxdConfig.class, sql.toString(), list.toArray());
	}
}
