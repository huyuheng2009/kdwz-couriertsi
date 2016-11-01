package com.yogapay.couriertsi.utils;

import java.util.Map;

import com.yogapay.couriertsi.api.ConfigInfo;
import com.yogapay.couriertsi.utils.http.HttpUtils;




public class WeiXinUtil {
	 
	 public static final String WEIXINPUSH = "/web/sendMessage/cod";// 
	 
	 public static final String WEIXINCOMMOMPUSH = "/web/sendMessage/commom";// 
	 
	
	public static void push(ConfigInfo configInfo ,Map<String, String> params,int url) throws Exception {
		WXPushThread pThread = new WXPushThread(configInfo,params, url) ;
		Thread t = new Thread(pThread) ;
		t.start();
	}
}

  class WXPushThread implements Runnable{
	Map<String, String> params ;
	int url ;
	ConfigInfo configInfo ;
	public WXPushThread(ConfigInfo configInfo ,Map<String, String> params,int url) {
    this.params = params ;
    this.url = url ;
    this.configInfo = configInfo ;
	}

	@Override
	public void run() {
		 HttpUtils httpUtils = new HttpUtils() ; 
		 Map<String, String> header = null ;
		 try {
			 System.out.println("WXPushThread start-------------------");
			 String r = "" ;
			 if (url==1) {
				r = httpUtils.post(configInfo.getWxpush_host()+WeiXinUtil.WEIXINPUSH, params, header) ;
			}else {
				r = httpUtils.post(configInfo.getWxpush_host()+WeiXinUtil.WEIXINCOMMOMPUSH, params, header) ;
			}
           System.out.println(r);
           System.out.println("WXPushThread end---------------------");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


  
