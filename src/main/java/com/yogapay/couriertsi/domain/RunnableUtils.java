package com.yogapay.couriertsi.domain;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yogapay.couriertsi.dataSource.DynamicDataSourceHolder;
import com.yogapay.couriertsi.services.LgcService;
import com.yogapay.couriertsi.services.OrderInfoService;
import com.yogapay.couriertsi.utils.AmapUtil;
import com.yogapay.couriertsi.utils.DateUtils;
import com.yogapay.couriertsi.utils.JsonUtil;
import com.yogapay.couriertsi.utils.Md5;
import com.yogapay.couriertsi.utils.SHA;
import com.yogapay.couriertsi.utils.http.HttpUtils;
/***
 * 请求高德数据
 * @author Administrator
 *
 */
public class RunnableUtils {
	public static final Logger log = LoggerFactory.getLogger(RunnableUtils.class);
	public  void queryLocation(String addr,int i,String orderNo,OrderInfoService orderInfoService,String uid ,DynamicDataSourceHolder dynamicDataSourceHolder){
		if(i==1){//发件地址请求
			addr =addr.replaceAll("-", "");
			SendThread send = new SendThread(addr,orderInfoService,orderNo,uid,dynamicDataSourceHolder);
			Thread t = new Thread(send);
			t.start();
		}
		if(i==2){//收件地址请求
			addr =addr.replaceAll("-", "");
			RevThread rev = new RevThread(addr,orderInfoService,orderNo,uid,dynamicDataSourceHolder);
			Thread t = new Thread(rev);
			t.start();
		}
	}
	public void pushTMX(String url,String status,	String orderNo,String realName,	String phone,String info,String operateTime,String uid){
		YMXRquest yMXRquest = new YMXRquest(url,status,orderNo,realName,phone,info,operateTime,uid);
		Thread t = new Thread(yMXRquest);
		t.start();
	}
	public void pushWEIXIN(OrderInfo order,User userInfo,int with){
		weixinPush weixinPush = new weixinPush(order, userInfo, with);
		Thread t = new Thread(weixinPush);
		t.start();
	}

public void KangmeiPush(String URL,String company_num,String userName,String orderNo,String context,String status){
	KangmeiPush kangmeiPush = new KangmeiPush(URL,company_num,userName,orderNo, context, status);
	Thread t = new Thread(kangmeiPush);
	t.start();
}

public void MessagePushClass(String channel,String lgcNo,String phone,String context,LgcService lgcService) throws SQLException{
	if (lgcService!=null) {
		if (lgcService.msgCount(lgcNo)<2) {
			lgcService.updateByTypeName("MOBILE_CONFIG", "TAKE_SEND_MSG", "0");
			lgcService.updateByTypeName("MOBILE_CONFIG", "SEND_SEND_MSG", "0");
		}
	}
	MessagePush messagePush = new MessagePush(channel,lgcNo,phone,context);
	Thread t = new Thread(messagePush);
	t.start();
}
}

/***
 * 向高德请求收件件地址坐标
 * @author Administrator
 *
 */
class RevThread implements Runnable{
	String addr;
	String orderNo;
	OrderInfoService orderInfoService;
	String uid;
	DynamicDataSourceHolder dynamicDataSourceHolder ;
	public RevThread(String addr,OrderInfoService orderInfoService,String orderNo,String uid,DynamicDataSourceHolder dynamicDataSourceHolder){
		this.addr=addr;
		this.orderInfoService=orderInfoService;
		this.orderNo=orderNo;
		this.uid=uid;
		this.dynamicDataSourceHolder = dynamicDataSourceHolder ;
	}
	@Override
	public void run() {
		System.out.println("--------------------------------------请求寄件地址坐标线程启动------------------------------------");
		dynamicDataSourceHolder.setDataSource(uid.substring(0,uid.indexOf("_")) );
		OrderInfo order  = new OrderInfo();
		order.setOrderNo(orderNo);
		Map<String, String> revMap=null;
		try {
			revMap = AmapUtil.addressToGPS(addr);
		} catch (Exception e) {
			System.out.println("-----------------------获取坐标失败------------------------------------------"+e);
			e.printStackTrace();
		}//寄件坐标
		if(revMap!=null){		
			order.setRevLongitude(revMap.get("lng"));//经度
			order.setRevLatitude(revMap.get("lat"));//纬度
			try {
				System.out.println(order);
				orderInfoService.updateRevLocation(order);
			} catch (SQLException e) {
				System.out.println("更新收件地址失败"+e);
			}
		}		

		System.out.println("--------------------------------------请求寄件地址坐标线程结束------------------------------------");
	}	
}
/***
 * 向高德请求发件地址坐标
 * @author Administrator
 *
 */
