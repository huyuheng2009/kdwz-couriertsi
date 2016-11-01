package com.yogapay.couriertsi.services;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.yogapay.couriertsi.utils.Dao;

@Service
public class YouxinService {
	@Resource
	private Dao dao ;
	
	public void saveYouxinCallLog(Map<String, String> params){
		String sql = "insert into youxin_call_log (start_time,end_time,call_type,callee_phone,caller_phone,call_status,call_time)"
				+ " values (?,?,?,?,?,?,?)";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date startTime = null;
		Date endTime = null;
		try {
			startTime = sdf.parse(params.get("startTime"));
			endTime = sdf.parse(params.get("endTime"));
			String start = sdf.format(startTime);
			String end = sdf.format(endTime);
			dao.update(sql, new Object[]{startTime, endTime, params.get("callType"), params.get("calleePhone"),
					params.get("callerPhone"),params.get("callStatus"),params.get("callTime")});
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
