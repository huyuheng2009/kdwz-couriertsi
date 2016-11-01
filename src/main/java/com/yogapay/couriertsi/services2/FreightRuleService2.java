package com.yogapay.couriertsi.services2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yogapay.couriertsi.domain.FreightRule;
import com.yogapay.sql.mybatis.BaseDAO;

@Service
public class FreightRuleService2 extends BaseDAO {

	
	
	public FreightRule getFirstRule(String timeType,String itype) {
		Map<String, Object> pMap = new HashMap<String, Object>() ;
		pMap.put("timeType", timeType) ;
		pMap.put("itype", itype) ;
		pMap.put("getfirst", "1") ;
		return (FreightRule) selectOne(STATEMENT_ID+"getRuleByParams", pMap) ;
	}
	
	
	public double freightCalculate(String time_type ,String item_type,double weight,double distance) {
		FreightRule rule = getFirstRule(time_type, item_type) ;
		return getRuleSettle(rule, weight, distance) ;
	}
	
	
	public double getRuleSettle(FreightRule rule,double weight,double distance) {
		double ret = 0 ;
		double w = 0 ;
	   if (rule==null||weight<0.01||distance<0.01) {
		return 0 ;
	   } 
	   ret = rule.getFmoney()+rule.getVpay() ;
	   
	   double rf_distance = rule.getFdistance() ;  
	   double rf_weight = rule.getFweight() ;
	   double by_distance = distance-rf_distance ;
	   double by_weight = weight-rf_weight ;
	   
	   double dmoney = 0 ;
	   double wmoney = 0 ;
	   if (by_distance>0.01) {
		  int step = (int) Math.ceil(by_distance/rule.getStep_distance()) ;
		  dmoney = step*rule.getStep_distance_money() ;
	   }
	   if (by_weight>0.01) {
			  int step = (int) Math.ceil(by_weight/rule.getStep_weight()) ;
			  wmoney = step*rule.getStep_weight_money() ;
	   }
	    ret = ret + dmoney + wmoney ;
		return ret ;
	}
	
	

}
