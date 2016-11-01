package com.yogapay.couriertsi.services;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.yogapay.couriertsi.domain.LgcCustomer;
import com.yogapay.couriertsi.domain.OrderInfo;
import com.yogapay.couriertsi.domain.OrderTrack;
import com.yogapay.couriertsi.dto.OrderDto;
import com.yogapay.couriertsi.utils.Dao;
import com.yogapay.couriertsi.utils.DateUtils;
import com.yogapay.couriertsi.utils.StringUtils;

/**
 * 订单service
 * 
 * @author
 * 
 */
@Service
public class OrderInfoService {

	@Resource
	private Dao dao;
	@Autowired
	private SequenceService sequenceService;
	@Autowired
	private OrderTrackService orderTrackService;
	@Autowired
	private LgcCustomerService lgcCustomerService ;

	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public long save(OrderInfo orderInfo) throws SQLException {
		String sql = "insert into order_info(order_no,lgc_no,user_no,send_area,send_addr,send_name,send_phone,send_location,"
				+ "rev_area,rev_addr,rev_name,rev_phone,rev_location,item_name,item_weight,freight_type,"
				+ "month_settle_name,month_settle_no,month_settle_card,cod,good_price,cod_card_no,cod_name,cod_card_cnaps_no,cod_bank,"
				+ "good_valuation,good_valuation_rate,take_time,take_time_begin,take_time_end,take_addr,take_location,order_note,"
				+ "create_time,last_update_time,status,source,item_Status,dis_user_no,parent_order)"
				+ " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		List<Object> list = new ArrayList<Object>();
		list.add(orderInfo.getOrderNo());
		list.add(orderInfo.getLgcNo());// 755201
		list.add(orderInfo.getUserNo());
		list.add(orderInfo.getSendArea());
		list.add(orderInfo.getSendAddr());
		list.add(orderInfo.getSendName());
		list.add(orderInfo.getSendPhone());
		list.add(orderInfo.getSendLocation());
		list.add(orderInfo.getRevArea());
		list.add(orderInfo.getRevAddr());
		list.add(orderInfo.getRevName());
		list.add(orderInfo.getRevPhone());
		list.add(orderInfo.getRevLocation());
		// list.add(orderInfo.getItemType());
		list.add(orderInfo.getItemName());
		list.add(orderInfo.getItemWeight());
		list.add(orderInfo.getFreightType());
		list.add(orderInfo.getMonthSettleName());
		list.add(orderInfo.getMonthSettleNo());
		list.add(orderInfo.getMonthSettleCard());
		list.add(orderInfo.getCod());
		list.add(orderInfo.getGoodPrice());
		list.add(orderInfo.getCodCardNo());
		list.add(orderInfo.getCodName());
		list.add(orderInfo.getCodCardCnapsNo());
		list.add(orderInfo.getCodBank());
		list.add(orderInfo.getGoodValuation());
		list.add(orderInfo.getGoodValuationRate());
		list.add(orderInfo.getTakeTime());
		list.add(orderInfo.getTakeTimeBegin());
		list.add(orderInfo.getTakeTimeEnd());
		list.add(orderInfo.getTakeAddr());
		list.add(orderInfo.getTakeLocation());
		list.add(orderInfo.getOrderNote());
		list.add(orderInfo.getCreateTime());
		list.add(orderInfo.getLastUpdateTime());
		list.add(orderInfo.getStatus());
		list.add(orderInfo.getSource());
		list.add(orderInfo.getItemStatus());
		list.add(orderInfo.getDisUserNo());
		list.add(orderInfo.getParentOrder());
		long id = dao.updateGetID(sql, list); 
		
		LgcCustomer lgcCustomer = new LgcCustomer() ;
		lgcCustomer.setConcat_phone(orderInfo.getSendPhone());
		lgcCustomer.setConcat_name(orderInfo.getSendName());
		lgcCustomer.setConcat_addr(orderInfo.getSendArea()+orderInfo.getSendAddr());
		lgcCustomer.setSource("COURIER");
		lgcCustomer.setCreate_time(DateUtils.formatDate(new Date()));
		lgcCustomerService.saveIfNotExsit(lgcCustomer);
		
		return id ;
	}

	/**
	 * 查询代收件
	 * 
	 * @param params
	 * @param pageRequest
	 * @return
	 * @throws SQLException
	 */
	public Page<Map<String, Object>> listByTypeOne(Map<String, String> params,
			PageRequest pageRequest) throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		sql.append(
				"select  o.for_no forNo,o.take_mark takeMark,o.status status,o.freight_type freightType,")
				.append("o.item_status itemStatus,o.order_no orderNo,o.readed readed,o.send_area addr,")
				// 大区地址
				.append("IF(ISNULL(o.send_addr),'',o.send_addr)  addrExf,")
				// /详细地址
				.append(" IF(ISNULL(o.send_name),'',o.send_name)  name, IF(ISNULL(o.send_phone),'',o.send_phone) phone,IF(o.delivery='Y','1','0') delivery,")
				.append(" o.status orderType,o.create_time orderTime,o.lgc_no lgcNo,IF(ISNULL(o.lgc_order_no),'',o.lgc_order_no)  lgcOrderNo, ")
				.append(" o.freight freight,o.vpay vpay,o.pay_type payType ")
				.append(" from order_info o where o.take_courier_no=? and o.status=1 and o.sub_station_no=? and o.lgc_no=?  ");
		list.add(params.get("userNo"));
		list.add(params.get("substation"));
		list.add(params.get("lgcNo"));
		if (!StringUtils.isEmptyWithTrim(params.get("beginTime"))
				&& StringUtils.isEmptyWithTrim(params.get("startTime"))) {
			sql.append(" and substr(o.create_time,1,10)=? ");
			list.add(params.get("beginTime"));
		}

