package com.yogapay.couriertsi.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yogapay.couriertsi.domain.OrderInfo;
import com.yogapay.couriertsi.domain.User;
import com.yogapay.couriertsi.services.LgcService;
import com.yogapay.couriertsi.services.MsgService;
import com.yogapay.couriertsi.services.MuserService;
import com.yogapay.couriertsi.services.OrderInfoService;
import com.yogapay.couriertsi.services.OrderPicService;
import com.yogapay.couriertsi.services.OrderTrackService;
import com.yogapay.couriertsi.services.PosInfoService;
import com.yogapay.couriertsi.services.UserService;
import com.yogapay.couriertsi.utils.CommonResponse;
import com.yogapay.couriertsi.utils.DateUtils;
import com.yogapay.couriertsi.utils.JsonUtil;
import com.yogapay.couriertsi.utils.Md5;
import com.yogapay.couriertsi.utils.ValidateUtil;
import com.yogapay.couriertsi.utils.http.HttpUtils;

@Controller
@RequestMapping(value = "/pay")
@Scope("prototype")
public class PayApi  extends BaseApi{
	@Resource
	private OrderInfoService orderInfoService ;
	@Resource
	private OrderPicService orderPicService ;
	@Resource
	private UserService userService ;
	@Resource
	private MsgService msgService ;
	@Resource
	private PosInfoService posInfoService ;
	@Resource
	private OrderTrackService orderTrackService ;
	@Resource
	private LgcService lgcService ;
	@Resource private MuserService muserService ;


