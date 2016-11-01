package com.yogapay.couriertsi.services2;

import com.yogapay.couriertsi.domain.OrderTrack;
import com.yogapay.couriertsi.services2.cdt.OrderTrackCondition;
import com.yogapay.sql.mybatis.BaseDAO;
import org.springframework.stereotype.Service;

@Service
public class OrderTrackService2 extends BaseDAO {

	public OrderTrack getLastOrderTrack(OrderTrackCondition cdt) {
		return (OrderTrack) selectOne(STATEMENT_ID + "getLastOrderTrack", cdt);
	}

	public void fullSave(OrderTrack orderTrack) {
		insert(STATEMENT_ID + "fullSave", orderTrack);
	}

	public void unLastTrack(OrderTrack orderTrack) {
		update(STATEMENT_ID + "unLastTrack", orderTrack);
	}

}
