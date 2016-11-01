package com.yogapay.couriertsi.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.druid.support.json.JSONUtils;
import com.yogapay.couriertsi.dataSource.DynamicDataSourceHolder;
import com.yogapay.couriertsi.domain.OrderTrack;
import com.yogapay.couriertsi.domain.RunnableUtils;
import com.yogapay.couriertsi.domain.User;
import com.yogapay.couriertsi.dto.OrderDto;
import com.yogapay.couriertsi.services.OrderInfoService;
import com.yogapay.couriertsi.services.OrderTrackService;
import com.yogapay.couriertsi.services.UserService;
import com.yogapay.couriertsi.utils.AmapUtil;
import com.yogapay.couriertsi.utils.DateUtils;
import com.yogapay.couriertsi.utils.JsonUtil;
import com.yogapay.couriertsi.utils.StringUtil;
import com.yogapay.couriertsi.utils.http.HttpUtils;

@Controller
@RequestMapping(value = "/addOrder",method=RequestMethod.POST)
@Scope("prototype")
public class YMXSocketApi {

	@Resource
	private OrderInfoService orderInfoService;
	@Resource
	private UserService userService;
	@Resource
	private OrderTrackService orderTrackService;
	@Resource
	private DynamicDataSourceHolder dynamicDataSourceHolder ;
	/**一米鲜下单
	 * 
	 * @param params
	 * @param request
	 * @param response
	 * @throws Exception 
	 */
	@RequestMapping(value = "/nwOrder")
	public void signRegist(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		System.out.println("一米鲜下单");
		Map<String,Object> map1;
		Map<String,Object> map2;
		String user_key = 	request.getParameter("user_key");//账户名
		System.out.println("user_key============="+user_key);
		String token = 	request.getParameter("token");//理解为密码
		System.out.println("token============="+token);
		String order_id  = 	request.getParameter("order_id");//⼀一⽶米鲜订单号
		System.out.println("order_id============="+order_id);
		String express_no   = 	request.getParameter("express_no");//⼀一⽶米鲜订单号
		String name   = 	request.getParameter("name");//收件⼈人
		String mobile   = 	request.getParameter("mobile");//收件⼈人电话
		String address   = 	request.getParameter("address");//收件⼈人地址
		String send_name   = 	request.getParameter("send_name");//发件⼈人
		String send_mobile   = 	request.getParameter("send_mobile");//发件⼈电话
		String send_address   = 	request.getParameter("send_address");//发件人地址
		float total_price   = Float.valueOf(request.getParameter("total_price"));//订单总价
		String receiving_code   = 	request.getParameter("receiving_code");//收货码
		if(!StringUtil.isEmptyWithTrim(request.getParameter("expected_time"))){
			Date expected_time    = DateUtils.parseDate(request.getParameter("expected_time"));//期望送达时间
		}
		if(!StringUtil.isEmptyWithTrim(request.getParameter("latest_time"))){
			Date latest_time    = DateUtils.parseDate(request.getParameter("latest_time"));//最晚送达时间
		}
		String province    = (request.getParameter("province"));//省
		String city    = (request.getParameter("city"));//市
		if(!StringUtil.isEmptyWithTrim(request.getParameter("request_time"))){
			Date request_time    = DateUtils.parseDate(request.getParameter("request_time"));//接⼝口请求时间
		}		
		String  memo    = request.getParameter("memo");//备注/⽤用户留⾔言
		String  callback    = request.getParameter("callback");//接⼝口回调地址 配送状态变更
		System.out.println("callback============="+callback);
		String  station_code    = request.getParameter("station_code");//站点代码
		String  station_name     = request.getParameter("station_name");//站点名称
		if(!user_key.equals("1mxian")){
			System.out.println("user_key输入错误");
			map1 = new HashMap<String,Object>();
			map2 = new HashMap<String,Object>();
			map2.put("express_no", express_no);
			map1.put("status", "1");
			map1.put("message", "user_key输入错误");
			map1.put("data", map2);
			String jsonStr = JSONUtils.toJSONString(map1);
			System.out.println("返回JSON字符串============"+jsonStr);
			response.setCharacterEncoding("UTF-8");  
			response.setContentType("application/json; charset=utf-8");  
			PrintWriter out = null;  
			try {  
				out = response.getWriter();  
				out.append(jsonStr);  		      
			} catch (IOException e) {  
				e.printStackTrace();  
			} finally {  
				if (out != null) {  
					out.close();  
				}  
			}  
			return;
		}

		if(!token.equals("00000000000000000000000000000000")){
			System.out.println("token输入错误");
			map1 = new HashMap<String,Object>();
			map2 = new HashMap<String,Object>();
			map2.put("express_no", express_no);
			map1.put("status", "1");
			map1.put("message", "token密码输入错误");
			map1.put("data", map2);
			String jsonStr = JSONUtils.toJSONString(map1);
			System.out.println("返回JSON字符串============"+jsonStr);
			response.setCharacterEncoding("UTF-8");  
			response.setContentType("application/json; charset=utf-8");  
			PrintWriter out = null;  
			try {  
				out = response.getWriter();  
				out.append(jsonStr);  		      
			} catch (IOException e) {  
				e.printStackTrace();  
			} finally {  
				if (out != null) {  
					out.close();  
				}  
			} 
			return ;
		}


		OrderDto order = orderInfoService.getOrderByOrderNo(order_id);//查询订单是否存在
		if(order!=null){
			System.out.println("订单号重复");
			map1 = new HashMap<String,Object>();
			map2 = new HashMap<String,Object>();
			map2.put("express_no", order.getLgcOrderNo());
			map1.put("status", "1");
			map1.put("message", "订单号"+order_id+"已存在,请勿重复下单");
			map1.put("data", map2);
			String jsonStr = JSONUtils.toJSONString(map1);
			System.out.println("返回JSON字符串============"+jsonStr);
			response.setCharacterEncoding("UTF-8");  
			response.setContentType("application/json; charset=utf-8");  
			PrintWriter out = null;  
			try {  
				out = response.getWriter();  
				out.append(jsonStr);  		      
			} catch (IOException e) {  
				e.printStackTrace();  
			} finally {  
				if (out != null) {  
					out.close();  
				}  
			} 
			return ;
		}

		OrderDto newOrderInfo = new OrderDto();
		newOrderInfo.setOrderNo(order_id);
		newOrderInfo.setRevName(name);
		newOrderInfo.setRevPhone(mobile);
		newOrderInfo.setRevArea(address);
		newOrderInfo.setRevAddr(address);

		newOrderInfo.setSendName(send_name);
		newOrderInfo.setSendPhone(send_mobile );
		newOrderInfo.setSendArea(send_address);
		newOrderInfo.setSendAddr(send_address);
		newOrderInfo.setSendAddr(send_address);

		newOrderInfo.setCod(1);
		newOrderInfo.setGoodPrice(total_price);
		newOrderInfo.setSource("YMX");//来源 一米鲜
		newOrderInfo.setOrderNote(callback);//推送地址
		
		
	/**
	 * 地址坐标
	 */
		String sendAddr = send_address;//发件地址
		String revAddr = address ;//收件地址
		
		orderInfoService.	saveYMXOrder(newOrderInfo);//保存下单数据
  
		RunnableUtils run = new RunnableUtils();
		run.queryLocation(sendAddr, 1, order_id,orderInfoService,"yx",dynamicDataSourceHolder);
		run.queryLocation(revAddr,2,  order_id,orderInfoService,"yx",dynamicDataSourceHolder);
		
		
		
		map1 = new HashMap<String,Object>();
		map2 = new HashMap<String,Object>();
		map2.put("express_no", order_id);
		map1.put("status", "0");
		map1.put("message", "下单成功");
		map1.put("data", map2);
		String jsonStr = JSONUtils.toJSONString(map1);
		response.setCharacterEncoding("UTF-8");  
		response.setContentType("application/json; charset=utf-8");  
		System.out.println("返回JSON字符串============"+jsonStr);
		PrintWriter out = null;  
		try {  
			out = response.getWriter();  
			out.append(jsonStr);  		      
		
		} catch (IOException e) {  
			e.printStackTrace();  
		} finally {  
			if (out != null) {  
				out.close();  
			}  
		} 
		return ;

	}