	//微信支付
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/weixin")
	public void weixin(@RequestParam Map<String, String> params,HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(new String[] {"orderNo" ,"money","payType","payStatus"}), true, userSessionService,checkVersion,appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String payStatus = params.get("payStatus");//ORDER订单支付 HUIYUAN会员充值
				//String URL="http://couturier3-2.yogapay.com/pay/weixinzf?";
				String URL = new URL(request.getRequestURL().toString()).toURI().resolve("weixinzf").toURL().toString()+"?";
				System.out.println(URL);
				/**
				 * 运单支付
				 */
				if("ORDER".equals(payStatus.trim())){

					String orderNo = params.get("orderNo");				
					float flo =  Float.valueOf(params.get("money"));
					String payType = params.get("payType");//0,1为收件 2为派件 3为会员功能参数(ios收件时微信支付传值为0，bug补丁增加0判断)
					flo= flo*100;	
					String lgcNo = userService.getUserLgcNo(ret.get("userNo")) ;
					OrderInfo orderInfo = orderInfoService.	getByLgcOrderNo(orderNo,lgcNo);
					if("1".equals(payType)||"0".equals(payType)){								
						if(orderInfo!=null){
							render(JSON_TYPE, CommonResponse.respFailJson("9040", "当前运单号已存在", params.get("reqNo")),response);
							return;
						}				
					}else{
						if(orderInfo==null){
							render(JSON_TYPE, CommonResponse.respFailJson("9041", "当前运单号不存在", params.get("reqNo")),response);
							return;
						}		
					}
					Map<String,Object> map1 = new TreeMap<String,Object>();
					map1.put("uid", params.get("uid"));
					map1.put("body", "快递费");
					map1.put("trade_no", orderNo);
					map1.put("total_fee", (int)flo);
					String strMd5 = Md5.md5Str(StringUtils.join(map1.values(),"")+"BdTsvhRmCS3yPKnsnjT9hERlCNHCanmC");
					map1.put("sign", strMd5);
					StringBuffer buffer = new StringBuffer();
					buffer.append("uid="+map1.get("uid"));
					buffer.append("&body="+ URLEncoder.encode(map1.get("body").toString(), "utf-8"));
					buffer.append("&trade_no="+map1.get("trade_no"));
					buffer.append("&total_fee="+map1.get("total_fee"));
					buffer.append("&sign="+map1.get("sign"));
					model = new HashMap<String,Object>();
					model.put("address",URL+buffer.toString());
					render(JSON_TYPE, CommonResponse.respSuccessJson("",model, params.get("reqNo")), response);   	
					/**
					 *会员充值支付
					 */
				}else if("HUIYUAN".equals(payStatus.trim())){	
					float flo =  Float.valueOf(params.get("money"));
					flo= flo*100;	
					String acountNumber = params.get("orderNo");
					Map<String,Object> map1 = new TreeMap<String,Object>();
					map1.put("uid", params.get("uid"));
					map1.put("body", "会员充值");
					map1.put("trade_no", acountNumber);
					map1.put("total_fee", (int)flo);
					String strMd5 = Md5.md5Str(StringUtils.join(map1.values(),"")+"BdTsvhRmCS3yPKnsnjT9hERlCNHCanmC");
					map1.put("sign", strMd5);
					StringBuffer buffer = new StringBuffer();
					buffer.append("uid="+map1.get("uid"));
					buffer.append("&body="+URLEncoder.encode(map1.get("body").toString(), "utf-8"));
					buffer.append("&trade_no="+map1.get("trade_no"));
					buffer.append("&total_fee="+map1.get("total_fee"));
					buffer.append("&sign="+map1.get("sign"));
					model = new HashMap<String,Object>();
					model.put("address", URLEncoder.encode(URL+buffer.toString(), "utf-8"));
					model.put("tradeNo",acountNumber );
					render(JSON_TYPE, CommonResponse.respSuccessJson("",model, params.get("reqNo")), response);   	
					/**
					 * 其他支付有误
					 */
				}else{
					render(JSON_TYPE, CommonResponse.respFailJson(ret.get("respCode"), "订单支付类型有误", params.get("reqNo")),response);
				}


			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE, CommonResponse.respFailJson(ret.get("respCode"), ret.get("respMsg"), params.get("reqNo")),response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE, CommonResponse.respFailJson("9000","服务器异常", params.get("reqNo")), response);
		}
	}
	@SuppressWarnings("static-method")
	@RequestMapping("/weixinzf")
	public Object  weixinzf(@RequestParam Map<String,String> params,HttpServletRequest request,
			HttpServletResponse response,ModelMap modelMap) throws IOException{
		System.out.println(params);
		String trade_no=		request.getParameter("trade_no");
		String total_fee = request.getParameter("total_fee");
		String sign=request.getParameter("sign");
		String uid=request.getParameter("uid");
		String body = request.getParameter("body");
		modelMap.put("uid", uid);
		modelMap.put("body",body);
		modelMap.put("trade_no", trade_no);
		modelMap.put("total_fee", total_fee);
		modelMap.put("sign", sign);
		return "redirect:http://wx.yogapay.com/web/pay/";	
	}
	@RequestMapping(value = "/alipay")
	public void zhifubao(@RequestParam Map<String, String> params,HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(new String[] {"lgcOrderNo" ,"money"}), true, userSessionService,checkVersion,appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String loginNo = ret.get("userNo") ;
				String lgcOrderNo = params.get("lgcOrderNo");
				String flo =  params.get("money");
				String lgcNo = userService.getUserLgcNo(ret.get("userNo")) ;
				OrderInfo orderInfo = orderInfoService.	getByLgcOrderNo(lgcOrderNo,lgcNo);
				if(orderInfo!=null){
					render(JSON_TYPE, CommonResponse.respFailJson(ret.get("respCode"), "当前运单号已存在", params.get("reqNo")),response);
					return;
				}
				model = new HashMap<String,Object>();
				model.put("address", "www.baidu.com");
				render(JSON_TYPE, CommonResponse.respSuccessJson("",model, params.get("reqNo")), response);   		
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE, CommonResponse.respFailJson(ret.get("respCode"), ret.get("respMsg"), params.get("reqNo")),response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE, CommonResponse.respFailJson("9000","服务器异常", params.get("reqNo")), response);
		}
	}
	/**
	 * 
	 * 
	 * 查询微信支付是否完成
	 * @param params
	 * @param request
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/checkWeixinPay")
	public void checkWeixinPay(@RequestParam Map<String, String> params,HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(new String[] {"orderNo","payStatus"} ), true, userSessionService,checkVersion,appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String payStatus = params.get("payStatus");//ORDER订单支付 HUIYUAN会员充值
				String courierNo  = ret.get("userNo");
				Date nowDate = new Date();
				if("ORDER".equals(payStatus.trim())){			
					String OrderNo = params.get("orderNo");			
					Map<String,Object> map = null;
					StringBuffer  strBf = new StringBuffer();
					strBf.append("uid="+params.get("uid"));
					strBf.append("&trade_no="+OrderNo);		
					System.out.println("request======================="+strBf);
					try {
						System.out.println(strBf.toString());
						InputStream is =HttpUtils.getSoapInputStream("http://wx.yogapay.com/web/pay/query", strBf.toString());		//http://japi.zto.cn/zto/api_utf8/commonOrder
						String responseStr=	IOUtils.toString(is, "utf-8");		
						System.out.println("response==========="+responseStr);
						map = JsonUtil.getMapFromJson(responseStr);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					if(map==null){		
						render(JSON_TYPE, CommonResponse.respFailJson("9049", "查询失败,请稍后重试", params.get("reqNo")),response);
						return;
					}else{
						int errerCode = (Integer)map.get("errorCode");
						if(0==errerCode){				
							orderInfoService.payStatus("SUCCESS", params.get("orderNo"));
							render(JSON_TYPE, CommonResponse.respSuccessJsonTO("0", "","支付成功", params.get("reqNo")),response);
							return;
						}else{
							orderInfoService.payStatus("FAIL", OrderNo);
							render(JSON_TYPE, CommonResponse.respFailJson("-10", "未支付成功", params.get("reqNo")),response);
							return;
						}
					}   			
					/**
					 * 会员充值的支付方式
					 */
				}else if("HUIYUAN".equals(payStatus.trim())){
					ValidateUtil.validateRequest(request,reqParams(new String[] {"vipNo","signal"} ), true, userSessionService,checkVersion,appVersionService,dynamicDataSourceHolder);
					if("ADD".equals(params.get("signal"))){
						System.out.println("=============新增会员==================");

						String vipNo = params.get("vipNo");
						String OrderNo = params.get("orderNo");			
						Map<String,Object> map = null;
						StringBuffer  strBf = new StringBuffer();
						strBf.append("uid="+params.get("uid"));
						strBf.append("&trade_no="+OrderNo);		
						System.out.println("request======================="+strBf);
						try {
							System.out.println(strBf.toString());
							InputStream is =HttpUtils.getSoapInputStream("http://wx.yogapay.com/web/pay/query", strBf.toString());		//http://japi.zto.cn/zto/api_utf8/commonOrder

							String responseStr=	IOUtils.toString(is, "utf-8");				
							System.out.println("response==========="+responseStr);
							map = JsonUtil.getMapFromJson(responseStr);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
						if(map==null){		
							render(JSON_TYPE, CommonResponse.respFailJson("9049", "查询失败,请稍后重试", params.get("reqNo")),response);
							return;
						}else{
							int errerCode = (Integer)map.get("errorCode");
							if(0==errerCode){	

								User user = userService.getUserByNo(courierNo);
								Map<String,Object> paramMap = new HashMap<String,Object>();
								paramMap.put("substation_no", user.getSubstationNo());
								paramMap.put("dis_user_no", params.get("vipNo"));
								paramMap.put("dis_type", params.get("privilegeType"));
								paramMap.put("dis_user_name", params.get("vipName"));
								paramMap.put("contact_name", params.get("contactName"));
								paramMap.put("contact_phone", params.get("contactPhone"));
								paramMap.put("email", params.get("email"));
								paramMap.put("pwd", Md5.md5Str(params.get("passWord")));
								paramMap.put("operator", user.getRealName());
								paramMap.put("create_time", DateUtils.formatDate(nowDate));
								paramMap.put("note",params.get("note"));
								paramMap.put("status","1");



								//插入后获取ID号
								long id = userService.addVip(paramMap);			

								paramMap.clear();
								paramMap.put("uid", id);
								paramMap.put("balance",  params.get("money"));
								paramMap.put("last_update_time",  DateUtils.formatDate(nowDate));
								userService.addVipMoney(paramMap);
								userService.updateStausAcount(OrderNo);//会员为流水号
								Map<String,Object> vipInfo = userService.checkVipbInfo(vipNo);//返回会员数据
								model = new HashMap<String,Object>();
								model.put("vipInfo", vipInfo);		
								render(JSON_TYPE,CommonResponse.respSuccessJson("支付成功",model,params.get("reqNo")), response);
								return;
							}else{
								render(JSON_TYPE, CommonResponse.respFailJson("-10", "未支付成功", params.get("reqNo")),response);
								return;
							}
						}   			
					}
					if("REC".equals(params.get("signal"))){					
						System.out.println("=============会员充值==================");

						String vipNo = params.get("vipNo");
						String OrderNo = params.get("orderNo");			
						Map<String,Object> map = null;
						StringBuffer  strBf = new StringBuffer();
						strBf.append("uid="+params.get("uid"));
						strBf.append("&trade_no="+OrderNo);		
						System.out.println("request======================="+strBf);
						try {
							System.out.println(strBf.toString());
							InputStream is =HttpUtils.getSoapInputStream("http://wx.yogapay.com/web/pay/query", strBf.toString());		//http://japi.zto.cn/zto/api_utf8/commonOrder

							String responseStr=	IOUtils.toString(is, "utf-8");				
							System.out.println("response==========="+responseStr);
							map = JsonUtil.getMapFromJson(responseStr);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
						if(map==null){		
							render(JSON_TYPE, CommonResponse.respFailJson(ret.get("respCode"), "查询失败,请稍后重试", params.get("reqNo")),response);
							return;
						}else{
							int errerCode = (Integer)map.get("errorCode");
							if(0==errerCode){									
								String vipPhone = params.get("vipNo");				
								Map<String,Object> vipMap = userService.checkVipbInfo(vipPhone);
								String uid =String.valueOf(vipMap.get("uid"));//会员余额UID
								String privilegeType   =params.get("privilegeType");				
								float sumBalance = Float.valueOf(params.get("money"));//新加金额								
								userService.addBalance( sumBalance,DateUtils.formatDate(nowDate,"yyyy-MM-dd HH:mm:ss"), uid);//保存充值金额
								
								float acountBalance = 	Float.valueOf(userService.queryBalance(uid));
								
								userService.updateVipType(vipPhone, privilegeType);//更改会员等级
								
								Map<String,Object> privilegeMap = userService.checkPrivilegeType(privilegeType);//查询优惠类型
								
								userService.updateStausAcount(sumBalance,acountBalance,OrderNo,(String)privilegeMap.get("discount_text"));//会员为流水号

								Map<String,Object> vipInfo = userService.checkVipbInfo(vipPhone);//返回会员数据
								model = new HashMap<String,Object>();
								model.put("vipInfo", vipInfo);		
								render(JSON_TYPE,CommonResponse.respSuccessJson("支付成功",model,params.get("reqNo")), response);
								return;
							}else{						
								render(JSON_TYPE, CommonResponse.respFailJson("-10", "未支付成功", params.get("reqNo")),response);
								return;
							}
						}  
					}
				}else{
					render(JSON_TYPE, CommonResponse.respFailJson(ret.get("respCode"), "订单支付类型有误", params.get("reqNo")),response);
				}
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE, CommonResponse.respFailJson(ret.get("respCode"), ret.get("respMsg"), params.get("reqNo")),response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE, CommonResponse.respFailJson("9000","服务器异常", params.get("reqNo")), response);
		}
	}


}