class SendThread implements Runnable{
	String addr;
	String orderNo;
	OrderInfoService orderInfoService;
	String uid;
	DynamicDataSourceHolder dynamicDataSourceHolder ;
	public SendThread(String addr,OrderInfoService orderInfoService,String orderNo,String uid,DynamicDataSourceHolder dynamicDataSourceHolder){
		this.addr=addr;
		this.orderInfoService=orderInfoService;
		this.orderNo=orderNo;
		this.uid=uid;
		this.dynamicDataSourceHolder  = dynamicDataSourceHolder ;
	}
	@Override
	public void run() {
		System.out.println("--------------------------------------请求发件地址坐标线程启动------------------------------------");
		dynamicDataSourceHolder.setDataSource(uid.substring(0,uid.indexOf("_")) );
		OrderInfo order  = new OrderInfo();
		order.setOrderNo(orderNo);
		Map<String, String> sendMap=null;
		try {
			sendMap = AmapUtil.addressToGPS(addr);
		} catch (Exception e) {
			System.out.println("-----------------------获取坐标失败------------------------------------------"+e);
			e.printStackTrace();
		}//发件坐标
		if(sendMap!=null){
			order.setSendLongitude(sendMap.get("lng"));//经度
			order.setSendLatitude(sendMap.get("lat"));//纬度
			try {
				orderInfoService.updateSendLocation(order);
			} catch (SQLException e) {
				System.out.println("更新寄件地址失败"+e);
			}
		}
		System.out.println("--------------------------------------请求发件地址坐标线程启动------------------------------------");
	}		
}
/***
 * 想一米鲜发送推送信息
 * @author Administrator
 *
 */
class YMXRquest implements Runnable{
	String URL;
	String status;//状态
	String orderNo;//单号
	String realName;//操作员姓名
	String phone;//操作员号码
	String info;//快件信息
	String operateTime;//操作时间
	String uid;

	public YMXRquest(String url,String status,	String orderNo,String realName,	String phone,String info,String operateTime,String uid){
		this.URL=url;
		this.status=status;
		this.orderNo=orderNo;
		this.realName=realName;
		this.phone=phone;
		this.info=info;
		this.operateTime= operateTime;
		this.uid=uid;
	}
	@Override
	public void run() {
		System.out.println("--------------------------------------向一米鲜推送消息线程启动------------------------------------");
		String nowdate = DateUtils.formatDate(new Date(),"yyyy-MM-dd HH:mm:ss");
		StringBuffer str = new StringBuffer();
		str.append("user_key=yixiang");
		str.append("&token=912601c3d7f7b7c4cf62333cae121765");
		str.append("&order_id="+orderNo);						
		str.append("&state="+status);
		str.append("&courier="+realName);
		str.append("&courier_mobile="+phone);
		str.append("&operate_time="+operateTime );
		str.append("&info="+info);
		str.append("&request_time ="+nowdate);
		System.out.println(str.toString());

		try {
			InputStream is =HttpUtils.getSoapInputStream(URL, str.toString());		//http://japi.zto.cn/zto/api_utf8/commonOrder
			ByteArrayOutputStream   baos   =   new   ByteArrayOutputStream(); 
			int  h=-1; 

			while((h=is.read())!=-1){ 
				baos.write(h); 	
			}
			String responseStr=baos.toString(); 
			System.out.println("response==========="+responseStr);
		} catch (IOException e) {

			e.printStackTrace();
		} 	

		System.out.println("--------------------------------------向一米鲜推送消息线程结束------------------------------------");
	}	
}
/***
 * 康美药业信息推送
 * @author Administrator
 *
 */
class KangmeiPush  implements Runnable{

	
    String URL;
	String company_num;
	String userName;
	String orderNo;
	String context;
	String status;
	
