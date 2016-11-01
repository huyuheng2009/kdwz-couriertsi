package com.yogapay.couriertsi.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.yogapay.couriertsi.domain.OrderInfo;
import com.yogapay.couriertsi.utils.Dao;
import com.yogapay.couriertsi.utils.DateUtils;

@Service
public class OtherService {
	@Resource
	private Dao dao ;

	/**
	 * 查询代收货款
	 * @param user
	 * @return
	 * @throws SQLException
	 */
	public Map<String,Object> selectCodBy(String codName) throws SQLException {
		String sql = "select c.cod_no codNo,  c.cod_sname codSname, c.contact_name contactName,c.contact_phone contactPhone,"+
				"d.discount,d.discount_text  discountText   from cod_settle_user c ,cod_rate_type d where c.cstype=d.id and c.status =1 and c.cod_no=?";
		List<Object> list = new ArrayList<Object>();
		list.add(codName);
		return	dao.findFirst(sql,list.toArray());

	}
	/**
	 * 快速收件绑定收件员
	 * @param user
	 * @return
	 * @throws SQLException
	 */
	public void quickTake(OrderInfo  orderInfo) throws SQLException {
		String sql ="update order_info"
				+ " set take_courier_no = ?,"
				+ "sub_station_no= ?,"
				+ "take_mark = 'Y',"
				+ "take_order_time = ?,"
				+ "last_update_time=? "
				+ "where order_no= ?";
		List<Object> list = new ArrayList<Object>();
		list.add(orderInfo.getTakeCourierNo());
		list.add(orderInfo.getSubStationNo());
		list.add(orderInfo.getTakeOrderTime());
		list.add(orderInfo.getLastUpdateTime());
		list.add(orderInfo.getOrderNo());
		dao.update(sql,list.toArray());
	}
	/**
	 * 签收审核录入信息
	 * @param user
	 * @return
	 * @throws SQLException
	 */
	public void sinputInfo(OrderInfo  orderInfo) throws SQLException {
		String sql ="insert into  sinput_info("
				+ "order_no,"
				+ "lgc_order_no,"
				+ "si_freight_type,"
				+ "si_freight,"
				+ "si_pay_type,"
				+ "si_month_settle_no,"
				+ "si_dis_user_no,"
				+ "si_cod_name,"
				+ "si_good_price,"
				+ "si_good_valuation,"
				+ "si_cpay,"
				+ "si_vpay,"
				+ "si_snpay,"
				+ "si_pay_acount,"
				+ "create_time,"
				+ "op_type,"
				+ "erred"
				+")values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";	
		List<Object> list = new ArrayList<Object>();
		list.add(orderInfo.getOrderNo());
		list.add(orderInfo.getLgcOrderNo());
		list.add(orderInfo.getFreightType());
		list.add(orderInfo.getFreight());
		list.add(orderInfo.getPayType());
		list.add(orderInfo.getMonthSettleNo());
		list.add(orderInfo.getDisUserNo());
		list.add(orderInfo.getCodName());
		list.add(orderInfo.getGoodPrice());
		list.add(orderInfo.getGoodValuation());
		list.add(orderInfo.getCpay());
		list.add(orderInfo.getVpay());
		list.add(orderInfo.getSnapy());
		list.add(orderInfo.getPayAcount());
		list.add(DateUtils.formatDate(new Date(),"yyyy-MM-dd HH:mm:ss"));		
		list.add("SIGN");		
		list.add("N");		
		dao.update(sql,list.toArray());
	}
	public Page<Map<String,Object>> substaion(Map<String, String> params,PageRequest pageRequest){
		String sql = "select substation_name   substationName ,phone     from substation ";
		return dao.find(sql, null, pageRequest);		
	}
	/**
	 * 未交清单
	 * @param params
	 * @param pageRequest
	 * @return
	 * @throws SQLException
	 */
	public Page<Map<String, Object>> onOrder(Map<String, String> params,PageRequest pageRequest) throws SQLException {
		StringBuffer sql = new StringBuffer() ;
		List<Object> list = new ArrayList<Object>();   
		sql.append( "select  o.take_mark takeMark,o.status status,o.freight_type freightType ,")
		.append( "o.item_status itemStatus,o.order_no orderNo,o.readed readed,o.send_area addr,")//大区地址
		.append("o.send_addr  addrExf,")///详细地址
		.append(" o.send_name  name, o.send_phone phone,IF(o.delivery='Y','1','0') delivery,") 
		.append(" o.status orderType,o.take_order_time orderTime,o.lgc_no lgcNo,o.lgc_order_no lgcOrderNo ,")
		.append(" o.freight freight,o.vpay vpay,o.tnpay acount  ,if(o.freight_type=2,'DAOFU',o.pay_type) payType ,o.take_courier_no  takeCourierNo ,o.send_courier_no sendCourierNo")
		.append(" from order_info o where o.take_courier_no=? and o.status=2 and (o.send_courier_no is null OR length(o.send_courier_no)<1) and o.sub_station_no=? and o.lgc_no=?"
				+ "and substr(o.take_order_time,1,10) < ? ");
		list.add(params.get("userNo"));
		list.add(params.get("substation")) ;	 
		list.add(params.get("lgcNo")) ;	 
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd")) ;	 
		sql.append(" order by o.take_order_time desc ");
		return dao.find(sql.toString(), list.toArray(),pageRequest) ;
	}
	/**
	 * 收派范围查询
	 * @param params
	 * @param pageRequest
	 * @return
	 * @throws SQLException
	 */
	public Map<String,Object> scope(String addr) throws SQLException {
		String sql = "select * from lgc_area  l where l.addr_area = ?";
		List<Object> list = new ArrayList<Object>();   
		list.add(addr);
		return dao.findFirst(sql, list.toArray()) ;
	}
	/**
	*收派大区
	 * @param params
	 * @param pageRequest
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String,Object>> scopeArea() throws SQLException {
		String sql = "select addr_area addrArea from lgc_area  l  ";
		return dao.find(sql) ;
	}
	
	/**
	 * 查询订单轨迹 
	 * @throws SQLException 
	 */
	public List<Map<String,Object>> orderTrack(String orderNo) throws SQLException{
		List<Object> list  = new ArrayList<Object>();
		String sql = "select o.order_time orderTime,o.context  from order_track o where o.order_no = ? order by id desc";
		list.add(orderNo);
		return dao.find(sql,list.toArray());		
	}
	/**
	 * 查询运转单号是否存在 返回订单号
	 * @throws SQLException 
	 */
	public String isExitOrder(String lgcOrderNo,String type) throws SQLException{
		List<Object> list  = new ArrayList<Object>();
		String sql ="";
		if("0".equals(type)){
			sql = "select  order_no  from order_info o where o.lgc_order_no  = ?";
		}
		if("1".equals(type)){
			sql = "select  order_no from order_info o where o.for_no  = ?";
		}
		list.add(lgcOrderNo);
		Map<String,Object> map = dao.findFirst( sql, list.toArray());
		if(map!=null){
			return (String)map.get("order_no");
		}
		return "";
	}
}
