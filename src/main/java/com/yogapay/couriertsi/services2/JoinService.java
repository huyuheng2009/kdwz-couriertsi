package com.yogapay.couriertsi.services2;

import com.yogapay.couriertsi.domain.JoinSubstationAcount;
import com.yogapay.couriertsi.domain.User;
import com.yogapay.sql.mybatis.BaseDAO;
import org.springframework.stereotype.Service;

@Service
public class JoinService extends BaseDAO {

	public JoinSubstationAcount queryByUser(User user) {
		return (JoinSubstationAcount) selectOne(STATEMENT_ID + "queryByUser", user);
	}
}
