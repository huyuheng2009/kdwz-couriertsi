package com.yogapay.couriertsi.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yogapay.couriertsi.domain.OrderInfo;
import com.yogapay.couriertsi.domain.OrderTrack;
import com.yogapay.couriertsi.domain.PushMsg;
import com.yogapay.couriertsi.domain.RunnableUtils;
import com.yogapay.couriertsi.domain.User;
import com.yogapay.couriertsi.domain.WxxdConfig;
import com.yogapay.couriertsi.enums.MsgType;
import com.yogapay.couriertsi.enums.PayStatus;
import com.yogapay.couriertsi.services.LgcService;
import com.yogapay.couriertsi.services.MsgService;
import com.yogapay.couriertsi.services.MuserService;
import com.yogapay.couriertsi.services.OrderInfoService;
import com.yogapay.couriertsi.services.OrderPicService;
import com.yogapay.couriertsi.services.OrderTrackService;
import com.yogapay.couriertsi.services.OtherService;
import com.yogapay.couriertsi.services.PosInfoService;
import com.yogapay.couriertsi.services.UserService;
import com.yogapay.couriertsi.services.WarehouseService;
import com.yogapay.couriertsi.services.WxxdConfigService;
import com.yogapay.couriertsi.utils.CommonResponse;
import com.yogapay.couriertsi.utils.DateUtils;
import com.yogapay.couriertsi.utils.JsonUtil;
import com.yogapay.couriertsi.utils.MapConverter;
import com.yogapay.couriertsi.utils.PushUtil;
import com.yogapay.couriertsi.utils.StringUtil;
import com.yogapay.couriertsi.utils.StringUtils;
import com.yogapay.couriertsi.utils.ValidateUtil;
import com.yogapay.couriertsi.utils.WeiXinUtil;

@Controller
@RequestMapping(value = "/other")
@Scope("prototype")
public class OtherApi extends BaseApi {

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
	@Resource
	private MuserService muserService ;
	@Resource
	private WarehouseService warehouseService;	
	@Resource
	private OtherService otherService;
	@Value("#{config['yx_cm_track_url']}")
	String URL;
	@Value("#{config['kangmei_company_num']}")
	String company_num;
	@Value("#k{config['angmei_user_name']}")
	String userName;
	@Resource private WxxdConfigService wxxdConfigService ;

	/**
	 *查询代收货款用户
	 * @param params
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/	codSea")
	public void codSea(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(new String[] { "codName"}), true, userSessionService,checkVersion,appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String codName = params.get("codName");
				Map<String,Object> mp =otherService.selectCodBy(codName);
				if(mp==null){
					render(JSON_TYPE, CommonResponse.respFailJson(ret.get("respCode"), "不存在的客户号", params.get("reqNo")),response);	
					return ;
				}
				model = new HashMap<String,Object>();
				model.put("codMap", mp);
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
	 *快速收件扫描
	 * @param params
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/quick")
	public void quick(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(new String[] { "type","no"}), true, userSessionService,checkVersion,appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String userNo = ret.get("userNo") ;
//				String lgcNo = userService.getUserLgcNo(userNo) ;
//				User loginUser = userService.getUserByNo(userNo) ;
				OrderInfo orderInfo = null;
				String no = params.get("no");
				if ("lgc".equals(params.get("type"))) {
					orderInfo = orderInfoService.getByLgcOrderNo(no);
					if (orderInfo == null) {
						render(JSON_TYPE, CommonResponse.respFailJson("9013", "运单号不存在", params.get("reqNo")), response);
						return;
					}
				} else if ("order".equals(params.get("type"))) {
					orderInfo = orderInfoService.getByOrderNo(no);
					if (orderInfo == null) {
						render(JSON_TYPE, CommonResponse.respFailJson("9013", "订单号不存在", params.get("reqNo")), response);
						return;
					}
				} else {
					render(JSON_TYPE, CommonResponse.respFailJson("9013", "单号类型错误", params.get("reqNo")), response);
					return;
				}
				if (1 != orderInfo.getStatus()) {
					render(JSON_TYPE, CommonResponse.respFailJson("9043", "订单号状态有误", params.get("reqNo")), response);
					return;
				}

				render(JSON_TYPE, CommonResponse.respSuccessJson("","可收件", params.get("reqNo")), response);   			
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
	 *快速收件扫描
	 * @param params
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/substation")
	public void substaion(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(new String[] { }), true, userSessionService,checkVersion,appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				setPageInfo(params);	
				Page<Map<String,Object>> pageList = otherService.substaion(params, pageRequest);
				model = new HashMap<String, Object>() ;
				model.put("pageList", pageList.getContent()) ;
				model.put("totalCount", pageList.getTotalElements()) ;
				model.put("cp", pageList.getNumber()+1) ;
				model.put("isLastPage", pageList.isLastPage()) ;
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
	 *未交清单
	 * @param params
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/onOrder")
	public void onOrder(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(new String[] { }), true, userSessionService,checkVersion,appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {

				setPageInfo(params);	

				User loginUser = userService.getUserByNo(ret.get("userNo")) ;
				String lgcNo = userService.getUserLgcNo(ret.get("userNo")) ;
				params.put("userNo", ret.get("userNo"));
				params.put("lgcNo", lgcNo);
				params.put("substation",loginUser.getSubstationNo());

				Page<Map<String, Object>> orderList = otherService.onOrder(params, pageRequest);
				List<Map<String,Object>> order= new ArrayList<Map<String, Object>>();
				for(Map<String,Object> map : orderList.getContent()){
					Map<String,Object> IOmap = warehouseService.getMapByOrder((String)map.get("lgcOrderNo"), "I", "Y");//是否入仓成功
					if(IOmap!=null){
						continue;
					}	
					String sendCourierNo = String.valueOf(map.get("sendCourierNo"));
					String takeCourierNo = String.valueOf(map.get("takeCourierNo"));					
					if(sendCourierNo.equals(takeCourierNo)){
						continue;
					}
					order.add(map);
				}

				model = new HashMap<String, Object>() ;
				model.put("orderList", order) ;
				model.put("totalCount", orderList.getTotalElements()) ;
				model.put("cp", orderList.getNumber()+1) ;
				model.put("isLastPage", orderList.isLastPage()) ;
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
	 *未交清单
	 * @param params
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/scope")
	public void scope(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(new String[] { "addr"}), true, userSessionService,checkVersion,appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {

				String addr = params.get("addr");

				Map<String,Object> map = otherService.scope(addr);
				if(map==null){
					render(JSON_TYPE, CommonResponse.respFailJson("9053","当前地区不支持派送", params.get("reqNo")), response);		 
					return;
				}
				String baddr = String.valueOf(map.get("baddr"));
				String naddr = String.valueOf(map.get("naddr"));
				String baddr1[] = null;
				String naddr1[]=null;
				if(baddr.contains(",")){
					baddr1 = baddr.split(",");
				}else{
					baddr1 = new String[]{baddr};
				}
				if(naddr.contains(",")){
					naddr1 = naddr.split(",");
				}else{
					naddr1 = new String[]{naddr};
				}

				model = new HashMap<String, Object>() ;
				model.put("baddr", baddr1) ;
				model.put("naddr", naddr1) ;		
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
	 *未交清单
	 * @param params
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/scopeArea")
	public void scopeArea(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(new String[] { }), true, userSessionService,checkVersion,appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {

				List<Map<String,Object>> listMap = otherService.scopeArea();

				model = new HashMap<String, Object>() ;
				model.put("listMap", listMap) ;	
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
	 *滞留件前置接口
	 * @param params
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/prepro")
	public void prepro(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(new String[] {"lgcOrderNo" }), true, userSessionService,checkVersion,appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				OrderInfo order  =  orderInfoService.getByLgcOrderNo(params.get("lgcOrderNo"),userService.getUserLgcNo(ret.get("userNo")));
				if(order==null){
					render(JSON_TYPE, CommonResponse.respFailJson("9013","订单不存在", params.get("reqNo")), response);		 
					return;	
				}
				if(order.getStatus()==1 || order.getStatus()==3 ){
					render(JSON_TYPE, CommonResponse.respFailJson("9048","订单状态不合法", params.get("reqNo")), response);		 
					return;	
				}					
				render(JSON_TYPE, CommonResponse.respSuccessJson("","可操作", params.get("reqNo")), response);   
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
	 *滞留件标记
	 * @param params
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/pro")
	public void pro(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(new String[] {"lgcOrderNo" ,"reasonNo"}), true, userSessionService,checkVersion,appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String loginUserNo = ret.get("userNo") ;
				User userInfo = userService.getUserByNo(loginUserNo);//查询快递员信息
				Date nowDate = new Date();
				User loginUser = userService.getUserByNo(loginUserNo) ;
				OrderInfo order  =  orderInfoService.getByLgcOrderNo(params.get("lgcOrderNo"),userService.getUserLgcNo(ret.get("userNo")));
				String reasonNo= params.get("reasonNo");
				String reasonContext = userService.queryReasonContext(reasonNo);//获取问题原因
				String orderNo = order.getOrderNo();
				/**
				 * 物流扭转信息更新
				 * 
				 */
				String cur_no ="";
				String cur_type ="";
				int id = 0;					
				Map<String,Object> firstTrackMap = orderTrackService.checkOrderTrack(orderNo);
				if(firstTrackMap!=null){
					id = (Integer)firstTrackMap.get("id");
					cur_no = (String)firstTrackMap.get("cur_no");//当前流转编号（快递员编号或分站编号）
					cur_type  = (String)firstTrackMap.get("cur_type");//当前编号类型，C为快递员编号，S为分站编号
					orderTrackService.updateIsLast(id);
				}
				String innerNo = loginUser.getInnerNo();
				if(StringUtil.isEmptyWithTrim(innerNo)){
					innerNo ="";
				}else{
					innerNo ="("+innerNo+")。";
				}