	/**一米鲜订单查询
	 * 
	 * @param params
	 * @param request
	 * @param response
	 * @throws SQLException
	 */
	@RequestMapping(value = "/queryOrder")
	public void queryOrder(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) throws SQLException {
		System.out.println("米鲜订单查询");
		Date nowDate = new Date();
		Map<String,Object> map1;
		Map<String,Object> map2;
		String user_key = 	request.getParameter("user_key");//账户名
		System.out.println("user_key============="+user_key);
		String token = 	request.getParameter("token");//理解为密码
		System.out.println("token============="+token);
		String order_id  = 	request.getParameter("order_id");//⼀一⽶米鲜订单号
		System.out.println("order_id============="+order_id);
		String express_no   = 	request.getParameter("express_no");//⼀一⽶米鲜订单号
		if(!StringUtil.isEmptyWithTrim(request.getParameter("request_time"))){
			Date request_time    = DateUtils.parseDate(request.getParameter("request_time"));//接⼝口请求时间
		}		

		if(!user_key.equals("1mxian")){
			map1 = new HashMap<String,Object>();
			map2 = new HashMap<String,Object>();
			map2.put("express_no", express_no);
			map1.put("status", "1");
			map1.put("message", "user_key输入错误");
			map1.put("data", map2);
			String jsonStr = JSONUtils.toJSONString(map1);
			System.out.println("返回JSON字符串============"+jsonStr);
			response.setCharacterEncoding("UTF-8");  
			response.setContentType("application/json; charset=utf-8");  
			PrintWriter out = null;  
			try {  
				out = response.getWriter();  
				out.append(jsonStr);  		      
			} catch (IOException e) {  
				e.printStackTrace();  
			} finally {  
				if (out != null) {  
					out.close();  
				}  
			}  
			return;
		}

		if(!token.equals("00000000000000000000000000000000")){
			map1 = new HashMap<String,Object>();
			map2 = new HashMap<String,Object>();
			map2.put("express_no", express_no);
			map1.put("status", "1");
			map1.put("message", "token密码输入错误");
			map1.put("data", map2);
			String jsonStr = JSONUtils.toJSONString(map1);
			System.out.println("返回JSON字符串============"+jsonStr);
			response.setCharacterEncoding("UTF-8");  
			response.setContentType("application/json; charset=utf-8");  
			PrintWriter out = null;  
			try {  
				out = response.getWriter();  
				out.append(jsonStr);  		      
			} catch (IOException e) {  
				e.printStackTrace();  
			} finally {  
				if (out != null) {  
					out.close();  
				}  
			} 
			return ;
		}


		OrderDto order = orderInfoService.getOrderByOrderNo(order_id);//查询订单是否存在
		if(order==null){
			map1 = new HashMap<String,Object>();
			map2 = new HashMap<String,Object>();
			map2.put("express_no", order.getLgcOrderNo());
			map1.put("status", "1");
			map1.put("message", "订单号"+order_id+"不存在,请重新输入");
			map1.put("data", map2);
			String jsonStr = JSONUtils.toJSONString(map1);
			System.out.println("返回JSON字符串============"+jsonStr);
			response.setCharacterEncoding("UTF-8");  
			response.setContentType("application/json; charset=utf-8");  
			PrintWriter out = null;  
			try {  
				out = response.getWriter();  
				out.append(jsonStr);  		      
			} catch (IOException e) {  
				e.printStackTrace();  
			} finally {  
				if (out != null) {  
					out.close();  
				}  
			} 
			return ;
		}
		map1 = new HashMap<String,Object>();
		map2 = new HashMap<String,Object>();
		List<Map<String,Object>> mapList  = new ArrayList<Map<String,Object>>();
			if(order.getStatus()==2){//运单状态 1 配送中 2 已送达 4 拒收 8 未送达
			map2.put("state", 1);
		}else if(order.getStatus()==3){
			map2.put("state", 2);
		}else  if(order.getStatus()==4){
			map2.put("state", 4);
		}else{
			map2.put("state", 8);
		}
		map2.put("order_id", order.getOrderNo());
		
		OrderTrack orderTrack = orderTrackService.getLastOrderTrack(order_id);
				
		if(StringUtil.isEmptyWithTrim(order.getSendCourierNo())){			
			User userInfo = userService.getUserByNo(order.getTakeCourierNo());
			map2.put("courier", userInfo.getRealName());
			map2.put("courier_mobile", userInfo.getPhone());
			map2.put("operate_time ",order.getTakeOrderTime());
			map2.put("info ",orderTrack.getContext());	
			map2.put("request_time ",nowDate);
		}else{
			User userInfo = userService.getUserByNo(order.getSendCourierNo());
			map2.put("courier", userInfo.getRealName());
			map2.put("courier_mobile", userInfo.getPhone());
			map2.put("operate_time ",order.getSendOrderTime());
			map2.put("info ",orderTrack.getContext());	
			map2.put("request_time ",nowDate);
		}
		mapList.add(map2);
		map1.put("status", "0");
		map1.put("message", "成功");
		map1.put("data", mapList);
		String jsonStr = JSONUtils.toJSONString(map1);
		System.out.println("返回JSON字符串============"+jsonStr);
		response.setCharacterEncoding("UTF-8");  
		response.setContentType("application/json; charset=utf-8");  
		PrintWriter out = null;  
		try {  
			out = response.getWriter();  
			out.append(jsonStr);  		      
		} catch (IOException e) {  
			e.printStackTrace();  
		} finally {  
			if (out != null) {  
				out.close();  
			}  
		} 
		return ;
	}


