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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.yogapay.couriertsi.domain.OrderInfo;
import com.yogapay.couriertsi.domain.OrderTrack;
import com.yogapay.couriertsi.domain.PushMsg;
import com.yogapay.couriertsi.domain.RunnableUtils;
import com.yogapay.couriertsi.domain.User;
import com.yogapay.couriertsi.enums.MsgType;
import com.yogapay.couriertsi.services.LgcService;
import com.yogapay.couriertsi.services.MsgService;
import com.yogapay.couriertsi.services.OrderInfoService;
import com.yogapay.couriertsi.services.OrderTrackService;
import com.yogapay.couriertsi.services.UserService;
import com.yogapay.couriertsi.services.WarehouseService;
import com.yogapay.couriertsi.utils.CommonResponse;
import com.yogapay.couriertsi.utils.DateUtils;
import com.yogapay.couriertsi.utils.PushUtil;
import com.yogapay.couriertsi.utils.StringUtil;
import com.yogapay.couriertsi.utils.StringUtils;
import com.yogapay.couriertsi.utils.ValidateUtil;

@Controller
@RequestMapping(value = "/manager", method = RequestMethod.POST)
@Scope("prototype")
public class WarehouseApi extends BaseApi {
	@Resource
	private OrderInfoService orderInfoService;
	@Resource
	private UserService userService;
	@Resource
	private OrderTrackService orderTrackService;
	@Resource
	private WarehouseService warehouseService;
	@Value("#{config['yx_cm_track_url']}")
	String URL;
	@Value("#{config['kangmei_company_num']}")
	String company_num;
	@Value("#{config['kangmei_user_name']}")
	String userName;
	@Resource
	 MsgService msgService ;
	@Resource
	 LgcService lgcService ;
	// 入仓扫描
	@RequestMapping(value = "/input")
	public void input(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.managerValidateRequest(
					request, reqParams(new String[] { "orderNo[]" }), true,
					userSessionService, checkVersion, appVersionService,
					dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String managerUser = ret.get("userNo");
				System.out.println("managerUser======================"+ managerUser + "==============");
				String orderNo[] = request.getParameterValues("orderNo[]");
				if (orderNo.length < 1) {
					render(JSON_TYPE,	CommonResponse.respFailJson("9037", "运单号为空",params.get("reqNo")), response);
					return;
				}

				String substationNo = warehouseService
						.getManagerSubStationByUserNo(managerUser);
				if (!warehouseService.SubStationISExist(substationNo)) {
					render(JSON_TYPE, CommonResponse.respFailJson("9000",
							"当前用户无分配分站,请联系客服人员分配分站", params.get("reqNo")),
							response);
					return;
				}
				String lgcNo = warehouseService.getLgcNo(substationNo);
				String substationName = warehouseService
						.getSubStationName(substationNo);
				List<Map<String, Object>> errorList = new ArrayList<Map<String, Object>>();
				Map<String, Object> map = new HashMap<String, Object>();
				for (String order : orderNo) {
					Date nowDate = new Date();
					OrderInfo orderInfo = orderInfoService.getByLgcOrderNo(order, lgcNo);
					if (orderInfo == null) {
						// String EorderNo =
						// sequenceService.getNextVal("order_no");
						// OrderTrack track = new OrderTrack() ;
						// track.setOrderNo(EorderNo);
						// track.setContext("订单被创建");
						// track.setOrderTime(DateUtils.formatDate(nowDate));
						// track.setCompleted("N");
						// track.setOrderStatus("INIT");
						// orderTrackService.add(track);
						//
						// OrderTrack orderTrack =
						// orderTrackService.queryOrderTrack(EorderNo);
						// if(orderTrack!=null){
						// int lastId=orderTrack.getId();
						// orderTrackService.updateIsLast(lastId);//将上一条ID
						//
						// String managerRealName=
						// warehouseService.getManagerRealName(managerUser);
						// OrderTrack track1 = new OrderTrack() ;
						// track1.setOrderNo(EorderNo);
						// track1.setContext(managerUser+",仓管员:"+managerRealName+",已进行入仓扫描,当前快件在"+substationName);
						// track1.setOrderTime(DateUtils.formatDate(nowDate));
						// track1.setCompleted("N");
						// track1.setPreNo(orderTrack.getCurNo());
						// track1.setPreType(orderTrack.getCurType());
						// track1.setCurNo(substationNo);
						// track1.setCurType("S");
						// track1.setNextNo("");
						// track1.setNextType("");
						// track1.setOrderStatus("REV");//到站
						// track1.setParentId(lastId);
						// track1.setIsLast("Y"); //
						// track1.setScanIno(managerUser);
						// track1.setScanIname(managerRealName);
						// orderTrackService.add(track1);
						// }
						//
						//
						// orderInfo = new OrderInfo();
						// orderInfo.setSubStationNo(substationNo);
						// orderInfo.setLgcNo(lgcNo);
						// orderInfo.setOrderNo(EorderNo);
						// orderInfo.setLgcOrderNo(order);
						// orderInfo.setCreateTime(DateUtils.formatDate(nowDate));
						// orderInfo.setLastUpdateTime(DateUtils.formatDate(nowDate));
						// warehouseService.insertNotInfo(orderInfo);
						//
						// Map<String,Object> paramsMap = new
						// HashMap<String,Object>();
						// paramsMap.put("managerNo", managerUser);
						// paramsMap.put("lgcOrderNo", order);
						// paramsMap.put("createTime",
						// DateUtils.formatDate(nowDate,
						// "yyyy-MM-dd HH:mm:ss"));
						// paramsMap.put("inputOrOutput", "I");
						// paramsMap.put("outSubOrCourier",null);
						// paramsMap.put("signal", "Y");
						// paramsMap.put("errorContext", null);
						// warehouseService.savaInputOrOutPut(paramsMap);

//						map = new HashMap<String, Object>();
//						map.put("orderNo", order);
//						map.put("error", "运单号未绑定订单");
//						errorList.add(map);
//
//						Map<String, Object> paramsMap = new HashMap<String, Object>();
//						paramsMap.put("managerNo", managerUser);
//						paramsMap.put("lgcOrderNo", order);
//						paramsMap.put("createTime", DateUtils.formatDate(
//								nowDate, "yyyy-MM-dd HH:mm:ss"));
//						paramsMap.put("inputOrOutput", "I");
//						paramsMap.put("outSubOrCourier", null);
//						paramsMap.put("signal", "N");
//						paramsMap.put("errorContext", "运单号未绑定订单");
//						warehouseService.savaInputOrOutPut(paramsMap);
//						continue;
						orderInfoService.insertNotInfo("KDWZ.WAREHOUSE.IN", nowDate, substationNo, lgcNo, order);
						orderInfo = orderInfoService.getByLgcOrderNo(order, lgcNo);
					}
					if (1 == orderInfo.getDeled()) {
						map = new HashMap<String, Object>();
						map.put("orderNo", order);
						map.put("error", "运单号为无效单");
						errorList.add(map);

						Map<String, Object> paramsMap = new HashMap<String, Object>();
						paramsMap.put("managerNo", managerUser);
						paramsMap.put("lgcOrderNo", order);
						paramsMap.put("createTime", DateUtils.formatDate(
								nowDate, "yyyy-MM-dd HH:mm:ss"));
						paramsMap.put("inputOrOutput", "I");
						paramsMap.put("outSubOrCourier", null);
						paramsMap.put("signal", "N");
						paramsMap.put("errorContext", "运单号为无效单");
						warehouseService.savaInputOrOutPut(paramsMap);
						continue;
					}
					if (orderInfo.getStatus() == 3) {
						map = new HashMap<String, Object>();
						map.put("orderNo", order);
						map.put("error", "当前运单已完成配送,无法再次入仓");
						errorList.add(map);
						Map<String, Object> paramsMap = new HashMap<String, Object>();
						paramsMap.put("managerNo", managerUser);
						paramsMap.put("lgcOrderNo", order);
						paramsMap.put("createTime", DateUtils.formatDate(
								nowDate, "yyyy-MM-dd HH:mm:ss"));
						paramsMap.put("inputOrOutput", "I");
						paramsMap.put("outSubOrCourier", null);
						paramsMap.put("signal", "N");
						paramsMap.put("errorContext", "当前运单已完成配送,无法再次入仓");
						warehouseService.savaInputOrOutPut(paramsMap);
						continue;
					}

					Map<String, Object> paramsMap = new HashMap<String, Object>();
					paramsMap.put("managerNo", managerUser);
					paramsMap.put("lgcOrderNo", order);
					paramsMap.put("createTime", DateUtils.formatDate(nowDate,
							"yyyy-MM-dd HH:mm:ss"));
					paramsMap.put("inputOrOutput", "I");
					paramsMap.put("outSubOrCourier", null);
					paramsMap.put("signal", "Y");
					paramsMap.put("errorContext", null);
					warehouseService.savaInputOrOutPut(paramsMap);

					warehouseService.managerUpdateOrderStatus(paramsMap);// 入仓及更改状态
					String managerRealName = warehouseService
							.getManagerRealName(managerUser);

			
					OrderTrack orderTrack = orderTrackService.queryOrderTrack(orderInfo.getOrderNo());

					if (orderTrack != null) {
						
						String content = 	orderTrack.getContext();
						String context = managerUser+",仓管员:"+managerRealName+",已进行入仓扫描,当前快件在"+substationName;
						if(!(content.trim()).equals(context.trim())){													
							int lastId = orderTrack.getId();
							orderTrackService.updateIsLast(lastId);// 将上一条ID
							OrderTrack track1 = new OrderTrack();
							track1.setOrderNo(orderInfo.getOrderNo());
							track1.setContext(context);
							track1.setOrderTime(DateUtils.formatDate(nowDate));
							track1.setCompleted("N");
							track1.setPreNo(orderTrack.getCurNo());
							track1.setPreType(orderTrack.getCurType());
							track1.setCurNo(substationNo);
							track1.setCurType("S");
							track1.setNextNo("");
							track1.setNextType("");
							track1.setOrderStatus("REV");// 到站
							track1.setParentId(lastId);
							track1.setIsLast("Y"); //
							track1.setScanIno(managerUser);
							track1.setScanIname(managerRealName);
							track1.setOpname(managerRealName);
							orderTrackService.add(track1);			
							if ("康美".equals(orderInfo.getSource())) {
								RunnableUtils run1 = new RunnableUtils();
								run1.KangmeiPush(URL, company_num, userName,
										orderInfo.getLgcOrderNo(), managerUser
												+ ",仓管员:" + managerRealName
												+ ",已进行入仓扫描,当前快件在" + substationName,	"0");
							}
						}else{
							int  lastId=orderTrack.getId();
							orderTrackService.updateIsLastTime(lastId);//将上一条ID		
						}
							
						
			
					}
				}
				model = new HashMap<String, Object>();
				model.put("errorList", errorList);
				render(JSON_TYPE,
						CommonResponse.respSuccessJson("上传成功", model,
								params.get("reqNo")), response);

			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,
						CommonResponse.respFailJson(ret.get("respCode"),
								ret.get("respMsg"), params.get("reqNo")),
						response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9000", "服务器异常",
							params.get("reqNo")), response);
		}
	}

	// 出仓扫描
	@RequestMapping(value = "/output")
	public void output(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.managerValidateRequest(
					request, reqParams(new String[] { "orderNo[]", "numberNo",
							"signal" }), true, userSessionService,
					checkVersion, appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String managerUser = ret.get("userNo");
				String numberNo = params.get("numberNo");// 快递员号码 或者 分站编号
				String signal = params.get("signal");// 出仓标识
				System.out.println("managerUser======================"+ managerUser + "==============");
				String orderNo[] = request.getParameterValues("orderNo[]");
				if (orderNo.length < 1) {
					render(JSON_TYPE,
							CommonResponse.respFailJson("9037", "运单号为空",
									params.get("reqNo")), response);
					return;
				}

				String substationNo = warehouseService
						.getManagerSubStationByUserNo(managerUser);
				if (!warehouseService.SubStationISExist(substationNo)) {
					render(JSON_TYPE, CommonResponse.respFailJson("9000",
							"当前用户无分配分站,请联系客服人员分配分站", params.get("reqNo")),
							response);
					return;
				}
				String lgcNo = warehouseService.getLgcNo(substationNo);
				String substationName = warehouseService
						.getSubStationName(substationNo);
				List<Map<String, Object>> errorList = new ArrayList<Map<String, Object>>();
				Map<String, Object> map = new HashMap<String, Object>();

				if ("C".equals(signal)) {
					User users  =	userService.getUserByNo(numberNo);
					if(!substationNo.equals(users.getSubstationNo())){
						render(JSON_TYPE, CommonResponse.respFailJson("9009","当前不是本站快递员,请重新选择", params.get("reqNo")), response);
						return;	
					}		
					System.out.println("-==========出仓给快递员=============");
					for (String order : orderNo) {
						Date nowDate = new Date();
						OrderInfo orderInfo = orderInfoService.getByLgcOrderNo(
								order, lgcNo);
						if (orderInfo == null) {
//							map = new HashMap<String, Object>();
//							map.put("orderNo", order);
//							map.put("error", "运单号未绑定订单");
//							errorList.add(map);
//							Map<String, Object> paramsMap = new HashMap<String, Object>();
//							paramsMap.put("managerNo", managerUser);
//							paramsMap.put("lgcOrderNo", order);
//							paramsMap.put("createTime", DateUtils.formatDate(
//									nowDate, "yyyy-MM-dd HH:mm:ss"));
//							paramsMap.put("inputOrOutput", "O");
//							paramsMap.put("outSubOrCourier", "COURIER");
//							paramsMap.put("outNumberNo", numberNo);
//							paramsMap.put("signal", "N");
//							paramsMap.put("errorContext", "运单号未绑定订单");
//							warehouseService.savaInputOrOutPut(paramsMap);
//							continue;
							orderInfoService.insertNotInfo("KDWZ.WAREHOUSE.OUT", nowDate, substationNo, lgcNo, order);
							orderInfo = orderInfoService.getByLgcOrderNo(order, lgcNo);
						}
						if (1 == orderInfo.getDeled()) {

							map = new HashMap<String, Object>();
							map.put("orderNo", order);
							map.put("error", "运单号为无效单");
							errorList.add(map);

							Map<String, Object> paramsMap = new HashMap<String, Object>();
							paramsMap.put("managerNo", managerUser);
							paramsMap.put("lgcOrderNo", order);
							paramsMap.put("createTime", DateUtils.formatDate(
									nowDate, "yyyy-MM-dd HH:mm:ss"));
							paramsMap.put("inputOrOutput", "I");
							paramsMap.put("outSubOrCourier", null);
							paramsMap.put("signal", "N");
							paramsMap.put("errorContext", "运单号为无效单");
							warehouseService.savaInputOrOutPut(paramsMap);
							continue;
						}
						if (orderInfo.getStatus() == 3) {
							map = new HashMap<String, Object>();
							map.put("orderNo", order);
							map.put("error", "当前运单已完成配送,无法再次出仓");
							errorList.add(map);
							Map<String, Object> paramsMap = new HashMap<String, Object>();
							paramsMap.put("managerNo", managerUser);
							paramsMap.put("lgcOrderNo", order);
							paramsMap.put("createTime", DateUtils.formatDate(
									nowDate, "yyyy-MM-dd HH:mm:ss"));
							paramsMap.put("inputOrOutput", "O");
							paramsMap.put("outSubOrCourier", "COURIER");
							paramsMap.put("outNumberNo", numberNo);
							paramsMap.put("signal", "N");
							paramsMap.put("errorContext", "当前运单已完成配送,无法再次出仓");
							warehouseService.savaInputOrOutPut(paramsMap);
							continue;
						}

						Map<String, Object> paramsMap = new HashMap<String, Object>();
						paramsMap.put("managerNo", managerUser);
						paramsMap.put("lgcOrderNo", order);
						paramsMap.put("createTime", DateUtils.formatDate(
								nowDate, "yyyy-MM-dd HH:mm:ss"));
						paramsMap.put("inputOrOutput", "O");
						paramsMap.put("outSubOrCourier", "COURIER");
						paramsMap.put("outNumberNo", numberNo);
						paramsMap.put("signal", "Y");
						paramsMap.put("errorContext", null);
						warehouseService.savaInputOrOutPut(paramsMap);// 新增出仓记录

						paramsMap.put("sendCourierNo", numberNo);
						paramsMap.put("sendSubstationNo", substationNo);

						warehouseService.managerUpdateOrder(paramsMap);// 将运单分配给快递员

						OrderTrack orderTrack = orderTrackService
								.queryOrderTrack(orderInfo.getOrderNo());
						if (orderTrack != null) {

							String managerRealName = warehouseService.getManagerRealName(managerUser);

							User user = userService.getUserByNo(numberNo);

							String content = orderTrack.getContext();

							String context ="快递员" + user.getRealName()+ "将很快进行派送，请保持电话畅通" + user.getPhone() ;
//							System.out.println(content);
//							System.out.println(context);
//							System.out.println((content.trim()).equals(context.trim()));
							if(!(content.trim()).equals(context.trim())){

								int lastId = orderTrack.getId();
								orderTrackService.updateIsLast(lastId);// 将上一条ID

								OrderTrack track1 = new OrderTrack();
								track1.setOrderNo(orderInfo.getOrderNo());
								track1.setContext(managerUser + ",仓管员:"	+ managerRealName + ",已进行出仓扫描,快递员"
										+ user.getRealName() + "将很快进行派送，请保持电话畅通"
										+ user.getPhone());
								track1.setOrderTime(DateUtils.formatDate(nowDate));
								track1.setCompleted("N");
								track1.setPreNo(orderTrack.getCurNo());
								track1.setPreType(orderTrack.getCurType());
								track1.setCurNo(substationNo);
								track1.setCurType("S");
								track1.setNextNo(user.getCourierNo());
								track1.setNextType("C");
								track1.setOrderStatus("SEND");
								track1.setScanIno(orderTrack.getScanIno());
								track1.setScanIname(orderTrack.getScanIname());
								track1.setScanOno(managerUser);
								track1.setScanOname(managerRealName);
								track1.setParentId(lastId);
								track1.setIsLast("N");
								track1.setOpname(managerRealName);
								orderTrackService.add(track1);	

								OrderTrack track2 = new OrderTrack();
								track2.setOrderNo(orderInfo.getOrderNo());
								track2.setContext(context);
								track2.setOrderTime(DateUtils.formatDate(nowDate));
								track2.setCompleted("N");
								track2.setPreNo(substationNo);
								track2.setPreType("S");
								track2.setCurNo(user.getCourierNo());
								track2.setCurType("C");
								track2.setNextNo("");
								track2.setNextType("");
								track2.setOrderStatus("SIGNING");
								track2.setScanIno("");
								track2.setScanIname("");
								track2.setScanOno(managerUser);
								track2.setScanOname(managerRealName);
								track2.setParentId(lastId);
								track2.setIsLast("Y");
								track2.setOpname(managerRealName);
								orderTrackService.add(track2);

								String uid = params.get("uid") ;
								if (uid.contains("_")) {
									uid = uid.substring(0, uid.indexOf("_"));
								}
								
								String isMessage = lgcService.getLgcConfig("MOBILE_CONFIG", "SEND_SEND_MSG", "0") ;
								if ("1".equals(isMessage)) {
									if(StringUtils.isNotEmptyWithTrim(orderInfo.getRevPhone())&&orderInfo.getRevPhone().length()==11){
										String s = user.getRealName()+"快递员正在派送中，电话："+user.getPhone()+"，请耐心等待。【快递王子】";
										if("kuaike".equals(uid)){
											s ="您好！欢迎您使用快客同城速配，现有您一件同城货物派送中，单号"+orderInfo.getLgcOrderNo()+".请您电话保持畅通，我们将及时为您配送。下单、查询、投诉建议请关注微信公众号“快刻同城速配”，服务热线：0592－7127770【快递王子】";
										}
										String channel = "szkyt" ;
										if("yx".equals(uid)){
											content ="亿翔快递--尊敬的"+StringUtil.nullString(orderInfo.getRevName())+"先生/女士 ，您的快递："+orderInfo.getLgcOrderNo()+"已由"+user.getRealName()+"快递员即将配送，电话："+user.getPhone()+",请留意收件并保持电话畅通。如您家中有生活垃圾需要带出请告知快递员，感谢您使用亿翔快递，祝您生活愉快！【快递王子】";
										    channel="yx";
										}
										RunnableUtils ui = new RunnableUtils();
										ui.MessagePushClass(channel,lgcNo, orderInfo.getRevPhone(),s,lgcService);	
									}	
								}
								
							/*	if("kuaike".equals(uid)){
									if(StringUtils.isNotEmptyWithTrim(orderInfo.getRevPhone())&&orderInfo.getRevPhone().length()==11){
										String content1 ="您好！欢迎您使用快客同城速配，现有您一件同城货物派送中，单号"+orderInfo.getLgcOrderNo()+".请您电话保持畅通，我们将及时为您配送。下单、查询、投诉建议请关注微信公众号“快刻同城速配”，服务热线：0592－7127770【快递王子】";
										RunnableUtils ui = new RunnableUtils();
										ui.MessagePushClass(lgcNo, orderInfo.getRevPhone(),content1);	
									}				
								}*/
								
								if ("康美".equals(orderInfo.getSource())) {
									RunnableUtils run1 = new RunnableUtils();
									run1.KangmeiPush(URL,	company_num,	userName,orderInfo.getLgcOrderNo(),"快递员" + user.getRealName()+ "将很快进行派送，请保持电话畅通"+ user.getPhone(), "0");

								}

							}else{
								int  lastId=orderTrack.getId();
								orderTrackService.updateIsLastTime(lastId);//将上一条ID										
							}				
						}
					}
				}
				if ("S".equals(signal)) {
					Date nowDate = new Date();

					System.out.println("-==========出仓给分站=============");
					for (String order : orderNo) {
						OrderInfo orderInfo = orderInfoService.getByLgcOrderNo(order, lgcNo);
						if (orderInfo == null) {
							orderInfoService.insertNotInfo("KDWZ.WAREHOUSE.OUT.MANAGER", nowDate, substationNo, lgcNo, order);
							orderInfo = orderInfoService.getByLgcOrderNo(order, lgcNo);
						}
						if (orderInfo == null) {
							map = new HashMap<String, Object>();
							map.put("orderNo", order);
							map.put("error", "运单号未绑定订单");
							errorList.add(map);
							Map<String, Object> paramsMap = new HashMap<String, Object>();
							paramsMap.put("managerNo", managerUser);
							paramsMap.put("lgcOrderNo", order);
							paramsMap.put("createTime", DateUtils.formatDate(
									nowDate, "yyyy-MM-dd HH:mm:ss"));
							paramsMap.put("inputOrOutput", "O");
							paramsMap.put("outSubOrCourier", "SUBSTATION");
							paramsMap.put("outNumberNo", numberNo);
							paramsMap.put("signal", "N");
							paramsMap.put("errorContext", "运单号未绑定订单");
							warehouseService.savaInputOrOutPut(paramsMap);
							continue;
						}
						if (1 == orderInfo.getDeled()) {
							map = new HashMap<String, Object>();
							map.put("orderNo", order);
							map.put("error", "运单号为无效单");
							errorList.add(map);

							Map<String, Object> paramsMap = new HashMap<String, Object>();
							paramsMap.put("managerNo", managerUser);
							paramsMap.put("lgcOrderNo", order);
							paramsMap.put("createTime", DateUtils.formatDate(
									nowDate, "yyyy-MM-dd HH:mm:ss"));
							paramsMap.put("inputOrOutput", "I");
							paramsMap.put("outSubOrCourier", null);
							paramsMap.put("signal", "N");
							paramsMap.put("errorContext", "运单号为无效单");
							warehouseService.savaInputOrOutPut(paramsMap);
							continue;
						}
						if (orderInfo.getStatus() == 3) {
							map = new HashMap<String, Object>();
							map.put("orderNo", order);
							map.put("error", "当前运单已完成配送,无法再次出仓");
							errorList.add(map);
							Map<String, Object> paramsMap = new HashMap<String, Object>();
							paramsMap.put("managerNo", managerUser);
							paramsMap.put("lgcOrderNo", order);
							paramsMap.put("createTime", DateUtils.formatDate(
									nowDate, "yyyy-MM-dd HH:mm:ss"));
							paramsMap.put("inputOrOutput", "O");
							paramsMap.put("outSubOrCourier", "SUBSTATION");
							paramsMap.put("outNumberNo", numberNo);
							paramsMap.put("signal", "N");
							paramsMap.put("errorContext", "当前运单已完成配送,无法再次出仓");
							warehouseService.savaInputOrOutPut(paramsMap);
							continue;
						}

						Map<String, Object> paramsMap = new HashMap<String, Object>();
						paramsMap.put("managerNo", managerUser);
						paramsMap.put("lgcOrderNo", order);
						paramsMap.put("createTime", DateUtils.formatDate(
								nowDate, "yyyy-MM-dd HH:mm:ss"));
						paramsMap.put("inputOrOutput", "O");
						paramsMap.put("outSubOrCourier", "SUBSTATION");
						paramsMap.put("outNumberNo", numberNo);
						paramsMap.put("signal", "Y");
						paramsMap.put("errorContext", null);
						warehouseService.savaInputOrOutPut(paramsMap);

						warehouseService
								.managerUpdateOrderToSubstation(paramsMap);// 将运单出仓到分站

						OrderTrack orderTrack = orderTrackService
								.queryOrderTrack(orderInfo.getOrderNo());

						String managerRealName = warehouseService
								.getManagerRealName(managerUser);

						if ("康美".equals(orderInfo.getSource())) {
							RunnableUtils run1 = new RunnableUtils();
							run1.KangmeiPush(
									URL,
									company_num,
									userName,
									orderInfo.getLgcOrderNo(),
									managerUser
											+ ",仓管员:"
											+ managerRealName
											+ ",已进行出仓扫描,将很快发往下一站点"
											+ warehouseService
													.getSubStationName(numberNo),
									"0");
						}
						if (orderTrack != null) {
							String content = orderTrack.getContext();
							String context = managerUser+ ",仓管员:"+ managerRealName+ ",已进行出仓扫描,将很快发往下一站点"+ warehouseService	.getSubStationName(numberNo);
					
							if(!(content.trim()).equals(context.trim())){
								
								int lastId = orderTrack.getId();
								orderTrackService.updateIsLast(lastId);// 将上一条ID
								User user = userService.getUserByNo(numberNo);

								OrderTrack track1 = new OrderTrack();
								track1.setOrderNo(orderInfo.getOrderNo());
								track1.setContext(context);
								track1.setOrderTime(DateUtils.formatDate(nowDate));
								track1.setCompleted("N");
								track1.setPreNo(orderTrack.getCurNo());
								track1.setPreType(orderTrack.getCurType());
								track1.setCurNo(substationNo);
								track1.setCurType("S");
								track1.setNextNo(numberNo);
								track1.setNextType("S");
								track1.setOrderStatus("SEND");
								track1.setScanIno(orderTrack.getScanIno());
								track1.setScanIname(orderTrack.getScanIname());
								track1.setScanOno(managerUser);
								track1.setScanOname(managerRealName);
								track1.setParentId(lastId);
								track1.setIsLast("Y"); //
								track1.setOpname(managerRealName);
								orderTrackService.add(track1);
													
								if ("康美".equals(orderInfo.getSource())) {
									RunnableUtils run1 = new RunnableUtils();
									run1.KangmeiPush(URL,company_num,userName,orderInfo.getLgcOrderNo(),	managerUser
											+ ",仓管员:"	 +managerRealName+ ",已进行出仓扫描,将很快发往下一站点"+ warehouseService	.getSubStationName(numberNo),"0");
								}
							}else{							
								int  lastId=orderTrack.getId();
								orderTrackService.updateIsLastTime(lastId);//将上一条ID	
							}						
						}
					}
				}

				model = new HashMap<String, Object>();
				model.put("errorList", errorList);
				render(JSON_TYPE,
						CommonResponse.respSuccessJson("上传成功", model,
								params.get("reqNo")), response);

			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,
						CommonResponse.respFailJson(ret.get("respCode"),
								ret.get("respMsg"), params.get("reqNo")),
						response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9000", "服务器异常",
							params.get("reqNo")), response);
		}
	}

	// 查询快递员
	@RequestMapping(value = "/checkUser")
	public void checkUser(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.managerValidateRequest(
					request, reqParams(new String[] {}), true,
					userSessionService, checkVersion, appVersionService,
					dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String managerUser = ret.get("userNo");
				System.out.println("managerUser======================"
						+ managerUser + "==============");
				String substationNo = warehouseService
						.getManagerSubStationByUserNo(managerUser);
				if (!warehouseService.SubStationISExist(substationNo)) {
					render(JSON_TYPE, CommonResponse.respFailJson("9000",
							"当前用户无分配分站,请联系客服人员分配分站", params.get("reqNo")),
							response);
					return;
				}
				List<Map<String, Object>> courierList = warehouseService
						.querySubCourier(substationNo);
				/*
				 * 由于仓管员角色无直接公司信息，只能通过分配分站查询快递公司号
				 */
				String lgcNo = warehouseService.getLgcNo(substationNo);
				List<Map<String, Object>> subList = warehouseService
						.queryAllSub(lgcNo);
				courierList.addAll(subList);
				model = new HashMap<String, Object>();
				model.put("infoList", courierList);
				render(JSON_TYPE,
						CommonResponse.respSuccessJson("创建成功", model,
								params.get("reqNo")), response);
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,
						CommonResponse.respFailJson(ret.get("respCode"),
								ret.get("respMsg"), params.get("reqNo")),
						response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9000", "服务器异常",
							params.get("reqNo")), response);
		}
	}

	// 查询出入仓历史
	@RequestMapping(value = "/checkList")
	public void checkList(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.managerValidateRequest(
					request, reqParams(new String[] { "io" }), true,
					userSessionService, checkVersion, appVersionService,
					dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				setPageInfo(params);
				Date noeDate = new Date();
				String managerUser = ret.get("userNo");
				params.put("managerUser", managerUser);
				params.put("signal", "Y");
				if (StringUtil.isEmptyWithTrim(params.get("beginTime"))) {
					params.put("startDate",
							DateUtils.formatDate(noeDate, "yyyy-MM-dd"));
				}
				Page<Map<String, Object>> orderList = warehouseService.list(
						params, pageRequest);
				model = new HashMap<String, Object>();
				model.put("orderList", orderList);
				model.put("totalCount", orderList.getTotalElements());
				model.put("cp", orderList.getNumber() + 1);
				model.put("isLastPage", orderList.isLastPage());
				;
				render(JSON_TYPE,
						CommonResponse.respSuccessJson("", model,
								params.get("reqNo")), response);
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,
						CommonResponse.respFailJson(ret.get("respCode"),
								ret.get("respMsg"), params.get("reqNo")),
						response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9000", "服务器异常",
							params.get("reqNo")), response);
		}
	}

	// 查询当日成功出入仓记录
	@RequestMapping(value = "/checkCount")
	public void checkCount(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.managerValidateRequest(
					request, reqParams(new String[] { "IOSignal" }), true,
					userSessionService, checkVersion, appVersionService,
					dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				setPageInfo(params);
				Date noeDate = new Date();
				String managerUser = ret.get("userNo");
				params.put("managerUser", managerUser);
				params.put("signal", "Y");
				params.put("startDate",
						DateUtils.formatDate(noeDate, "yyyy-MM-dd"));
				String count = warehouseService.count(params);
				model = new HashMap<String, Object>();
				model.put("count", count);
				render(JSON_TYPE,
						CommonResponse.respSuccessJson("", model,
								params.get("reqNo")), response);
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,
						CommonResponse.respFailJson(ret.get("respCode"),
								ret.get("respMsg"), params.get("reqNo")),
						response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9000", "服务器异常",
							params.get("reqNo")), response);
		}
	}
	// 仓管员批量问题件扫描
	@RequestMapping(value = "/batchPro")
	public void batchPro(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.managerValidateRequest(request, reqParams(new String[] { "lgcOrderNo" ,"reasonNo"}), true,
					userSessionService, checkVersion, appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				
			
				OrderInfo order  =  orderInfoService.getByLgcOrderNo(params.get("lgcOrderNo"));
				if(order==null){
					render(JSON_TYPE, CommonResponse.respFailJson("9013","订单不存在", params.get("reqNo")), response);		 
					return;	
				}
				if(order.getStatus()==1 || order.getStatus()==3 ){
					render(JSON_TYPE, CommonResponse.respFailJson("9048","订单状态不合法,已完成或未收件", params.get("reqNo")), response);		 
					return;	
				}					
	
				String loginUserNo = ret.get("userNo") ;
				String managerName = warehouseService.getMangerRealName(loginUserNo);
				String substationNo = warehouseService.getManagerSubStationByUserNo(loginUserNo);
				Date nowDate = new Date();
				String reasonNo= params.get("reasonNo");
				String reasonContext  ="";	
				reasonContext=reasonContext+ userService.queryReasonContext(reasonNo);//获取问题原因
				if(!StringUtil.isEmptyWithTrim(params.get("reasonContext"))){
					reasonContext =reasonContext+"详细原因："+params.get("reasonContext") ;
				}
		
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
			

				Map<String, Object> sMap = lgcService.getSubstationInfo(substationNo) ;
				OrderTrack track1 = new OrderTrack() ;
				track1.setOrderNo(orderNo);
//				track1.setContext("仓管员："+loginUserNo+"标为问题件："+reasonContext);
				track1.setContext("问题件："+reasonContext);
				track1.setOrderTime(DateUtils.formatDate(nowDate));
				track1.setPreNo(cur_no);
				track1.setPreType(cur_type);
				track1.setCompleted("N");
				track1.setCurNo(loginUserNo);
				track1.setCurType("C");
				track1.setNextNo(substationNo);
				track1.setNextType("S");
				track1.setOrderStatus("PRO");
				track1.setParentId(id);
				track1.setIsLast("Y");    //
				track1.setOpname(managerName);
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
				refuseMap.put("check_name", loginUserNo);//登记人
				long proOrderID=0l;
				Map<String,Object> questionCheck = orderInfoService.doubtCheck(orderNo);//查询问题件是否存在
				if(questionCheck==null){					
					proOrderID=	orderInfoService.refuse(refuseMap);
				}else{
					proOrderID =	orderInfoService.updateQuestion(refuseMap);//更新到最新的问题件信息
				}

				String uid = params.get("uid");
				uid = uid.substring(0, uid.indexOf("_"));
				if ("yx".equals(uid)) {
					orderInfoService.YXreusePro(orderNo,reasonContext,String.valueOf(proOrderID),substationNo,loginUserNo) ;
				}else{
					orderInfoService.reusePro(orderNo,reasonContext,String.valueOf(proOrderID)) ;
				}
				
				orderInfoService.insertBatchPROCount(order.getLgcOrderNo(),loginUserNo);

				/***
				 * 一米鲜订单信息更新推送
				 * 
				 */

				if("YMX".equals(order.getSource())){
					System.out.println("一米鲜快递推送通知");
					String info ="您的订单："+orderNo+" 被客户拒签，或将二次投递";
					RunnableUtils ymx = new RunnableUtils();
					ymx.pushTMX(order.getOrderNote(), "4", orderNo,	managerName, "", info, DateUtils.formatDate(nowDate,"yyyy-MM-dd HH:mm:ss"),params.get("uid"));
				}

//				/**
//				 * 
//				 * 微信推送
//				 * 
//				 * */
//				if(!StringUtil.isEmptyWithTrim(order.getWxOpenid())){
//					System.out.println("微信推送线程开始");
//					RunnableUtils run1   = new RunnableUtils();
//					run1.pushWEIXIN(order, loginUser,6);	
//				}				
				render(JSON_TYPE, CommonResponse.respSuccessJson("","状态更新成功", params.get("reqNo")), response); 			
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,	CommonResponse.respFailJson(ret.get("respCode"),
								ret.get("respMsg"), params.get("reqNo")),	response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9000", "服务器异常",
							params.get("reqNo")), response);
		}
	}
	// 仓管员批量问题件扫描
	@RequestMapping(value = "/error")
	public void error(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.managerValidateRequest(request, reqParams(new String[] { }), true,
					userSessionService, checkVersion, appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				List<Map<String, Object>> list = userService.getProOrderReason();
				model = new HashMap<String, Object>();
				model.put("list", list);
				render(JSON_TYPE,
						CommonResponse.respSuccessJson("", model,
								params.get("reqNo")), response);
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,	CommonResponse.respFailJson(ret.get("respCode"),
						ret.get("respMsg"), params.get("reqNo")),	response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9000", "服务器异常",
							params.get("reqNo")), response);
		}
	}
	// 仓管员批量问题件扫描
	@RequestMapping(value = "/proCount")
	public void proCount(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.managerValidateRequest(request, reqParams(new String[] { }), true,
					userSessionService, checkVersion, appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				
				String userNo = ret.get("userNo");
				model = new HashMap<String, Object>();
				model.put("count", orderInfoService.getPROCount(DateUtils.formatDate(new Date(), "yyyy-MM-dd"),userNo));
				render(JSON_TYPE, CommonResponse.respSuccessJson("",model, params.get("reqNo")), response);   				
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,	CommonResponse.respFailJson(ret.get("respCode"),
						ret.get("respMsg"), params.get("reqNo")),	response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9000", "服务器异常",
							params.get("reqNo")), response);
		}
	}

	
	
	
}