				Map<String, Object> sMap = lgcService.getSubstationInfo(loginUser.getSubstationNo()) ;
				OrderTrack track1 = new OrderTrack() ;
				track1.setOrderNo(orderNo);
				track1.setContext(sMap.get("substation_name")+",快递员:"+loginUser.getRealName()+innerNo+"联系方式："+loginUser.getPhone()+"!"+reasonContext);
				track1.setOrderTime(DateUtils.formatDate(nowDate));
				track1.setPreNo(cur_no);
				track1.setPreType(cur_type);
				track1.setCompleted("N");
				track1.setCurNo(loginUserNo);
				track1.setCurType("C");
				track1.setNextNo(loginUser.getSubstationNo());
				track1.setNextType("S");
				track1.setOrderStatus("PRO");
				track1.setParentId(id);
				track1.setIsLast("Y");    //
				track1.setOpname(loginUser.getRealName());
				orderTrackService.add(track1);

				/**
				 *信息推送
				 * */

				PushMsg msg = new PushMsg() ;
				msg.setUserNo(order.getUserNo());
				msg.setUserType(1);
				msg.setMsgCode(MsgType.REFUSE.getValue());
				msg.setMsgContent("您的快递在"+DateUtils.formatDate(nowDate)+"!"+reasonContext);
				msg.setMsgData(orderNo);
				msg.setCreateTime(DateUtils.formatDate(nowDate));
				msg.setExpireTime(DateUtils.formatDate(DateUtils.addDate(nowDate, 0, 6, 0)));
				long msgId = msgService.save(msg) ;
				PushUtil.pushById(configInfo,String.valueOf(msgId),1,params.get("uid"));


				/**
				 * 问题件处理	
				 */
				Map<String,Object> refuseMap = new HashMap<String,Object>();
				refuseMap.put("order_no", orderNo);
				refuseMap.put("lgc_order_no", order.getLgcOrderNo());
				refuseMap.put("pro_type", reasonNo);//问题编号
				refuseMap.put("descb", reasonContext);//问题描述
				refuseMap.put("status", "1");//处理状态 
				refuseMap.put("check_name", userInfo.getRealName());//登记人
				long proOrderID=0l;
				Map<String,Object> questionCheck = orderInfoService.doubtCheck(orderNo);//查询问题件是否存在
				if(questionCheck==null){					
					proOrderID=	orderInfoService.refuse(refuseMap);
				}else{
					proOrderID =	orderInfoService.updateQuestion(refuseMap);//更新到最新的问题件信息
				}

				orderInfoService.YXreusePro(orderNo,reasonContext,String.valueOf(proOrderID),loginUser.getSubstationNo(),loginUserNo);
//				String uid = params.get("uid");
//				uid = uid.substring(0, uid.indexOf("_"));
//				if ("yx".equals(uid)) {
//					orderInfoService.YXreusePro(orderNo,reasonContext,String.valueOf(proOrderID),loginUser.getSubstationNo(),loginUserNo);
//				}else{
//					orderInfoService.reusePro(orderNo,reasonContext,String.valueOf(proOrderID)) ;
//				}

				/***
				 * 一米鲜订单信息更新推送
				 * 
				 */

				if("YMX".equals(order.getSource())){
					System.out.println("一米鲜快递推送通知");
					String info ="您的订单："+orderNo+" 被客户拒签，或将二次投递";
					RunnableUtils ymx = new RunnableUtils();
					ymx.pushTMX(order.getOrderNote(), "4", orderNo,
							userInfo.getRealName(), userInfo.getPhone(), info, DateUtils.formatDate(nowDate,"yyyy-MM-dd HH:mm:ss"),params.get("uid"));
				}