	public static void main(String[] args) {
		Date nowDate  = new Date();
		String noew = DateUtils.formatDate(nowDate,"HH-mm-ss");
				String nosew = DateUtils.formatDate(nowDate,"hh-mm-ss");
		System.out.println(noew);

		
//		Map<String,String> values = new HashMap<String,String>();
		StringBuffer str = new StringBuffer();
		str.append("user_key=1mxian");
		str.append("&token=00000000000000000000000000000000");
		str.append("&order_id=YMX0001322535");
		str.append("&state=1");
		str.append("&courier=11111");
		str.append("&courier_mobile=11111");
		str.append("&station_code =11111");
		str.append("&station_name =11111");
		str.append("&operate_time="+DateUtils.formatDate(nowDate,"yyyy-MM-dd HH:mm:ss"));
		str.append("&info=11111");
		str.append("&request_time ="+DateUtils.formatDate(nowDate,"yyyy-MM-dd HH:mm:ss"));
//		String paramsJsonStr=JsonUtil.toJson(values);
		Map<String, Object> map=null;
		try {
			InputStream is =HttpUtils.getSoapInputStream("http://localhost:6044/addOrder/pushInfo", "");		//http://japi.zto.cn/zto/api_utf8/commonOrder
			ByteArrayOutputStream   baos   =   new   ByteArrayOutputStream(); 
			int   i=-1; 

			while((i=is.read())!=-1){ 
				baos.write(i); 	
			}
			String response=baos.toString(); 
			System.out.println("response==========="+response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
