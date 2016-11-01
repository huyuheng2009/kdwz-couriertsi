package com.yogapay.couriertsi.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.yogapay.couriertsi.domain.LgcCustomer;
import com.yogapay.couriertsi.utils.Dao;
@Service
public class LgcCustomerService {
	@Resource
	private Dao dao ;
	@Resource
	private SequenceService sequenceService ;
	
	public void saveIfNotExsit(LgcCustomer lgcCustomer) throws SQLException{
		if (lgcCustomer.getConcat_phone()==null||lgcCustomer.getConcat_phone().length()<1) {
			return ;
		}
		if (!isExsit(lgcCustomer.getConcat_phone())) {
			 String customer_no = sequenceService.getNextVal("customer_no") ;
			 if (!"0".equals(customer_no)) {
				 String sql = "insert into lgc_customer(customer_no,concat_name,concat_phone,concat_addr,source,create_time) values (?,?,?,?,?,?)";
					List<Object> list = new ArrayList<Object>();
			        list.add(customer_no);
			        list.add(lgcCustomer.getConcat_name());
			        list.add(lgcCustomer.getConcat_phone());
			        list.add(lgcCustomer.getConcat_addr());
			        list.add(lgcCustomer.getSource());
			        list.add(lgcCustomer.getCreate_time());
					dao.updateGetID(sql, list);
			}
			
		}
	}
	


public LgcCustomer getByCancatPhone(String  concatPhone) throws SQLException {
	String sql = "select * from lgc_customer where  concat_phone=?";
	List<Object> list = new ArrayList<Object>();
    list.add(concatPhone);
     return dao.findFirst(LgcCustomer.class, sql,list.toArray()) ;	
	
}

	  public boolean isExsit(String concatPhone) throws SQLException {
		  LgcCustomer map = getByCancatPhone(concatPhone) ;
	        boolean flag = true;
	        if (map == null) {
	            flag = false;
	        }
	        return flag;
	    }



}
