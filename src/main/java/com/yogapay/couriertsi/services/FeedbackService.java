package com.yogapay.couriertsi.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.yogapay.couriertsi.domain.Feedback;
import com.yogapay.couriertsi.utils.Dao;
import com.yogapay.couriertsi.utils.DateUtils;
/**
 * 意见反馈service
 * @author 
 *
 */
@Service
public class FeedbackService {
	
	@Resource
	private Dao dao ;
	
	
	public boolean save(Feedback feedback) throws SQLException {
		String sql = "insert into feedback(type,info,name,phone,user_no,app_version,user_type,create_time) values(?,?,?,?,?,?,?,?)";
		List<Object> list = new ArrayList<Object>();
        list.add(feedback.getType());
        list.add(feedback.getInfo());
        list.add(feedback.getName());
        list.add(feedback.getPhone());
        list.add(feedback.getUserNo());
        list.add(feedback.getAppVersion());
        list.add(feedback.getUserType());
        list.add(DateUtils.formatDate(feedback.getCreateTime()));
		dao.updateGetID(sql, list);
		return true ;
	}
	
	

}
