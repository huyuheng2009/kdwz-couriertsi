package com.yogapay.couriertsi.utils;

import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yogapay.couriertsi.api.ConfigInfo;
import com.yogapay.couriertsi.enums.BizType;
import com.yogapay.couriertsi.utils.http.HttpUtils;

public class PosUtil {
	static final Logger log = LoggerFactory.getLogger(PosUtil.class);
	
	private static final String INITURL = "/pos/login";//  ;
	private static final String SALEURL = "/pos/sale";//  ;
	private static final String ROLLBACKURL = "/pos/rollback";//  ;
	
	public static Map<String, Object> sendAndRev(ConfigInfo configInfo,Map<String, String> params,BizType bizType) throws Exception{
		HttpUtils httpUtils = new HttpUtils() ;
		String jsonRet = "{}" ;
		switch (bizType) {
		case INIT:
			log.info(INITURL);
			jsonRet = httpUtils.post(configInfo.getPos_host()+INITURL, params,60000);
			break;
         case SALE:
        	 log.info(SALEURL);
        	 jsonRet = httpUtils.post(configInfo.getPos_host()+SALEURL, params,60000);
			break;
        case ROLLBACK:
        	log.info(ROLLBACKURL);
        	jsonRet = httpUtils.post(configInfo.getPos_host()+ROLLBACKURL, params,60000);
 			break;
		default:
			break;
		}  
		if (StringUtils.isEmptyWithTrim(jsonRet)) {
			jsonRet = "{}" ;
		 }
		   System.out.println("jsonRet:----->"+jsonRet);
		   Map<String, Object> retMap = JsonUtil.getMapFromJson(jsonRet) ;
		   log.info("***********rev from pos*****************");
		   log.info(retMap.toString());
		   log.info("***********rev from pos*****************");
		return retMap ;
	}
	

}