		sql.append(" order by o.order_no desc ");
		return dao.find(sql.toString(), list.toArray(), pageRequest);
	}

	public Page<Map<String, Object>> listByUnix(Map<String, String> params,
			PageRequest pageRequest) throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		sql.append(
				"select    o.for_no forNo,a.revLongitude ,a.revLatitude ,a.sendLongitude,a.sendLatitude, a.takeMark,a.status,a.freightType ,a.areaAddr,")
				.append("a.itemStatus,a.orderNo,a.readed,a.addr,a.addrExf,a.name, a.phone,a.delivery, a.orderType,a.orderTime,")
				.append("a.lgcNo,a.lgcOrderNo  from ")
				.append(" ( select o.rev_longitude revLongitude ,o.rev_latitude revLatitude ,o.send_longitude sendLongitude,o.send_latitude ")
				.append("sendLatitude, o.take_mark takeMark,o.status status,o.freight_type freightType ,o.send_area  areaAddr")
				.append(",o.item_status itemStatus,o.order_no orderNo,o.readed readed,o.rev_area addr,o.rev_addr addrExf,")
				.append("o.rev_name name, o.rev_phone phone,IF(o.delivery='Y','1','0') delivery, o.status orderType,o.send_order_time ")
				.append("orderTime,o.lgc_no lgcNo,o.lgc_order_no lgcOrderNo from order_info o ")
				.append(" where o.send_courier_no=? and o.status =2 and o.send_substation_no=?");
		list.add(params.get("userNo"));
		list.add(params.get("substation"));
		if (!StringUtils.isEmptyWithTrim(params.get("beginTime"))
				&& StringUtils.isEmptyWithTrim(params.get("startTime"))) {
			sql.append(" and substr(o.create_time,1,10)=? ");
			list.add(params.get("beginTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("startTime"))) {
			sql.append(" and o.create_time>=? ");
			list.add(params.get("startTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("topTime"))) {
			sql.append(" and o.create_time<=? ");
			list.add(params.get("topTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("itemStatus"))) {
			sql.append(" and o.item_status=? ");
			list.add(params.get("itemStatus"));
		}
		sql.append("union all  ");
		sql.append(
				"select   o.for_no forNo,o.rev_longitude revLongitude ,o.rev_latitude revLatitude ,o.send_longitude sendLongitude,o.send_latitude ")
				.append("sendLatitude, o.take_mark takeMark,o.status status,o.freight_type freightType ,o.rev_area areaAddr")
				.append(" ,o.item_status itemStatus,o.order_no orderNo,o.readed readed,o.send_area addr, ")
				.append("IF(ISNULL(o.take_addr) OR LENGTH(TRIM(o.take_addr))<1,o.send_addr,o.take_addr) addrExf")
				.append("addrExf,o.send_name name, o.send_phone phone,IF(o.delivery='Y','1','0') delivery, o.status orderType,o.create_time orderTime,")
				.append("o.lgc_no lgcNo,o.lgc_order_no lgcOrderNo from order_info o")
				.append("  where o.take_courier_no=? and o.status =1 and o.sub_station_no=? ");
		list.add(params.get("userNo"));
		list.add(params.get("substation"));
		if (!StringUtils.isEmptyWithTrim(params.get("beginTime"))
				&& StringUtils.isEmptyWithTrim(params.get("startTime"))) {
			sql.append(" and substr(o.create_time,1,10)=? ");
			list.add(params.get("beginTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("startTime"))) {
			sql.append(" and o.create_time>=? ");
			list.add(params.get("startTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("topTime"))) {
			sql.append(" and o.create_time<=? ");
			list.add(params.get("topTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("itemStatus"))) {
			sql.append(" and o.item_status=? ");
			list.add(params.get("itemStatus"));
		}
		sql.append(") a  group by a.orderNo desc ");
		return dao.find(sql.toString(), list.toArray(), pageRequest);
	}

	public Page<Map<String, Object>> list(Map<String, String> params,
			PageRequest pageRequest) throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		sql.append(
				" select   o.for_no forNo,o.take_mark takeMark,o.status status,o.freight_type freightType , IF(o.take_mark='Y',o.send_area,o.rev_area) areaAddr,o.item_status itemStatus,o.order_no orderNo,o.readed readed,IF(o.status=1,o.send_area,o.rev_area) addr,")
				// 大区地址
				.append("IF(o.status=1,IF(ISNULL(o.take_addr) OR LENGTH(TRIM(o.take_addr))<1,o.send_addr,o.take_addr),o.rev_addr) addrExf,")
				// /详细地址
				.append(" IF(o.status=1,o.send_name,o.rev_name) name, IF(o.status=1,o.send_phone,o.rev_phone) phone,IF(o.delivery='Y','1','0') delivery,")
				.append(" o.status orderType,IF(o.status=1,o.take_order_time,o.send_order_time) orderTime,o.lgc_no lgcNo,o.lgc_order_no lgcOrderNo ")
				.append(" from order_info o where IF(o.status=1,o.take_courier_no,o.send_courier_no)=?");
		list.add(params.get("userNo"));
		if (!StringUtils.isEmptyWithTrim(params.get("status"))
				&& !"-1".equals(params.get("status"))) {
			sql.append("and o.status =?");
			list.add(params.get("status"));
		}
		if ("1".equals(params.get("status"))
				&& !StringUtils.isEmptyWithTrim(params.get("substation"))) {
			sql.append(" and o.sub_station_no=? ");
			list.add(params.get("substation"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("beginTime"))
				&& StringUtils.isEmptyWithTrim(params.get("startTime"))) {
			sql.append(" and substr(o.create_time,1,10)=? ");
			list.add(params.get("beginTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("startTime"))) {
			sql.append(" and o.create_time>=? ");
			list.add(params.get("startTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("topTime"))) {
			sql.append(" and o.create_time<=? ");
			list.add(params.get("topTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("itemStatus"))) {
			sql.append(" and o.item_status=? ");
			list.add(params.get("itemStatus"));
		}
		sql.append(" order by o.order_no desc ");
		return dao.find(sql.toString(), list.toArray(), pageRequest);
	}

	/**
	 * 查询代派件
	 * 
	 * @param params
	 * @param pageRequest
	 * @return
	 * @throws SQLException
	 */
	public Page<Map<String, Object>> listByTypeTwo(Map<String, String> params,
			PageRequest pageRequest) throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		sql.append(
				"select   o.for_no forNo,o.pro_order proOrder, o.take_mark takeMark,o.status status,o.freight_type freightType,")
				.append("o.item_status itemStatus,o.cod, o.order_no orderNo,o.readed readed,o.rev_area addr,")
				// 大区地址
				.append("o.rev_addr addrExf,")
				// /详细地址
				.append("o.rev_name name, o.rev_phone phone,IF(o.delivery='Y','1','0') delivery,")
				.append(" o.status orderType,o.send_order_time orderTime,o.lgc_no lgcNo,o.lgc_order_no lgcOrderNo ,")
				.append(" o.freight freight,o.vpay vpay,o.snpay acount,o.pay_type payType,o.zid ")
				.append(" from order_info o where o.send_courier_no=? and o.status !=3 and o.status !=9 and o.send_substation_no=? and o.lgc_no=? ");
		list.add(params.get("userNo"));
		list.add(params.get("substation"));
		list.add(params.get("lgcNo"));
		if (!StringUtils.isEmptyWithTrim(params.get("beginTime"))
				&& StringUtils.isEmptyWithTrim(params.get("startTime"))) {
			sql.append(" and substr(o.create_time,1,10)=? ");
			list.add(params.get("beginTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("startTime"))) {
			sql.append(" and o.create_time>=? ");
			list.add(params.get("startTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("topTime"))) {
			sql.append(" and o.create_time<=? ");
			list.add(params.get("topTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("itemStatus"))) {
			sql.append(" and o.item_status=? ");
			list.add(params.get("itemStatus"));
		}
		sql.append(" order by o.pro_order asc,o.send_order_time desc ");
		return dao.find(sql.toString(), list.toArray(), pageRequest);
	}

	/**
	 * 查询已经收件
	 * 
	 * @param params
	 * @param pageRequest
	 * @return
	 * @throws SQLException
	 */
	public Page<Map<String, Object>> listByTypeOneBefore(
			Map<String, String> params, PageRequest pageRequest)
			throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		sql.append(
				"select    o.for_no forNo,o.pro_order proOrder, o.take_mark takeMark,o.status status,o.freight_type freightType ,")
				.append("o.item_status itemStatus,o.order_no orderNo,o.readed readed,o.send_area addr,")
				// 大区地址
				.append("o.send_addr  addrExf,")
				// /详细地址
				.append(" o.send_name  name, o.send_phone phone,IF(o.delivery='Y','1','0') delivery,")
				.append(" o.status orderType,o.take_order_time orderTime,o.lgc_no lgcNo,o.lgc_order_no lgcOrderNo ,")
				.append(" o.freight freight,o.vpay vpay,o.tnpay acount,if(o.freight_type=2,'DAOFU',o.pay_type) payType ,o.take_courier_no  takeCourierNo ,o.send_courier_no sendCourierNo,o.zid ")
				.append(" from order_info o where o.take_courier_no=? and o.status=2 and (o.send_courier_no is null OR length(o.send_courier_no)<1)  and o.sub_station_no=? and o.lgc_no=?");
		list.add(params.get("userNo"));
		list.add(params.get("substation"));
		list.add(params.get("lgcNo"));
		if (!StringUtils.isEmptyWithTrim(params.get("startTime"))) {
			sql.append(" and o.take_order_time>=? ");
			list.add(params.get("startTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("topTime"))) {
			sql.append(" and o.take_order_time<=? ");
			list.add(params.get("topTime"));
		}

		sql.append(" order by o.take_order_time desc ");
		return dao.find(sql.toString(), list.toArray(), pageRequest);
	}

	/**
	 * 查询明细
	 * 
	 * @param params
	 * @param pageRequest
	 * @return
	 * @throws SQLException
	 */
	public Page<Map<String, Object>> detailList(Map<String, String> params,
			PageRequest pageRequest) throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		sql.append("select n.* from");
		sql.append("((select o.take_mark takeMark,3 status,o.item_status itemStatus,o.`status` orderType ,o.cpay_type cPayType, ");
		sql.append("o.order_no orderNo,o.lgc_order_no lgcOrderNo,o.cod cod,o.good_price goodPrice,o.zid,");
		sql.append("o.send_order_time orderTime,  (o.snpay-good_price)  orderMoney ,o.pay_type payType ,o.freight_type freightType from order_info o ");
		sql.append("where o.send_courier_no=?  AND  o.status =3  AND o.lgc_no = ? AND (o.freight_type =2 OR o.cod=1 ) and  o.zid!=1  ");
		list.add(params.get("userNo"));
		list.add(params.get("lgcNo"));
		if (!StringUtils.isEmptyWithTrim(params.get("beginTime"))) {
			sql.append(" AND SUBSTR(o.send_order_time,1,10)>=? ");
			list.add(params.get("beginTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("endTime"))) {
			sql.append(" AND SUBSTR(o.send_order_time,1,10)<=? ");
			list.add(params.get("endTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("payType"))) {
			sql.append(" AND o.pay_type IN (" + params.get("payType") + ") ");

		}
		sql.append(") UNION ALL( ");
		sql.append("  select o.take_mark takeMark,2 status,o.item_status itemStatus,o.`status` orderType ,o.cpay_type cPayType, ");
		sql.append("o.order_no orderNo,o.lgc_order_no lgcOrderNo,o.cod cod,o.good_price goodPrice,o.zid,");
		sql.append("o.take_order_time orderTime,o.tnpay orderMoney   ,o.pay_type payType ,o.freight_type freightType	from order_info o ");
		sql.append("where o.take_courier_no=?  AND o.freight_type =1 AND  o.status in (2,3,4,6,7,8)  and  o.zid!=1 ");
		list.add(params.get("userNo"));
		if (!StringUtils.isEmptyWithTrim(params.get("beginTime"))) {
			sql.append("AND SUBSTR(o.take_order_time,1,10)>=? ");
			list.add(params.get("beginTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("endTime"))) {
			sql.append("AND SUBSTR(o.take_order_time,1,10)<=? ");
			list.add(params.get("endTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("payType"))) {
			sql.append("AND o.pay_type IN (" + params.get("payType") + ")  ");

		}
		sql.append(") ) n order by n.orderTime desc");
		return dao.find(sql.toString(), list.toArray(), pageRequest);
	}

	/**
	 * 查询明细 已经收件
	 * 
	 * @param params
	 * @param pageRequest
	 * @return
	 * @throws SQLException
	 */
	public Page<Map<String, Object>> detailListTakeOrder(
			Map<String, String> params, PageRequest pageRequest)
			throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		sql.append(" select o.take_mark takeMark,2 status,o.item_status itemStatus,o.`status` orderType ,o.cpay_type cPayType, ");
		sql.append("o.order_no orderNo,o.lgc_order_no lgcOrderNo,o.zid,");
		sql.append("o.take_order_time orderTime,o.tnpay orderMoney ,o.pay_type payType	,o.freight_type freightType ,o.zid zid from order_info o ");
		sql.append("where o.take_courier_no=?  AND o.lgc_no = ?  AND o.freight_type =1 AND o.status in (2,3,4,6,7,8) and  o.zid!=1  ");
		list.add(params.get("userNo"));
		list.add(params.get("lgcNo"));
		if (!StringUtils.isEmptyWithTrim(params.get("beginTime"))) {
			sql.append("AND SUBSTR(o.take_order_time,1,10)>=? ");
			list.add(params.get("beginTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("endTime"))) {
			sql.append("AND SUBSTR(o.take_order_time,1,10)<=? ");
			list.add(params.get("endTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("payType"))) {
			sql.append("AND o.pay_type IN (" + params.get("payType") + ") ");

		}
		sql.append(" order by o.take_order_time desc");
		return dao.find(sql.toString(), list.toArray(), pageRequest);
	}

	/**
	 * 查询明细 已派件
	 * 
	 * @param params
	 * @param pageRequest
	 * @return
	 * @throws SQLException
	 */
	public Page<Map<String, Object>> detailListSendOrder(
			Map<String, String> params, PageRequest pageRequest)
			throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();

		sql.append("select o.take_mark takeMark,3 status,o.item_status itemStatus,o.`status` orderType ,o.cpay_type cPayType, ");
		sql.append("o.order_no orderNo,o.lgc_order_no lgcOrderNo,o.cod cod,o.good_price goodPrice,o.zid,");
		sql.append("o.send_order_time orderTime,  (o.snpay-good_price)   orderMoney 	,o.pay_type payType,o.freight_type freightType  from order_info o ");
		sql.append("where o.send_courier_no=? AND o.status =3 AND o.lgc_no = ? AND (o.freight_type =2 OR o.cod=1) and o.zid !=1  ");
		list.add(params.get("userNo"));
		list.add(params.get("lgcNo"));
		if (!StringUtils.isEmptyWithTrim(params.get("beginTime"))) {
			sql.append("AND SUBSTR(o.send_order_time,1,10)>=? ");
			list.add(params.get("beginTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("endTime"))) {
			sql.append("AND SUBSTR(o.send_order_time,1,10)<=? ");
			list.add(params.get("endTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("payType"))) {
			sql.append("AND o.pay_type IN (" + params.get("payType") + ")  ");

		}
		sql.append(" order by send_order_time desc");
		return dao.find(sql.toString(), list.toArray(), pageRequest);
	}

	// 登录页面总金额 收件金额
	public BigDecimal loginTnpaySum(Map<String, String> params,
			PageRequest pageRequest) throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		sql.append("select  IF(ISNULL(sum(o.tnpay)),0,sum(o.tnpay))  tnpay from order_info o where o.take_courier_no=? AND substr(o.take_order_time,1,10)=?");
		list.add(params.get("userNo"));
		list.add(params.get("beginTime"));
		Map<String, Object> mao = dao.findFirst(sql.toString(), list.toArray());
		return (BigDecimal) mao.get("tnpay");
	}

	// 登录页面总金额 派件金额
	public BigDecimal loginSnpaySum(Map<String, String> params,
			PageRequest pageRequest) throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		sql.append("select IF(ISNULL(sum( o.snpay)),0,sum(o.snpay))  snpay from order_info o where o.send_courier_no=? AND substr(o.send_order_time,1,10)=? AND  status =3 ");
		list.add(params.get("userNo"));
		list.add(params.get("beginTime"));
		Map<String, Object> mao = dao.findFirst(sql.toString(), list.toArray());
		return (BigDecimal) mao.get("snpay");
	}

	// 明细页面总金额 收件金额
	public BigDecimal detailTnpaySum(Map<String, String> params,
			PageRequest pageRequest) throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		sql.append("select  IF(ISNULL(sum(o.tnpay)),0,sum(o.tnpay))  tnpay from order_info o where o.take_courier_no=? AND o.lgc_no = ? AND o.freight_type = 1 AND zid!=1 ");
		list.add(params.get("userNo"));
		list.add(params.get("lgcNo"));
		if (!StringUtils.isEmptyWithTrim(params.get("beginTime"))) {
			sql.append("AND substr(o.take_order_time,1,10)>=?");
			list.add(params.get("beginTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("endTime"))) {
			sql.append("AND substr(o.take_order_time,1,10)<=?");
			list.add(params.get("endTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("payType"))) {
			sql.append("AND o.pay_type IN (" + params.get("payType") + ") ");

		}
		Map<String, Object> mao = dao.findFirst(sql.toString(), list.toArray());
		return (BigDecimal) mao.get("tnpay");
	}

	// 明细页面收件现金总金额
	public BigDecimal detailTnpaySumCash(Map<String, String> params)
			throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		sql.append("select  IF(ISNULL(sum(o.tnpay)),0,sum(o.tnpay))  tnpay from order_info o where o.take_courier_no=?  AND o.pay_type =? AND o.lgc_no = ?  AND o.status !=9 and zid!=1  "
				+ "AND o.freight_type = 1 ");
		list.add(params.get("userNo"));
		list.add("CASH");
		list.add(params.get("lgcNo"));
		if (!StringUtils.isEmptyWithTrim(params.get("beginTime"))) {
			sql.append("AND substr(o.take_order_time,1,10)>=substr(?,1,10)");
			list.add(params.get("beginTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("endTime"))) {
			sql.append("AND substr(o.take_order_time,1,10)<=substr(?,1,10)");
			list.add(params.get("endTime"));
		}
		Map<String, Object> mao = dao.findFirst(sql.toString(), list.toArray());
		return (BigDecimal) mao.get("tnpay");
	}

	// 明细页面总金额 派件金额
	public BigDecimal detailSnpaySum(Map<String, String> params,
			PageRequest pageRequest) throws SQLException {

		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		sql.append("select  IF(ISNULL(sum(o.snpay)),0,sum(o.snpay)) snpay from order_info o where o.send_courier_no=?  and status =3 AND o.lgc_no = ? AND zid!=1 ");
		list.add(params.get("userNo"));
		list.add(params.get("lgcNo"));
		if (!StringUtils.isEmptyWithTrim(params.get("beginTime"))) {
			sql.append("AND substr(o.send_order_time,1,10)>=?");
			list.add(params.get("beginTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("endTime"))) {
			sql.append("AND substr(o.send_order_time,1,10)<=?");
			list.add(params.get("endTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("payType"))) {
			sql.append("AND o.pay_type IN (" + params.get("payType") + ") ");

		}
		Map<String, Object> mao = dao.findFirst(sql.toString(), list.toArray());
		return (BigDecimal) mao.get("snpay");
	}

	// 明细页面派件现金总金额
	public BigDecimal detailSnpaySumCash(Map<String, String> params)
			throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		sql.append("select  IF(ISNULL(sum(o.snpay-o.good_price)),0,sum(o.snpay-o.good_price)) snpay from order_info o where o.send_courier_no=?  and o.status =3  and zid!=1 "
				+ "and o.pay_type = ?  AND o.lgc_no = ?   ");
		list.add(params.get("userNo"));
		list.add("CASH");
		list.add(params.get("lgcNo"));
		if (!StringUtils.isEmptyWithTrim(params.get("beginTime"))) {
			sql.append("AND substr(o.send_order_time,1,10)>=substr(?,1,10)");
			list.add(params.get("beginTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("endTime"))) {
			sql.append("AND substr(o.send_order_time,1,10)<=substr(?,1,10)");
			list.add(params.get("endTime"));
		}
		Map<String, Object> mao = dao.findFirst(sql.toString(), list.toArray());
		return (BigDecimal) mao.get("snpay");
	}

	// 明细页面 除现金收款外的所有支付方式的代收货款
	public BigDecimal detailSnpaySumCodPay(Map<String, String> params)
			throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		sql.append("select  if(isnull(sum(if((ISNuLL(o.good_price)  or LENGTH(o.good_price)<1),0, o.good_price)) ),0,sum(if((ISNuLL(o.good_price)  or LENGTH(o.good_price)<1),0, o.good_price) )) "
				+ "  goodPrice   from order_info o where o.send_courier_no=?  and o.status =3   AND o.lgc_no = ? AND cpay_type = 'CASH'  and zid!=1 ");
		list.add(params.get("userNo"));
		list.add(params.get("lgcNo"));
		if (!StringUtils.isEmptyWithTrim(params.get("beginTime"))) {
			sql.append("AND substr(o.send_order_time,1,10)>=substr(?,1,10)");
			list.add(params.get("beginTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("endTime"))) {
			sql.append("AND substr(o.send_order_time,1,10)<=substr(?,1,10)");
			list.add(params.get("endTime"));

		}
		Map<String, Object> mao = dao.findFirst(sql.toString(), list.toArray());
		return (BigDecimal) mao.get("goodPrice");
	}

	// 可接单的
	public Page<Map<String, Object>> listTask(Map<String, String> params,
			PageRequest pageRequest) throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>(); // CONCAT(o.send_area,o.send_addr)
		sql.append("select oi.take_mark takeMark, oi.status status, oi.freight_type freightType,oi.item_status itemStatus, ");
		sql.append("oi.order_no orderNo ,oi.send_addr addr,oi.send_area addrExf, ");
		sql.append("oi.rev_addr revAddr,oi.rev_area revAddrExf,order_note orderNote,");
		sql.append(" oi.send_name name,oi.send_phone phone,oi.create_time  orderTime  ");
		sql.append("from order_info oi , order_substation os where oi.id = os.order_id  ");
		sql.append("AND os.substation_no= ?  and  oi.take_courier_no is null  and os.taked =1 and oi.status=1 AND oi.lgc_no = ? ");
		list.add(params.get("substationNo"));
		list.add(params.get("lgcNo"));
		if (!StringUtils.isEmptyWithTrim(params.get("beginTime"))) {
			sql.append("  and  oi.create_time>=? ");
			list.add(params.get("beginTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("endTime"))) {
			sql.append(" and  oi.create_time<=? ");
			list.add(params.get("endTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("itemStatus"))) {
			sql.append(" and oi.item_status=? ");
			list.add(params.get("itemStatus"));
		}
		sql.append(" order by oi.order_no desc ");
		return dao.find(sql.toString(), list.toArray(), pageRequest);
	}

	// 可接单的 所有快递员可见
	public Page<Map<String, Object>> allCourierTask(Map<String, String> params,
			PageRequest pageRequest) throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>(); // CONCAT(o.send_area,o.send_addr)
		sql.append("select oi.take_mark takeMark, oi.status status, oi.freight_type freightType,oi.item_status itemStatus, ");
		sql.append("oi.order_no orderNo ,oi.send_addr addr,oi.send_area addrExf, ");
		sql.append("oi.rev_addr revAddr,oi.rev_area revAddrExf, ");
		sql.append(" oi.send_name name,oi.send_phone phone,oi.create_time  orderTime,order_note orderNote  ");
		sql.append("from order_info oi  where  ");
		sql.append("  oi.take_courier_no is null and oi.status=1 AND oi.lgc_no = ? ");
		list.add(params.get("lgcNo"));
		if (!StringUtils.isEmptyWithTrim(params.get("beginTime"))) {
			sql.append("  and  oi.create_time>=? ");
			list.add(params.get("beginTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("endTime"))) {
			sql.append(" and  oi.create_time<=? ");
			list.add(params.get("endTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("itemStatus"))) {
			sql.append(" and oi.item_status=? ");
			list.add(params.get("itemStatus"));
		}
		sql.append(" order by oi.order_no desc ");
		return dao.find(sql.toString(), list.toArray(), pageRequest);
	}

	// 查询乐接单的
	public Page<Map<String, Object>> checkListTask(Map<String, String> params,
			PageRequest pageRequest) throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>(); // CONCAT(o.send_area,o.send_addr)
		sql.append(
				"select  o.take_mark takeMark, o.status status, o.freight_type freightType,o.item_status itemStatus, o.order_no orderNo"
				+ ",IF(ISNULL(o.take_addr) OR LENGTH(TRIM(o.take_addr))<1,o.send_addr,o.take_addr) addr,")
				// 详细
				.append("o.send_area addrExf,   o.rev_area areaAddr,")
				// 取件大区
				.append("send_name name,send_phone phone,o.create_time  orderTime ")
				.append(" from order_info o where o.sub_station_no=? and o.take_courier_no is null and o.status=1 ");
		list.add(params.get("substationNo"));
		sql.append("AND (o.lgc_order_no LIKE '%" + params.get("checkMessage")
				+ "%'  OR o.rev_phone LIKE '%" + params.get("checkMessage")
				+ "%'  OR  o.send_phone Like '%" + params.get("checkMessage")
				+ "%'  OR o.send_name Like '%" + params.get("checkMessage")
				+ "%'  OR  o.rev_name Like '%" + params.get("checkMessage")
				+ "%' )");
		sql.append(" order by o.order_no desc ");
		return dao.find(sql.toString(), list.toArray(), pageRequest);
	}

	/**
	 * |收派件历史订单
	 * 
	 * @param params
	 * @param pageRequest
	 * @return
	 * @throws SQLException
	 */
	public Page<Map<String, Object>> listBefore(Map<String, String> params,
			PageRequest pageRequest) throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		sql.append("select n.* from");
		sql.append("((select   o.order_no orderNo,o.item_Status itemStatus, o.pro_order proOrder, o.take_mark takeMark,2 status,o.freight_type freightType,"
				+ "o.send_area   addrExf,o.send_addr addr,o.take_order_time orderTime,o.cod cod ,o.good_price goodPrice,"
				+ "o.send_name name,o.send_phone phone,o.lgc_order_no lgcOrderNo,o.sign_name signName,"
				+ "IF(o.delivery='Y','1','0') delivery,o.pay_type payType,o.freight freight,o.cpay_type cPayType,o.zid ,"
				+ "o.vpay vpay,o.tnpay acount  from order_info o where o.take_courier_no =? and o.lgc_no = ? and o.status in (2,3,4,6,7,8)  ");
		list.add(params.get("userNo"));
		list.add(params.get("lgcNo"));
		if (StringUtils.isNotEmptyWithTrim(params.get("beginTime"))) {
			sql.append("  AND SUBSTR(o.take_order_time,1,10)>=? ");
			list.add(params.get("beginTime"));
		}
		if (StringUtils.isNotEmptyWithTrim(params.get("endTime"))) {
			sql.append("  AND SUBSTR(o.take_order_time,1,10)<=?  ");
			list.add(params.get("endTime"));
		}
		sql.append(" ) UNION ALL  ");

		sql.append(" ( select  o.order_no orderNo,o.item_Status itemStatus,  o.pro_order proOrder, o.take_mark takeMark,3 status,o.freight_type freightType,"
				+ "o.rev_area   addrExf,o.rev_addr addr,o.send_order_time orderTime,o.cod cod ,o.good_price goodPrice,"
				+ "o.rev_name name,o.rev_phone phone,o.lgc_order_no lgcOrderNo,o.sign_name signName,"
				+ "IF(o.delivery='Y','1','0') delivery,o.pay_type payType,o.freight freight,o.cpay_type cPayType,o.zid ,"
				+ "o.cpay cpay,(o.snpay-o.good_price) acount  from order_info o where o.send_courier_no =? AND o.status = 3 and o.lgc_no = ?  ");
		list.add(params.get("userNo"));
		list.add(params.get("lgcNo"));
		if (StringUtils.isNotEmptyWithTrim(params.get("beginTime"))) {
			sql.append(" AND SUBSTR(o.send_order_time,1,10)>=? ");
			list.add(params.get("beginTime"));
		}
		if (StringUtils.isNotEmptyWithTrim(params.get("endTime"))) {
			sql.append(" AND SUBSTR(o.send_order_time,1,10)<=?  ");
			list.add(params.get("endTime"));
		}
		sql.append(")) n  order by n.orderTime desc");

		return dao.find(sql.toString(), list.toArray(), pageRequest);
	}

	/**
	 * 派件历史订单
	 * 
	 * @param params
	 * @param pageRequest
	 * @return
	 * @throws SQLException
	 */
	public Page<Map<String, Object>> listBeforeGetSendOrder(
			Map<String, String> params, PageRequest pageRequest)
			throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();

		sql.append("select   o.order_no orderNo,o.item_Status itemStatus, o.pro_order proOrder,o.take_mark takeMark,3 status,o.freight_type freightType,"
				+ "o.rev_area   addrExf,o.rev_addr addr,o.send_order_time orderTime,o.cod cod ,o.good_price goodPrice,"
				+ "o.rev_name name,o.rev_phone phone,o.lgc_order_no lgcOrderNo,o.sign_name signName,"
				+ "IF(o.delivery='Y','1','0') delivery,o.pay_type payType,o.freight freight,o.cpay_type cPayType,o.zid,"
				+ "o.vpay vpay,(o.snpay-o.good_price ) acount  from order_info o where o.send_courier_no =? AND o.status = 3  and o.lgc_no = ? ");
		list.add(params.get("userNo"));
		list.add(params.get("lgcNo"));
		if (StringUtils.isNotEmptyWithTrim(params.get("beginTime"))) {
			sql.append("AND SUBSTR(o.send_order_time,1,10)>=? ");
			list.add(params.get("beginTime"));
		}
		if (StringUtils.isNotEmptyWithTrim(params.get("endTime"))) {
			sql.append("AND SUBSTR(o.send_order_time,1,10)<=? ");
			list.add(params.get("endTime"));
		}
		sql.append("  order by o.send_order_time desc");

		return dao.find(sql.toString(), list.toArray(), pageRequest);
	}

	/**
	 * 收件历史订单
	 * 
	 * @param params
	 * @param pageRequest
	 * @return
	 * @throws SQLException
	 */
	public Page<Map<String, Object>> listBeforeGetTakeOrder(
			Map<String, String> params, PageRequest pageRequest)
			throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		sql.append("select   o.order_no orderNo,o.item_Status itemStatus,  o.pro_order proOrder,o.take_mark takeMark,2 status,o.freight_type freightType,"
				+ "o.send_area   addrExf,o.send_addr addr,o.take_order_time orderTime,o.cod cod ,o.good_price goodPrice,"
				+ "o.send_name name,o.send_phone phone,o.lgc_order_no lgcOrderNo,o.sign_name signName,"
				+ "IF(o.delivery='Y','1','0') delivery,o.pay_type payType,o.freight freight,o.cpay_type cPayType, o.zid ,"
				+ "o.vpay vpay,o.tnpay acount from order_info o where o.take_courier_no =? and o.lgc_no = ?  and o.status in (2,3,4,6,7,8)   ");
		list.add(params.get("userNo"));
		list.add(params.get("lgcNo"));
		if (StringUtils.isNotEmptyWithTrim(params.get("beginTime"))) {
			sql.append("AND SUBSTR(o.take_order_time,1,10)>=?");
			list.add(params.get("beginTime"));
		}
		if (StringUtils.isNotEmptyWithTrim(params.get("endTime"))) {
			sql.append("AND SUBSTR(o.take_order_time,1,10)<=? ");
			list.add(params.get("endTime"));
		}

		sql.append("  order by o.take_order_time desc");

		return dao.find(sql.toString(), list.toArray(), pageRequest);
	}

	public Page<Map<String, Object>> checkList(Map<String, String> params,
			PageRequest pageRequest) throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		sql.append(
				"select  pro_order proOrder,   o.take_mark takeMark,IF(( LENGTH(TRIM(o.send_courier_no))>1) and o.status=2 ,7,o.status) status ,o.freight_type freightType")
				.append(",IF(o.status=1 ,o.rev_area,o.send_area) areaAddr,")
				.append("o.item_status itemStatus, o.order_no orderNo,o.readed readed,o.lgc_no lgcNo ,o.lgc_order_no lgcOrderNo,IF(o.delivery='Y','1','0') delivery,")
				.append("o.freight freight, o.vpay vpay,o.pay_type payType,")
				.append("IF(o.status=1,o.send_area,o.rev_area) addr,")
				// 大区
				.append("IF(o.status=1,o.send_addr,o.rev_addr) addrExf,")
				// 详细地址
				.append("IF(o.status=1,o.take_order_time,o.send_order_time) orderTime ,")
				.append("IF(o.status=1,o.send_name,o.rev_name) name ,")
				.append("IF(o.status=1,o.send_phone,o.rev_phone) phone ,")
				.append("IF(o.send_courier_no=" + params.get("userNo") + ",IF(o.status!=6,'5','7'),IF(o.status!=5,'4','6')) orderType ")
				.append(" from order_info o  where 1=1  and o.lgc_no = ?  ")// (o.take_courier_no=?
				// or
				// o.send_courier_no=?)
				// .append(" and POSITION(o.status IN IF(o.take_courier_no=?,'2-3-4-5-6','3-6'))>0 ")
				.append("AND (o.lgc_order_no LIKE '%" + params.get("checkMessage")
						+ "%'  OR o.rev_phone LIKE '%" + params.get("checkMessage")
						+ "%'  OR  o.send_phone Like '%" + params.get("checkMessage")
						+ "%'  OR o.send_name Like '%" + params.get("checkMessage")
						+ "%'  OR  o.rev_name Like '%" + params.get("checkMessage") + "%' )");
		list.add(params.get("lgcNo"));
		if (!StringUtils.isEmptyWithTrim(params.get("orderStatus"))
				&& Integer.valueOf(params.get("orderStatus")) == 1) {
			sql.append("AND status =?  AND take_courier_no=?    ");
			list.add(params.get("orderStatus"));
			list.add(params.get("userNo"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("orderStatus"))
				&& Integer.valueOf(params.get("orderStatus")) != 1) {
			sql.append("AND status =? AND (o.take_courier_no=? or o.send_courier_no=?)");
			list.add(params.get("orderStatus"));
			list.add(params.get("userNo"));
			list.add(params.get("userNo"));
		}
		sql.append(" order by o.order_no desc   ");
		return dao.find(sql.toString(), list.toArray(), pageRequest);
	}

	/**
	 * 只查询自己有的订单
	 * 
	 * @param params
	 * @param pageRequest
	 * @return
	 * @throws SQLException
	 */
	public Page<Map<String, Object>> checkListByMe(Map<String, String> params,
			PageRequest pageRequest) throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		sql.append(
				"select  o.pro_order proOrder,   o.take_mark takeMark,o.status status ,o.freight_type freightType,")
				.append("o.item_status itemStatus, o.order_no orderNo,o.readed readed,o.lgc_no lgcNo ,o.lgc_order_no lgcOrderNo,IF(o.delivery='Y','1','0') delivery,")
				.append("o.freight freight, o.vpay vpay,o.pay_type payType,o.cpay_type cPayType,o.cod cod,o.good_price goodPrice,")
				.append("IF(o.freight_type=1,o.tnpay,(o.snpay-o.good_price)) acount,")
				.append("IF(o.status=1,o.rev_area,IF(ISNULL(o.send_courier_no) or LENGTH(TRIM(o.send_courier_no))<1,rev_area,o.send_area)) areaAddr,")
				.append("IF(o.status=1,o.send_area, IF(ISNULL(o.send_courier_no) or LENGTH(TRIM(o.send_courier_no))<1,o.send_area,rev_area)) addr,")
				// 大区
				.append("IF(o.status=1,o.send_addr, IF(ISNULL(o.send_courier_no) or LENGTH(TRIM(o.send_courier_no))<1,o.send_addr,o.rev_addr)) addrExf,")
				// 详细地址
				.append("IF(o.status=1,o.create_time, IF(ISNULL(o.send_courier_no) or LENGTH(TRIM(o.send_courier_no))<1,o.take_order_time,o.send_order_time)) orderTime ,")
				.append("IF(o.status=1,o.send_name, IF(ISNULL(o.send_courier_no) or LENGTH(TRIM(o.send_courier_no))<1,o.send_name,o.rev_name)) name ,")
				.append("IF(o.status=1,o.send_phone, IF(ISNULL(o.send_courier_no) or LENGTH(TRIM(o.send_courier_no))<1,o.send_phone,o.rev_phone)) phone, ")
				.append("IF(o.send_courier_no="
						+ params.get("userNo")
						+ ",IF(o.status!=6,'5','7'),IF(o.status!=5,'4','6')) orderType ")
				.append(" from order_info o  where  (o.take_courier_no=? or o.send_courier_no=?)  and o.lgc_no = ?   ")
				.append("AND (o.lgc_order_no LIKE '%" + params.get("checkMessage")
						+ "%'  OR o.rev_phone LIKE '%" + params.get("checkMessage")
						+ "%'  OR  o.send_phone Like '%" + params.get("checkMessage")
						+ "%'  OR o.send_name Like '%" + params.get("checkMessage")
						+ "%'  OR  o.rev_name Like '%" + params.get("checkMessage") + "%' )");
		list.add(params.get("userNo"));
		list.add(params.get("userNo"));
		list.add(params.get("lgcNo"));
		sql.append(" order by o.pro_order asc, o.order_no desc   ");
		return dao.find(sql.toString(), list.toArray(), pageRequest);
	}

	// 查询当天派件次数
	public String sendTimes(Map<String, String> params) throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		sql.append("select count(*) sendTimes").append(
				" from order_info  where send_courier_no=?   AND status = 3 ");
		list.add(params.get("userNo"));
		if (!StringUtils.isEmptyString(params.get("beginTime"))) {
			sql.append("AND substr(send_order_time,1,10)>=?");
			list.add(params.get("beginTime"));
		}
		if (!StringUtils.isEmptyString(params.get("endTime"))) {
			sql.append("AND substr(send_order_time,1,10)<=?");
			list.add(params.get("endTime"));
		}
		Map<String, Object> mao = dao.findFirst(sql.toString(), list.toArray());
		if (mao == null) {
			return "0";
		}
		if (mao.get("sendTimes") == null) {
			return "0";
		}
		return String.valueOf(mao.get("sendTimes"));
	}

	// 查询当天取件次数
	public String takeTimes(Map<String, String> params) throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		sql.append("select count(*) takeTimes")
				.append(" from order_info  where take_courier_no=?   AND status in (2,3,4,6,7,8)");
		list.add(params.get("userNo"));
		if (!StringUtils.isEmptyString(params.get("beginTime"))) {
			sql.append("AND substr(take_order_time,1,10)>=?");
			list.add(params.get("beginTime"));
		}
		if (!StringUtils.isEmptyString(params.get("endTime"))) {
			sql.append("AND substr(take_order_time,1,10)<=?");
			list.add(params.get("endTime"));
		}

		Map<String, Object> mao = dao.findFirst(sql.toString(), list.toArray());
		if (mao == null) {
			return "0";
		}
		if (mao.get("takeTimes") == null) {
			return "0";
		}
		return String.valueOf(mao.get("takeTimes"));

	}

	public int unreadCount(Map<String, String> params) throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		sql.append("select count(*) unreadCount from order_info  where IF(status=1,take_courier_no,send_courier_no)=? and status in (1,2) and readed=0 ");
		list.add(params.get("userNo"));
		if (!StringUtils.isEmptyWithTrim(params.get("status"))
				&& !"-1".equals(params.get("status"))) {
			sql.append("and status in (" + params.get("status") + ") ");
		}
		if (!StringUtils.isEmptyWithTrim(params.get("beginTime"))
				&& params.get("beginTime").trim().length() == 7) {
			sql.append(" and substr(create_time,1,7)=? ");
			list.add(params.get("beginTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("beginTime"))
				&& params.get("beginTime").trim().length() == 10) {
			sql.append(" and substr(create_time,1,10)=? ");
			list.add(params.get("beginTime"));
		}
		Map<String, Object> ret = dao.findFirst(sql.toString(), list.toArray());
		return Integer.parseInt(ret.get("unreadCount").toString());
	}

	public void read(String orderNo, int read) throws SQLException {
		String sql = "update order_info set readed=? where order_no =? ";
		List<Object> list = new ArrayList<Object>();
		list.add(read);
		list.add(orderNo);
		dao.update(sql, list.toArray());
	}

	public OrderInfo getByOrderNo(String orderNo, String lgcNo)
			throws SQLException {
		String sql = "select o.*,l.name lgc_name from order_info o left join lgc l on o.lgc_no=l.lgc_no where o.order_no =? and o.lgc_no= ? ";
		List<Object> list = new ArrayList<Object>();
		list.add(orderNo);
		list.add(lgcNo);
		return dao.findFirst(OrderInfo.class, sql, list.toArray());
	}

	public OrderInfo getByOrderNo(String orderNo)
			throws SQLException {
		String sql = "select o.* from order_info o  where o.order_no =? ";
		List<Object> list = new ArrayList<Object>();
		list.add(orderNo);
		return dao.findFirst(OrderInfo.class, sql, list.toArray());
	}

	public String getOrderNote(String id) throws SQLException {
		String sql = "select note from order_note  o where o.order_id=? order by id desc limit 1";
		List<Object> list = new ArrayList<Object>();
		list.add(id);
		Map<String, Object> map = dao.findFirst(sql, list.toArray());
		if (map != null) {
			return String.valueOf(map.get("note"));
		}
		return "";
	}

	public OrderInfo getByLgcOrderNo(String lgcOrderNo, String lgcNo)
			throws SQLException {
		String sql = "select * from order_info where lgc_order_no =? and lgc_no=? ";
		List<Object> list = new ArrayList<Object>();
		list.add(lgcOrderNo);
		list.add(lgcNo);
		return dao.findFirst(OrderInfo.class, sql, list.toArray());
	}

	public OrderInfo getByLgcOrderNo(String lgcOrderNo) throws SQLException {
		String sql = "select * from order_info where lgc_order_no =?";
		List<Object> list = new ArrayList<Object>();
		list.add(lgcOrderNo);
		return dao.findFirst(OrderInfo.class, sql, list.toArray());
	}

	public OrderInfo getByLgcOrderNoAndStatus(String lgcOrderNo, String status)
			throws SQLException {
		String sql = "select * from order_info where lgc_order_no =? and status =? ";
		List<Object> list = new ArrayList<Object>();
		list.add(lgcOrderNo);
		list.add(status);
		return dao.findFirst(OrderInfo.class, sql, list.toArray());
	}

	public OrderDto getOrderByOrderNo(String orderNo, String lgcNo)
			throws SQLException {
		String sql = "select * from order_info where order_no =?  and lgc_no = ?";
		List<Object> list = new ArrayList<Object>();
		list.add(orderNo);
		list.add(lgcNo);
		return dao.findFirst(OrderDto.class, sql, list.toArray());
	}

	public OrderDto getOrderByOrderNo(String orderNo) throws SQLException {
		String sql = "select * from order_info where order_no =? ";
		List<Object> list = new ArrayList<Object>();
		list.add(orderNo);
		return dao.findFirst(OrderDto.class, sql, list.toArray());
	}

	public OrderDto getOrderByLgcOrderNo(String orderNo, String lgcNo)
			throws SQLException {
		String sql = "select * from order_info where lgc_order_no=? and lgc_no=? ";
		List<Object> list = new ArrayList<Object>();
		list.add(orderNo);
		list.add(lgcNo);
		return dao.findFirst(OrderDto.class, sql, list.toArray());
	}

	public int sendTotalCount(Map<String, String> params) throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		sql.append("select count(*) c from order_info where send_courier_no=? and status=3 ");
		list.add(params.get("userNo"));
		if (!StringUtils.isEmptyWithTrim(params.get("beginTime"))) {
			sql.append(" and create_time>=? ");
			list.add(params.get("beginTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("endTime"))) {
			sql.append(" and create_time<? ");
			list.add(params.get("endTime"));
		}
		Map<String, Object> ret = dao.findFirst(sql.toString(), list.toArray());
		return Integer.parseInt(ret.get("c").toString());
	}

	public int takeTotalCount(Map<String, String> params) throws SQLException {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		sql.append("select count(*) c from order_info where take_courier_no=? and status=2 ");
		list.add(params.get("userNo"));
		if (!StringUtils.isEmptyWithTrim(params.get("beginTime"))) {
			sql.append(" and create_time>=? ");
			list.add(params.get("beginTime"));
		}
		if (!StringUtils.isEmptyWithTrim(params.get("endTime"))) {
			sql.append(" and create_time<=? ");
			list.add(params.get("endTime"));
		}
		Map<String, Object> ret = dao.findFirst(sql.toString(), list.toArray());
		return Integer.parseInt(ret.get("c").toString());
	}

	public String getUserNoByOrderNo(String orderNo) throws SQLException {
		String sql = "select user_no from order_info where order_no=?";
		List<Object> list = new ArrayList<Object>();
		list.add(orderNo);
		Map<String, Object> ret = dao.findFirst(sql, list.toArray());
		String userNo = "";
		if (ret != null) {
			userNo = ret.get("user_no").toString();
		}
		return userNo;
	}

	public boolean cancel(String orderNo) throws SQLException {
		String sql = "update order_info set status=3 where order_no=?";
		List<Object> list = new ArrayList<Object>();
		list.add(orderNo);
		dao.update(sql, list.toArray());
		return true;
	}

	public boolean unTake(OrderInfo orderInfo) throws SQLException {
		String sql = "update order_info set status='5',sign=?,take_courier_no=?,sub_station_no=?,complete_time=?,last_update_time=? ,take_order_time=? where order_no=?";
		List<Object> list = new ArrayList<Object>();
		list.add(orderInfo.getSign());
		list.add(orderInfo.getTakeCourierNo());
		list.add(orderInfo.getSubStationNo());
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
		list.add(orderInfo.getTakeOrderTime());
		list.add(orderInfo.getOrderNo());
		dao.update(sql, list.toArray());
		return true;
	}

	// 上传取件面单地址
	public boolean uploadTakeOrder(String lgcOrderNo, String loction)
			throws SQLException {
		String sql = "update  order_info set take_plane=?  where lgc_order_no =? ";
		List<Object> list = new ArrayList<Object>();
		list.add(loction);
		list.add(lgcOrderNo);
		dao.update(sql, list.toArray());
		return true;
	}

	// 上传派件面单地址
	public boolean uploadSendOrder(String lgcOrderNo, String loction)
			throws SQLException {
		String sql = "update  order_info set send_plane=?  where lgc_order_no =? ";
		List<Object> list = new ArrayList<Object>();
		list.add(loction);
		list.add(lgcOrderNo);
		dao.update(sql, list.toArray());
		return true;
	}

	public boolean send(String orderNo, String userNo, String sendSubstationNo)
			throws SQLException {
		String sql = "update order_info set  `status`=8 ,send_order_time=?, send_courier_no=?,send_substation_no=?,take_mark='N' where order_no=? ";
		List<Object> list = new ArrayList<Object>();
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
		list.add(userNo);
		list.add(sendSubstationNo);
		list.add(orderNo);
		dao.update(sql, list.toArray());
		return true;
	}

	/**
	 * 拒签问题件
	 * 
	 * @param orderNo
	 * @param sign
	 * @param proOrderId
	 * @return
	 * @throws SQLException
	 */
	public boolean refuse(String orderNo, String sign, String proOrderId)
			throws SQLException {// pro_order
		String sql = "update order_info set  send_order_time=?, pro_order='Y',status=8,sign=?,complete_time=?,last_update_time=? ,pro_id=? where order_no=?";
		//String sql = "update order_info set   pro_order='Y',status=8,sign=?,complete_time=?,last_update_time=? ,pro_id=? where order_no=?";
		List<Object> list = new ArrayList<Object>();
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
		list.add(sign);
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
		list.add(proOrderId);
		list.add(orderNo);
		dao.update(sql, list.toArray());
		return true;
	}

	/**
	 * 标记滞留件
	 * 
	 * @param orderNo
	 * @param sign
	 * @param proOrderId
	 * @return
	 * @throws SQLException
	 */
	public boolean YXreusePro(String orderNo, String sign, String proOrderId, String substation, String sendCourierNo)
			throws SQLException {// pro_order
		String sql = "update order_info set  send_substation_no =?,send_order_time=?,send_courier_no=?,"
				+ "status=8 , pro_order='Y',last_update_time=? ,pro_id=? where order_no=?";
		List<Object> list = new ArrayList<Object>();
		list.add(substation);
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
		list.add(sendCourierNo);
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
		list.add(proOrderId);
		list.add(orderNo);
		dao.update(sql, list.toArray());
		return true;
	}

	/**
	 *标记滞留件
	 * @param orderNo
	 * @param sign
	 * @param proOrderId
	 * @return
	 * @throws SQLException
	 */
	public boolean reusePro(String orderNo, String sign, String proOrderId) throws SQLException {//pro_order
		String sql = "update order_info set   pro_order='Y',last_update_time=? ,pro_id=? where order_no=?";
		List<Object> list = new ArrayList<Object>();
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
		list.add(proOrderId);
		list.add(orderNo);
		dao.update(sql, list.toArray());
		return true;
	}

	public boolean sign(OrderInfo orderInfo) throws SQLException {
		String sql = "update order_info set send_order_time =? ,"
		//String sql = "update order_info set "
				+ "dis_reality_freight= ?," + "dis_discount= ?,"
				+ "status='3'," + "sign=?," + "pay_type=?," + "cpay_type=?"
				+ ",pay_status=?," + "snpay=?," + "pay_acount=?,"
				+ "sign_type=?,sign_name=?," + "signature=?," + "complete_time=?,"
				+ "last_update_time=?," + "fpay_status=?," + "cpay_status=? ,"
				+ "rev_pay_type=?," + "rev_mpay=?," + "dis_user_no=? "
				+ ",pro_order='N' " + "where order_no=?";
		List<Object> list = new ArrayList<Object>();
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
		list.add(orderInfo.getDisRealityFreight());
		list.add(orderInfo.getDisDiscount());
		list.add(orderInfo.getSign());
		list.add(orderInfo.getPayType());
		list.add(orderInfo.getCpayType());
		list.add(orderInfo.getPayStatus());
		list.add(orderInfo.getSnapy());
		list.add(orderInfo.getPayAcount());
		list.add(orderInfo.getSignType());
		list.add(orderInfo.getSignType());
		list.add(orderInfo.getSignature());
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
		list.add(orderInfo.getFpayStatus());
		list.add(orderInfo.getCpayStatus());
		list.add(orderInfo.getRevPayType());
		list.add(orderInfo.getRevMpay());
		list.add(orderInfo.getDisUserNo());
		list.add(orderInfo.getOrderNo());
		dao.update(sql, list.toArray());
		OrderInfo oInfo = getByOrderNo(orderInfo.getOrderNo());
		if (orderInfo.getZid()==0&&isJmSno(oInfo.getSendSubstationNo())) {
			 saveJMorder(oInfo, "P");
		}
		if (orderInfo.getZid()==0&&isJmSno(oInfo.getSubStationNo())) {
			updateJMorder(oInfo);
		}
		return true;
	}

	public boolean updateMnay(OrderInfo orderInfo) throws SQLException {
		String sql = "update order_info set " + " mpay =?  "
				+ " where order_no=?";
		List<Object> list = new ArrayList<Object>();
		list.add(orderInfo.getMpay());
		list.add(orderInfo.getOrderNo());
		dao.update(sql, list.toArray());
		return true;
	}

	public boolean updateMonthNo(String monthNo, String orderNo)
			throws SQLException {
		String sql = "update order_info set month_settle_no=? where order_no=?";
		List<Object> list = new ArrayList<Object>();
		list.add(monthNo);
		list.add(orderNo);
		dao.update(sql, list.toArray());
		return true;
	}

	// 二次投递月结号
	public boolean updateTwoMonthNo(String monthNo, String orderNo)
			throws SQLException {
		String sql = "update order_info set rev_month_settle_no=? where order_no=?";
		List<Object> list = new ArrayList<Object>();
		list.add(monthNo);
		list.add(orderNo);
		dao.update(sql, list.toArray());
		return true;
	}

	public boolean isExist(String orderNo) throws SQLException {
		String sql = "select order_no from order_info where order_no=?";
		List<Object> list = new ArrayList<Object>();
		list.add(orderNo);
		Map<String, Object> ret = dao.findFirst(sql, list.toArray());
		if (ret != null) {
			return true;
		}
		return false;
	}

	public String getOrderStatus(String orderNo) throws SQLException {
		String sql = "select status from order_info where order_no=?";
		List<Object> list = new ArrayList<Object>();
		list.add(orderNo);
		Map<String, Object> ret = dao.findFirst(sql, list.toArray());
		String status = "";
		if (ret != null) {
			status = ret.get("status").toString();
		}
		return status;
	}

	public String getTakeCourierNo(String orderNo) throws SQLException {
		String sql = "select take_courier_no from order_info where order_no=?";
		List<Object> list = new ArrayList<Object>();
		list.add(orderNo);
		Map<String, Object> ret = dao.findFirst(sql, list.toArray());
		String takeCourierNo = "";
		if (ret != null && ret.get("take_courier_no") != null) {
			takeCourierNo = ret.get("take_courier_no").toString();
		}
		return takeCourierNo;
	}

	public boolean update(OrderInfo orderInfo) throws SQLException {
		String sql = "update order_info set send_area=?,send_addr=?,send_name=?,send_phone=?,send_location=?,"
				+ "rev_area=?,rev_addr=?,rev_name=?,rev_phone=?,rev_location=?,"
				+ "item_Status=?,item_name=?,item_weight=?,freight_type=?,"
				+ "month_settle_name=?,month_settle_no=?,month_settle_card=?,cod=?,good_price=?,cod_card_no=?,cod_name=?,cod_card_cnaps_no=?,cod_bank=?,"
				+ "good_valuation=?,good_valuation_rate=?,take_time=?,take_time_begin=?,take_time_end=?,take_addr=?,take_location=?,order_note=?,last_update_time=? "
				+ "where order_no=? ";
		List<Object> list = new ArrayList<Object>();
		list.add(orderInfo.getSendArea());
		list.add(orderInfo.getSendAddr());
		list.add(orderInfo.getSendName());
		list.add(orderInfo.getSendPhone());
		list.add(orderInfo.getSendLocation());
		list.add(orderInfo.getRevArea());
		list.add(orderInfo.getRevAddr());
		list.add(orderInfo.getRevName());
		list.add(orderInfo.getRevPhone());
		list.add(orderInfo.getRevLocation());
		list.add(orderInfo.getItemStatus());
		list.add(orderInfo.getItemName());
		list.add(orderInfo.getItemWeight());
		list.add(orderInfo.getFreightType());
		list.add(orderInfo.getMonthSettleName());
		list.add(orderInfo.getMonthSettleNo());
		list.add(orderInfo.getMonthSettleCard());
		list.add(orderInfo.getCod());
		list.add(orderInfo.getGoodPrice());
		list.add(orderInfo.getCodCardNo());
		list.add(orderInfo.getCodName());
		list.add(orderInfo.getCodCardCnapsNo());
		list.add(orderInfo.getCodBank());
		list.add(orderInfo.getGoodValuation());
		list.add(orderInfo.getGoodValuationRate());
		list.add(orderInfo.getTakeTime());
		list.add(orderInfo.getTakeTimeBegin());
		list.add(orderInfo.getTakeTimeEnd());
		list.add(orderInfo.getTakeAddr());
		list.add(orderInfo.getTakeLocation());
		list.add(orderInfo.getOrderNote());
		list.add(orderInfo.getLastUpdateTime());
		list.add(orderInfo.getOrderNo());
		dao.updateGetID(sql, list);
		return true;

	}

	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public boolean takeUpdate(OrderInfo orderInfo) throws SQLException {
		String sql = "update order_info set  item_status=?,take_mark=?,item_name=?,item_weight=?,good_valuation_rate=?,freight=?,pay_acount=?,take_courier_no=?,sub_station_no=?,lgc_order_no=?,"
				+ "status='2',pay_type=?,pay_status=?,cpay=?,vpay=?,tnpay=?,mpay=?,good_valuation_rate=?,last_update_time=?  , take_order_time=? ,fpay_status=?,cpay_status=?,dis_user_no=? ,"
				+ "month_discount=?,cod_rate = ? ,id_card=?  ,asign_status ='C' , snpay  = ?,dis_reality_freight= ? ,dis_discount = ?,message=?  where order_no=? ";
		List<Object> list = new ArrayList<Object>();
		list.add(orderInfo.getItemStatus());
		list.add("Y");
		list.add(orderInfo.getItemName());
		list.add(orderInfo.getItemWeight());
		list.add(orderInfo.getGoodValuationRate());
		list.add(orderInfo.getFreight());
		list.add(orderInfo.getPayAcount());
		list.add(orderInfo.getTakeCourierNo());
		list.add(orderInfo.getSubStationNo());
		list.add(orderInfo.getLgcOrderNo());
		list.add(orderInfo.getPayType());
		list.add(orderInfo.getPayStatus());
		list.add(orderInfo.getCpay());
		list.add(orderInfo.getVpay());
		list.add(orderInfo.getTnpay());
		list.add(orderInfo.getMpay());
		list.add(orderInfo.getGoodValuationRate());
		list.add(orderInfo.getLastUpdateTime());
		list.add(orderInfo.getTakeOrderTime());
		list.add(orderInfo.getFpayStatus());
		list.add(orderInfo.getCpayStatus());
		list.add(orderInfo.getDisUserNo());
		list.add(orderInfo.getMonthDiscount());
		list.add(orderInfo.getCodRate());
		list.add(orderInfo.getIdCard());
		list.add(orderInfo.getSnapy());
		list.add(orderInfo.getDisRealityFreight());
		list.add(orderInfo.getDisDiscount());
		list.add(orderInfo.getMessage()) ;
		list.add(orderInfo.getOrderNo());
		dao.updateGetID(sql, list);
		if (orderInfo.getZid()==0&&isJmSno(orderInfo.getSubStationNo())) {
			saveJMorder(orderInfo, "Z");
		}
		return true;
	}

	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public boolean yxTakeUpdate(OrderInfo orderInfo) throws SQLException {
		String sql = "update order_info set  item_status=?,take_mark=?,item_name=?,item_weight=?,good_valuation_rate=?,freight=?,pay_acount=?,take_courier_no=?,sub_station_no=?,lgc_order_no=?,"
				+ "status='2',pay_type=?,pay_status=?,cpay=?,vpay=?,tnpay=?,mpay=?,good_valuation_rate=?,last_update_time=?  , take_order_time=? ,fpay_status=?,cpay_status=?,dis_user_no=? ,"
				+ "month_discount=?,cod_rate = ? ,id_card=?  ,asign_status ='C' , snpay  = ?,dis_reality_freight= ? ,dis_discount = ? ,req_rece=?,rece_no=?,zidan_number=?,zidan_order=? ,item_count=? ,"
				+ " for_no = ?,zid=? ,time_type=?,message=? where order_no=? ";
		List<Object> list = new ArrayList<Object>();
		list.add(orderInfo.getItemStatus());
		list.add("Y");
		list.add(orderInfo.getItemName());
		list.add(orderInfo.getItemWeight());
		list.add(orderInfo.getGoodValuationRate());
		list.add(orderInfo.getFreight());
		list.add(orderInfo.getPayAcount());
		list.add(orderInfo.getTakeCourierNo());
		list.add(orderInfo.getSubStationNo());
		list.add(orderInfo.getLgcOrderNo());
		list.add(orderInfo.getPayType());
		list.add(orderInfo.getPayStatus());
		list.add(orderInfo.getCpay());
		list.add(orderInfo.getVpay());
		list.add(orderInfo.getTnpay());
		list.add(orderInfo.getMpay());
		list.add(orderInfo.getGoodValuationRate());
		list.add(orderInfo.getLastUpdateTime());
		list.add(orderInfo.getTakeOrderTime());
		list.add(orderInfo.getFpayStatus());
		list.add(orderInfo.getCpayStatus());
		list.add(orderInfo.getDisUserNo());
		list.add(orderInfo.getMonthDiscount());
		list.add(orderInfo.getCodRate());
		list.add(orderInfo.getIdCard());
		list.add(orderInfo.getSnapy());
		list.add(orderInfo.getDisRealityFreight());
		list.add(orderInfo.getDisDiscount());
		list.add(orderInfo.getReqRece());
		list.add(orderInfo.getReceNo());
		list.add(orderInfo.getZidanNumber());
		list.add(orderInfo.getZidanOrder());
		list.add(orderInfo.getItemCount());
		list.add(orderInfo.getForNo());
		list.add(orderInfo.getZid());
		list.add(orderInfo.getTimeType());
		list.add(orderInfo.getMessage()) ;
		list.add(orderInfo.getOrderNo());
		dao.updateGetID(sql, list);
		if (orderInfo.getZid()==0&&isJmSno(orderInfo.getSubStationNo())) {
			saveJMorder(orderInfo, "Z");
		}
		return true;
	}

	public void saveJMorder(OrderInfo orderInfo, String type) throws SQLException {
		String sql = "insert into franchise_order(order_no,lgc_order_no,take_substation_no,send_substation_no,item_type,item_weight,money_type,create_time) "
				+ " values (?,?,?,?,?,?,?,?)";
		List<Object> list = new ArrayList<Object>();
		list.add(orderInfo.getOrderNo());
		list.add(orderInfo.getLgcOrderNo());
		list.add(orderInfo.getSubStationNo());
		list.add(orderInfo.getSendSubstationNo());
		list.add(orderInfo.getItemStatus());
		list.add(orderInfo.getItemWeight());
		list.add(type);
		list.add(DateUtils.formatDate(new Date()));
		dao.updateGetID(sql, list);
	}

	public void updateJMorder(OrderInfo orderInfo) throws SQLException {
		String sql = "update franchise_order set send_substation_no=? where order_no=? ";
		List<Object> list = new ArrayList<Object>();
		list.add(orderInfo.getSendSubstationNo());
		list.add(orderInfo.getOrderNo());
		dao.updateGetID(sql, list);
	}

	/**
	 *  网点是否加盟网点
	 * @param sno
	 * @return
	 * @throws SQLException
	 */
	public boolean isJmSno(String sno) throws SQLException {
		String sql = "select id from substation  where substation_type='J' and `status`=1 and substation_no=? ";
		List<Object> list = new ArrayList<Object>();
		list.add(sno);
		Map<String, Object> map = dao.findFirst(sql, list.toArray());
		if (map != null) {
			return true;
		}
		return false;
	}

	public Map<String, Object> getNpay(OrderInfo order) {

		System.out.println("获取应收金额" + order.getStatus());
		System.out.println("获取应收金额" + order.getTnpay());
		System.out.println("获取应收金额" + order.getFreightType());
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("npay", 0.00f);
		map.put("disNpay", 0.00f);
		if (1 == order.getStatus()) {
			map.put("npay", order.getTnpay());
			map.put("disNpay", 0.00f);
		} else {
			if (2 == order.getStatus()) {
				if ("1".equals(order.getFreightType())) {
					map.put("npay",
							(order.getFreight() + order.getVpay()) * 100 / 100f);
					map.put("disNpay",
							(order.getDisRealityFreight() + order.getVpay()) * 100 / 100f);
				}
			} else {
				if ("2".equals(order.getFreightType())) {
					map.put("npay",
							(order.getFreight() + order.getVpay()) * 100 / 100f);
					map.put("disNpay",
							(order.getDisRealityFreight() + order.getVpay()) * 100 / 100f);
					if (order.getCod() == 1) {
						map.put("npay",
								(order.getFreight() + order.getVpay() + order
								.getGoodPrice()) * 100 / 100f);
						map.put("disNpay",
								(order.getDisRealityFreight() + order.getVpay() + order
								.getGoodPrice()) * 100 / 100f);
					}
				} else {
					map.put("npay", (order.getGoodPrice()) * 100 / 100f);
				}
			}
		}
		return map;
	}

	public void acpt(OrderInfo order) throws SQLException {
		String sql = "update order_info set take_courier_no=?,sub_station_no = ? ,asign_status = 'C'  where order_no=? ";
		List<Object> list = new ArrayList<Object>();
		list.add(order.getTakeCourierNo());
		list.add(order.getSubStationNo());
		list.add(order.getOrderNo());
		dao.updateGetID(sql, list);
	}

	/**
	 * 维护订单—分站列表
	 * 
	 * @param order
	 * @return
	 * @throws SQLException
	 */
	public void updaOrderStation(String orderNo) throws SQLException {
		String sql1 = "select id from order_info  o where o.order_no = ? ";
		List<Object> list1 = new ArrayList<Object>();
		list1.add(orderNo);
		Map<String, Object> map = dao.findFirst(sql1, list1.toArray());
		String id = String.valueOf(map.get("id"));
		String sql = "update order_substation set taked= 0 where order_id =? ";
		List<Object> list = new ArrayList<Object>();
		list.add(id);
		dao.updateGetID(sql, list);
	}

	/**
	 * 分配到所有人大厅
	 * 
	 * @param order
	 * @return
	 * @throws SQLException
	 */
	public boolean calcelAsignAll(OrderInfo order) throws SQLException {
		String sql = "update order_info set sub_station_no = null ,take_courier_no=null ,take_order_time = null , asign_status = null where order_no=? ";
		List<Object> list = new ArrayList<Object>();
		list.add(order.getOrderNo());
		dao.updateGetID(sql, list);
		return true;
	}

	/**
	 * 分配到分站或者个人
	 * 
	 * @param order
	 * @return
	 * @throws SQLException
	 */
	public void calcelAsign(String orderNo) throws SQLException {
		String sql = "update order_info set sub_station_no = null ,take_courier_no=null ,take_order_time = null , asign_status ='S' where order_no=? ";
		List<Object> list = new ArrayList<Object>();
		list.add(orderNo);
		dao.updateGetID(sql, list);
		String sql1 = "select id from order_info  o where o.order_no = ? ";
		List<Object> list1 = new ArrayList<Object>();
		list1.add(orderNo);
		Map<String, Object> map = dao.findFirst(sql1, list1.toArray());
		String id = String.valueOf(map.get("id"));
		String sql2 = "update order_substation set taked=1 where order_id= ? ";
		List<Object> list2 = new ArrayList<Object>();
		list2.add(id);
		dao.updateGetID(sql2, list2);
	}

	public int completeMsg(OrderInfo orderInfo) throws SQLException {
		String sql = "update order_info set send_area=?,send_addr=?,send_name=?,send_phone=?,send_location=?,"
				+ "rev_area=?,rev_addr=?,rev_name=?,rev_phone=?,rev_location=?,"
				+ "item_Status=?,freight_type=?,take_mark='Y' ,"
				+ "month_settle_name=?,month_settle_no=?,month_settle_card=?,cod=?,good_price=?,cod_card_no=?,cod_name=?,cod_card_cnaps_no=?,cod_bank=?,"
				+ "good_valuation=?,good_valuation_rate=?,order_note=?,last_update_time=? ,take_order_time=? "
				+ "  where order_no=? ";
		List<Object> list = new ArrayList<Object>();
		list.add(orderInfo.getSendArea());
		list.add(orderInfo.getSendAddr());
		list.add(orderInfo.getSendName());
		list.add(orderInfo.getSendPhone());
		list.add(orderInfo.getSendLocation());
		list.add(orderInfo.getRevArea());
		list.add(orderInfo.getRevAddr());
		list.add(orderInfo.getRevName());
		list.add(orderInfo.getRevPhone());
		list.add(orderInfo.getRevLocation());
		list.add(orderInfo.getItemStatus());
		list.add(orderInfo.getFreightType());
		list.add(orderInfo.getMonthSettleName());
		list.add(orderInfo.getMonthSettleNo());
		list.add(orderInfo.getMonthSettleCard());
		list.add(orderInfo.getCod());
		list.add(orderInfo.getGoodPrice());
		list.add(orderInfo.getCodCardNo());
		list.add(orderInfo.getCodName());
		list.add(orderInfo.getCodCardCnapsNo());
		list.add(orderInfo.getCodBank());
		list.add(orderInfo.getGoodValuation());
		list.add(orderInfo.getGoodValuationRate());
		list.add(orderInfo.getOrderNote());
		list.add(orderInfo.getLastUpdateTime());
		list.add(orderInfo.getTakeOrderTime());
		list.add(orderInfo.getOrderNo());
		dao.updateGetID(sql, list);
		return 1;
	}

	public boolean isExistLgcOrderNo(String lgcOrderNo, String lgcNo)
			throws SQLException {
		String sql = "select id from order_info where lgc_order_no=? and lgc_no=? ";
		List<Object> list = new ArrayList<Object>();
		list.add(lgcOrderNo);
		list.add(lgcNo);
		Map<String, Object> ret = dao.findFirst(sql, list.toArray());
		if (ret != null) {
			return true;
		}
		return false;
	}

	// GDT网页下单 运单判断
	public boolean isExistLgcOrderNo(String lgcOrderNo, String lgcNo,
			String orderNo) throws SQLException {
		String sql = "select id from order_info where lgc_order_no=? and lgc_no=?  and order_no !=?";
		List<Object> list = new ArrayList<Object>();
		list.add(lgcOrderNo);
		list.add(lgcNo);
		list.add(orderNo);
		Map<String, Object> ret = dao.findFirst(sql, list.toArray());
		if (ret != null) {
			return true;
		}
		return false;
	}

	public String getOrderNo(String lgcOrderNo, String lgcNo)
			throws SQLException {
		String sql = "select  order_no from order_info where lgc_order_no=? and lgc_no=? ";
		List<Object> list = new ArrayList<Object>();
		list.add(lgcOrderNo);
		list.add(lgcNo);
		Map<String, Object> ret = dao.findFirst(sql, list.toArray());
		if (ret != null) {
			return (String) ret.get("order_no");
		}
		return "";
	}

	// 当前是否为派件员
	public OrderDto isExistLgcOrderNoBySendCourier(String lgcOrderNo,
			String lgcNo, String sendNo) throws SQLException {
		String sql = "select *  from order_info where lgc_order_no=? and lgc_no=?  and send_courier_no=?";
		List<Object> list = new ArrayList<Object>();
		list.add(lgcOrderNo);
		list.add(lgcNo);
		list.add(sendNo);
		return dao.findFirst(OrderDto.class, sql, list.toArray());
	}

	public void delivery(String orderNo) throws SQLException {
		String sql = "update order_info set delivery=?,delivery_time=delivery_time+1 where order_no=? ";
		List<Object> list = new ArrayList<Object>();
		list.add("Y");
		list.add(orderNo);
		dao.update(sql, list.toArray());
	}

	// YMX下单
	public void saveYMXOrder(OrderDto order) throws SQLException {
		String sql = "insert into order_info(order_no,send_area,send_addr,send_name,send_phone,rev_area,rev_addr,rev_name,rev_phone,cod,good_price,source,"
				+ "order_note,create_time,lgc_no,status)"
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		List<Object> list = new ArrayList<Object>();
		list.add(order.getOrderNo());
		list.add(order.getSendArea());
		list.add(order.getSendAddr());
		list.add(order.getSendName());
		list.add(order.getSendPhone());
		list.add(order.getRevArea());
		list.add(order.getRevAddr());
		list.add(order.getRevName());
		list.add(order.getRevPhone());
		list.add(order.getCod());
		list.add(order.getGoodPrice());
		list.add(order.getSource());
		list.add(order.getOrderNote());
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
		list.add("1131");
		list.add("1");// 订单状态
		dao.update(sql, list.toArray());
	}

	/**
	 * 微信支付成功
	 * 
	 * @param lgcOrderNo
	 * @throws SQLException
	 */
	public void payStatus(String status, String lgcOrderNo) throws SQLException {
		String sql = "update order_info  set fpay_status =? where lgc_order_no =? ";
		List<Object> list = new ArrayList<Object>();
		list.add(status);
		list.add(lgcOrderNo);
		dao.update(sql, list.toArray());
	}

	/**
	 * 取件时更新代收货款收方姓名 卡号
	 * 
	 * @param lgcOrderNo
	 * @throws SQLException
	 */
	public void updateCodBank(OrderInfo order) throws SQLException {
		String sql = "update order_info  set cod_name =? where order_no =? ";
		List<Object> list = new ArrayList<Object>();
		list.add(order.getCodName());
		list.add(order.getOrderNo());
		dao.update(sql, list.toArray());
	}

	/**
	 * 问题件登记
	 * 
	 * @param orderNo
	 * @throws SQLException
	 */
	public long refuse(Map<String, Object> map) throws SQLException {
		String sql = "insert into pro_order(order_no,lgc_order_no,pro_reason,descb,deal_status,check_name,create_time) values(?,?,?,?,?,?,?)";
		List<Object> list = new ArrayList<Object>();
		list.add(map.get("order_no"));
		list.add(map.get("lgc_order_no"));
		list.add(map.get("pro_type"));
		list.add(map.get("descb"));
		list.add(map.get("status"));
		list.add(map.get("check_name"));
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
		return dao.updateGetID(sql, list);
	}

	/**
	 * 问题件查询
	 */
	public Map<String, Object> doubtCheck(String orderNo) throws SQLException {
		String sql = "select * from  pro_order where order_no=?";
		List<Object> list = new ArrayList<Object>();
		list.add(orderNo);
		return dao.findFirst(sql, list.toArray());
	}

	/**
	 * 最新问题件信息更新
	 */
	public long updateQuestion(Map<String, Object> map) throws SQLException {
		String sql = "update pro_order set pro_reason=? ,descb=?,deal_status=?,check_name=? ,create_time=? where order_no=?";
		List<Object> list = new ArrayList<Object>();
		list.add(map.get("pro_type"));
		list.add(map.get("descb"));
		list.add(map.get("status"));
		list.add(map.get("check_name"));
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
		list.add(map.get("order_no"));
		return dao.updateGetID(sql, list);
	}

	/**
	 * 问题件状态更新
	 */
	public long updateQuestionStatus(String status, String orderNo)
			throws SQLException {
		String sql = "update pro_order set deal_status=? where order_no=?";
		List<Object> list = new ArrayList<Object>();
		list.add(status);
		list.add(orderNo);
		return dao.updateGetID(sql, list);
	}

	/***
	 * 收件地址坐标更新
	 * 
	 * @param order
	 * @throws SQLException
	 */
	public void updateRevLocation(OrderInfo order) throws SQLException {
		String sql = "update order_info set rev_longitude=?,rev_latitude=? where order_no=?";
		List<Object> list = new ArrayList<Object>();
		list.add(order.getRevLongitude());
		list.add(order.getRevLatitude());
		list.add(order.getOrderNo());
		dao.update(sql, list.toArray());

	}

	/***
	 * 寄件地址坐标更新
	 * 
	 * @param order
	 * @throws SQLException
	 */
	public void updateSendLocation(OrderInfo order) throws SQLException {
		String sql = "update order_info set send_longitude=?,send_latitude=? where order_no=?";
		List<Object> list = new ArrayList<Object>();
		list.add(order.getSendLongitude());
		list.add(order.getSendLatitude());
		list.add(order.getOrderNo());
		dao.update(sql, list.toArray());
	}

	/***
	 * 修改订单流水记录
	 * 
	 * @param order
	 * @throws SQLException
	 */
	public void changeOrderRegister(Map<String, String> map)
			throws SQLException {
		String sql = "insert  into order_change_report("
				+ "order_no,"
				+ "user_no,"
				+ "create_time,"
				+ "send_area,"
				+ "send_addr,"
				+ "send_name,"
				+ "send_phone,"
				+ "rev_area,"
				+ "rev_addr,"
				+ "rev_name,"
				+ "rev_phone,"
				+ "item_Status,"
				+ "item_name,"
				+ "item_weight,"
				+ "freight_type,"
				+ "freight,"
				+ "cod,"
				+ "good_price,"
				+ "cpay,"
				+ "good_valuation,"
				+ "good_valuation_rate,"
				+ "vpay,"
				+ "tnpay,"
				+ "pay_count,"
				+ "order_note,"
				+ "pay_type,"
				+ "id_card)values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		List<Object> list = new ArrayList<Object>();
		list.add(map.get("orderNo"));
		list.add(map.get("userNo"));
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
		list.add(map.get("sendArea"));
		list.add(map.get("sendAddr"));
		list.add(map.get("sendName"));
		list.add(map.get("sendPhone[]"));
		list.add(map.get("revArea"));
		list.add(map.get("revAddr"));
		list.add(map.get("revName"));
		list.add(map.get("revPhone[]"));
		list.add(map.get("itemStatus"));
		list.add(map.get("itemName"));
		list.add(map.get("itemWeight"));
		list.add(map.get("freightType"));
		list.add(map.get("freight"));
		list.add(map.get("cod"));
		if ("1".equals(map.get("cod"))) {
			list.add(map.get("goodPrice"));
		} else {
			list.add(0.00f);
		}
		if (StringUtils.isEmptyString(map.get("cpay"))) {
			list.add("0.00");
		} else {
			list.add(map.get("cpay"));
		}
		if (StringUtils.isEmptyString(map.get("goodValuation"))) {
			list.add("0.00");
		} else {
			list.add(map.get("goodValuation"));
		}
		list.add(map.get("goodValuationRate"));
		list.add(map.get("vpay"));
		list.add(map.get("tnpay"));
		list.add(map.get("payCount"));
		list.add(map.get("orderNote"));
		list.add(map.get("payType"));
		list.add(map.get("idCard"));
		dao.update(sql, list.toArray());
	}

	/***
	 * 修改订单流水记录
	 * 
	 * @param order
	 * @throws SQLException
	 */
	public void HUIYUANchangeOrderRegister(Map<String, String> map)
			throws SQLException {
		String sql = "insert  into order_change_report(" + "order_no,"
				+ "user_no," + "create_time," + "send_area," + "send_addr,"
				+ "send_name," + "send_phone," + "rev_area," + "rev_addr,"
				+ "rev_name," + "rev_phone," + "item_Status," + "item_name,"
				+ "item_weight," + "freight_type," + "order_note,"
				+ "pay_type,"
				+ "id_card)values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		List<Object> list = new ArrayList<Object>();
		list.add(map.get("orderNo"));
		list.add(map.get("userNo"));
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
		list.add(map.get("sendArea"));
		list.add(map.get("sendAddr"));
		list.add(map.get("sendName"));
		list.add(map.get("sendPhone[]"));
		list.add(map.get("revArea"));
		list.add(map.get("revAddr"));
		list.add(map.get("revName"));
		list.add(map.get("revPhone[]"));
		list.add(map.get("itemStatus"));
		list.add(map.get("itemName"));
		list.add(map.get("itemWeight"));
		list.add(map.get("freightType"));
		list.add(map.get("orderNote"));
		list.add(map.get("payType"));
		list.add(map.get("idCard"));
		dao.update(sql, list.toArray());
	}

	/***
	 * 订单流水记录第一登记
	 * 
	 * @param order
	 * @throws SQLException
	 */
	public void changeOrderRegisterFirst(OrderInfo orderInfo)
			throws SQLException {
		String sql = "insert  into order_change_report("
				+ "order_no,"
				+ "user_no,"
				+ "create_time,"
				+ "send_area,"
				+ "send_addr,"
				+ "send_name,"
				+ "send_phone,"
				+ "rev_area,"
				+ "rev_addr,"
				+ "rev_name,"
				+ "rev_phone,"
				+ "item_status,"
				+ "item_name,"
				+ "item_weight,"
				+ "freight_type,"
				+ "freight,"
				+ "cod,"
				+ "good_price,"
				+ "cpay,"
				+ "good_valuation,"
				+ "good_valuation_rate,"
				+ "vpay,"
				+ "tnpay,"
				+ "pay_count,"
				+ "order_note)values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		List<Object> list = new ArrayList<Object>();
		list.add(orderInfo.getOrderNo());
		list.add(orderInfo.getTakeCourierNo());
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
		list.add(orderInfo.getSendArea());
		list.add(orderInfo.getSendAddr());
		list.add(orderInfo.getSendName());
		list.add(orderInfo.getSendPhone());
		list.add(orderInfo.getRevArea());
		list.add(orderInfo.getRevAddr());
		list.add(orderInfo.getRevName());
		list.add(orderInfo.getRevPhone());
		list.add(orderInfo.getItemStatus());
		list.add(orderInfo.getItemName());
		list.add(orderInfo.getItemWeight());
		list.add(orderInfo.getFreightType());
		list.add(orderInfo.getFreight());
		list.add(orderInfo.getCod());
		list.add(orderInfo.getGoodPrice());
		if (StringUtils.isEmptyString(orderInfo.getCpay())) {
			list.add("0.00");
		} else {
			list.add(orderInfo.getCpay());
		}
		list.add(orderInfo.getGoodValuation());
		list.add(orderInfo.getGoodValuationRate());
		if (StringUtils.isEmptyString(orderInfo.getVpay())) {
			list.add("0.00");
		} else {
			list.add(orderInfo.getVpay());
		}
		list.add(orderInfo.getTnpay());
		list.add(orderInfo.getPayAcount());
		list.add(orderInfo.getOrderNote());
		dao.update(sql, list.toArray());
	}

	/***
	 * 修改订单信息
	 * 
	 * @param order
	 * @throws SQLException
	 */
	public void changeOrder(Map<String, String> map) throws SQLException {
		String sql = "update  order_info " + " set last_update_time = ?,"
				+ "send_area=?," + "send_addr=?," + "send_name=?,"
				+ "send_phone=?," + "rev_area=?," + "rev_addr=?,"
				+ "rev_name=?," + "rev_phone=?," + "item_status=?,"
				+ "item_name=?," + "item_weight=?," + "freight_type=?,"
				+ "freight=?," + "cod=?," + "good_price=?," + "cpay=?,"
				+ "good_valuation=?," + "good_valuation_rate=?," + "vpay=?,"
				+ "tnpay=?," + "pay_acount=?," + "order_note=?,"
				+ "pay_type=? ," + "id_card = ?,  "
				+ "time_type=?  where order_no =?";
		List<Object> list = new ArrayList<Object>();
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
		list.add(map.get("sendArea"));
		list.add(map.get("sendAddr"));
		list.add(map.get("sendName"));
		list.add(map.get("sendPhone[]"));
		list.add(map.get("revArea"));
		list.add(map.get("revAddr"));
		list.add(map.get("revName"));
		list.add(map.get("revPhone[]"));
		list.add(map.get("itemStatus"));
		list.add(map.get("itemName"));
		list.add(map.get("itemWeight"));
		list.add(map.get("freightType"));
		list.add(map.get("freight"));
		list.add(map.get("cod"));
		if ("1".equals(map.get("cod"))) {
			list.add(map.get("goodPrice"));
		} else {
			list.add(0.00f);
		}
		if (StringUtils.isEmptyString(map.get("cpay"))) {
			list.add("0.00");
		} else {
			list.add(map.get("cpay"));
		}
		if (StringUtils.isEmptyString(map.get("goodValuation"))) {
			list.add("0.00");
		} else {
			list.add(map.get("goodValuation"));
		}
		list.add(map.get("goodValuationRate"));
		list.add(map.get("vpay"));
		list.add(map.get("tnpay"));
		list.add(map.get("payCount"));
		list.add(map.get("orderNote"));
		list.add(map.get("payType"));
		list.add(map.get("idCard"));
		list.add(map.get("timeType"));
		list.add(map.get("orderNo"));
		System.out.println(map);
		dao.update(sql, list.toArray());
	}

	/***
	 * 修改订单信息
	 * 
	 * @param order
	 * @throws SQLException
	 */
	public void HUIYUANchangeOrder(Map<String, String> map) throws SQLException {
		String sql = "update  order_info " + " set last_update_time = ?,"
				+ "send_area=?," + "send_addr=?," + "send_name=?,"
				+ "send_phone=?," + "rev_area=?," + "rev_addr=?,"
				+ "rev_name=?," + "rev_phone=?," + "item_status=?,"
				+ "item_name=?," + "item_weight=?," + "order_note=?,"
				+ "id_card = ?  " + "where order_no =?";
		List<Object> list = new ArrayList<Object>();
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
		list.add(map.get("sendArea"));
		list.add(map.get("sendAddr"));
		list.add(map.get("sendName"));
		list.add(map.get("sendPhone[]"));
		list.add(map.get("revArea"));
		list.add(map.get("revAddr"));
		list.add(map.get("revName"));
		list.add(map.get("revPhone[]"));
		list.add(map.get("itemStatus"));
		list.add(map.get("itemName"));
		list.add(map.get("itemWeight"));
		list.add(map.get("orderNote"));
		list.add(map.get("idCard"));
		list.add(map.get("orderNo"));
		dao.update(sql, list.toArray());
	}

	/**
	 * 插入最新的备注信息
	 * 
	 * @param orderInfo
	 * @param userNo
	 * @param id
	 * @throws SQLException
	 */
	public void insertOrderNote(OrderInfo orderInfo, String userNo, String id,
			String src) throws SQLException {
		String sql = "insert into order_note(order_id,note,operator,op_src,create_time)values(?,?,?,?,?)";
		List<Object> list = new ArrayList<Object>();
		list.add(id);
		list.add(orderInfo.getOrderNote());
		list.add(userNo);
		list.add(src);
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
		dao.update(sql, list.toArray());
	}

	/**
	 * 查询问题件处理信息
	 * 
	 * @param orderInfo
	 * @param userNo
	 * @param id
	 * @throws SQLException
	 */
	public String checkProInfoByOrderNo(String orderNo) throws SQLException {
		String sql = "select  pdh.deal_text dealText     from pro_order pr ,pro_deal_history pdh where pr.id  = pdh.pro_id  and  pr.order_no = ? order by pdh.id desc ";
		List<Object> list = new ArrayList<Object>();
		list.add(orderNo);
		Map<String, Object> mao = dao.findFirst(sql, list.toArray());
		if (mao != null) {
			return (String) mao.get("dealText");
		}
		return "";
	}

	/**
	 * 查询快递员最新收派件时间
	 */
	public String getTime(String userNo) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		list.add(userNo);
		String sql1 = "select take_order_time  takeOrderTime from order_info where take_courier_no=? group by substr(take_order_time,1,10) order by take_order_time desc";
		Map<String, Object> map1 = dao.findFirst(sql1, list.toArray());
		String sql2 = "select send_order_time  sendOrderTime from order_info where send_courier_no=? group by substr(send_order_time,1,10) order by send_order_time desc";
		Map<String, Object> map2 = dao.findFirst(sql2, list.toArray());

		if (map1 == null) {
			if (map2 == null) {
				return "";
			} else {
				if (map2.get("sendOrderTime") == null) {
					return "";
				} else {
					return DateUtils.formatDate((Date) map2
							.get("sendOrderTime"));
				}
			}
		} else {
			if (map1.get("takeOrderTime") == null) {
				if (map2 == null) {
					return "";
				} else {
					if (map2.get("sendOrderTime") == null) {
						return "";
					} else {
						return DateUtils.formatDate((Date) map2
								.get("sendOrderTime"));
					}
				}
			} else {
				if (map2 == null) {
					return DateUtils.formatDate((Date) map1
							.get("takeOrderTime"));
				} else {
					if (map2.get("sendOrderTime") == null) {
						return DateUtils.formatDate((Date) map1
								.get("takeOrderTime"));
					} else {
						Date takeOrderTime = (Date) map1.get("takeOrderTime");
						Date sendOrderTime = (Date) map2.get("sendOrderTime");
						if (takeOrderTime.after(sendOrderTime)) {
							return DateUtils.formatDate((Date) map1
									.get("takeOrderTime"));
						} else {
							return DateUtils.formatDate((Date) map2
									.get("sendOrderTime"));
						}
					}
				}
			}
		}
	}

	public void insertBatchTakeCount(String lgcOrderNo, String courierNo) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "insert into batch_take_dayCount(lgc_order_no,courier_no)values(?,?)";
		list.add(lgcOrderNo);
		list.add(courierNo);
		dao.update(sql, list.toArray());
	}

	public void insertBatchPROCount(String lgcOrderNo, String courierNo) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "insert into batch_pro_daycount(lgc_order_no,courier_no)values(?,?)";
		list.add(lgcOrderNo);
		list.add(courierNo);
		dao.update(sql, list.toArray());
	}

	public String getTakeCount(String newDate, String courierNo) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "select count(id)  count1 from batch_take_dayCount where substr(day_time,1,10) =? and courier_no = ?";
		list.add(newDate);
		list.add(courierNo);
		Map<String, Object> map = dao.findFirst(sql, list.toArray());
		if (map == null) {
			return "0";
		}
		return String.valueOf(map.get("count1"));
	}

	public String getPROCount(String newDate, String courierNo) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "select count(id)  count1 from batch_pro_daycount where substr(day_time,1,10) =? and courier_no = ?";
		list.add(newDate);
		list.add(courierNo);
		Map<String, Object> map = dao.findFirst(sql, list.toArray());
		if (map == null) {
			return "0";
		}
		return String.valueOf(map.get("count1"));
	}

	public void insertNotInfo(String source, Date nowDate, String substationNo, String lgcNo, String lgcOrderNo) throws SQLException {
		if (!Arrays.asList("KDWZ.WAREHOUSE.IN", "KDWZ.WAREHOUSE.OUT", "KDWZ.WAREHOUSE.OUT.MANAGER", "KDWZ.SEND.SCAN").contains(source)) {
			throw new RuntimeException(source);
		}
		String EorderNo = sequenceService.getNextVal("order_no");
		OrderTrack track = new OrderTrack();
		track.setOrderNo(EorderNo);
		track.setContext("订单被创建");
		track.setOrderTime(DateUtils.formatDate(nowDate));
		track.setCompleted("N");
		track.setOrderStatus("INIT");
		orderTrackService.add(track);

		OrderInfo orderInfo = new OrderInfo();
		orderInfo.setSubStationNo(substationNo);
		orderInfo.setLgcNo(lgcNo);
		orderInfo.setOrderNo(EorderNo);
		orderInfo.setLgcOrderNo(lgcOrderNo);
		orderInfo.setSource(source);
		orderInfo.setCreateTime(DateUtils.formatDate(nowDate));
		orderInfo.setLastUpdateTime(DateUtils.formatDate(nowDate));

		String sql = "insert into order_info(sub_station_no,order_no,lgc_order_no,last_update_time,create_time,`source`,`status`,lgc_no)values(?,?,?,?,?,?,?,?)";
		List<Object> list = new ArrayList<Object>();
		list.add(orderInfo.getSubStationNo());
		list.add(orderInfo.getOrderNo());
		list.add(orderInfo.getLgcOrderNo());
		list.add(orderInfo.getLastUpdateTime());
		list.add(orderInfo.getCreateTime());
		if ("KDWZ.WAREHOUSE.IN".equals(source)) {
			list.add("入仓扫描");
			list.add(2);
		} else if ("KDWZ.WAREHOUSE.OUT".equals(source)) {
			list.add("出仓扫描");
			list.add(2);
		} else if ("KDWZ.WAREHOUSE.OUT.MANAGER".equals(source)) {
			list.add("出仓到分站");
			list.add(2);
		} else if ("KDWZ.SEND.SCAN".equals(source)) {
			list.add("派件扫描");
			list.add(2);
		} else {
			throw new RuntimeException(source);
		}
		list.add(orderInfo.getLgcNo());
		dao.update(sql, list.toArray());
	}
	
	
	public String defaultItemStatus(){
		String sql="select item_text from item_type where default_item =1";
		Map<String, Object> m =null;
		try {
			 m = dao.findFirst(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(m==null){
			return "其他";
		}
		return m.get("item_text").toString();
	}
	
	public int updateRealSendTime(String order_no){
		String sql="update order_info set real_send_time =now() where order_no=?";
		List<Object> list = new ArrayList<Object>();
		list.add(order_no);
		int ret =0;
		try {
			ret= dao.update(sql, list.toArray());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}
}