				/**
				 * 
				 * 微信推送
				 * 
				 * */
				if(!StringUtil.isEmptyWithTrim(order.getWxOpenid())){
					System.out.println("微信推送线程开始");
					RunnableUtils run1   = new RunnableUtils();
					run1.pushWEIXIN(order, userInfo,6);	

				}				
				render(JSON_TYPE, CommonResponse.respSuccessJson("","状态更新成功", params.get("reqNo")), response); 

			} else {
				log.info("validate false!!!!");		
				render(JSON_TYPE, CommonResponse.respFailJson(ret.get("respCode"), ret.get("respMsg"), params.get("reqNo")),response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE, CommonResponse.respFailJson("9000","服务器异常", params.get("reqNo")), response);
		}
	}




	/*查询订单轨迹
	 * @param params
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/trackInfo")
	public void trackInfo(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(new String[] {"lgcOrderNo" ,"type"}), false, userSessionService,checkVersion,appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				List<Map<String,Object>> trackList = null;
				String orderNo = otherService.isExitOrder(params.get("lgcOrderNo"),params.get("type"));
				if(StringUtil.isEmptyWithTrim(orderNo)){
					render(JSON_TYPE, CommonResponse.respFailJson("9013","查无此单", params.get("reqNo")), response);		 
					return;					
				}				
				trackList= otherService.orderTrack(orderNo);						
				model = new HashMap<String,Object>();
				model.put("list", trackList);

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



	/*批量收件
	 * @param params
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/batchTake")
	public void batchTake(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(new String[] {"lgcOrderNo" ,"freight","freightType","payType","cod"}), true, userSessionService,checkVersion,appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				Date nowDate = new Date();
				String userNo = ret.get("userNo");
				User loginUser = userService.getUserByNo(userNo);
				String lgcNo = userService.getUserLgcNo(userNo);

				params.put("userNo", ret.get("userNo"));			
				params.put("createTime",DateUtils.formatDate(nowDate, "yyyy-MM-dd"));
				// 签到限制
				List<Map<String, Object>> signList = userSessionService.signInfoAll(params);// 查询今日签到次数
				List<Map<String, Object>> signOutList = userSessionService.signOutInfoAll(params);// 查询今日签退次数
				int signTimes = signList.size();
				int signOutTimes = signOutList.size();
				System.out.println("signTimes=============" + signTimes+ "//////////signOutTimes=============" + signOutTimes);
				if (signTimes < 1 || signTimes == signOutTimes) {
					render(JSON_TYPE, CommonResponse.respFailJson("9034","尚未签到或已签退", params.get("reqNo")), response);
					return;
				}				
				
				OrderInfo order = orderInfoService.getByLgcOrderNo(params.get("lgcOrderNo"), lgcNo);
				//订单不存在 新建订单
				if (order == null) {
					if(params.get("lgcOrderNo").contains(".")||params.get("lgcOrderNo").contains("'")){
						render(JSON_TYPE, CommonResponse.respFailJson("9034","订单号违法，不能包含标点。", params.get("reqNo")), response);
						return;
					}

					if ("MONTH".equals(params.get("payType"))) {// 支付方式为月结
						if (StringUtils.isEmptyWithTrim(params.get("monthSettleNo"))) {
							render(JSON_TYPE, CommonResponse.respFailJson("9001","缺少参数：monthSettleNo", params.get("reqNo")),	response);
							return;
						}
						Map<String, Object> map = muserService	.selectMonthByFive(params.get("monthSettleNo"));
						if (map == null) {
							render(JSON_TYPE, CommonResponse.respFailJson("9031","不存在的月结号", params.get("reqNo")), response);
							return;
						}
						params.put("monthSettleNo", (String)map.get("monthSettleNo"));
					}
					OrderInfo orderInfo = (OrderInfo) MapConverter.convertMap(	OrderInfo.class, params);				
					orderInfo.setTakeCourierNo(userNo);
					orderInfo.setSubStationNo(loginUser.getSubstationNo());
					orderInfo.setLgcNo(lgcNo);					
					String orderNo = sequenceService.getNextVal("order_no");
					orderInfo.setOrderNo(orderNo);
					orderInfo.setUserNo("k" + userNo);
					orderInfo.setCreateTime(DateUtils.formatDate(nowDate));
					orderInfo.setLastUpdateTime(DateUtils.formatDate(nowDate));
					orderInfo.setStatus(1); // 处理中
					orderInfo.setPayStatus(PayStatus.NOPAY.getValue());
					orderInfo.setSource("COURIER");
					orderInfo.setTakeOrderTime(DateUtils.formatDate(nowDate));
					orderInfo.setLgcOrderNo(params.get("lgcOrderNo"));

					String isMessage = lgcService.getLgcConfig("MOBILE_CONFIG", "TAKE_SEND_MSG", "0") ;
					String message = "0" ;
					if ("1".equals(isMessage)) {
						if ("1".equals(params.get("message"))) {
							message = "1" ;
						}
					}
					orderInfo.setMessage(message);


					if (!"1".equals(params.get("freightType"))&& !"2".equals(params.get("freightType"))) {
						render(JSON_TYPE, CommonResponse.respFailJson("9026","参数不正确：方选择错误", params.get("reqNo")), response);
						return;
					}

					orderInfo.setFreightType(params.get("freightType"));
					if (!"MONTH".equals(params.get("payType"))&& !"CASH".equals(params.get("payType"))) {
						render(JSON_TYPE, CommonResponse.respFailJson("9026","支付方式选择错误", params.get("reqNo")), response);
						return;
					}
					orderInfo.setPayType(params.get("payType"));
					if ("MONTH".equals(params.get("payType"))) {// 支付方式为月结
						if (StringUtils.isEmptyWithTrim(params.get("monthSettleNo"))) {
							render(JSON_TYPE, CommonResponse.respFailJson("9001","缺少参数：monthSettleNo", params.get("reqNo")),	response);
							return;
						}
						Map<String, Object> map = muserService.selectMonthBy(params.get("monthSettleNo"));
						if (map == null) {
							render(JSON_TYPE, CommonResponse.respFailJson("9031","不存在的月结号", params.get("reqNo")), response);
							return;
						}
						orderInfo.setMonthSettleNo((String)map.get("monthSettleNo"));
					}else{
						orderInfo.setMonthSettleNo("");
					}

					Map<String, Object> vRate = null;

					float vpay = 0;
					float freight = Math.round(Float.valueOf(params.get("freight")) * 100) / 100f;
					float goodPrice = 0;
					float lcpay = 0;
					float lvpay = 0;
					float lpayAcount = 0;
					float lnpay = 0;
					float mpay = 0; // 月结费用
					lpayAcount=freight+lpayAcount;
					String goodValuationStr= params.get("goodValuation");
					orderInfo.setGoodValuation(StringUtils.isNotEmptyWithTrim(goodValuationStr)?Float.valueOf(goodValuationStr) * 100/100f:0);
					// 代收货款手续费
					if ("1".equals(params.get("cod"))) {
						if (StringUtils.isEmptyWithTrim(params.get("goodPrice"))) {
							render(JSON_TYPE, CommonResponse.respFailJson("9031","请输入代收货款金额", params.get("reqNo")), response);
							return;
						}		
						orderInfo.setCod(1);
						goodPrice = Math.round(Float.valueOf(params.get("goodPrice")) * 100) / 100f;
						orderInfo.setGoodPrice(goodPrice);
						orderInfo.setCpayStatus("INIT");		
						if (StringUtils.isNotEmptyWithTrim(params.get("codName"))) {
							String codName = params.get("codName");
							Map<String, Object> codMap = otherService.selectCodBy(codName);
							if (codMap != null) {
								int discount = (Integer) codMap.get("discount");// 代收货款费率
								// 千分比
								orderInfo.setCodRate(String.valueOf(discount));// 保存费率
								orderInfo.setCodName(codName);// 户名
								lcpay = (discount * goodPrice) / 1000f;// 代收货款手续费
							}
						}
						lpayAcount = lpayAcount + orderInfo.getGoodPrice();
					}else{
						orderInfo.setGoodPrice(0.00f);
					}
					// 保价手续费计算
					if (orderInfo.getGoodValuation() > 0) {
						if (StringUtils.isEmptyWithTrim(params.get("vpay"))) {
							render(JSON_TYPE, CommonResponse.respFailJson("9001", "缺少参数：vpay保价手续费",params.get("reqNo")), response);
							return;
						}
						// 保价手续费计算
						vpay = Math.round(Float.valueOf(params.get("vpay")) * 100) / 100f;
						vRate = lgcService.getLgcVrate();
						lvpay = getPayByRate(orderInfo.getGoodValuation(), vRate);
						lpayAcount = lpayAcount + lvpay;
						orderInfo.setGoodValuationRate(vRate.get("rate").toString());
					}

					if ("1".equals(orderInfo.getFreightType())) {
						lnpay = lvpay + freight;
					} else {
						lnpay = 0;
					}

					if ("MONTH".equals(params.get("payType"))) {
						lnpay = lvpay + freight;
						mpay = lvpay + freight;
					}
					if (Math.abs(lvpay - vpay) >= 0.01) {
						render(JSON_TYPE, CommonResponse.respFailJson("9026",
								"参数不正确：vpay", params.get("reqNo")), response);
						return;
					}
					orderInfo.setReqRece("N");//是否有回单
					orderInfo.setCpay(lcpay);
					orderInfo.setVpay(lvpay);
					orderInfo.setFreight(freight);
					orderInfo.setPayAcount(lpayAcount);
					orderInfo.setTnpay(lnpay);
					orderInfo.setMpay(mpay);
					if ("2".equals(params.get("freightType"))&& "1".equals(params.get("cod"))) {			
						orderInfo.setSnapy(lvpay + freight + goodPrice);
					}
					if ("2".equals(params.get("freightType"))&& !"1".equals(params.get("cod"))) {
						orderInfo.setSnapy(lvpay + freight);
					}
					if ("1".equals(params.get("freightType"))&& "1".equals(params.get("cod"))) {
						orderInfo.setSnapy(goodPrice);
					}
					if ("1".equals(params.get("freightType"))&& !"1".equals(params.get("cod"))) {
						orderInfo.setSnapy(0);
					}


					orderInfo.setStatus(2);
					if ("1".equals(orderInfo.getFreightType())) {
						orderInfo.setFpayStatus("SUCCESS");
					} else {
						orderInfo.setFpayStatus("INIT");
					}

					long id = orderInfoService.save(orderInfo); // 保存订单信息 获取ID	

					String uid = params.get("uid");
					uid = uid.substring(0, uid.indexOf("_"));
					if(StringUtil.isEmptyWithTrim(params.get("itemWeight"))){
						orderInfo.setItemWeight(1);
					}else{
						orderInfo.setItemWeight(Float.valueOf(params.get("itemWeight")));
					}
					if ("yx".equals(uid)) {
						orderInfo.setForNo(params.get("forNo"));					
						orderInfoService.yxTakeUpdate(orderInfo); // 更新订单信息
					} else {
						orderInfoService.takeUpdate(orderInfo); // 更新订单信息
					}

					orderInfoService.changeOrderRegisterFirst(orderInfo);// 登记第一次录入信息
					orderInfoService.insertBatchTakeCount(orderInfo.getLgcOrderNo(),userNo);

					OrderTrack track = new OrderTrack();
					track.setOrderNo(orderInfo.getOrderNo());
					track.setContext("订单被创建");
					track.setOrderTime(DateUtils.formatDate(nowDate));
					track.setCompleted("N");
					track.setOrderStatus("INIT");
					track.setOpname(loginUser.getRealName());
					orderTrackService.add(track);

					Map<String, Object> sMap = lgcService.getSubstationInfo(loginUser.getSubstationNo());
					track = new  OrderTrack();
					track.setOrderNo(orderInfo.getOrderNo());
					track.setContext(sMap.get("substation_name") + ",快递员:"+ loginUser.getRealName() + ",已收件,联系方式："	+ loginUser.getPhone());
					track.setOrderTime(DateUtils.formatDate(nowDate));
					track.setCompleted("N");
					track.setCurNo(userNo);
					track.setCurType("C");
					track.setNextNo(loginUser.getSubstationNo());
					track.setNextType("S");
					track.setOrderStatus("TAKEING");
					track.setParentId(0);
					track.setIsLast("Y"); //
					track.setOpname(loginUser.getRealName());
					orderTrackService.add(track);			

					render(JSON_TYPE, CommonResponse.respSuccessJson("","收件成功", params.get("reqNo")), response);
					return;		
				}




				//订单已经存在
				if(order.getStatus()!=1){
					render(JSON_TYPE,CommonResponse.respFailJson("9013", "订单状态有误，不是待收件状态",params.get("reqNo")), response);
					return;
				}	
				if(StringUtils.isEmptyWithTrim(order.getSendArea()+order.getSendAddr())
						|| StringUtils.isEmptyWithTrim(order.getSendPhone())||StringUtils.isEmptyWithTrim(order.getSendName())||
						StringUtils.isEmptyWithTrim(order.getRevArea()+order.getRevAddr())|| 
						StringUtils.isEmptyWithTrim(order.getRevPhone())||StringUtils.isEmptyWithTrim(order.getRevName())){				
					render(JSON_TYPE,
							CommonResponse.respFailJson("9051", "订单收派件信息不完善！请完善后提交！",params.get("reqNo")), response);
					return;					
				}


				order.setTakeCourierNo(userNo);
				order.setSubStationNo(loginUser.getSubstationNo());
				order.setTakeOrderTime(DateUtils.formatDate(nowDate));
				order.setLastUpdateTime(DateUtils.formatDate(nowDate));

				if (!"1".equals(params.get("freightType"))&& !"2".equals(params.get("freightType"))) {
					render(JSON_TYPE, CommonResponse.respFailJson("9026","参数不正确：支付方选择错误", params.get("reqNo")), response);
					return;
				}

				order.setFreightType(params.get("freightType"));
				if (!"MONTH".equals(params.get("payType"))&& !"CASH".equals(params.get("payType"))) {
					render(JSON_TYPE, CommonResponse.respFailJson("9026","支付方式选择错误", params.get("reqNo")), response);
					return;
				}
				order.setPayType(params.get("payType"));
				if ("MONTH".equals(params.get("payType"))) {// 支付方式为月结
					if (StringUtils.isEmptyWithTrim(params.get("monthSettleNo"))) {
						render(JSON_TYPE, CommonResponse.respFailJson("9001","缺少参数：monthSettleNo", params.get("reqNo")),	response);
						return;
					}
					Map<String, Object> map = muserService	.selectMonthByFive(params.get("monthSettleNo"));
					if (map == null) {
						render(JSON_TYPE, CommonResponse.respFailJson("9031","不存在的月结号", params.get("reqNo")), response);
						return;
					}
					order.setMonthSettleNo((String)map.get("monthSettleNo"));
				}else{
					order.setMonthSettleNo("");
				}

				Map<String, Object> vRate = null;

				float vpay = 0;
				float freight = Math.round(Float.valueOf(params.get("freight")) * 100) / 100f;
				float goodPrice = 0;
				float lcpay = 0;
				float lvpay = 0;
				float lpayAcount = 0;
				float lnpay = 0;
				float mpay = 0; // 月结费用
				lpayAcount=freight+lpayAcount;
				String goodValuationStr= params.get("goodValuation");
				order.setGoodValuation(StringUtils.isNotEmptyWithTrim(goodValuationStr)?Float.valueOf(goodValuationStr) * 100/100f:0);
				// 代收货款手续费
				if ("1".equals(params.get("cod"))) {
					if (StringUtils.isEmptyWithTrim(params.get("goodPrice"))) {
						render(JSON_TYPE, CommonResponse.respFailJson("9031","请输入代收货款金额", params.get("reqNo")), response);
						return;
					}		
					order.setCod(1);
					goodPrice = Math.round(Float.valueOf(params.get("goodPrice")) * 100) / 100f;
					order.setGoodPrice(goodPrice);
					order.setCpayStatus("INIT");		
					if (StringUtils.isNotEmptyWithTrim(params.get("codName"))) {
						String codName = params.get("codName");
						Map<String, Object> codMap = otherService.selectCodBy(codName);
						if (codMap != null) {
							int discount = (Integer) codMap.get("discount");// 代收货款费率
							// 千分比
							order.setCodRate(String.valueOf(discount));// 保存费率
							order.setCodName(codName);// 户名
							lcpay = (discount * goodPrice) / 1000f;// 代收货款手续费
						}
					}
					lpayAcount = lpayAcount + order.getGoodPrice();
				}else{
					order.setGoodPrice(0.00f);
				}
				// 保价手续费计算
				if (order.getGoodValuation() > 0) {
					if (StringUtils.isEmptyWithTrim(params.get("vpay"))) {
						render(JSON_TYPE, CommonResponse.respFailJson("9001", "缺少参数：vpay保价手续费",params.get("reqNo")), response);
						return;
					}
					// 保价手续费计算
					vpay = Math.round(Float.valueOf(params.get("vpay")) * 100) / 100f;
					vRate = lgcService.getLgcVrate();
					lvpay = getPayByRate(order.getGoodValuation(), vRate);
					lpayAcount = lpayAcount + lvpay;
					order.setGoodValuationRate(vRate.get("rate").toString());
				}

				if ("1".equals(order.getFreightType())) {
					lnpay = lvpay + freight;
				} else {
					lnpay = 0;
				}

				if ("MONTH".equals(params.get("payType"))) {
					lnpay = lvpay + freight;
					mpay = lvpay + freight;
				}
				if (Math.abs(lvpay - vpay) >= 0.01) {
					render(JSON_TYPE, CommonResponse.respFailJson("9026",
							"参数不正确：vpay", params.get("reqNo")), response);
					return;
				}

				order.setCpay(lcpay);
				order.setVpay(lvpay);
				order.setFreight(freight);
				order.setPayAcount(lpayAcount);
				order.setTnpay(lnpay);
				order.setMpay(mpay);
				if ("2".equals(params.get("freightType"))&& "1".equals(params.get("cod"))) {			
					order.setSnapy(lvpay + freight + goodPrice);
				}
				if ("2".equals(params.get("freightType"))&& !"1".equals(params.get("cod"))) {
					order.setSnapy(lvpay + freight);
				}
				if ("1".equals(params.get("freightType"))&& "1".equals(params.get("cod"))) {
					order.setSnapy(goodPrice);
				}
				if ("1".equals(params.get("freightType"))&& !"1".equals(params.get("cod"))) {
					order.setSnapy(0);
				}


				order.setStatus(2);
				if ("1".equals(order.getFreightType())) {
					order.setFpayStatus("SUCCESS");
				} else {
					order.setFpayStatus("INIT");
				}
				if(StringUtil.isEmptyWithTrim(params.get("itemWeight"))){
					order.setItemWeight(1);
				}else{
					order.setItemWeight(Float.valueOf(params.get("itemWeight")));
				}
				orderInfoService.takeUpdate(order); // 更新订单信息

				orderInfoService.completeMsg(order); // 补全订单信息
				orderInfoService.changeOrderRegisterFirst(order);// 登记第一次录入信息

				orderInfoService.insertBatchTakeCount(order.getLgcOrderNo(),userNo);



				String uid = params.get("uid");
				uid = uid.substring(0, uid.indexOf("_"));

				if("kuaike".equals(uid)){
					if(StringUtils.isNotEmptyWithTrim(order.getRevPhone())&&order.getRevPhone().length()==11){
						RunnableUtils ui = new RunnableUtils();
						ui.MessagePushClass("szkyt",lgcNo, order.getLgcOrderNo(), order.getRevPhone(),lgcService);	
					}				
				}							

				Map<String, Object> sMap = lgcService.getSubstationInfo(loginUser.getSubstationNo());
				OrderTrack track = new OrderTrack();
				track.setOrderNo(order.getOrderNo());
				track.setContext(sMap.get("substation_name") + ",快递员:"+ loginUser.getRealName() + ",已收件,联系方式："	+ loginUser.getPhone());
				track.setOrderTime(DateUtils.formatDate(nowDate));
				track.setCompleted("N");
				track.setCurNo(userNo);
				track.setCurType("C");
				track.setNextNo(loginUser.getSubstationNo());
				track.setNextType("S");
				track.setOrderStatus("TAKEING");
				track.setParentId(0);
				track.setIsLast("Y"); //
				track.setOpname(loginUser.getRealName());
				orderTrackService.add(track);

				User userInfo = userService.getUserByNo(userNo);// 查询快递员信息
				/***
				 * 微信信息推送
				 */
				if (!StringUtil.isEmptyWithTrim(order.getWxOpenid())) {
					System.out.println("微信推送线程开始");
					/*RunnableUtils run1 = new RunnableUtils();
					run1.pushWEIXIN(order, userInfo, 4);*/
					WxxdConfig wxxdConfig = wxxdConfigService.getByDskey(params.get("uid").substring(0,params.get("uid").indexOf("_"))) ;
					String wxxd_url = wxxdConfig==null?"":wxxdConfig.getXdUrl() ;
					
					Map<String, String> wxMap = new HashMap<String, String>() ;
					wxMap.put("url",buildUrl(wxxd_url,lgcNo, uid,order.getOrderNo(), order.getLgcOrderNo()));
					wxMap.put("dskey", params.get("uid").substring(0,params.get("uid").indexOf("_"))) ;
					wxMap.put("t","2");
					wxMap.put("touser",order.getWxOpenid());
					wxMap.put("lgcOrderNo",order.getLgcOrderNo());
					wxMap.put("remark","快递已妥妥的打包完毕，即将出发，请放下");
					wxMap.put("first","尊敬的客户，您好,您的快件已被取走");
					WeiXinUtil.push(configInfo,wxMap,1);
				}
				/***
				 * 康美药业推送
				 */
				if ("康美".equals(order.getSource())) {
					RunnableUtils run1 = new RunnableUtils();
					run1.KangmeiPush(URL, company_num, userName,
							order.getLgcOrderNo(),
							sMap.get("substation_name") + ",快递员:"
									+ loginUser.getRealName()
									+ ",已收件,联系方式：" + loginUser.getPhone(),
							"0");
				}

				/**			
				 * 一米鲜快递推送通知
				 */
				if ("YMX".equals(order.getSource())) {
					System.out.println("一米鲜快递推送通知");
					String info = "快递员" + userInfo.getRealName() + "已收件";
					RunnableUtils ymx = new RunnableUtils();
					ymx.pushTMX(order.getOrderNote(), "1", params
							.get("orderNo"), userInfo.getRealName(),
							userInfo.getPhone(), info, DateUtils
							.formatDate(nowDate,
									"yyyy-MM-dd HH:mm:ss"), params
									.get("uid"));
				}
				/**
				 * 信息推送
				 * 
				 */
				String innerNo = loginUser.getInnerNo();
				if (StringUtil.isEmptyWithTrim(innerNo)) {
					innerNo = "";
				} else {
					innerNo = "(" + innerNo + ")。";
				}
				PushMsg msg = new PushMsg();
				msg.setUserNo(order.getUserNo());
				msg.setUserType(1);
				msg.setMsgCode(MsgType.TAKE.getValue());
				msg.setMsgContent("您的快递在" + DateUtils.formatDate(nowDate)
						+ "被取走！请确认是否为本人操作。取件快递员:" + loginUser.getRealName()
						+ innerNo);
				msg.setMsgData(order.getOrderNo());
				msg.setCreateTime(DateUtils.formatDate(nowDate));
				msg.setExpireTime(DateUtils.formatDate(DateUtils.addDate(
						nowDate, 0, 6, 0)));
				long msgId = msgService.save(msg);
				PushUtil.pushById(configInfo,String.valueOf(msgId), 1,params.get("uid"));


				render(JSON_TYPE, CommonResponse.respSuccessJson("","收件成功", params.get("reqNo")), response); 

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
	 * @param params
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/takeCount")
	public void takeCount(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(new String[] { }), true, userSessionService,checkVersion,appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {	
				String userNo = ret.get("userNo");
				model = new HashMap<String, Object>();
				model.put("count", orderInfoService.getTakeCount(DateUtils.formatDate(new Date(), "yyyy-MM-dd"),userNo));
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

	/*批量收件
	 * @param params
	 * @param request
	 * @param response
	 * 
	 */
	@RequestMapping(value = "/offBatchTake")
	public void offBatchTake(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(new String[] {"orderMap"}), true, userSessionService,checkVersion,appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				Date nowDate = new Date();
				String userNo = ret.get("userNo");
				User loginUser = userService.getUserByNo(userNo);
				String lgcNo = userService.getUserLgcNo(userNo);

				params.put("userNo", ret.get("userNo"));			
				params.put("createTime",DateUtils.formatDate(nowDate, "yyyy-MM-dd"));

				List<Map<String,Object>> errList  = new ArrayList<Map<String,Object>>();
				if(StringUtils.isEmptyWithTrim(params.get("orderMap"))){			
					render(JSON_TYPE, CommonResponse.respFailJson("9001","缺少参数：orderMap", params.get("reqNo")),	response);
					return;			
				}
				String orderJsonStr= params.get("orderMap");
				if(StringUtil.isEmptyWithTrim(orderJsonStr)){
					render(JSON_TYPE, CommonResponse.respFailJson("9001","订单信息不能为空", params.get("reqNo")),	response);
					return;			
				}
				System.out.println(orderJsonStr);
				Map<String,Object> jsonMap = JsonUtil.getMapFromJson(orderJsonStr);	
				List<Map<String,Object>> orderList  =  (List<Map<String, Object>>) jsonMap.get("orderInfo");
				if(orderList.size()<1){
					render(JSON_TYPE, CommonResponse.respFailJson("9001","订单信息不能为空", params.get("reqNo")),	response);
					return;			
				}
				for(Map<String,Object> offMap :orderList){
					Map<String,Object> errMap =  new HashMap<String,Object>();

					String ylgcOrderNo  = StringUtil.nullString(offMap.get("lgcOrderNo"));
					String yfreight  = StringUtil.nullString(offMap.get("freight"));
					String yfreightType  = StringUtil.nullString(offMap.get("freightType"));
					String ypayType  = StringUtil.nullString(offMap.get("payType"));
					String ycod  = StringUtil.nullString(offMap.get("cod"));
					String yitemWeight  = StringUtil.nullString(offMap.get("itemWeight"));
					String ygoodPrice  = StringUtil.nullString(offMap.get("goodPrice"));
					String ycodName  = StringUtil.nullString(offMap.get("codName"));
					String ygoodValuation  = StringUtil.nullString(offMap.get("goodValuation"));
					String yvpay  = StringUtil.nullString(offMap.get("vpay"));
					String ymonthSettleNo  = StringUtil.nullString(offMap.get("monthSettleNo"));
					if(StringUtils.isEmptyWithTrim(ylgcOrderNo)){
						errMap.put("lgcOrderNo", ylgcOrderNo);
						errMap.put("message", "订单信息有误，单号不能为空");
						errList.add(errMap);
						continue;		
					}



					OrderInfo order = orderInfoService.getByLgcOrderNo(ylgcOrderNo, lgcNo);
					//订单不存在 新建订单
					if (order == null) {
						if(ylgcOrderNo.contains(".")||ylgcOrderNo.contains("'")){
							errMap.put("lgcOrderNo", ylgcOrderNo);
							errMap.put("message", "订单号违法!");
							errList.add(errMap);
							continue;
						}

						if ("MONTH".equals(ypayType)) {// 支付方式为月结
							if (StringUtils.isEmptyWithTrim(ymonthSettleNo)) {
								errMap.put("lgcOrderNo", ylgcOrderNo);
								errMap.put("message", "缺少月结号!");
								errList.add(errMap);
								continue;
							}
							Map<String, Object> map = muserService	.selectMonthByFive(ymonthSettleNo);
							if (map == null) {
								errMap.put("lgcOrderNo", ylgcOrderNo);
								errMap.put("message", "不存在月结号!");
								errList.add(errMap);
								continue;
							}
							ymonthSettleNo=(String)map.get("monthSettleNo");
						}
						OrderInfo orderInfo = new  OrderInfo();	
						orderInfo.setTakeCourierNo(userNo);
						orderInfo.setSubStationNo(loginUser.getSubstationNo());
						orderInfo.setLgcNo(lgcNo);					
						String orderNo = sequenceService.getNextVal("order_no");
						orderInfo.setOrderNo(orderNo);
						orderInfo.setUserNo("k" + userNo);
						orderInfo.setCreateTime(DateUtils.formatDate(nowDate));
						orderInfo.setLastUpdateTime(DateUtils.formatDate(nowDate));
						orderInfo.setStatus(1); // 处理中
						orderInfo.setPayStatus(PayStatus.NOPAY.getValue());
						orderInfo.setSource("OFFBatch");
						orderInfo.setTakeOrderTime(DateUtils.formatDate(nowDate));
						orderInfo.setLgcOrderNo(ylgcOrderNo);



						if (!"1".equals(yfreightType)&& !"2".equals(yfreightType)) {
							errMap.put("lgcOrderNo", ylgcOrderNo);
							errMap.put("message", "支付方选择错误!");
							errList.add(errMap);
							continue;
						}

						orderInfo.setFreightType(yfreightType);
						if (!"MONTH".equals(ypayType)&& !"CASH".equals(ypayType)) {
							errMap.put("lgcOrderNo", ylgcOrderNo);
							errMap.put("message", "支付方式选择错误");					
							errList.add(errMap);
							continue;
						}
						orderInfo.setPayType(ypayType);
						if ("MONTH".equals(ypayType)) {// 支付方式为月结

							orderInfo.setMonthSettleNo(ymonthSettleNo);
						}else{
							orderInfo.setMonthSettleNo("");
						}

						Map<String, Object> vRate = null;

						float vpay = 0;
						float freight = Math.round(Float.valueOf(yfreight) * 100) / 100f;
						float goodPrice = 0;
						float lcpay = 0;
						float lvpay = 0;
						float lpayAcount = 0;
						float lnpay = 0;
						float mpay = 0; // 月结费用
						lpayAcount=freight+lpayAcount;
						String goodValuationStr=ygoodValuation;
						orderInfo.setGoodValuation(StringUtils.isNotEmptyWithTrim(goodValuationStr)?Float.valueOf(goodValuationStr) * 100/100f:0);
						// 代收货款手续费
						if ("1".equals(ycod)) {
							if (StringUtils.isEmptyWithTrim(ygoodPrice)) {
								errMap.put("lgcOrderNo", ylgcOrderNo);
								errMap.put("message", "请输入代收货款金额");					
								errList.add(errMap);
								continue;			
							}		
							orderInfo.setCod(1);
							goodPrice = Math.round(Float.valueOf(ygoodPrice) * 100) / 100f;
							orderInfo.setGoodPrice(goodPrice);
							orderInfo.setCpayStatus("INIT");		
							if (StringUtils.isNotEmptyWithTrim(ycodName)) {
								String codName = ycodName;
								Map<String, Object> codMap = otherService.selectCodBy(codName);
								if (codMap != null) {
									int discount = (Integer) codMap.get("discount");// 代收货款费率
									// 千分比
									orderInfo.setCodRate(String.valueOf(discount));// 保存费率
									orderInfo.setCodName(codName);// 户名
									lcpay = (discount * goodPrice) / 1000f;// 代收货款手续费
								}
							}
							lpayAcount = lpayAcount + orderInfo.getGoodPrice();
						}else{
							orderInfo.setGoodPrice(0.00f);
						}
						// 保价手续费计算
						if (orderInfo.getGoodValuation() > 0) {
							if (StringUtils.isEmptyWithTrim(yvpay)) {
								errMap.put("lgcOrderNo", ylgcOrderNo);
								errMap.put("message", "缺少参数：vpay保价手续费");
								errList.add(errMap);
								continue;				
							}
							// 保价手续费计算
							vpay = Math.round(Float.valueOf(yvpay) * 100) / 100f;
							vRate = lgcService.getLgcVrate();
							lvpay = getPayByRate(orderInfo.getGoodValuation(), vRate);
							lpayAcount = lpayAcount + lvpay;
							orderInfo.setGoodValuationRate(vRate.get("rate").toString());
						}

						if ("1".equals(orderInfo.getFreightType())) {
							lnpay = lvpay + freight;
						} else {
							lnpay = 0;
						}

						if ("MONTH".equals(ypayType)) {
							lnpay = lvpay + freight;
							mpay = lvpay + freight;
						}
						if (Math.abs(lvpay - vpay) >= 0.01) {
							errMap.put("lgcOrderNo", ylgcOrderNo);
							errMap.put("message", "参数不正确：vpay保价手续费");
							errList.add(errMap);
							continue;									
						}
						orderInfo.setReqRece("N");//是否有回单
						orderInfo.setCpay(lcpay);
						orderInfo.setVpay(lvpay);
						orderInfo.setFreight(freight);
						orderInfo.setPayAcount(lpayAcount);
						orderInfo.setTnpay(lnpay);
						orderInfo.setMpay(mpay);
						if ("2".equals(yfreightType)&& "1".equals(ycod)) {			
							orderInfo.setSnapy(lvpay + freight + goodPrice);
						}
						if ("2".equals(yfreightType)&& !"1".equals(ycod)) {
							orderInfo.setSnapy(lvpay + freight);
						}
						if ("1".equals(yfreightType)&& "1".equals(ycod)) {
							orderInfo.setSnapy(goodPrice);
						}
						if ("1".equals(yfreightType)&& !"1".equals(ycod)) {
							orderInfo.setSnapy(0);
						}


						orderInfo.setStatus(2);
						if ("1".equals(orderInfo.getFreightType())) {
							orderInfo.setFpayStatus("SUCCESS");
						} else {
							orderInfo.setFpayStatus("INIT");
						}

						long id = orderInfoService.save(orderInfo); // 保存订单信息 获取ID	

						String uid = params.get("uid");
						uid = uid.substring(0, uid.indexOf("_"));
						if(StringUtil.isEmptyWithTrim(yitemWeight)){
							orderInfo.setItemWeight(1);
						}else{
							orderInfo.setItemWeight(Float.valueOf(yitemWeight));
						}
						if ("yx".equals(uid)) {		
							orderInfoService.yxTakeUpdate(orderInfo); // 更新订单信息
						} else {
							orderInfoService.takeUpdate(orderInfo); // 更新订单信息
						}

						orderInfoService.changeOrderRegisterFirst(orderInfo);// 登记第一次录入信息
						orderInfoService.insertBatchTakeCount(orderInfo.getLgcOrderNo(),userNo);

						OrderTrack track = new OrderTrack();
						track.setOrderNo(orderInfo.getOrderNo());
						track.setContext("订单被创建");
						track.setOrderTime(DateUtils.formatDate(nowDate));
						track.setCompleted("N");
						track.setOrderStatus("INIT");
						track.setOpname(loginUser.getRealName());
						orderTrackService.add(track);

						Map<String, Object> sMap = lgcService.getSubstationInfo(loginUser.getSubstationNo());
						track = new  OrderTrack();
						track.setOrderNo(orderInfo.getOrderNo());
						track.setContext(sMap.get("substation_name") + ",快递员:"+ loginUser.getRealName() + ",已收件,联系方式："	+ loginUser.getPhone());
						track.setOrderTime(DateUtils.formatDate(nowDate));
						track.setCompleted("N");
						track.setCurNo(userNo);
						track.setCurType("C");
						track.setNextNo(loginUser.getSubstationNo());
						track.setNextType("S");
						track.setOrderStatus("TAKEING");
						track.setParentId(0);
						track.setIsLast("Y"); //
						track.setOpname(loginUser.getRealName());
						orderTrackService.add(track);			

						continue;//新建订单完成

					}




					//订单已经存在
					if(order.getStatus()!=1){
						errMap.put("lgcOrderNo", ylgcOrderNo);
						errMap.put("message", "订单状态有误，不是待收件状态");
						errList.add(errMap);
						continue;							
					}	
					if(StringUtils.isEmptyWithTrim(order.getSendArea()+order.getSendAddr())
							|| StringUtils.isEmptyWithTrim(order.getSendPhone())||StringUtils.isEmptyWithTrim(order.getSendName())||
							StringUtils.isEmptyWithTrim(order.getRevArea()+order.getRevAddr())|| 
							StringUtils.isEmptyWithTrim(order.getRevPhone())||StringUtils.isEmptyWithTrim(order.getRevName())){				
						errMap.put("lgcOrderNo", ylgcOrderNo);
						errMap.put("message", "订单收派件信息不完善！请完善后提交！");
						errList.add(errMap);
						continue;							
					}


					order.setTakeCourierNo(userNo);
					order.setSubStationNo(loginUser.getSubstationNo());
					order.setTakeOrderTime(DateUtils.formatDate(nowDate));
					order.setLastUpdateTime(DateUtils.formatDate(nowDate));

					if (!"1".equals(yfreightType)&& !"2".equals(yfreightType)) {
						errMap.put("lgcOrderNo", ylgcOrderNo);
						errMap.put("message", "参数不正确：支付方选择错误");
						errList.add(errMap);
						continue;				
					}

					order.setFreightType(yfreightType);
					if (!"MONTH".equals(ypayType)&& !"CASH".equals(ypayType)) {
						errMap.put("lgcOrderNo", ylgcOrderNo);
						errMap.put("message", "参数不正确：支付方式选择错误");
						errList.add(errMap);
						continue;								
					}
					order.setPayType(ypayType);
					if ("MONTH".equals(ypayType)) {// 支付方式为月结
						if (StringUtils.isEmptyWithTrim(ymonthSettleNo)) {
							errMap.put("lgcOrderNo", ylgcOrderNo);
							errMap.put("message", "参数不正确：缺少参数：monthSettleNo");
							errList.add(errMap);
							continue;					
						}
						Map<String, Object> map = muserService	.selectMonthByFive(ymonthSettleNo);
						if (map == null) {
							errMap.put("lgcOrderNo", ylgcOrderNo);
							errMap.put("message", "参数不正确：不存在的月结号");
							errList.add(errMap);
							continue;		
						}
						order.setMonthSettleNo((String)map.get("monthSettleNo"));
					}else{
						order.setMonthSettleNo("");
					}

					Map<String, Object> vRate = null;

					float vpay = 0;
					float freight = Math.round(Float.valueOf(yfreight) * 100) / 100f;
					float goodPrice = 0;
					float lcpay = 0;
					float lvpay = 0;
					float lpayAcount = 0;
					float lnpay = 0;
					float mpay = 0; // 月结费用
					lpayAcount=freight+lpayAcount;
					String goodValuationStr=ygoodValuation;
					order.setGoodValuation(StringUtils.isNotEmptyWithTrim(goodValuationStr)?Float.valueOf(goodValuationStr) * 100/100f:0);
					// 代收货款手续费
					if ("1".equals(ycod)) {
						if (StringUtils.isEmptyWithTrim(ygoodPrice)) {
							errMap.put("lgcOrderNo", ylgcOrderNo);
							errMap.put("message", "参数不正确：请输入代收货款金额");
							errList.add(errMap);
							continue;		
						}		
						order.setCod(1);
						goodPrice = Math.round(Float.valueOf(ygoodPrice) * 100) / 100f;
						order.setGoodPrice(goodPrice);
						order.setCpayStatus("INIT");		
						if (StringUtils.isNotEmptyWithTrim(ycodName)) {
							String codName = ycodName;
							Map<String, Object> codMap = otherService.selectCodBy(codName);
							if (codMap != null) {
								int discount = (Integer) codMap.get("discount");// 代收货款费率
								// 千分比
								order.setCodRate(String.valueOf(discount));// 保存费率
								order.setCodName(codName);// 户名
								lcpay = (discount * goodPrice) / 1000f;// 代收货款手续费
							}
						}
						lpayAcount = lpayAcount + order.getGoodPrice();
					}else{
						order.setGoodPrice(0.00f);
					}
					// 保价手续费计算
					if (order.getGoodValuation() > 0) {
						if (StringUtils.isEmptyWithTrim(yvpay)) {
							errMap.put("lgcOrderNo", ylgcOrderNo);
							errMap.put("message", "参数不正确：缺少参数：vpay保价手续费");
							errList.add(errMap);
							continue;		
						}
						// 保价手续费计算
						vpay = Math.round(Float.valueOf(yvpay) * 100) / 100f;
						vRate = lgcService.getLgcVrate();
						lvpay = getPayByRate(order.getGoodValuation(), vRate);
						lpayAcount = lpayAcount + lvpay;
						order.setGoodValuationRate(vRate.get("rate").toString());
					}

					if ("1".equals(order.getFreightType())) {
						lnpay = lvpay + freight;
					} else {
						lnpay = 0;
					}

					if ("MONTH".equals(ypayType)) {
						lnpay = lvpay + freight;
						mpay = lvpay + freight;
					}
					if (Math.abs(lvpay - vpay) >= 0.01) {
						errMap.put("lgcOrderNo", ylgcOrderNo);
						errMap.put("message", "参数不正确：vpay保价手续费");
						errList.add(errMap);
						continue;		
					}

					order.setCpay(lcpay);
					order.setVpay(lvpay);
					order.setFreight(freight);
					order.setPayAcount(lpayAcount);
					order.setTnpay(lnpay);
					order.setMpay(mpay);
					if ("2".equals(yfreightType)&& "1".equals(ycod)) {			
						order.setSnapy(lvpay + freight + goodPrice);
					}
					if ("2".equals(yfreightType)&& !"1".equals(ycod)) {
						order.setSnapy(lvpay + freight);
					}
					if ("1".equals(yfreightType)&& "1".equals(ycod)) {
						order.setSnapy(goodPrice);
					}
					if ("1".equals(yfreightType)&& !"1".equals(ycod)) {
						order.setSnapy(0);
					}


					order.setStatus(2);
					if ("1".equals(order.getFreightType())) {
						order.setFpayStatus("SUCCESS");
					} else {
						order.setFpayStatus("INIT");
					}

					if(StringUtil.isEmptyWithTrim(yitemWeight)){
						order.setItemWeight(1);
					}else{
						order.setItemWeight(Float.valueOf(yitemWeight));
					}

					orderInfoService.takeUpdate(order); // 更新订单信息

					orderInfoService.completeMsg(order); // 补全订单信息
					orderInfoService.changeOrderRegisterFirst(order);// 登记第一次录入信息

					orderInfoService.insertBatchTakeCount(order.getLgcOrderNo(),userNo);



					String uid = params.get("uid");
					uid = uid.substring(0, uid.indexOf("_"));




					if("kuaike".equals(uid)){
						if(StringUtils.isNotEmptyWithTrim(order.getRevPhone())&&order.getRevPhone().length()==11){
							RunnableUtils ui = new RunnableUtils();
							ui.MessagePushClass("szkyt",lgcNo, order.getLgcOrderNo(), order.getRevPhone(),lgcService);	
						}				
					}							

					Map<String, Object> sMap = lgcService.getSubstationInfo(loginUser.getSubstationNo());
					OrderTrack track = new OrderTrack();
					track.setOrderNo(order.getOrderNo());
					track.setContext(sMap.get("substation_name") + ",快递员:"+ loginUser.getRealName() + ",已收件,联系方式："	+ loginUser.getPhone());
					track.setOrderTime(DateUtils.formatDate(nowDate));
					track.setCompleted("N");
					track.setCurNo(userNo);
					track.setCurType("C");
					track.setNextNo(loginUser.getSubstationNo());
					track.setNextType("S");
					track.setOrderStatus("TAKEING");
					track.setParentId(0);
					track.setIsLast("Y"); //
					track.setOpname(loginUser.getRealName());
					orderTrackService.add(track);

					User userInfo = userService.getUserByNo(userNo);// 查询快递员信息
					/***
					 * 微信信息推送
					 */
					if (!StringUtil.isEmptyWithTrim(order.getWxOpenid())) {
						System.out.println("微信推送线程开始");
						/*RunnableUtils run1 = new RunnableUtils();
						run1.pushWEIXIN(order, userInfo, 4);*/
						WxxdConfig wxxdConfig = wxxdConfigService.getByDskey(params.get("uid").substring(0,params.get("uid").indexOf("_"))) ;
						String wxxd_url = wxxdConfig==null?"":wxxdConfig.getXdUrl() ;
						
						Map<String, String> wxMap = new HashMap<String, String>() ;
						wxMap.put("url",buildUrl(wxxd_url,lgcNo, uid,order.getOrderNo(), order.getLgcOrderNo()));
						wxMap.put("dskey", params.get("uid").substring(0,params.get("uid").indexOf("_"))) ;
						wxMap.put("t","2");
						wxMap.put("touser",order.getWxOpenid());
						wxMap.put("lgcOrderNo",order.getLgcOrderNo());
						wxMap.put("remark","快递已妥妥的打包完毕，即将出发，请放下");
						wxMap.put("first","尊敬的客户，您好,您的快件已被取走");
						WeiXinUtil.push(configInfo,wxMap,1);
					}
					/***
					 * 康美药业推送
					 */
					if ("康美".equals(order.getSource())) {
						RunnableUtils run1 = new RunnableUtils();
						run1.KangmeiPush(URL, company_num, userName,
								order.getLgcOrderNo(),
								sMap.get("substation_name") + ",快递员:"
										+ loginUser.getRealName()
										+ ",已收件,联系方式：" + loginUser.getPhone(),
								"0");
					}

					/**			
					 * 一米鲜快递推送通知
					 */
					if ("YMX".equals(order.getSource())) {
						System.out.println("一米鲜快递推送通知");
						String info = "快递员" + userInfo.getRealName() + "已收件";
						RunnableUtils ymx = new RunnableUtils();
						ymx.pushTMX(order.getOrderNote(), "1", params
								.get("orderNo"), userInfo.getRealName(),
								userInfo.getPhone(), info, DateUtils
								.formatDate(nowDate,
										"yyyy-MM-dd HH:mm:ss"), params
										.get("uid"));
					}
					/**
					 * 信息推送
					 * 
					 */
					String innerNo = loginUser.getInnerNo();
					if (StringUtil.isEmptyWithTrim(innerNo)) {
						innerNo = "";
					} else {
						innerNo = "(" + innerNo + ")。";
					}
					PushMsg msg = new PushMsg();
					msg.setUserNo(order.getUserNo());
					msg.setUserType(1);
					msg.setMsgCode(MsgType.TAKE.getValue());
					msg.setMsgContent("您的快递在" + DateUtils.formatDate(nowDate)
							+ "被取走！请确认是否为本人操作。取件快递员:" + loginUser.getRealName()
							+ innerNo);
					msg.setMsgData(order.getOrderNo());
					msg.setCreateTime(DateUtils.formatDate(nowDate));
					msg.setExpireTime(DateUtils.formatDate(DateUtils.addDate(
							nowDate, 0, 6, 0)));
					long msgId = msgService.save(msg);
					PushUtil.pushById(configInfo,String.valueOf(msgId), 1,params.get("uid"));
		

				}
				model = new HashMap<String, Object>() ;
				model.put("errList", errList);	
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


}
