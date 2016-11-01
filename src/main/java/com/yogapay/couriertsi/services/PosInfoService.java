package com.yogapay.couriertsi.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.yogapay.couriertsi.dto.PosInfoDto;
import com.yogapay.couriertsi.utils.Dao;

/**
 * pos交易记录service
 * @author 
 *
 */
@Service
public class PosInfoService {
	
	@Resource
	private Dao dao ;
	
	public void save(Map<String, Object> params) throws SQLException {
		String sql = "insert into pos_info (order_no,courier_no,merchant_name,merchant_no,terminal_no,amount,currency,issuer,card_no,op_type,batch_no,"
				                       + "voucher_no,auth_no,ref_no,operator_no,trans_time,biz_type) "
				       +" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
		List<Object> list = new ArrayList<Object>();
		list.add(params.get("orderNo")) ;
		list.add(params.get("courierNo")) ;
		list.add(params.get("merchantName")) ;
		list.add(params.get("merchantNo")) ;
		list.add(params.get("terminalNo")) ;
		list.add(params.get("amount")) ;
		list.add(params.get("currency")) ;
		list.add(params.get("issuer")) ;
		list.add(params.get("cardNo")) ;
		list.add(params.get("opType")) ;
		list.add(params.get("batchNo")) ;
		list.add(params.get("voucherNo")) ;
		list.add(params.get("authNo")) ;
		list.add(params.get("refNo")) ;
		list.add(params.get("operatorNo")) ;
		list.add(params.get("transTime")) ;
		list.add(params.get("bizType")) ;
		dao.update(sql, list.toArray()) ;
 	}

	public List<PosInfoDto> getByOrderNoCourierNo(String orderNo,String courierNo) throws SQLException {
		String sql = "select * from pos_info where order_no=? and courier_no=? order by id desc" ;
		List<Object> list = new ArrayList<Object>();
		list.add(orderNo) ;
		list.add(courierNo) ;
		return dao.find(PosInfoDto.class, sql, list.toArray());
	}
	
	public Map<String, Object> getKsnByKsnNo(String ksnNo) throws SQLException {
		String sql = "select id from `posp`.ksn_info k where k.ksn_no=? and k.is_activated=1";
		return dao.findFirst(sql, ksnNo);
	}

	public Map<String, Object> getLgcMerchantByLgcNo(String lgcName) throws SQLException {
		String sql = "select * from `manager_lgc`.lgc_merchant l where l.lgc_no=?";
		return dao.findFirst(sql, lgcName);
	}

	public Map<String, Object> getPosByMerchantNo(String merchantNo) throws SQLException {
		String sql = "select pt.*,m.merchant_name from `posp`.pos_terminal pt  left join  `posp`.pos_merchant m on m.merchant_no=pt.merchant_no  where pt.merchant_no=?";
		return dao.findFirst(sql, merchantNo);
	}

	public Map<String, Object> getKeyByKsnNo(String ksnNo) throws SQLException {
		String sql = "select * from `posp`.secret_key sk where sk.device_id=?";
		return dao.findFirst(sql, ksnNo);
	}

	public int updatePosVoucherNo(Map<String,Object> pos){
		String sql = "update `posp`.pos_terminal set voucher_no=? where terminal_no=?";
		int i = 0;
		try {
			 i = dao.update(sql, new Object[]{(String)pos.get("voucher_no"),(String) pos.get("terminal_no")});
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return i;
	}
	
	public Map<String, Object> findCardbin(String cardNo) throws SQLException{
		String sql = "select * from `posp`.card_bin c where c.card_length=length(?) and c.verify_code=left(?, c.verify_length) order by c.verify_length desc";
		return dao.findFirst(sql, new Object[]{cardNo,cardNo});
	}
    
}
