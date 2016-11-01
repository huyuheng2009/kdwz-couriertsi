package com.yogapay.couriertsi.services2;

import com.yogapay.core.ErrorCodeException;
import com.yogapay.couriertsi.domain.OrderInfo;
import com.yogapay.couriertsi.domain.OrderTrack;
import com.yogapay.couriertsi.domain.Substation;
import com.yogapay.couriertsi.domain.User;
import com.yogapay.couriertsi.services2.cdt.OrderTrackCondition;
import com.yogapay.couriertsi.utils.DateUtils;
import com.yogapay.sql.mybatis.BaseDAO;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderInfoService2 extends BaseDAO {

	@Autowired
	private OrderTrackService2 orderTrackService2;
	@Autowired
	private OthersService othersService;
	@Autowired
	private SubstationService2 substationService2;

	public OrderInfo queryByOrderNo(String orderNo) {
		return (OrderInfo) selectOne(STATEMENT_ID + "queryByOrderNo", orderNo);
	}

	public void transfer_order_tx(User user, String c_order_no, int cpn_id, String cpn_order_no) throws ErrorCodeException {
		Map<String, Object> map = (Map<String, Object>) selectOne(STATEMENT_ID + "transfer_order_checkExists", c_order_no);
		if (((Number) map.get("num")).intValue() == 0) {
			throw new ErrorCodeException(8000);
		}
		if (StringUtils.isNotBlank((String) map.get("for_no"))) {
			throw new ErrorCodeException(8001);
		}
		if (StringUtils.isNotBlank((String) map.get("order_no"))) {
			throw new ErrorCodeException(8001);
		}
		Map<String, Object> cpn = othersService.get_cpn(cpn_id);
		if (cpn == null) {
			throw new ErrorCodeException(8002);
		}
		OrderTrackCondition cdt = new OrderTrackCondition();
		cdt.lgcOrderNo = c_order_no;
		OrderTrack lastOrderTrack = orderTrackService2.getLastOrderTrack(cdt);
		if (lastOrderTrack == null) {
			throw new ErrorCodeException(8003);
		}
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("courier_no", user.getCourierNo());
		param.put("c_order_no", c_order_no);
		param.put("cpn_id", cpn_id);
		param.put("cpn_order_no", cpn_order_no);
		if (update(STATEMENT_ID + "transfer_order_insert", param) == 0) {
			throw new ErrorCodeException(8002);
		}
		update(STATEMENT_ID + "transfer_order_for_no", param);
		//
		OrderTrack orderTrack = new OrderTrack();
		orderTrack.setOrderNo(lastOrderTrack.getOrderNo());
		orderTrack.setOrderTime(DateUtils.formatDate(new Date()));
		orderTrack.setCompleted("N");
		orderTrack.setIsLast("Y");
		orderTrack.setScanIno(lastOrderTrack.getScanIno());
		orderTrack.setScanIname(lastOrderTrack.getScanIname());
		orderTrack.setScanOno(user.getUserName());
		orderTrack.setScanOname(user.getRealName());
		orderTrack.setOrderStatus("FORD");
		orderTrack.setCurNo(lastOrderTrack.getCurNo());
		orderTrack.setCurType(lastOrderTrack.getCurType());
		Substation substation = substationService2.getByNO(orderTrack.getCurNo());
		orderTrack.setContext(String.format("快件转出：%s,上一站是:%s", cpn.get("cpn_name"), substation == null ? "" : substation.getSubstationName()));
		orderTrack.setParentId(lastOrderTrack.getId());
		orderTrackService2.fullSave(orderTrack);
		orderTrackService2.unLastTrack(lastOrderTrack);
	}

}