	public KangmeiPush(String URL,String company_num,String userName,String orderNo,String context,String status){
		this.URL=URL;
		this.company_num=company_num;
		this.userName=userName;
		this.orderNo=orderNo;
		this.context=context;
		this.status=status;		
	}
	@Override
	public void run() {
		System.out.println("-------------------------------------康美药业信息推送线程启动------------------------------------");
	


		Date date = new Date();
		String key = date.getTime()+"";
		Map<String,Object> map = new HashMap<String,Object>();
		Map<String,Object> head = new HashMap<String,Object>();
		head.put("company_num", company_num);
		head.put("key", key);
		head.put("sign",Md5.md5Str("updateOrderTrack"+key+Md5.md5Str(userName).toLowerCase()).toUpperCase());
		map.put("head", head);
		List<Map<String,Object>>  dataList = new ArrayList<Map<String,Object>>();

		Map<String,Object> dateMap = new HashMap<String,Object>();
		dateMap.put("complete", status);
		dateMap.put("current_time",DateUtils.formatDate(date, "yyyy-MM-dd HH:mm:ss"));
		dateMap.put("order_id",orderNo);
		dateMap.put("logis_num",orderNo);
		dateMap.put("addr_info",context);
		dataList.add(dateMap);
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("list", dataList);
		map.put("data", data);	
		System.out.println("URL==="+URL);
		System.out.println("company_num==="+company_num);
		System.out.println("userName==="+userName);
		try{
		String json = JsonUtil.toJson(map);	
	
		String resultStr = 	HttpUtils.postJson(URL, json, 60000, "utf-8");

		System.out.println("resultStr================="+resultStr);
		Map<String,Object> resultMap = JsonUtil.getMapFromJson(resultStr);
		System.out.println(resultMap.get("description"));
		
		}catch(Exception e){
			e.printStackTrace();
		}
	
		
		System.out.println("-------------------------------------康美药业信息推送消息线程结束------------------------------------");
	}	
}
/**
 * 微信推送线程
 * 
 * @author Administrator
 *
 */
class weixinPush implements Runnable{
	OrderInfo order;
	User userInfo;
	int with;
	public weixinPush(OrderInfo order,User userInfo,int with){
		this.order=order;
		this.userInfo=userInfo;
		this.with=with;
	}

