package com.yogapay.couriertsi.services2;

import com.yogapay.couriertsi.domain.FranchiseOrder;
import com.yogapay.couriertsi.domain.OrderInfo;
import com.yogapay.couriertsi.domain.Substation;
import com.yogapay.sql.mybatis.BaseDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FranOrderService2 extends BaseDAO {

	@Autowired
	private OrderInfoService2 orderInfoService2;
	@Autowired
	private SubstationService2 substationService2;

	private void insertOne(String orderNo, int type) {
		OrderInfo o = orderInfoService2.queryByOrderNo(orderNo);
		if (o == null) {
			return;
		}
		Substation substation;
		switch (type) {
			case 1:
				substation = substationService2.getByNO(o.getSubStationNo());
				break;
			case 2:
				substation = substationService2.getByNO(o.getSendSubstationNo());
				break;
			default:
				throw new RuntimeException();
		}
		if (substation == null || !"J".equals(substation.getSubstationType())) {
			return;
		}
		FranchiseOrder f = new FranchiseOrder();
		f.setOrder_no(o.getOrderNo());
		f.setLgc_order_no(o.getLgcOrderNo());
		f.setTake_substation_no(o.getSubStationNo());
		f.setSend_substation_no(o.getSendSubstationNo());
		f.setItem_type(o.getItemType());
		f.setItem_weight(String.valueOf(o.getItemWeight()));
		switch (type) {
			case 1:
				f.setMoney_type("Z");
				break;
			case 2:
				f.setMoney_type("P");
				break;
			default:
				throw new RuntimeException();
		}
		f.setCreate_time(o.getCreateTime());
		insert(STATEMENT_ID + "insertOne", f);
	}

	public void tryInsertOnTake_tx(String orderNo) {
//		insertOne(orderNo, 1);
	}

	public void tryInsertOnSend_tx(String orderNo) {
//		insertOne(orderNo, 2);
	}
}
