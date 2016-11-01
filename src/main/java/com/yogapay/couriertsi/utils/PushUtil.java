package com.yogapay.couriertsi.utils;

import java.util.HashMap;
import java.util.Map;

import com.yogapay.couriertsi.api.ConfigInfo;
import com.yogapay.couriertsi.domain.PushMsg;
import com.yogapay.couriertsi.utils.http.HttpUtils;


public class PushUtil {
	 
	 public static final String USERPUSHBYID = "/push/user/msg";// 
	 
	 public static final String COURIERPUSHBYID = "/push/courier/msg";// 
	 
	 public static final String COURIERPUSHBYMSG = "/push/courier/msgdata";// 
	
	public static void pushById(ConfigInfo configInfo ,String id,int userType,String uid) throws Exception {
		PushThread pThread = new PushThread(configInfo ,id,userType,uid) ;
		Thread t = new Thread(pThread) ;
		t.start();
	}
	
	
	public static void pushByMSG(ConfigInfo configInfo ,PushMsg msg,String uid) throws Exception {
		PushMsgThread pThread = new PushMsgThread(configInfo,msg,uid) ;
		Thread t = new Thread(pThread) ;
		t.start();
	}
}


 class PushThread implements Runnable{
	String msgId ;
	int userType ;
	String uid;
	ConfigInfo configInfo ;
	public PushThread(ConfigInfo configInfo ,String msgId,int userType,String uid) {
   this.msgId = msgId ;
   this.userType = userType ;
   this.uid = uid ;
   this.configInfo = configInfo ;
	}

	@Override
	public void run() {
		 HttpUtils httpUtils = new HttpUtils() ; 
		 Map<String, String> params = new HashMap<String, String>() ;
		 params.put("msgId", msgId) ;
		 params.put("uid", uid) ;
		 Map<String, String> header = null ;
		 try {
			 if (userType==1) {
				 httpUtils.post(configInfo.getPush_host()+PushUtil.USERPUSHBYID, params,header);
			}else {
				 httpUtils.post(configInfo.getPush_host()+PushUtil.COURIERPUSHBYID, params,header);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
 
 class PushMsgThread implements Runnable{
	  PushMsg msg  ;
	  String uid ;
	  ConfigInfo configInfo ;
	public PushMsgThread(ConfigInfo configInfo ,PushMsg msg,String uid) {
   this.msg = msg ;
   this.uid = uid;
   this.configInfo = configInfo ;
	}

	@Override
	public void run() {
		 HttpUtils httpUtils = new HttpUtils() ; 
		 Map<String, String> params = new HashMap<String, String>() ;
		 params.put("msg", JsonUtil.toJson(msg)) ;
		 params.put("uid", uid) ;
		 Map<String, String> header = null ;
		 try {
			httpUtils.post(configInfo.getPush_host()+PushUtil.COURIERPUSHBYMSG, params,header);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}  
 
 