	@Override
	public void run() {
		System.out.println("----------------------------微信信息推送线程启动--------------------------------------");
		switch (with) {
		case 1:			
			StringBuffer strBuf  = new StringBuffer();
			strBuf.append("touser="+order.getWxOpenid());
			strBuf.append("&template_id=LTE8bPpcQvN6cLg884AWMMEdoCcNFTvhBGSHO7EWqso");
			strBuf.append("&templateType=1");
			strBuf.append("&number="+order.getOrderNo());
			strBuf.append("&statusName=最新状态");
			strBuf.append("&remark="+"订单"+order.getOrderNo()+"已被快递员:"+userInfo.getRealName() + userInfo.getCourierNo()+"接单，请耐心等待，保持电话畅通");
			strBuf.append("&frist=快递员接单通知");
			strBuf.append("&appid=wx9bad8d049d6b43dd");
			try {
				InputStream is= HttpUtils.getSoapInputStream("http://183.62.232.130:6080/sendMessage/orderNotice", strBuf.toString());	//发送推送信息	
				ByteArrayOutputStream   baos   =   new   ByteArrayOutputStream(); 
				int   i=-1;	
				while((i=is.read())!=-1){ 
					baos.write(i); 
				}
				String str=baos.toString(); 
				System.out.println("response==========="+str);
			} catch (IOException e) {
				e.printStackTrace();
			} 		
			break;
		case 2:
			StringBuffer strBuf1  = new StringBuffer();
			strBuf1.append("touser="+order.getWxOpenid());
			strBuf1.append("&template_id=LTE8bPpcQvN6cLg884AWMMEdoCcNFTvhBGSHO7EWqso");
			strBuf1.append("&templateType=1");
			strBuf1.append("&number="+order.getOrderNo());
			strBuf1.append("&statusName=最新状态");
			strBuf1.append("&remark="+"订单"+order.getOrderNo()+"已被快递员:"+userInfo.getRealName() + userInfo.getCourierNo()+"接收，请耐心等待，保持电话畅通");
			strBuf1.append("&frist=快递员接单通知");
			strBuf1.append("&appid=wx9bad8d049d6b43dd");
			System.out.println(strBuf1.toString());
			try{
				InputStream is= HttpUtils.getSoapInputStream("http://183.62.232.130:6080/sendMessage/orderNotice", strBuf1.toString());	//发送推送信息	
				ByteArrayOutputStream   baos   =   new   ByteArrayOutputStream(); 
				int  j=-1; 
				while((j=is.read())!=-1){ 
					baos.write(j); 
				} 
				String str=baos.toString(); 
				System.out.println("response==========="+str);
			}catch (IOException e) {
				e.printStackTrace();
			} 			
			break;
		case 3:
			StringBuffer strBuf2  = new StringBuffer();
			strBuf2.append("touser="+order.getWxOpenid());
			strBuf2.append("&template_id=LTE8bPpcQvN6cLg884AWMMEdoCcNFTvhBGSHO7EWqso");
			strBuf2.append("&templateType=1");
			strBuf2.append("&number="+order.getOrderNo());
			strBuf2.append("&statusName=最新状态");
			strBuf2.append("&remark="+"订单"+order.getOrderNo()+"快递员:"+userInfo.getRealName() + userInfo.getCourierNo()+"已取消接单");
			strBuf2.append("&frist=快递员取消接单通知");
			strBuf2.append("&appid=wx9bad8d049d6b43dd");
			try{
				InputStream is= HttpUtils.getSoapInputStream("http://183.62.232.130:6080/sendMessage/orderNotice", strBuf2.toString());	//发送推送信息	
				ByteArrayOutputStream   baos   =   new   ByteArrayOutputStream(); 
				int   i=-1; 
				while((i=is.read())!=-1){ 
					baos.write(i); 
				} 
				String str=baos.toString(); 
				System.out.println("response==========="+str);
			}catch (IOException e) {
				e.printStackTrace();
			} 		

			break;	
		case 4:

			StringBuffer strBuf3 = new StringBuffer();
			strBuf3.append("touser="+order.getWxOpenid());
			strBuf3.append("&template_id=b0tc3IWLvQGiLj3ReDbaOvJZI4sD_9lzPP3F1Aq5jrc");
			strBuf3.append("&templateType=3");
			strBuf3.append("&number="+order.getLgcOrderNo());
			strBuf3.append("&statusName=最新状态");
			strBuf3.append("&remark="+"运单"+order.getOrderNo()+"  快递员:"+userInfo.getRealName() + userInfo.getCourierNo()+"取件成功");
			strBuf3.append("&frist=快递员取件通知");
			strBuf3.append("&appid=wx9bad8d049d6b43dd");
			try{
				InputStream is= HttpUtils.getSoapInputStream("http://183.62.232.130:6080/sendMessage/orderNotice", strBuf3.toString());	//发送推送信息	
				ByteArrayOutputStream   baos   =   new   ByteArrayOutputStream(); 
				int   i=-1; 
				while((i=is.read())!=-1){ 
					baos.write(i); 
				} 
				String str=baos.toString(); 
				System.out.println("response==========="+str);
			}catch (IOException e) {
				e.printStackTrace();
			} 	
			break;
		case 5:
			StringBuffer strBuf4  = new StringBuffer();
			strBuf4.append("touser="+order.getWxOpenid());
			strBuf4.append("&template_id=dp48ZvD8xlRkWiVHgVm0N8VTA0JuyGy8vaXg7D3024g");
			strBuf4.append("&templateType=2");
			strBuf4.append("&number="+order.getLgcOrderNo());
			strBuf4.append("&statusName=最新状态");
			strBuf4.append("&remark="+"运单"+order.getOrderNo()+"快递员:"+userInfo.getRealName() + userInfo.getCourierNo()+"揽件成功，即将派送，请保持电话畅通");
			strBuf4.append("&frist=快递员派件件通知");
			strBuf4.append("&appid=wx9bad8d049d6b43dd");
			try{
				InputStream is= HttpUtils.getSoapInputStream("http://183.62.232.130:6080/sendMessage/orderNotice", strBuf4.toString());	//发送推送信息	
				ByteArrayOutputStream   baos   =   new   ByteArrayOutputStream(); 
				int  j =-1; 
				while((j=is.read())!=-1){ 
					baos.write(j); 
				} 
				String str=baos.toString(); 
				System.out.println("response==========="+str);
			}catch (Exception e) {
				e.printStackTrace();
			}			
			break;	
		case 6:
			StringBuffer strBuf5  = new StringBuffer();
			strBuf5.append("touser="+order.getWxOpenid());
			strBuf5.append("&template_id=KZIR04DnPKWVK7fl8eHgm_Ms0g-PHZocVzVFNu5GY9I");
			strBuf5.append("&templateType=6");
			strBuf5.append("&number="+order.getLgcOrderNo());
			strBuf5.append("&statusName=签收通知");
			strBuf5.append("&remark="+"运单"+order.getOrderNo()+"于"+order.getSendOrderTime()+"被拒签！请确认是否为本人操作" );
			strBuf5.append("&frist=签收通知");
			strBuf5.append("&appid=wx9bad8d049d6b43dd");
			try{
				InputStream is= HttpUtils.getSoapInputStream("http://183.62.232.130:6080/sendMessage/orderNotice", strBuf5.toString());	//发送推送信息	
				ByteArrayOutputStream   baos   =   new   ByteArrayOutputStream(); 
				int  j =-1; 
				while((j=is.read())!=-1){ 
					baos.write(j); 
				} 
				String str=baos.toString(); 
				System.out.println("response==========="+str);
			}catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case 7:
			StringBuffer strBuf6  = new StringBuffer();
			strBuf6.append("touser="+order.getWxOpenid());
			strBuf6.append("&template_id=KZIR04DnPKWVK7fl8eHgm_Ms0g-PHZocVzVFNu5GY9I");
			strBuf6.append("&templateType=6");
			strBuf6.append("&number="+order.getLgcOrderNo());
			strBuf6.append("&statusName=签收通知");
			strBuf6.append("&remark="+"运单"+order.getOrderNo()+"于"+order.getSendOrderTime()+"被签收！请确认是否为本人操作" );
			strBuf6.append("&frist=签收通知");
			strBuf6.append("&appid=wx9bad8d049d6b43dd");
			try{
				InputStream is= HttpUtils.getSoapInputStream("http://183.62.232.130:6080/sendMessage/orderNotice", strBuf6.toString());	//发送推送信息	

				ByteArrayOutputStream   baos   =   new   ByteArrayOutputStream(); 
				int  j =-1; 
				while((j=is.read())!=-1){ 
					baos.write(j); 
				} 
				String str=baos.toString(); 
				System.out.println("response==========="+str);
			}catch (Exception e) {
				e.printStackTrace();
			}
			break;
		default:
			System.out.println("推送信息有误");
			break;
		}

		System.out.println("----------------------------微信信息推送线程结束--------------------------------------");
	}
		
}
/***
 * 快客短信推送
 * @author Administrator
 *
 */
