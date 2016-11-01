package com.yogapay.couriertsi.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.yogapay.couriertsi.domain.ValidateCode;
import com.yogapay.couriertsi.utils.Dao;
import com.yogapay.couriertsi.utils.DateUtils;

/**
 * 验证码service
 * @author 
 *
 */
@Service
public class ValidateCodeService {
	
	@Resource
	private Dao dao ;
	
	
	public boolean save(ValidateCode validateCode) throws SQLException {
		String sql = "insert into validate_code(phone,code,business_type,create_time,expire_time,repeat_time) values(?,?,?,?,?,?)";
		List<Object> list = new ArrayList<Object>();
		list.add(validateCode.getPhone());
		list.add(validateCode.getCode());
		list.add(validateCode.getBusinessType());
		list.add(DateUtils.formatDate(validateCode.getCreateTime()));
		list.add(DateUtils.formatDate(validateCode.getExpireTime()));
		list.add(validateCode.getRepeatTime());
		dao.updateGetID(sql, list);
		return true ;
	}
	
	//根据业务类型获取相应手机号的验证码信息
	public ValidateCode getLastCode(String phone, int businessType) throws SQLException {
		String sql = "select * from validate_code where phone=? and business_type=? order by create_time desc" ;
		List<Object> list = new ArrayList<Object>();
		list.add(phone);
		list.add(businessType);
		return dao.findFirst(ValidateCode.class,sql,list.toArray()) ;
	}
	
	

}
