package com.yogapay.couriertsi.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.yogapay.couriertsi.domain.Feedback;
import com.yogapay.couriertsi.utils.Dao;
import com.yogapay.couriertsi.utils.DateUtils;
import com.yogapay.couriertsi.utils.StringUtils;
/**
 * 意见反馈service
 * @author 
 *
 */
@Service
public class OrderSubstationService {
	
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
	
	   public String getStationString(Integer id,String spit) throws SQLException{
		   String sql = "select * from order_substation where order_id=?  and taked=1";
		   List<Object> list = new ArrayList<Object>();
	       list.add(id);
	        
		   List<Map<String, Object>> map = dao.find(sql, list.toArray()) ;
		   StringBuffer rev = new StringBuffer();
		   for(Map<String, Object> m : map){
				if (m.get("substation_no")!=null&&!StringUtils.isEmptyWithTrim(m.get("substation_no").toString())) {
			    rev.append(spit+m.get("substation_no")+spit);
				rev.append(",");
			}
			}
			if (null != rev && rev.length() > 0) {
				rev.setLength(rev.length() - 1);
			}
			return rev.toString() ;
		}
	

}
