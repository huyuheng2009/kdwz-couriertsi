package com.yogapay.couriertsi.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.yogapay.couriertsi.utils.Dao;
/**
 * 联行号service
 * @author 
 *
 */
@Service
public class CnapsNoService {
	
	@Resource
	private Dao dao ;
	
	
	public Page<Map<String, Object>> search(String [] keyWords,PageRequest pageRequest) throws SQLException {
		 StringBuffer sql = new StringBuffer() ;
		 sql.append("select bank_name bankName,cnaps_no cnapsNo from dict_cnaps_no where 1=1 ") ;
		 List<Object> list = new ArrayList<Object>();
		 for (String keyWord:keyWords) {
			sql.append(" and (bank_name like ? or bank_addr like ?) ") ;
			list.add("%"+keyWord+"%");
			list.add("%"+keyWord+"%");
		 }
		return dao.find(sql.toString(), list.toArray(), pageRequest) ;
	}
	

}
