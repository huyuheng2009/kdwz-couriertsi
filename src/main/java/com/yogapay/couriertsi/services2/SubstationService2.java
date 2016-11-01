package com.yogapay.couriertsi.services2;

import com.yogapay.couriertsi.domain.Substation;
import com.yogapay.sql.mybatis.BaseDAO;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class SubstationService2 extends BaseDAO {

	public Substation getByNO(String no) {
		if (StringUtils.isEmpty(no)) {
			return null;
		}
		return (Substation) selectOne(STATEMENT_ID + "getByNO", no);
	}
}