class MessagePush  implements Runnable{	
		String lgcNo;
		String phone;
		String context;
		String channel ;
			
	public MessagePush(String channel,String lgcNo,String phone,String context){
		this.lgcNo=lgcNo;
		this.phone=phone;
		this.context= context;
		this.channel= channel ;
	
	}
	@Override
	public void run() {
		System.out.println("-------------------------------------快客短信信息推送线程启动------------------------------------");
		try{
			
		
		Date date = new Date();
		String key = date.getTime()+"";
//		String content ="您好！欢迎您使用快客同城速配，现有您一件同城货物已收件，单号"+lgcOrderNo+".请您电话保持畅通，我们将及时为您配送。下单、查询、投诉建议请关注微信公众号“快刻同城速配”，服务热线：0592－7127770【快递王子】"; 
	
		String check = SHA.SHA1Encode1(phone+"yogapayHFT"+context+"PTSD");
		
		StringBuffer   bufStr = new StringBuffer();
		bufStr.append("target="+phone);
		bufStr.append("&content="+context);
		bufStr.append("&operation=S");
		bufStr.append("&note.businessCode=PTSD");
		bufStr.append("&note.usage=验证码");
		bufStr.append("&check="+check);
		//bufStr.append("&channel=szkyt");
		bufStr.append("&channel="+channel);
		bufStr.append("&lgcNo="+lgcNo);
	
		InputStream resultStr = 	HttpUtils.getSoapInputStream("http://message.yogapay.com/message/post", bufStr.toString());
		
		ByteArrayOutputStream   baos   =   new   ByteArrayOutputStream(); 
		int  h=-1; 

		while((h=resultStr.read())!=-1){ 
			baos.write(h); 	
		}
		String responseStr=baos.toString(); 
	
		System.out.println("response==========="+responseStr);
		Map<String,Object> map = JsonUtil.getMapFromJson(responseStr);
		System.out.println("--------------------------------"+map.get("rescode")+"---------------------------------------");	
		}catch(Exception e){
			e.printStackTrace();
		}
	
		
		System.out.println("--------------------------------------快客短信信息推送线程结束------------------------------------");
	}	
}
