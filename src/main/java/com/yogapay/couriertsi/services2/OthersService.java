package com.yogapay.couriertsi.services2;

import com.yogapay.sql.mybatis.BaseDAO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class OthersService extends BaseDAO {

	/**
	 @return [{"id":1(Number),"cpn_name":""}]
	 */
	public List<Map<String, Object>> get_cpn() {
		return (List<Map<String, Object>>) selectList(STATEMENT_ID + "get_cpn");
	}

	/**
	 @param id
	 @return {"id":1(Number),"cpn_name":""}
	 */
	public Map<String, Object> get_cpn(int id) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("id", id);
		return (Map<String, Object>) selectOne(STATEMENT_ID + "get_cpn", param);
	}
}
