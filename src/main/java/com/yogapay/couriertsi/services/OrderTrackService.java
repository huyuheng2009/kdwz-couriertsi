package com.yogapay.couriertsi.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.yogapay.couriertsi.domain.OrderTrack;
import com.yogapay.couriertsi.utils.Dao;
import com.yogapay.couriertsi.utils.DateUtils;

/**
 * 订单物流信息service
 * @author 
 *
 */
@Service
public class OrderTrackService {

	@Resource
	private Dao dao ;

	public void add(Map<String, String> track) throws SQLException {
		String sql = "insert into order_track (order_no,order_time,context,completed) values (?,?,?,?) " ;
		List<Object> list = new ArrayList<Object>();
		list.add(track.get("orderNo")) ;
		list.add(track.get("orderTime")) ;
		list.add(track.get("context")) ;
		list.add(track.get("check")) ;
		dao.update(sql, list.toArray()) ;
	}

//新增运转信息
	public void add(OrderTrack track) throws SQLException {
		String sql = "insert into order_track (order_no,order_time,context,completed,pre_no,pre_type,cur_no,cur_type,next_no,next_type,order_status,parent_id,is_last,scan_ino,scan_iname,scan_ono,scan_oname,opname) "
				+ " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) " ;
		List<Object> list = new ArrayList<Object>();
		list.add(track.getOrderNo()) ;
		list.add(track.getOrderTime()) ;
		list.add(track.getContext()) ;
		list.add(track.getCompleted()) ;
		list.add(track.getPreNo()) ;
		list.add(track.getPreType()) ;
		list.add(track.getCurNo()) ;
		list.add(track.getCurType()) ;
		list.add(track.getNextNo()) ;
		list.add(track.getNextType()) ;
		list.add(track.getOrderStatus()) ;
		list.add(track.getParentId()) ;
		list.add(track.getIsLast()) ;
		list.add(track.getScanIno()) ;
		list.add(track.getScanIname()) ;
		list.add(track.getScanOno()) ;
		list.add(track.getScanOname()) ;  	   
		list.add(track.getOpname()) ;
		dao.update(sql, list.toArray()) ;
	}
	//查询最新的运转信息
	public Map<String,Object> checkOrderTrack(String orderNo) throws SQLException{
		List<Object> list = new ArrayList<Object>();
		String sql="select *  from order_track where order_no=? order by order_time desc ";	
		list.add(orderNo);
		Map<String,Object> map = dao.findFirst(sql,list.toArray());	
		return 	map;			
	}
	//查询最新的运转信息 返回BEAN
		public OrderTrack queryOrderTrack(String orderNo) throws SQLException{
			List<Object> list = new ArrayList<Object>();
			String sql="select *  from order_track where order_no=? order by id desc ";	
			list.add(orderNo);
			return dao.findFirst(OrderTrack.class,sql,list.toArray());					
		}
	//更新上一条信息是否最新
	public void updateIsLast(int id) throws SQLException{
		List<Object> list = new ArrayList<Object>();
		String sql="update order_track set is_last='N' where id=?";		
		list.add(id);
		dao.update(sql,list.toArray());
				
	}
	//更新上一条信息是否
		public void updateIsLastTime(int id) throws SQLException{
			List<Object> list = new ArrayList<Object>();
			String sql="update order_track set is_last='Y'  ,order_time = ? where id=?";		
			list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
			list.add(id);
			dao.update(sql,list.toArray());
			
		}

	public List<OrderTrack> getByOrderNo(String orderNo,boolean orderby) throws SQLException {
		String sql = "select * from  order_track  where order_no=? " ;
		if (orderby) {
			sql+=" order by id desc ";
		}
		List<Object> list = new ArrayList<Object>();
		list.add(orderNo) ;
		return dao.find(OrderTrack.class,sql, list.toArray()) ;
	}


	public OrderTrack getLastOrderTrack(String orderNo) throws SQLException {
		String sql = "select * from order_track where order_no=? order by id desc" ;
		List<Object> list = new ArrayList<Object>();
		list.add(orderNo) ;
		return dao.findFirst(OrderTrack.class,sql, list.toArray()) ;

	}

	public void unLastTrack(OrderTrack lastOrderTrack) throws SQLException {
		String sql = "update order_track set is_last='N'  where id=? " ;
		List<Object> list = new ArrayList<Object>();
		list.add(lastOrderTrack.getId()) ;
		dao.update(sql, list.toArray()) ;
	}

}
