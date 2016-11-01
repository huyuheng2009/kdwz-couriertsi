package com.yogapay.couriertsi.api;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
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
import com.yogapay.couriertsi.domain.OrderPic;
import com.yogapay.couriertsi.domain.OrderTrack;
import com.yogapay.couriertsi.domain.PushMsg;
import com.yogapay.couriertsi.domain.RunnableUtils;
import com.yogapay.couriertsi.domain.User;
import com.yogapay.couriertsi.domain.WxxdConfig;
import com.yogapay.couriertsi.dto.OrderDto;
import com.yogapay.couriertsi.dto.PosInfoDto;
import com.yogapay.couriertsi.enums.BizType;
import com.yogapay.couriertsi.enums.MsgType;
import com.yogapay.couriertsi.enums.PayStatus;
import com.yogapay.couriertsi.enums.PayType;
import com.yogapay.couriertsi.enums.PushUserType;
import com.yogapay.couriertsi.exception.FileUnknowTypeException;
import com.yogapay.couriertsi.services.LgcService;
import com.yogapay.couriertsi.services.MsgService;
import com.yogapay.couriertsi.services.MuserService;
import com.yogapay.couriertsi.services.OrderInfoService;
import com.yogapay.couriertsi.services.OrderPicService;
import com.yogapay.couriertsi.services.OrderSubstationService;
import com.yogapay.couriertsi.services.OrderTrackService;
import com.yogapay.couriertsi.services.OtherService;
import com.yogapay.couriertsi.services.PosInfoService;
import com.yogapay.couriertsi.services.UserService;
import com.yogapay.couriertsi.services.WarehouseService;
import com.yogapay.couriertsi.services.WxxdConfigService;
import com.yogapay.couriertsi.utils.CommonResponse;
import com.yogapay.couriertsi.utils.ConstantsLoader;
import com.yogapay.couriertsi.utils.DateUtils;
import com.yogapay.couriertsi.utils.JsonUtil;
import com.yogapay.couriertsi.utils.MapConverter;
import com.yogapay.couriertsi.utils.Md5;
import com.yogapay.couriertsi.utils.PosUtil;
import com.yogapay.couriertsi.utils.PushUtil;
import com.yogapay.couriertsi.utils.RequestFile;
import com.yogapay.couriertsi.utils.ResizeImage;
import com.yogapay.couriertsi.utils.SHA;
import com.yogapay.couriertsi.utils.StringUtil;
import com.yogapay.couriertsi.utils.StringUtils;
import com.yogapay.couriertsi.utils.ValidateUtil;
import com.yogapay.couriertsi.utils.WeiXinUtil;

/**
 * 订单接口类
 * 
 * @author hhh
 * 
 */
@Controller
@RequestMapping(value = "/order", method = RequestMethod.POST)
@Scope("prototype")
public class OrderApi extends BaseApi {

	@Resource
	private OrderInfoService orderInfoService;
	@Resource
	private OrderPicService orderPicService;
	@Resource
	private UserService userService;
	@Resource
	private MsgService msgService;
	@Resource
	private PosInfoService posInfoService;
	@Resource
	private OrderTrackService orderTrackService;
	@Resource
	private LgcService lgcService;
	@Resource
	private MuserService muserService;
	@Resource
	private WarehouseService warehouseService;
	@Resource
	private OtherService otherService;
	@Resource
	private WxxdConfigService wxxdConfigService ;
	@Resource
	private OrderSubstationService orderSubstationService;
	@Value("#{config['yx_cm_track_url']}")
	String URL;
	@Value("#{config['kangmei_company_num']}")
	String company_num;
	@Value("#{config['kangmei_user_name']}")
	String userName;

	// 新增订单
	@RequestMapping(value = "/add")
	public void add(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {

			System.out.println(params);
			String[] orderParams = new String[] { "freightType"};
			String[] freightTypeParams = new String[] { "monthSettleNo"};
			// String [] codParams = new String[] {
			// "goodPrice","codCardNo","codName","codCardCnapsNo","codBank"} ;
			String[] codParams = new String[] { "goodPrice" };
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(orderParams), true, userSessionService,checkVersion, appVersionService, dynamicDataSourceHolder);
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
				if(params.get("lgcOrderNo").contains(".")||params.get("lgcOrderNo").contains("'")){
					render(JSON_TYPE, CommonResponse.respFailJson("9034","订单号违法，不能包含标点。", params.get("reqNo")), response);
					return;
				}
				if(orderInfoService.isExistLgcOrderNo(params.get("lgcOrderNo"),lgcNo)){
					render(JSON_TYPE, CommonResponse.respFailJson("9034","订单号已存在", params.get("reqNo")), response);
					return;
				}
				if (!"1".equals(params.get("itemType"))&& !"2".equals(params.get("itemType"))) {
					render(JSON_TYPE, CommonResponse.respFailJson("9026","参数不正确：itemType", params.get("reqNo")), response);
					return;
				}
				if (!"1".equals(params.get("freightType"))&& !"2".equals(params.get("freightType"))) {
					render(JSON_TYPE, CommonResponse.respFailJson("9026",
							"参数不正确：freightType", params.get("reqNo")), response);
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
				if ("1".equals(params.get("cod"))) {
					ret = ValidateUtil.validateRequest(request,	reqParams(codParams), false, null, false, null,dynamicDataSourceHolder);
					if (!"TRUE".equals(ret.get("isSuccess"))) {
						log.info("validate false!!!!---->"+ ret.get("respCode") + "---------"+ ret.get("respMsg"));
						render(JSON_TYPE, CommonResponse.respFailJson(ret.get("respCode"), ret.get("respMsg"),params.get("reqNo")), response);
						return;
					}
				}
				// //////////////////////////////////////////////////////////////////
				OrderInfo orderInfo = (OrderInfo) MapConverter.convertMap(	OrderInfo.class, params);

				orderInfo.setLgcNo(lgcNo);
				String[] sendPhones = request.getParameterValues("sendPhone[]");
				orderInfo.setSendPhone(StringUtils.combineArray(sendPhones));
				String[] revPhones = request.getParameterValues("revPhone[]");
				orderInfo.setRevPhone(StringUtils.combineArray(revPhones));
				orderInfo.setTakeTime(null);
				orderInfo.setTakeTimeBegin(null);
				orderInfo.setTakeTimeEnd(null);
				// orderInfo.setTakeAddr(orderInfo.getSendArea()+orderInfo.getSendAddr());
				String orderNo = sequenceService.getNextVal("order_no");
				orderInfo.setOrderNo(orderNo);
				orderInfo.setUserNo("k" + userNo);
				orderInfo.setCreateTime(DateUtils.formatDate(nowDate));
				orderInfo.setLastUpdateTime(DateUtils.formatDate(nowDate));
				orderInfo.setStatus(1); // 处理中
				orderInfo.setPayStatus(PayStatus.NOPAY.getValue());
				orderInfo.setSource("COURIER");
				orderInfo.setTakeOrderTime(DateUtils.formatDate(nowDate));

				List<String> mime = new ArrayList<String>();
				mime.add("image/jpeg");
				mime.add("image/pjpeg");
				mime.add("image/gif");
				mime.add("image/png");
				List<RequestFile> files = getFile(request, null,
						configInfo.getFile_root(), "/order/"+ DateUtils.formatDate(nowDate, "yyyyMMddHH"),	mime);
				List<OrderPic> imagesList = new ArrayList<OrderPic>();
				ResizeImage resizeImage = new ResizeImage();
				float resizeTimes = 1;
				for (RequestFile image : files) {
					OrderPic orderPic = new OrderPic();
					orderPic.setOrderNo(orderInfo.getOrderNo());
					orderPic.setFileType(image.getFileType());
					orderPic.setFileName(image.getFileName());
					orderPic.setFileSize(image.getFileSize());
					orderPic.setFilePath(image.getFilePath());
					orderPic.setFileUri("/codfile/order/"+ DateUtils.formatDate(nowDate, "yyyyMMddHH") + "/"	+ image.getFileName());
					orderPic.setOrginalName(image.getOrginalName());
					if (Integer.valueOf(image.getFileSize()) > 1024 * 800) { // 大于5mb压缩
						resizeTimes = (1.0f * 1024 * 800)/ Integer.valueOf(image.getFileSize());
						resizeImage.zoomImage(configInfo.getFile_root()
								+ image.getFilePath(), resizeTimes);
					}
					imagesList.add(orderPic);
				}
				// orderPicService.save(imagesList) ; //批量保存订单图片

				ret = ValidateUtil.validateRequest(request,reqParams(new String[] { "freight", "npay" }), false,null, false, null, dynamicDataSourceHolder);
				if (!"TRUE".equals(ret.get("isSuccess"))) {
					render(JSON_TYPE, CommonResponse.respFailJson(ret.get("respCode"), ret.get("respMsg"),params.get("reqNo")), response);
					return;
				}
				float goodValuationRate = 0;
				if (orderInfo.getGoodValuation() > 0) {
					ret = ValidateUtil.validateRequest(request,	reqParams(new String[] { "goodValuationRate" }),	false, null, false, null, dynamicDataSourceHolder);
					if (!"TRUE".equals(ret.get("isSuccess"))) {
						render(JSON_TYPE, CommonResponse.respFailJson(ret.get("respCode"), ret.get("respMsg"),params.get("reqNo")), response);
						return;
					}
					goodValuationRate = Float.valueOf(params.get("goodValuationRate")); // 货物保价费率
				}

				Map<String, Object> vRate = null;
				float vpay = 0;
				float freight = Math.round(Float.valueOf(params.get("freight")) * 100) / 100f;// 运费
				float goodPrice = 0;
				float npay = Math.round(Float.valueOf(params.get("npay")) * 100) / 100f;// 如果要求精确4位就*10000然后/10000
				// 此次应付金额
				float lcpay = 0;
				float lvpay = 0;
				float lpayAcount = 0;
				float lnpay = 0;
				if ("1".equals(params.get("cod"))) {
					goodPrice = Math.round(Float.valueOf(params.get("goodPrice")) * 100) / 100f;// 代收货款
					System.out.println("******goodPrice1********" + goodPrice);
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
				} else {
					orderInfo.setGoodPrice(0.00f);
				}

				if (orderInfo.getGoodValuation() > 0) {
					if (StringUtils.isEmptyWithTrim(params.get("vpay"))) {
						render(JSON_TYPE, CommonResponse.respFailJson("9001",
								"缺少参数：vpay", params.get("reqNo")), response);
						return;
					}
					vpay = Math.round(Float.valueOf(params.get("vpay")) * 100) / 100f;
					vRate = lgcService.getLgcVrate();// 货物保价费率
					lvpay = getPayByRate(orderInfo.getGoodValuation(), vRate);// 保价费
					lpayAcount = lpayAcount + lvpay;
					orderInfo
					.setGoodValuationRate(vRate.get("rate").toString());
				}

				if ("1".equals(params.get("freightType"))) {
					lnpay = lvpay + freight;// 保价费+运费
				} else {
					lnpay = 0;// 代收货款手续费+保价费
				}

				if ("MONTH".equals(params.get("payType"))) {
					orderInfo.setMpay(lvpay + freight);
				}
				System.out.println("******goodPrice2********" + goodPrice);
				System.out.println("******vpay********" + vpay);
				System.out.println("******freight********" + freight);
				System.out.println("********npay******" + npay);
				lpayAcount = lpayAcount + freight;
				System.out.println("******lcpay********" + lcpay);
				System.out.println("******lvpay********" + lvpay);
				System.out.println("******lpayAcount********" + lpayAcount);
				System.out.println("******lnpay********" + lnpay);

				if (Math.abs(lvpay - vpay) >= 0.01) {
					render(JSON_TYPE, CommonResponse.respFailJson("9026",
							"参数不正确：vpay", params.get("reqNo")), response);
					return;
				}

				// if (Math.abs(lpayAcount-payAcount)>=0.01) {
				// render(JSON_TYPE, CommonResponse.respFailJson("9026",
				// "参数不正确：payAcount", params.get("reqNo")),response);
				// return ;
				// }

				if (Math.abs(lnpay - npay) >= 0.01) {
					render(JSON_TYPE, CommonResponse.respFailJson("9026",
							"参数不正确：npay", params.get("reqNo")), response);
					return;
				}

				orderInfo.setCpay(lcpay);// 代收货款费
				orderInfo.setVpay(lvpay);// 保价手续费
				orderInfo.setFreight(freight);// 运费
				orderInfo.setPayAcount(lpayAcount);// 应付
				orderInfo.setTnpay(npay);// 取件收总额
				if ("2".equals(params.get("freightType"))
						&& "1".equals(params.get("cod"))) {					orderInfo.setSnapy(lvpay + freight + goodPrice);
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
				String lgcOrderNo = "2" + orderInfo.getOrderNo();
				if (StringUtils.isNotEmptyWithTrim(params.get("lgcOrderNo"))) {
					lgcOrderNo = params.get("lgcOrderNo");
					if (orderInfoService.isExistLgcOrderNo(lgcOrderNo, lgcNo)) {render(JSON_TYPE, CommonResponse.respFailJson("9030",	"运单号重复", params.get("reqNo")), response);
					return;
					}
				}
				orderInfo.setLgcOrderNo(lgcOrderNo);

				if (lnpay > 0) {

					if (StringUtils.isEmptyWithTrim(params.get("payType"))) {
						render(JSON_TYPE, CommonResponse.respFailJson("9001","缺少参数：payType", params.get("reqNo")), response);
						return;
					}

					String payType = params.get("payType");
					orderInfo.setPayType(payType);

					// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					/**
					 * 会员支付状态
					 * */
					if ("HUIYUAN".equals(params.get("payType"))
							&& "1".equals(params.get("freightType"))) {// 
						if (StringUtils.isEmptyWithTrim(params.get("vipNo"))) {
							render(JSON_TYPE, CommonResponse.respFailJson(	"9001", "缺少参数：vipNo", params.get("reqNo")),
									response);
							return;
						}
						if (StringUtils.isEmptyWithTrim(params.get("vipPwd"))&& StringUtils.isEmptyWithTrim(params	.get("monthSettleNo"))
								&& StringUtils.isEmptyWithTrim(params.get("monthSettleCard"))) {

							render(JSON_TYPE	,CommonResponse.respFailJson("9001","缺少参数：vipPwd", params.get("reqNo")),
									response);
							return;
						}
						Map<String, Object> map = userService.checkVipbInfo(params.get("vipNo"));
						if (map == null) {
							render(JSON_TYPE, CommonResponse.respFailJson("9031", "不存在的会员号", params.get("reqNo")),	response);
							return;
						}
						Map<String, Object> mapByPwd = userService	.checkVipbInfoByPwd(params.get("vipNo"),Md5.md5Str(params.get("vipPwd")));
						if (mapByPwd == null) {
							render(JSON_TYPE, CommonResponse.respFailJson("9031", "会员号密码错误", params.get("reqNo")),
									response);
							return;
						}
						if (!"1".equals(String.valueOf(mapByPwd.get("status")))) {
							render(JSON_TYPE, CommonResponse.respFailJson("9031", "此会员号不是启用状态", params.get("reqNo")),
									response);
							return;
						}
						float v = Float.valueOf(((BigDecimal) mapByPwd.get("balance")).toString());

						int disType = (Integer) mapByPwd.get("disType");// 优惠类型

						int uid = (Integer) mapByPwd.get("uid");// 余额表ID

						float balance = Float.valueOf(((BigDecimal) mapByPwd	.get("balance")).toString());// 余额

						System.out.println("HUIYUAN,扣除前balance余额======================"+ balance);
						Map<String, Object> disMap = userService.disType(disType);// 优惠类型数据

						int discount = (Integer) disMap.get("discount");// 折扣等级

						//float onlyFreight = Float	.valueOf(params.get("freight"))* discount/ 100f;
						float onlyFreight = Float	.valueOf(params.get("freight"));

						float reality = onlyFreight + lvpay; // 实际扣除金额

						if (v < reality) {
							render(JSON_TYPE, CommonResponse.respFailJson("9031", "会员号余额不足", params.get("reqNo")),response);
							return;
						}

						System.out.println("HUIYUAN,reality======================"+ reality);

						balance = balance - reality;

						System.out.println("HUIYUAN,扣除后balance余额======================"+ balance);
						userService.minusBalance(reality, DateUtils.formatDate(nowDate, "yyyy-MM-dd HH:mm:ss"), String.valueOf(uid));// 更新会员余额表
						Map<String, Object> insertVipHistory = new HashMap<String, Object>();
						insertVipHistory.put("dis_user_no", params.get("vipNo"));
						insertVipHistory.put("rmoney", reality);
						insertVipHistory.put("omoney", npay);
						insertVipHistory.put("af_balance", balance);
						insertVipHistory.put("status", "SUCCESS");
						insertVipHistory.put("order_no", orderInfo.getOrderNo());
						insertVipHistory.put("discount_text",disMap.get("discount_text"));
						insertVipHistory.put("lied", "N");
						insertVipHistory.put("courier_no", userNo);
						insertVipHistory.put("operator",	loginUser.getRealName());
						insertVipHistory.put("create_time",DateUtils.formatDate(new Date()));
						insertVipHistory.put("last_update_time",DateUtils.formatDate(new Date()));
						insertVipHistory.put("note", "快递员取件扣费");
						insertVipHistory.put("source", "COURIER_TAKE");
						userService.vipExpense(insertVipHistory);// VIP消费记录插入
						orderInfo.setFpayStatus("SUCCESS");
						model.put("omoney", npay);
						model.put("rmoney", reality);
						model.put("balance", balance);

						lpayAcount = lpayAcount - freight + onlyFreight;
						orderInfo.setDisRealityFreight(onlyFreight);// 运费
						orderInfo.setDisDiscount(String.valueOf(disMap	.get("discount_text")));
						orderInfo.setPayAcount(lpayAcount);
						orderInfo.setTnpay(reality);// 取件收总额
						orderInfo.setDisUserNo(params.get("vipNo"));
					}
					// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					if (payType.equals(PayType.POS.toString())) {
						log.info("**********************************pos");
						ret = ValidateUtil.validateRequest(request,reqParams(new String[] { "merchantName","merchantNo", "terminalNo","encPinblock" }), false, null, false,
								null, dynamicDataSourceHolder);
						if (!"TRUE".equals(ret.get("isSuccess"))) {
							render(JSON_TYPE,	CommonResponse.respFailJson(	ret.get("respCode"),	ret.get("respMsg"),params.get("reqNo")), response);
							return;
						}
						// encPinblock
						{
							if (StringUtils.isEmptyWithTrim(params.get("encTracks2"))
									&& StringUtils.isEmptyWithTrim(params	.get("icData"))) {
								render(JSON_TYPE, CommonResponse.respFailJson("9001", "缺少参数：encTracks2或icData",params.get("reqNo")), response);
								return;
							}
							String merchantName = params.get("merchantName"); // 保存用户打印
							String merchantNo = params.get("merchantNo");
							String terminalNo = params.get("terminalNo");
							String encTracks2 = params.get("encTracks2");
							String encTracks3 = params.get("encTracks3");
							String encPinblock = params.get("encPinblock");
							String icData = params.get("icData");
							String mac = params.get("mac");
							String localMac = SHA.SHA1Encode(params.get("reqTime") + merchantNo + freight);
							/*
							 * if (!mac.equals(localMac)) {
							 * log.info("mac:"+mac);
							 * log.info("localMac:"+localMac); render(JSON_TYPE,
							 * CommonResponse.respFailJson("9021", "非法参数，不允许交易",
							 * params.get("reqNo")),response); return ; }
							 */

							Map<String, String> posParams = new HashMap<String, String>();
							posParams.put("merchantNo", merchantNo);
							posParams.put("terminalNo", terminalNo);
							posParams.put("amount", String.format("%.2f", npay));
							posParams.put("encryptPin", encPinblock);
							posParams.put("track2", encTracks2);
							posParams.put("track3", encTracks3);
							posParams.put("icData", icData);

							log.info(posParams.toString());
							/*
							 * if (StringUtils.isEmptyWithTrim(icData)) { //磁条卡
							 * log.info("------------------------->磁条卡");
							 * posParams.put("icData", icData) ; }else { //ic卡
							 * log.info("------------------------->ic卡");
							 * posParams.put("encTracks2", encTracks2) ;
							 * posParams.put("encTracks3", encTracks3) ; }
							 */

							Map<String, Object> saleMap = PosUtil.sendAndRev(configInfo,posParams, BizType.SALE);

							System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
							System.out.println(saleMap);
							if (saleMap != null&& "SUCCESS".equals(saleMap.get("respCode"))) {								log.info("**********************************pos_su");
							// 交易成功
							orderInfo.setPayStatus(PayStatus.FREIGTH.getValue());
							model.put("amount", npay);
							model.put("currency", "CNY");
							model.put("issuer", saleMap.get("issuer"));
							model.put("cardNo", saleMap.get("cardNo"));
							model.put("opType", "消费");
							model.put("batchNo", saleMap.get("batchNo"));
							model.put("voucherNo", saleMap.get("voucherNo"));
							model.put("authNo", saleMap.get("authNo"));
							model.put("refNo", saleMap.get("refNo"));
							model.put("operatorNo",
									saleMap.get("operatorNo"));
							model.put("icData", saleMap.get("icData"));
							model.put("transTime", saleMap.get("transTime"));

							Map<String, Object> posInfo = new HashMap<String, Object>();
							posInfo.putAll(model);
							posInfo.put("merchantName", merchantName); //
							posInfo.put("merchantNo", merchantNo);
							posInfo.put("terminalNo", terminalNo);
							posInfo.put("orderNo", orderInfo.getOrderNo());
							posInfo.put("courierNo", userNo);
							posInfo.put("bizType", "FREIGHT");
							posInfoService.save(posInfo);

							} else {
								// order.setPayStatus(0);
								String msg = "未知错误";
								if (saleMap != null	&& saleMap.get("respCode") != null) {
									msg = saleMap.get("respCode").toString()	+ saleMap.get("respMsg").toString();
								}
								log.info("******************************");
								log.info(msg);
								render(JSON_TYPE, CommonResponse.respFailJson("9022", "支付失败:" + msg,params.get("reqNo")), response);
								return;
							}

						}
					} else {
						orderInfo.setPayStatus(PayStatus.FREIGTH.getValue());
						log.info("***********cash_pay************");
					}
				} else {
					orderInfo.setPayStatus(PayStatus.NOPAY.getValue());
				}

				// ////////////////

				if (StringUtils.isNotEmptyWithTrim(params.get("itemName"))) {
					orderInfo.setItemName(params.get("itemName"));
				}
				if (StringUtils.isNotEmptyWithTrim(params.get("itemWeight"))) {
					orderInfo.setItemWeight(Float.valueOf(params
							.get("itemWeight")));
				}
				User user = userService.getUserByNo(userNo);
				orderInfo.setStatus(1); // 已取件
				orderInfo.setSubStationNo(user.getSubstationNo());
				orderInfo.setTakeCourierNo(userNo);
				orderInfo.setLastUpdateTime(DateUtils.formatDate(nowDate));
				orderInfo.setTakeMark("Y");// Y为取件 N为派件


				if ("1".equals(orderInfo.getFreightType())) {
					orderInfo.setFpayStatus("SUCCESS");
				} else {
					orderInfo.setFpayStatus("INIT");
				}
				orderInfo.setIdCard(params.get("idCard"));
				if (!StringUtils.isEmptyString(params.get("receNo"))) {
					orderInfo.setReceNo(params.get("receNo"));
					orderInfo.setReqRece("Y");
				} else {
					orderInfo.setReceNo("");
					orderInfo.setReqRece("N");
				}
				if (!StringUtils.isEmptyString(params.get("zidanOrder"))) {
					if (params.get("zidanOrder").length() > 255) {
						render(JSON_TYPE, CommonResponse.respFailJson("9022","回单号数量过多,单词最多输出20个", ""), response);
						return;
					}
				}
				if (StringUtils.isEmptyString(params.get("zidanNumber"))) {
					orderInfo.setZidanNumber("0");
					orderInfo.setItemCount("1");
				} else {
					orderInfo.setZidanNumber(params.get("zidanNumber"));
					orderInfo.setItemCount((1+Integer.valueOf(params.get("zidanNumber"))+""));
				}
				OrderInfo oInfo = (OrderInfo) orderInfo.clone() ;
				oInfo.setZidanOrder(null);
				oInfo.setZidanNumber("0");
				oInfo.setItemCount(1+"");
				oInfo.setZid(1);	
				oInfo.setCpay(0);
				oInfo.setVpay(0);
				oInfo.setParentOrder(orderInfo.getOrderNo());
				oInfo.setMpay(0);
				oInfo.setGoodValuation(0);
				oInfo.setSnapy(0);
				oInfo.setGoodPrice(0);
				oInfo.setFreight(0);


				String[] zidanOrderStr = {} ;
				int zidanOrderStrLength = 0 ;
				List<String> orderNos =  new ArrayList<String>() ;//子单orderNo


				String uid = params.get("uid");
				uid = uid.substring(0, uid.indexOf("_"));

				String fix = "" ;
				/*	if ("yx".equals(uid)) {
					fix = "yx" ;
				}*/


				boolean isExist = false ;
				String st = "" ;

				if (!StringUtils.isEmptyString(params.get("zidanOrder"))) {
					String zidanOrder = params.get("zidanOrder");
					if (zidanOrder.contains(",")) {
						int i = 1;
						zidanOrderStr = zidanOrder.split(",");
						zidanOrderStrLength = zidanOrderStr.length;
						String sumOrder = "";

						for (String strOrder : zidanOrderStr) {
							st = strOrder ;
							if (i != zidanOrderStrLength) {
								sumOrder = sumOrder + fix + strOrder + ",";
							} else {
								sumOrder = sumOrder + fix + strOrder;
							}
							if (orderInfoService.isExistLgcOrderNo(fix +strOrder, lgcNo)) {
								isExist = true ;
								break ;
							}

							i++;
						}

						orderInfo.setZidanOrder(sumOrder);
						orderInfo.setItemCount(i+"");
					} else {
						orderInfo.setZidanOrder(fix + zidanOrder);
						orderInfo.setItemCount(1+"");
						zidanOrderStr = new  String[1] ;
						zidanOrderStr[0] =  zidanOrder ;
						if (orderInfoService.isExistLgcOrderNo(fix +zidanOrder, lgcNo)) {
							isExist = true ;
						}
					}
				}


				if (isExist) {
					render(JSON_TYPE, CommonResponse.respFailJson("9030",	st+"子单号重复", params.get("reqNo")), response);
					return ;
				}


				long id = orderInfoService.save(orderInfo); // 保存订单信息 获取ID
				if (StringUtils.isNotEmptyWithTrim(orderInfo.getOrderNote())) {
					orderInfoService.insertOrderNote(orderInfo, userNo,	String.valueOf(id), "新建订单备注");
				}
				orderPicService.save(imagesList); // 批量保存订单图片




				//以下保存子单信息
				for (int i = 0; i < zidanOrderStr.length; i++) {
					String orderno = sequenceService.getNextVal("order_no") ;
					orderNos.add(orderno) ;

					oInfo.setLgcOrderNo(fix+zidanOrderStr[i]);
					oInfo.setOrderNo(orderno);
					long zid = orderInfoService.save(oInfo) ; 
					if (StringUtils.isNotEmptyWithTrim(orderInfo.getOrderNote())) {
						orderInfoService.insertOrderNote(oInfo, userNo,	String.valueOf(zid), "新建订单备注");
					}
					//					if ("yx".equals(uid)) {
					oInfo.setForNo(params.get("forNo"));

					orderInfoService.yxTakeUpdate(oInfo); // 更新订单信息 子单信息
					//					} else {
					//						orderInfoService.takeUpdate(oInfo); // 更新订单信息
					//					}
				}

				String isMessage = lgcService.getLgcConfig("MOBILE_CONFIG", "TAKE_SEND_MSG", "0") ;
				String message = "0" ;
				if ("1".equals(isMessage)) {
					if ("1".equals(params.get("message"))) {
						message = "1" ;
					}
				}
				orderInfo.setMessage(message);

				//				if ("yx".equals(uid)) {
				orderInfo.setForNo(params.get("forNo"));
				orderInfo.setZid(0);
				orderInfoService.yxTakeUpdate(orderInfo); // 更新订单信息 //主单
				//				} else {
				//					orderInfoService.takeUpdate(orderInfo); // 更新订单信息
				//				}

				orderInfoService.changeOrderRegisterFirst(orderInfo);// 登记第一次录入信息
				//				orderInfoService.updateCodBank(orderInfo);// 更新代收货款银行信息

				if("kuaike".equals(uid)){
					if(StringUtils.isNotEmptyWithTrim(orderInfo.getRevPhone())&&orderInfo.getRevPhone().length()==11){
						String content ="您好！欢迎您使用快客同城速配，现有您一件同城货物已收件，单号"+lgcOrderNo+".请您电话保持畅通，我们将及时为您配送。下单、查询、投诉建议请关注微信公众号“快刻同城速配”，服务热线：0592－7127770【快递王子】";
						RunnableUtils ui = new RunnableUtils();
						ui.MessagePushClass("szkyt",lgcNo, orderInfo.getRevPhone(),content,lgcService);	
					}				
				}			


				OrderTrack track = new OrderTrack();
				track.setOrderNo(orderInfo.getOrderNo());
				track.setContext("订单被创建");
				track.setOrderTime(DateUtils.formatDate(nowDate));
				track.setCompleted("N");
				track.setOrderStatus("INIT");
				orderTrackService.add(track);

				for (String sno:orderNos) {
					track.setOrderNo(sno);
					orderTrackService.add(track);
				}


				String innerNo = loginUser.getInnerNo();
				if (StringUtil.isEmptyWithTrim(innerNo)) {
					innerNo = "";
				} else {
					innerNo = "(" + innerNo + ")。";
				}
				Map<String, Object> sMap = lgcService
						.getSubstationInfo(loginUser.getSubstationNo());
				OrderTrack track1 = new OrderTrack();
				track1.setOrderNo(orderInfo.getOrderNo());
				track1.setContext(sMap.get("substation_name") + ",快递员:"
						+ loginUser.getRealName() + innerNo + ",已收件,联系方式："
						+ loginUser.getPhone());
				track1.setOrderTime(DateUtils.formatDate(nowDate));
				track1.setCompleted("N");
				track1.setCurNo(orderInfo.getTakeCourierNo());
				track1.setCurType("C");
				track1.setNextNo(loginUser.getSubstationNo());
				track1.setNextType("S");
				track1.setOrderStatus("TAKEING");
				track1.setParentId(0);
				track1.setIsLast("Y"); //
				track1.setOpname(loginUser.getRealName());
				orderTrackService.add(track1);


				for (String sno:orderNos) {
					track1.setOrderNo(sno);
					orderTrackService.add(track1);
				}


				// RunnableUtils run = new RunnableUtils();
				// run.queryLocation(sendAddr, 1,
				// orderInfo.getOrderNo(),orderInfoService,params.get("uid"));
				// run.queryLocation(revAddr,2,
				// orderInfo.getOrderNo(),orderInfoService,params.get("uid"));
				// 更新用户地址库 发件人
				//				if (!StringUtils.isEmptyWithTrim(orderInfo.getSendPhone())
				//						&& !StringUtils
				//						.isEmptyWithTrim(orderInfo.getSendArea())) {
				//					System.out.println("更新用户地址库 发件人");
				//					List<Map<String, Object>> synList = userService
				//							.checkAddrList(userNo, orderInfo.getSendPhone(),
				//									orderInfo.getSendArea(),
				//									orderInfo.getSendAddr());
				//					String inputAddrCombine = orderInfo.getSendArea()
				//							+ orderInfo.getSendAddr();
				//					Map<String, Object> newAddr = new HashMap<String, Object>();
				//					newAddr.put("courierId", userNo);
				//					newAddr.put("senderUserId", orderInfo.getUserNo());
				//					newAddr.put("name", orderInfo.getSendName());
				//					newAddr.put("phone", orderInfo.getSendPhone());
				//					newAddr.put("cityId", "1");
				//					newAddr.put("areaAddr", orderInfo.getSendArea());
				//					newAddr.put("addr", orderInfo.getSendAddr());
				//					newAddr.put("defaultz", "0");
				//					newAddr.put("useTimes", "1");
				//					newAddr.put("areaId", "0");
				//					newAddr.put("createTime", DateUtils.formatDate(nowDate));
				//					if (synList.size() > 0) {
				//						for (Map<String, Object> addrMap : synList) {
				//							String addrCombine = addrMap.get("areaAddr")
				//									+ (String) addrMap.get("addr");
				//							if (!(inputAddrCombine.trim()).equals(addrCombine
				//									.trim())) {
				//								userService.addrSyn(newAddr);
				//							}
				//						}
				//					} else {
				//						userService.addrSyn(newAddr);
				//					}
				//				}
				//
				//				// 更新用户地址库 收件人
				//				if (!StringUtils.isEmptyWithTrim(orderInfo.getRevPhone())
				//						&& !StringUtils.isEmptyWithTrim(orderInfo.getRevArea())) {
				//					System.out.println("更新用户地址库 收件人");
				//					List<Map<String, Object>> synList = userService
				//							.checkAddrList(userNo, orderInfo.getRevPhone(),
				//									orderInfo.getRevArea(),
				//									orderInfo.getRevAddr());
				//					String inputAddrCombine = orderInfo.getRevArea()
				//							+ orderInfo.getRevAddr();
				//					Map<String, Object> newAddr = new HashMap<String, Object>();
				//					newAddr.put("courierId", userNo);
				//					newAddr.put("senderUserId", orderInfo.getUserNo());
				//					newAddr.put("name", orderInfo.getRevName());
				//					newAddr.put("phone", orderInfo.getRevPhone());
				//					newAddr.put("cityId", "1");
				//					newAddr.put("areaAddr", orderInfo.getRevArea());
				//					newAddr.put("addr", orderInfo.getRevAddr());
				//					newAddr.put("defaultz", "0");
				//					newAddr.put("useTimes", "1");
				//					newAddr.put("areaId", "0");
				//					newAddr.put("createTime", DateUtils.formatDate(nowDate));
				//					if (synList.size() > 0) {
				//						for (Map<String, Object> addrMap : synList) {
				//							String addrCombine = (String) addrMap.get("areaAddr")+ (String) addrMap.get("addr");
				//							if (!(inputAddrCombine.trim()).equals(addrCombine
				//									.trim())) {
				//								userService.addrSyn(newAddr);
				//							}
				//						}
				//					} else {
				//						userService.addrSyn(newAddr);
				//					}
				//				}

				model.put("lgcOrderNo", lgcOrderNo);
				render(JSON_TYPE,
						CommonResponse.respSuccessJson("创建成功", model,
								params.get("reqNo")), response);
			} else {
				log.info("validate false!!!!---->" + ret.get("respCode")
						+ "---------" + ret.get("respMsg"));
				render(JSON_TYPE,
						CommonResponse.respFailJson(ret.get("respCode"),
								ret.get("respMsg"), params.get("reqNo")),
								response);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9010", "数据解析错误",
							params.get("reqNo")), response);
		} catch (ConnectException e) {
			e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9024", "连接已断开",
							params.get("reqNo")), response);
		} catch (FileUnknowTypeException e) {
			// e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9012", "文件类型错误",
							params.get("reqNo")), response);
		} catch (SocketTimeoutException exception) {
			exception.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9023", "连接超时",
							params.get("reqNo")), response);
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9000", "服务器异常",
							params.get("reqNo")), response);
		}
	}

	/**
	 * 0代收 1待派 2已收3历史
	 */

	@RequestMapping(value = "/list")
	public void list(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "orderType" }), true,
					userSessionService, checkVersion, appVersionService,
					dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				setPageInfo(params);
				System.out.println("beginTime================="
						+ params.get("beginTime"));
				System.out.println("endTime=================="
						+ params.get("endTime"));
				params.put("userNo", ret.get("userNo"));
				User loginUser = userService.getUserByNo(ret.get("userNo"));
				String lgcNo = userService.getUserLgcNo(ret.get("userNo"));
				params.put("lgcNo", lgcNo);
				Date nowDate = new Date();
				params.put("createTime",
						DateUtils.formatDate(nowDate, "yyyy-MM-dd"));
				// 签到限制
				List<Map<String, Object>> signList = userSessionService
						.signInfoAll(params);// 查询今日签到次数
				List<Map<String, Object>> signOutList = userSessionService
						.signOutInfoAll(params);// 查询今日签退次数
				int signTimes = signList.size();
				int signOutTimes = signOutList.size();
				System.out.println("signTimes=============" + signTimes
						+ "//////////signOutTimes=============" + signOutTimes);
				if (signTimes < 1 || signTimes == signOutTimes) {
					render(JSON_TYPE, CommonResponse.respFailJson("9035",
							"尚未签到或已签退", params.get("reqNo")), response);
					return;
				}
				int orderType = Integer.parseInt(params.get("orderType"));

				Page<Map<String, Object>> orderList = null;// 查询内容
				int unreadCount = 0;
				params.put("orderType", params.get("orderType"));
				params.put("substation", loginUser.getSubstationNo());
				List<Map<String, Object>> orderListByBefore = new ArrayList<Map<String, Object>>();

				if (orderType == 0) {// 查询待收件
					orderList = orderInfoService.listByTypeOne(params,
							pageRequest);// 查询代收
					orderListByBefore = orderList.getContent();
					unreadCount = orderInfoService.unreadCount(params);
				} else if (orderType == 1) {// 查询待派件
					orderList = orderInfoService.listByTypeTwo(params,
							pageRequest);
					orderListByBefore = orderList.getContent();
					unreadCount = orderInfoService.unreadCount(params);
				} else if (orderType == 2) {// 查询已收件
					orderList = orderInfoService.listByTypeOneBefore(params,pageRequest);
					for (Map<String, Object> map : orderList.getContent()){
						Map<String, Object> IOmap = warehouseService.getMapByOrder((String) map.get("lgcOrderNo"),"I", "Y");// 是否入仓成功
						if (IOmap != null) {
							continue;
						}
						String sendCourierNo = String.valueOf(map.get("sendCourierNo"));
						String takeCourierNo = String.valueOf(map.get("takeCourierNo"));
						if (sendCourierNo.equals(takeCourierNo)) {
							continue;
						}
						orderListByBefore.add(map);
					}
					unreadCount = orderInfoService.unreadCount(params);
				} else {
					// //收派件历史
					// if (StringUtils.isEmptyWithTrim(params.get("beginTime")))
					// {
					// params.put("beginTime",
					// DateUtils.formatDate(DateUtils.addDate(nowDate, -365, 0,
					// 0),"yyyy-MM-dd"));
					// params.put("endTime",
					// DateUtils.formatDate(nowDate,"yyyy-MM-dd"));
					// }
					if (StringUtils.isEmptyWithTrim(params.get("status"))) {
						orderList = orderInfoService.listBefore(params,	pageRequest);
						orderListByBefore = orderList.getContent();
						unreadCount = orderInfoService.unreadCount(params);
					}

					if ("1".equals((params.get("status")))) { // 收件
						orderList = orderInfoService.listBeforeGetTakeOrder(params, pageRequest);
						orderListByBefore = orderList.getContent();
						unreadCount = orderInfoService.unreadCount(params);
					}

					if ("2".equals((params.get("status")))) { // 派件
						orderList = orderInfoService.listBeforeGetSendOrder(params, pageRequest);
						orderListByBefore = orderList.getContent();
						unreadCount = orderInfoService.unreadCount(params);
					}
				}
				model = new HashMap<String, Object>();
				model.put("orderList", orderListByBefore);
				model.put("totalCount", orderList.getTotalElements());
				model.put("cp", orderList.getNumber() + 1);
				model.put("isLastPage", orderList.isLastPage());
				model.put("unreadCount", unreadCount);
				render(JSON_TYPE,	CommonResponse.respSuccessJson("", model,params.get("reqNo")), response);
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

	// 订单详细查询。。通过条件checkMessage查询订单
	@RequestMapping(value = "/checkList")
	public void checkList(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "checkMessage" }), true,
					userSessionService, checkVersion, appVersionService,
					dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {// ,"longitude","latitude"
				setPageInfo(params);
				Page<Map<String, Object>> orderList;
				params.put("userNo", ret.get("userNo"));
				params.put("checkMessage", params.get("checkMessage"));
				params.put("createTime",
						DateUtils.formatDate(new Date(), "yyyy-MM-dd"));
				String lgcNo = userService.getUserLgcNo(ret.get("userNo"));
				params.put("lgcNo", lgcNo);
				// 签到限制
				List<Map<String, Object>> signList = userSessionService
						.signInfoAll(params);// 查询今日签到次数
				List<Map<String, Object>> signOutList = userSessionService
						.signOutInfoAll(params);// 查询今日签退次数
				int signTimes = signList.size();
				int signOutTimes = signOutList.size();
				System.out.println("signTimes=============" + signTimes
						+ "//////////signOutTimes=============" + signOutTimes);
				if (signTimes < 1 || signTimes == signOutTimes) {
					render(JSON_TYPE, CommonResponse.respFailJson("9034",
							"尚未签到或已签退", params.get("reqNo")), response);
					return;
				}

				User user = userService.getUserByNo(ret.get("userNo"));
				params.put("substationNo", user.getSubstationNo());
				params.put("orderStatus", params.get("orderStatus"));

				System.out.println("orderStatus============="
						+ params.get("orderStatus"));
				// 查询可接单的
				if ("0".equals(params.get("orderStatus"))) {
					orderList = orderInfoService.checkListByMe(params,
							pageRequest);
				} else {
					orderList = orderInfoService.checkList(params, pageRequest);
				}
				model = new HashMap<String, Object>();
				model.put("orderList", orderList.getContent());
				model.put("totalCount", orderList.getTotalElements());
				model.put("cp", orderList.getNumber() + 1);
				model.put("isLastPage", orderList.isLastPage());
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

	// 收入明细查询 根据时间段查询收入明细
	@RequestMapping(value = "/detail")
	public void detail(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] {}), true, userSessionService,
					checkVersion, appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				setPageInfo(params);
				params.put("userNo", ret.get("userNo"));
				User loginUser = userService.getUserByNo(ret.get("userNo"));
				String status = params.get("status");
				String payType = params.get("payType");
				String lgcNo = userService.getUserLgcNo(ret.get("userNo"));
				params.put("lgcNo", lgcNo);
				params.put("substation", loginUser.getSubstationNo());
				model = new HashMap<String, Object>();

				Page<Map<String, Object>> orderList = null;
				if (StringUtils.isEmptyWithTrim(status)) {
					orderList = orderInfoService.detailList(params, pageRequest);
					BigDecimal tnpay = orderInfoService.detailTnpaySum(params,	pageRequest);// 收件收总金额
					BigDecimal snpay = orderInfoService.detailSnpaySum(params,	pageRequest);// 派件件收总金额
					tnpay = tnpay.add(snpay);
					BigDecimal tnpayCash = orderInfoService.detailTnpaySumCash(params);// 收件收总金额
					BigDecimal snpayCash = orderInfoService.detailSnpaySumCash(params);
					BigDecimal goodPrice = orderInfoService.detailSnpaySumCodPay(params);
					tnpayCash = tnpayCash.add(snpayCash).add(goodPrice);
					model.put("orderSum", tnpay.toString());// 总金额
					model.put("cashMoney", tnpayCash.toString());// 现金总金额
				}
				if ("1".equals(status)) {
					// 已收件
					orderList = orderInfoService.detailListTakeOrder(params,pageRequest);
					BigDecimal tnpay = orderInfoService.detailTnpaySum(params,	pageRequest);// 收件收总金额
					BigDecimal tnpayCash = orderInfoService.detailTnpaySumCash(params);// 收件收总金额
					model.put("orderSum", tnpay.toString());// 总金额
					model.put("cashMoney", tnpayCash.toString());// 现金总金额
				}

				if ("2".equals(status)) {
					// 已派件
					orderList = orderInfoService.detailListSendOrder(params,
							pageRequest);
					BigDecimal snpay = orderInfoService.detailSnpaySum(params,
							pageRequest);// 派件件收总金额
					BigDecimal snpayCash = orderInfoService.detailSnpaySumCash(params);
					BigDecimal goodPrice = orderInfoService.detailSnpaySumCodPay(params);
					snpayCash = snpayCash.add(goodPrice);
					model.put("orderSum", snpay.toString());// 总金额
					model.put("cashMoney", snpayCash.toString());// 现金总金额
				}
				model.put("orderList", orderList.getContent());
				model.put("totalCount", orderList.getTotalElements());
				model.put("cp", orderList.getNumber() + 1);
				model.put("isLastPage", orderList.isLastPage());
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

	// 获取可接单列表
	@RequestMapping(value = "/task")
	public void task(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] {}), true, userSessionService,
					checkVersion, appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				setPageInfo(params);
				params.put("userNo", ret.get("userNo"));
				params.put("createTime",
						DateUtils.formatDate(new Date(), "yyyy-MM-dd"));
				params.put("lgcNo", userService.getUserLgcNo(ret.get("userNo")));
				// 签到限制
				List<Map<String, Object>> signList = userSessionService
						.signInfoAll(params);// 查询今日签到次数
				List<Map<String, Object>> signOutList = userSessionService
						.signOutInfoAll(params);// 查询今日签退次数
				int signTimes = signList.size();
				int signOutTimes = signOutList.size();
				System.out.println("signTimes=============" + signTimes
						+ "//////////signOutTimes=============" + signOutTimes);
				if (signTimes < 1 || signTimes == signOutTimes) {
					render(JSON_TYPE, CommonResponse.respFailJson("9034",
							"尚未签到或已签退", params.get("reqNo")), response);
					return;
				}

				Date nowDate = new Date();
				String beginTime = DateUtils.formatDate(DateUtils.addDate(
						nowDate, -365, 0, 0));
				String endTime = DateUtils.formatDate(nowDate);
				if (!StringUtils.isEmptyWithTrim(params.get("beginTime"))) {
					beginTime = DateUtils.formatDate(params.get("beginTime"),
							null);
				}
				if (!StringUtils.isEmptyWithTrim(params.get("endTime"))) {
					endTime = DateUtils.formatDate(params.get("endTime"), null);
				}
				User user = userService.getUserByNo(ret.get("userNo"));
				params.put("beginTime", beginTime);
				params.put("endTime", endTime);
				params.put("substationNo", user.getSubstationNo());

				Map<String, Object> map = userService.lgcMode();
				if (map == null) {
					render(JSON_TYPE, CommonResponse.respFailJson("9047",
							"当前公司未配置大厅功能,请于后台配置", params.get("reqNo")),
							response);
					return;
				}
				String code = (String) map.get("code");
				Page<Map<String, Object>> orderList = null;
				if ("AUTO_ALL".equals(code)) {// 分配到大厅，所有人可以看到
					orderList = orderInfoService.allCourierTask(params,
							pageRequest);
				}
				if ("AUTO_AREA".equals(code) || "MANUAL_STATION".equals(code)) {// 分配到大厅，寄件区域的网点可以看到
					orderList = orderInfoService.listTask(params, pageRequest);
				}
				model = new HashMap<String, Object>();
				model.put("orderList", orderList.getContent());
				model.put("totalCount", orderList.getTotalElements());
				model.put("cp", orderList.getNumber() + 1);
				model.put("isLastPage", orderList.isLastPage());
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

	// 获取订单详情 通过订单号
	@RequestMapping(value = "/info")
	public void info(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "orderNo", "type" }), true,
					userSessionService, checkVersion, appVersionService,
					dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String loginNo = ret.get("userNo");
				String lgcNo = userService.getUserLgcNo(loginNo);
				OrderDto orderInfo = null;

				if (StringUtil.isEmptyWithTrim(params.get("lgcOrder"))) {
					orderInfo = orderInfoService.getOrderByOrderNo(params.get("orderNo"), lgcNo);
				} else {
					orderInfo = orderInfoService.getOrderByLgcOrderNo(params.get("orderNo"), lgcNo);
				}

				if (orderInfo == null) {
					render(JSON_TYPE,CommonResponse.respFailJson("9013", "订单不存在",
							params.get("reqNo")), response);
				} else {
					OrderInfo orInfo = orderInfoService.getByOrderNo(	orderInfo.getOrderNo(), lgcNo);
					Map<String, Object> payMap = orderInfoService	.getNpay(orInfo);
					orderInfo.setNpay((Float) payMap.get("npay"));
					orderInfo.setDisNpay((Float) payMap.get("disNpay"));
					Map<String, Object> codMap = otherService
							.selectCodBy(orderInfo.getCodName());
					if (codMap != null) {
						orderInfo.setCodSname(codMap.get("codSname"));
					} else {
						orderInfo.setCodSname("");
					}

					if (StringUtils.isNotEmptyWithTrim(orInfo.getTakeCourierNo())) {
						orderInfo.setTask(false);
					} else {
						orderInfo.setTask(true);
					}

					// float vpay = orderInfo.getVpay() ;
					// if
					// (orderInfo.getGoodValuationRate()!=null&&orderInfo.getGoodValuation()>0&&vpay<=0)
					// {
					// vpay =
					// Math.round(orderInfo.getGoodValuation()*Float.valueOf(orderInfo.getGoodValuationRate())*100)/100f
					// ;
					// }
					orderInfo.setVpay(orderInfo.getVpay());
					orderInfo.setTakeStatus(orInfo.getTakeMark());// 快件状态
					List<PosInfoDto> posInfo = posInfoService.getByOrderNoCourierNo(params.get("orderNo"),loginNo);

					orderInfo.setPosCount(posInfo.size());

					List<Map<String, Object>> imagesList = orderPicService
							.list(params);
					Map<String, Object> muser = null;
					if ("MONTH".equals(orderInfo.getPayType())) {
						if (StringUtils.isNotEmptyWithTrim(orderInfo.getMonthSettleNo())) {
							muser = muserService.selectMonthBy(orderInfo.getMonthSettleNo());// 查询月结客户信息
						}
					}
					String lgcName = "";
					String lgcPhone = "";

					String orderNote = orderInfoService.getOrderNote(orderInfo.getId());
					orderInfo.setOrderNote(orderNote);
					orderInfo.setProInfo(orderInfoService.checkProInfoByOrderNo(orderInfo.getOrderNo()));
					for (int i = 0; i < 2; i++) {
						if (orderInfo.getSendArea()!=null&&orderInfo.getSendArea().endsWith("-")) {
							orderInfo.setSendArea(orderInfo.getSendArea().substring(0, orderInfo.getSendArea().length()-1));
						}
						if (orderInfo.getRevArea()!=null&&orderInfo.getRevArea().endsWith("-")) {
							orderInfo.setRevArea(orderInfo.getRevArea().substring(0, orderInfo.getRevArea().length()-1));
						}
					}
					
					
					model = new HashMap<String, Object>();
					model.put("orderInfo", orderInfo);
					model.put("muser", muser);
					model.put("imagesList", imagesList);
					Map<String, Object> lgc = lgcService.getLgc(lgcNo);
					if (lgc != null) {
						if (lgc.get("name") != null) {
							lgcName = lgc.get("name").toString();
						}
						if (lgc.get("contact") != null) {
							lgcPhone = lgc.get("contact").toString();
						}
					}
					model.put("lgcName", lgcName);
					model.put("lgcPhone", lgcPhone);
					model.put("imagesList", imagesList);
					System.out.println("takeStatus=" + orInfo.getTakeMark());
					if (loginNo.equals(orInfo.getTakeCourierNo())) {
						orderInfoService.read(orderInfo.getOrderNo(), 1);
					}
					render(JSON_TYPE,
							CommonResponse.respSuccessJson("", model,params.get("reqNo")), response);
				}
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

	// 取消订单
	@RequestMapping(value = "/cancel")
	public void cancel(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "orderNo" }), true,
					userSessionService, checkVersion, appVersionService,
					dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String userNo = orderInfoService.getUserNoByOrderNo(params
						.get("orderNo"));
				String loginUserNo = ret.get("userNo");
				String lgcNo = userService.getUserLgcNo(loginUserNo);
				OrderInfo order = orderInfoService.getByOrderNo(
						params.get("orderNo"), lgcNo);
				Date nowDate = new Date();
				params.put("userNo", ret.get("userNo"));
				params.put("createTime",
						DateUtils.formatDate(new Date(), "yyyy-MM-dd"));
				// 签到状态
				// 签到限制
				List<Map<String, Object>> signList = userSessionService
						.signInfoAll(params);// 查询今日签到次数
				List<Map<String, Object>> signOutList = userSessionService
						.signOutInfoAll(params);// 查询今日签退次数
				int signTimes = signList.size();
				int signOutTimes = signOutList.size();
				System.out.println("signTimes=============" + signTimes
						+ "//////////signOutTimes=============" + signOutTimes);
				if (signTimes < 1 || signTimes == signOutTimes) {
					render(JSON_TYPE, CommonResponse.respFailJson("9035",
							"尚未签到或已签退", params.get("reqNo")), response);
					return;
				}

				if (StringUtils.isEmptyWithTrim(userNo)) {
					render(JSON_TYPE,
							CommonResponse.respFailJson("9013", "订单不存在",
									params.get("reqNo")), response);
				} else {
					if (!userNo.equals(ret.get("userNo"))) {
						render(JSON_TYPE, CommonResponse.respFailJson("9015",
								"操作不合法", params.get("reqNo")), response);
					} else {
						orderInfoService.cancel(params.get("orderNo"));

						User userInfo = userService.getUserByNo(ret
								.get("userNo"));// 查询快递员信息
						if ("YMX".equals(order.getSource())) {
							System.out.println("一米鲜推送");
							String info = "您的订单"
									+ params.get("orderNo")
									+ "已于"
									+ DateUtils.formatDate(nowDate,
											"yyyy-MM-dd HH:mm:ss") + "被取消";
							RunnableUtils run = new RunnableUtils();
							run.pushTMX(order.getOrderNote(), "8", params
									.get("orderNo"), userInfo.getRealName(),
									userInfo.getPhone(), info, DateUtils
									.formatDate(nowDate,
											"yyyy-MM-dd HH:mm:ss"),
											params.get("uid"));
						}

						render(JSON_TYPE, CommonResponse.respSuccessJson("",
								"取消成功", params.get("reqNo")), response);
					}
				}
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

	// 接单
	@RequestMapping(value = "/acpt")
	public void acpt(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "orderNo" }), true,
					userSessionService, checkVersion, appVersionService,
					dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String userNo = ret.get("userNo");
				String lgcNo = userService.getUserLgcNo(userNo);
				params.put("userNo", userNo);
				params.put("createTime",
						DateUtils.formatDate(new Date(), "yyyy-MM-dd"));
				// 签到限制
				List<Map<String, Object>> signList = userSessionService
						.signInfoAll(params);// 查询今日签到次数
				List<Map<String, Object>> signOutList = userSessionService
						.signOutInfoAll(params);// 查询今日签退次数
				int signTimes = signList.size();
				int signOutTimes = signOutList.size();
				System.out.println("signTimes=============" + signTimes
						+ "//////////signOutTimes=============" + signOutTimes);
				if (signTimes < 1 || signTimes == signOutTimes) {
					render(JSON_TYPE, CommonResponse.respFailJson("9035",
							"尚未签到或已签退", params.get("reqNo")), response);
					return;
				}

				OrderInfo order = orderInfoService.getByOrderNo(
						params.get("orderNo"), lgcNo);
				if (order == null) {
					render(JSON_TYPE,
							CommonResponse.respFailJson("9013", "订单不存在",
									params.get("reqNo")), response);
					return;
				}

				User user = userService.getUserByNo(userNo);

				if (order.getStatus() != 1) {
					render(JSON_TYPE, CommonResponse.respFailJson("9016",
							"此订单不允许接单", params.get("reqNo")), response);
					return;
				}
				if (StringUtils.isNotEmptyWithTrim(order.getTakeCourierNo())) {
					if (userNo.equals(order.getTakeCourierNo())) {
						render(JSON_TYPE, CommonResponse.respFailJson("9026",
								"已接单,请刷新列表", params.get("reqNo")), response);
						return;
					}
					render(JSON_TYPE, CommonResponse.respFailJson("9026",
							"此订单已被其他快递员接下", params.get("reqNo")), response);
					return;
				}
				order.setTakeCourierNo(userNo);
				order.setSubStationNo(user.getSubstationNo());

				orderInfoService.acpt(order);

				orderInfoService.updaOrderStation(order.getOrderNo());// 接单后所有快递员无法再次接单
				Date nowDate = new Date();
				Map<String, Object> lgcMode = userService.lgcMode();
				if ("AUTO_ALL".equals(lgcMode.get("code"))) {
					PushMsg msg = new PushMsg();
					msg.setUserNo(lgcNo);
					msg.setUserType(PushUserType.ALLUSER.getValue());
					msg.setMsgCode(MsgType.ASIGNTS.getValue());
					msg.setMsgContent("订单已被其他快递员接下");
					msg.setMsgData(order.getOrderNo());
					msg.setCreateTime(DateUtils.formatDate(nowDate));
					msg.setExpireTime(DateUtils.formatDate(DateUtils.addDate(
							nowDate, 0, 6, 0)));
					// long msgId = msgService.save(msg) ;
					PushUtil.pushByMSG(configInfo,msg, params.get("uid"));
				} else {
					String sno = orderSubstationService.getStationString(
							order.getId(), "");
					PushMsg msg = new PushMsg();
					msg.setUserNo(sno);
					msg.setUserType(PushUserType.SUBSTATION.getValue());
					msg.setMsgCode(MsgType.ASIGNED.getValue());
					msg.setMsgContent("订单已被其他快递员接下");
					msg.setMsgData(order.getOrderNo());
					msg.setCreateTime(DateUtils.formatDate(nowDate));
					msg.setExpireTime(DateUtils.formatDate(DateUtils.addDate(
							nowDate, 0, 6, 0)));
					// long msgId = msgService.save(msg) ;
					PushUtil.pushByMSG(configInfo,msg, params.get("uid"));

				}
				User userInfo = userService.getUserByNo(userNo);// 查询快递员信息
				/**
				 * 微信信息推送通知
				 */
				if (!StringUtil.isEmptyWithTrim(order.getWxOpenid())) {
					RunnableUtils run = new RunnableUtils();
					run.pushWEIXIN(order, userInfo, 1);
				}

				/**
				 * 
				 * 
				 * 
				 * 
				 * 
				 * 一米鲜快递推送通知
				 */
				if ("YMX".equals(order.getSource())) {
					System.out.println("一米鲜快递推送通知");

					String info = "快递员已接单,请耐心等待上门取件";
					RunnableUtils run = new RunnableUtils();
					run.pushTMX(order.getOrderNote(), "1", params
							.get("orderNo"), userInfo.getRealName(), userInfo
							.getPhone(), info, DateUtils.formatDate(nowDate,
									"yyyy-MM-dd HH:mm:ss"), params.get("uid"));
				}
				render(JSON_TYPE,
						CommonResponse.respSuccessJson("", "接单成功",
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

	// 取消接单
	@RequestMapping(value = "/cancel_asign")
	public void cancel_asign(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "orderNo" }), true,
					userSessionService, checkVersion, appVersionService,
					dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String loginUserNo = ret.get("userNo");
				String lgcNo = userService.getUserLgcNo(loginUserNo);
				params.put("userNo", ret.get("userNo"));
				params.put("createTime",
						DateUtils.formatDate(new Date(), "yyyy-MM-dd"));

				OrderInfo order = orderInfoService.getByOrderNo(
						params.get("orderNo"), lgcNo);
				if (order == null) {
					render(JSON_TYPE,
							CommonResponse.respFailJson("9013", "订单不存在",
									params.get("reqNo")), response);
					return;
				}
				// 签到限制
				List<Map<String, Object>> signList = userSessionService
						.signInfoAll(params);// 查询今日签到次数
				List<Map<String, Object>> signOutList = userSessionService
						.signOutInfoAll(params);// 查询今日签退次数
				int signTimes = signList.size();
				int signOutTimes = signOutList.size();
				System.out.println("signTimes=============" + signTimes
						+ "//////////signOutTimes=============" + signOutTimes);
				if (signTimes < 1 || signTimes == signOutTimes) {
					render(JSON_TYPE, CommonResponse.respFailJson("9035",
							"尚未签到或已签退", params.get("reqNo")), response);
					return;
				}

				// User user = userService.getUserByNo(loginUserNo) ;
				// String lgcNo = userService.getUserLgcNo(loginUserNo) ;
				if (!loginUserNo.equals(order.getTakeCourierNo())) {
					render(JSON_TYPE, CommonResponse.respFailJson("9015",
							"操作不合法,不是当前分配!", params.get("reqNo")), response);
					return;
				}
				if (order.getStatus() != 1) {
					render(JSON_TYPE, CommonResponse.respFailJson("9016",
							"此订单不允许取消接单", params.get("reqNo")), response);
					return;
				}
				Map<String, Object> map = userService.lgcMode();
				if (map == null) {
					render(JSON_TYPE, CommonResponse.respFailJson("9047",
							"当前公司未配置大厅功能,请于后台配置", params.get("reqNo")),
							response);
					return;
				}
				String code = (String) map.get("code");
				log.info("指派模式------------------------" + code
						+ "--------------------------------");
				if ("AUTO_ALL".equals(code)) {// 分配到大厅，所有人可以看到
					orderInfoService.calcelAsignAll(order);
				}

				if ("AUTO_AREA".equals(code) || "MANUAL_STATION".equals(code)
						|| "MANUAL_PERSON".equals(code)) {// 分配到大厅，寄件区域的网点可以看到
					orderInfoService.calcelAsign(order.getOrderNo());
				}

				Date nowDate = new Date();
				if ("AUTO_ALL".equals(map.get("code"))) {

					PushMsg msg = new PushMsg();
					msg.setUserNo(lgcNo);
					msg.setUserType(5);
					msg.setMsgCode(MsgType.ASIGNTS.getValue());
					msg.setMsgContent(DateUtils.formatDate(
							order.getTakeTimeBegin(), "yyyy-MM-dd HH:mm:ss")
							+ "在"
							+ order.getSendAddr()
							+ "有一个新的快件！联系方式"
							+ order.getSendPhone());
					msg.setMsgData(order.getOrderNo());
					msg.setCreateTime(DateUtils.formatDate(nowDate));
					msg.setExpireTime(DateUtils.formatDate(DateUtils.addDate(
							nowDate, 0, 6, 0)));
					// long msgId = msgService.save(msg) ;
					PushUtil.pushByMSG(configInfo,msg, params.get("uid"));
				} else {
					String sno = orderSubstationService.getStationString(
							order.getId(), "");
					PushMsg msg = new PushMsg();
					msg.setUserNo(order.getSubStationNo());
					msg.setUserType(PushUserType.SUBSTATION.getValue());
					msg.setMsgCode(MsgType.ASIGNTS.getValue());
					msg.setMsgContent(DateUtils.formatDate(
							order.getTakeTimeBegin(), "yyyy-MM-dd HH:mm:ss")
							+ "在"
							+ order.getSendAddr()
							+ "有一个新的快件！联系方式"
							+ order.getSendPhone());
					msg.setMsgData(order.getOrderNo());
					msg.setCreateTime(DateUtils.formatDate(nowDate));
					msg.setExpireTime(DateUtils.formatDate(DateUtils.addDate(
							nowDate, 0, 6, 0)));
					// long msgId = msgService.save(msg) ;
					PushUtil.pushByMSG(configInfo,msg, params.get("uid"));
				}

				User userInfo = userService.getUserByNo(loginUserNo);// 查询快递员信息
				/**
				 * 
				 * 
				 * 
				 * 
				 * 
				 * 微信消息通知
				 */
				if (!StringUtil.isEmptyWithTrim(order.getWxOpenid())) {
					System.out.println("微信推送线程开始");
					RunnableUtils run = new RunnableUtils();
					run.pushWEIXIN(order, userInfo, 3);
				}
				/**
				 * 
				 * 
				 * 
				 * 
				 * 
				 * 一米鲜快递推送通知
				 */
				if ("YMX".equals(order.getSource())) {
					System.out.println("一米鲜快递推送通知");
					String info = "快递员已取消接单,请耐心等待闲时快递员接单";
					RunnableUtils run = new RunnableUtils();
					run.pushTMX(order.getOrderNote(), "8", params
							.get("orderNo"), userInfo.getRealName(), userInfo
							.getPhone(), info, DateUtils.formatDate(nowDate,
									"yyyy-MM-dd HH:mm:ss"), params.get("uid"));
				}

				render(JSON_TYPE,
						CommonResponse.respSuccessJson("", "撤单成功",
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

	// 快递员收件接口
	@RequestMapping(value = "/take")
	public void take(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {

		try {
			Date nowDate = new Date();
			model = new HashMap<String, Object>();
			String[] orderParams = new String[] { "orderNo", "rev" };
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(orderParams), true, userSessionService,
					checkVersion, appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String userNo = ret.get("userNo");
				String loginUserNo = ret.get("userNo");
				String lgcNo = userService.getUserLgcNo(loginUserNo);
				params.put("createTime",
						DateUtils.formatDate(new Date(), "yyyy-MM-dd"));
				params.put("userNo", userNo);
				// 签到限制
				List<Map<String, Object>> signList = userSessionService	.signInfoAll(params);// 查询今日签到次数
				List<Map<String, Object>> signOutList = userSessionService.signOutInfoAll(params);// 查询今日签退次数
				int signTimes = signList.size();
				int signOutTimes = signOutList.size();
				System.out.println("signTimes=============" + signTimes
						+ "//////////signOutTimes=============" + signOutTimes);
				if (signTimes < 1 || signTimes == signOutTimes) {
					render(JSON_TYPE, CommonResponse.respFailJson("9047",
							"尚未签到或已签退", params.get("reqNo")), response);
					return;
				}

				OrderInfo order = orderInfoService.getByOrderNo(params.get("orderNo"), lgcNo);
				if(!StringUtils.isEmptyWithTrim(params.get("lgcOrderNo"))){
					if(params.get("lgcOrderNo").contains(".")||params.get("lgcOrderNo").contains("'")){
						render(JSON_TYPE, CommonResponse.respFailJson("9034","订单号违法，不能包含标点。", params.get("reqNo")), response);
						return;
					}
				}			
				if (order == null) {
					render(JSON_TYPE,
							CommonResponse.respFailJson("9013", "订单不存在",params.get("reqNo")), response);
					return;
				}

				if (!lgcNo.equals(order.getLgcNo())) {
					render(JSON_TYPE, CommonResponse.respFailJson("9015","操作不合法,不是当前选择的快递公司!", params.get("reqNo")),
							response);
					return;
				}

				if (order.getStatus() != 1) {
					render(JSON_TYPE, CommonResponse.respFailJson("9016",
							"不允许修改订单", params.get("reqNo")), response);
					return;
				}

				User loginUser = userService.getUserByNo(loginUserNo);
				order.setTakeCourierNo(loginUserNo);
				order.setSubStationNo(loginUser.getSubstationNo());
				order.setTakeOrderTime(DateUtils.formatDate(nowDate));// 具体收件时间
				order.setPayType(params.get("payType"));//更新最新的支付方式
				order.setFreightType(params.get("freightType"));//更新最新的支付方

				if (Integer.parseInt(params.get("rev")) == 0) {
					ret = ValidateUtil.validateRequest(request,reqParams(new String[] { "rmsg" }), false, null,
							false, appVersionService, dynamicDataSourceHolder);
					if (!"TRUE".equals(ret.get("isSuccess"))) {
						render(JSON_TYPE, CommonResponse.respFailJson(ret.get("respCode"), ret.get("respMsg"),	params.get("reqNo")), response);
						return;
					}
					if (!loginUserNo.equals(order.getTakeCourierNo())) {
						render(JSON_TYPE, CommonResponse.respFailJson("9015",	"操作不合法,不是当前分配快递员无法拒收!", params.get("reqNo")),response);
						return;			
					}

					// 拒收
					order.setSign(params.get("rmsg"));
					orderInfoService.unTake(order);

					String innerNo = loginUser.getInnerNo();
					if (StringUtil.isEmptyWithTrim(innerNo)) {
						innerNo = "";
					} else {
						innerNo = "(" + innerNo + ")。";
					}

					OrderTrack track = new OrderTrack();
					track.setOrderNo(order.getOrderNo());
					track.setContext("订单被拒收，拒收理由：" + params.get("rmsg")
							+ "操作快递员:" + loginUser.getRealName() + "innerNo");
					track.setOrderTime(DateUtils.formatDate(nowDate));
					track.setCompleted("N");
					track.setOpname(loginUser.getRealName());
					orderTrackService.add(track);

					PushMsg msg = new PushMsg();
					msg.setUserNo(order.getUserNo());
					msg.setUserType(1);
					msg.setMsgCode(MsgType.RETAKE.getValue());
					msg.setMsgContent("您的快递在" + DateUtils.formatDate(nowDate)
							+ "被拒收！请确认是否为本人操作");
					msg.setMsgData(order.getOrderNo());
					msg.setCreateTime(DateUtils.formatDate(nowDate));
					msg.setExpireTime(DateUtils.formatDate(DateUtils.addDate(
							nowDate, 0, 6, 0)));
					long msgId = msgService.save(msg);
					PushUtil.pushById(configInfo,String.valueOf(msgId), 1,
							params.get("uid"));

					render(JSON_TYPE,
							CommonResponse.respSuccessJson("", "更新成功",
									params.get("reqNo")), response);

				} else {
					// 判断快递员是否能修改订单

					String[] tp = new String[] {};
					String[] freightTypeParams = new String[] { "monthSettleNo" };
					String[] codParams = new String[] { "goodPrice" };
					ret = ValidateUtil.validateRequest(request, reqParams(tp),
							false, null, checkVersion, appVersionService,
							dynamicDataSourceHolder);
					if ("TRUE".equals(ret.get("isSuccess"))) {

						if (!"1".equals(params.get("freightType"))&& !"2".equals(params.get("freightType"))) {
							render(JSON_TYPE, CommonResponse.respFailJson("9026", "参数不正确：运费类型不能为空",params.get("reqNo")), response);
							return;
						}
						if ("MONTH".equals(params.get("payType"))) {
							if (StringUtils.isEmptyWithTrim(params.get("monthSettleNo"))) {	render(JSON_TYPE, CommonResponse.respFailJson(	"9001", "缺少参数：月结帐号不能为空",
									params.get("reqNo")), response);
							return;
							}
							Map<String, Object> map = muserService	.selectMonthByFive(params	.get("monthSettleNo"));
							if (map == null) {
								render(JSON_TYPE,CommonResponse.respFailJson("9031",	"不存在的月结号", params.get("reqNo")),
										response);
								return;
							}
							params.put("monthSettleNo", (String)map.get("monthSettleNo"));
						}
						if ("1".equals(params.get("cod"))) {
							ret = ValidateUtil.validateRequest(request,	reqParams(codParams), false, null, false,null, dynamicDataSourceHolder);
							if (!"TRUE".equals(ret.get("isSuccess"))) {
								log.info("validate false!!!!---->"+ ret.get("respCode") + "---------"+ ret.get("respMsg"));
								render(JSON_TYPE,CommonResponse.respFailJson(ret.get("respCode"),ret.get("respMsg"),params.get("reqNo")), response);
								return;
							}
						}

						if (StringUtils.isNotEmptyWithTrim(params	.get("goodValuation"))
								&& Float.parseFloat(params.get("goodValuation")) > 0) {
							ret = ValidateUtil.validateRequest(request,reqParams(new String[] { "goodValuationRate" }),false, null, false, null,
									dynamicDataSourceHolder);
							if (!"TRUE".equals(ret.get("isSuccess"))) {
								render(JSON_TYPE,CommonResponse.respFailJson(ret.get("respCode"),ret.get("respMsg"),params.get("reqNo")), response);
								return;
							}
						}
						System.out.println("goodValuationRate--------------->"+ params.get("goodValuationRate"));
						OrderInfo orderInfo = (OrderInfo) MapConverter	.convertMap(OrderInfo.class, params);
						String[] sendPhones = request.getParameterValues("sendPhone[]");
						orderInfo.setSendPhone(StringUtils.combineArray(sendPhones));
						String[] revPhones = request.getParameterValues("revPhone[]");
						orderInfo.setRevPhone(StringUtils.combineArray(revPhones));

						order.setSendArea(orderInfo.getSendArea());
						order.setSendAddr(orderInfo.getSendAddr());
						order.setSendName(orderInfo.getSendName());
						order.setSendPhone(orderInfo.getSendPhone());

						order.setRevArea(orderInfo.getRevArea());
						order.setRevAddr(orderInfo.getRevAddr());
						order.setRevName(orderInfo.getRevName());
						order.setRevPhone(orderInfo.getRevPhone());

						order.setItemType(orderInfo.getItemType());
						order.setFreightType(orderInfo.getFreightType());
						order.setMonthSettleName(orderInfo.getMonthSettleName());
						order.setMonthSettleNo(orderInfo.getMonthSettleNo());
						order.setMonthSettleCard(orderInfo.getMonthSettleCard());
						order.setCod(Integer.valueOf(params.get("cod")));
						order.setCodName(orderInfo.getCodName());
						order.setCodCardCnapsNo(orderInfo.getCodCardCnapsNo());
						order.setCodBank(orderInfo.getCodBank());
						order.setGoodValuation(orderInfo.getGoodValuation());
						order.setGoodValuationRate(orderInfo.getGoodValuationRate());
						order.setTimeType(orderInfo.getTimeType());
						if (order.getGoodValuation() > 0&& Float.parseFloat(order.getGoodValuationRate()) <= 0) {
							render(JSON_TYPE, CommonResponse.respFailJson("9026", "参数不正确：goodValuationRate",params.get("reqNo")), response);
							return;
						}

						System.out.println("******orderinfo***********");
						System.out.println(orderInfo);

					} else {
						render(JSON_TYPE, CommonResponse.respFailJson(ret.get("respCode"), ret.get("respMsg"),params.get("reqNo")), response);
						return;
					}

					ret = ValidateUtil.validateRequest(request,
							reqParams(new String[] { "freight", "npay" }),
							false, null, false, null, dynamicDataSourceHolder);
					if (!"TRUE".equals(ret.get("isSuccess"))) {
						render(JSON_TYPE, CommonResponse.respFailJson(
								ret.get("respCode"), ret.get("respMsg"),
								params.get("reqNo")), response);
						return;
					}

					Map<String, Object> vRate = null;

					float vpay = 0;
					float freight = Math.round(Float.valueOf(params.get("freight")) * 100) / 100f;
					float goodPrice = 0;
					float npay = Math.round(Float.valueOf(params.get("npay")) * 100) / 100f;// 如果要求精确4位就*10000然后/10000
					float lcpay = 0;
					float lvpay = 0;
					float lpayAcount = 0;
					float lnpay = 0;
					float mpay = 0; // 月结费用

					// 代收货款手续费
					if ("1".equals(params.get("cod"))) {
						goodPrice = Math.round(Float.valueOf(params.get("goodPrice")) * 100) / 100f;
						order.setCpayStatus("INIT");
						if (StringUtils.isNotEmptyWithTrim(params	.get("codName"))) {
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
					}

					// 保价手续费
					if (order.getGoodValuation() > 0) {
						if (StringUtils.isEmptyWithTrim(params.get("vpay"))) {
							render(JSON_TYPE, CommonResponse.respFailJson(
									"9001", "缺少参数：vpay保价手续费",
									params.get("reqNo")), response);
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
						System.out.println("支付方式为寄方付");
						lnpay = lvpay + freight;
					} else {
						System.out.println("支付方式为收方付");
						lnpay = 0;
					}

					if ("MONTH".equals(params.get("payType"))) {
						System.out.println("支付方式为月结...");
						//lnpay = lvpay + freight;
						mpay = lvpay + freight;
					}

					System.out.println("******vpay********" + vpay);
					System.out.println("******freight********" + freight);
					// System.out.println("******payAcount********"+payAcount);
					System.out.println("********npay******" + npay);
					lpayAcount = lpayAcount + freight;
					System.out.println("******lcpay********" + lcpay);
					System.out.println("******lvpay********" + lvpay);
					System.out.println("******lpayAcount********" + lpayAcount);
					System.out.println("******lnpay********" + lnpay);

					if (Math.abs(lvpay - vpay) >= 0.01) {
						render(JSON_TYPE, CommonResponse.respFailJson("9026",
								"参数不正确：vpay", params.get("reqNo")), response);
						return;
					}

					// if (Math.abs(lpayAcount-payAcount)>=0.01) {
					// render(JSON_TYPE, CommonResponse.respFailJson("9026",
					// "参数不正确：payAcount", params.get("reqNo")),response);
					// return ;
					// }

					if (Math.abs(lnpay - npay) >= 0.01) {
						render(JSON_TYPE, CommonResponse.respFailJson("9026",
								"参数不正确：npay", params.get("reqNo")), response);
						return;
					}
					order.setGoodPrice(goodPrice);
					order.setCpay(lcpay);
					order.setVpay(lvpay);
					order.setFreight(freight);
					order.setPayAcount(lpayAcount);
					order.setTnpay(npay);
					order.setMpay(mpay);
					if ("2".equals(params.get("freightType"))
							&& "1".equals(params.get("cod"))) {
						order.setSnapy(lvpay + freight + goodPrice);
					}
					if ("2".equals(params.get("freightType"))
							&& !"1".equals(params.get("cod"))) {
						order.setSnapy(lvpay + freight);
					}
					if ("1".equals(params.get("freightType"))
							&& "1".equals(params.get("cod"))) {
						order.setSnapy(goodPrice);
					}
					if ("1".equals(params.get("freightType"))
							&& !"1".equals(params.get("cod"))) {
						order.setSnapy(0);
					}
					// /////以下为从快递公司获取快递公司物流单号
					String lgcOrderNo = params.get("lgcOrderNo");
					if (StringUtil.isEmptyWithTrim(lgcOrderNo)) {
						render(JSON_TYPE, CommonResponse.respFailJson("9030",
								"运单号不能为空", params.get("reqNo")), response);
						return;
					}
					if (StringUtil.isEmptyWithTrim(order.getLgcOrderNo())) {
						if (orderInfoService.isExistLgcOrderNo(lgcOrderNo,
								lgcNo)) {
							render(JSON_TYPE, CommonResponse.respFailJson(
									"9030", "运单号重复", params.get("reqNo")),
									response);
							return;
						}
					} else {
						if (orderInfoService.isExistLgcOrderNo(lgcOrderNo,
								lgcNo, order.getOrderNo())) {
							render(JSON_TYPE, CommonResponse.respFailJson(
									"9030", "运单号重复", params.get("reqNo")),
									response);
							return;
						}

					}
					order.setLgcOrderNo(lgcOrderNo);

					// ////////////////

					if (lnpay > 0) {

						if (StringUtils.isEmptyWithTrim(params.get("payType"))) {
							render(JSON_TYPE, CommonResponse.respFailJson(
									"9001", "缺少参数：payType支付类型",
									params.get("reqNo")), response);
							return;
						}
						String payType = params.get("payType");
						order.setPayType(payType);

						/**
						 * 
						 * 
						 * 会员支付状态
						 * 
						 * 
						 * */

						if ("HUIYUAN".equals(params.get("payType"))&& "1".equals(params.get("freightType"))) {
							if (StringUtils.isEmptyWithTrim(params.get("vipNo"))) {
								render(JSON_TYPE,	CommonResponse.respFailJson("9001",
										"缺少参数：会员号", params.get("reqNo")),	response);
								return;
							}
							if (StringUtils.isEmptyWithTrim(params.get("vipPwd"))&& StringUtils.isEmptyWithTrim(params
									.get("monthSettleNo"))&& StringUtils.isEmptyWithTrim(params.get("monthSettleCard"))) {

								render(JSON_TYPE, CommonResponse.respFailJson(
										"9001", "缺少参数：会员密码",
										params.get("reqNo")), response);
								return;
							}
							Map<String, Object> map = userService.checkVipbInfo(params.get("vipNo"));
							if (map == null) {
								render(JSON_TYPE,CommonResponse.respFailJson("9031",
										"不存在的会员号", params.get("reqNo")),	response);
								return;
							}
							Map<String, Object> mapByPwd = userService	.checkVipbInfoByPwd(params.get("vipNo"),
									Md5.md5Str(params.get("vipPwd")));
							if (mapByPwd == null) {
								render(JSON_TYPE,CommonResponse.respFailJson("9031","会员号密码错误", params.get("reqNo")),	response);
								return;
							}
							if (!"1".equals(String.valueOf(mapByPwd.get("status")))) {
								render(JSON_TYPE, CommonResponse.respFailJson("9031", "此会员号不是启用状态",params.get("reqNo")), response);
								return;
							}
							float v = Float.valueOf(((BigDecimal) mapByPwd.get("balance")).toString());

							int disType = (Integer) mapByPwd.get("disType");// 优惠类型

							int uid = (Integer) mapByPwd.get("uid");// 余额表ID

							float balance = Float.valueOf(((BigDecimal) mapByPwd.get("balance")).toString());// 余额

							System.out
							.println("HUIYUAN,扣除前balance余额======================"
									+ balance);
							Map<String, Object> disMap = userService
									.disType(disType);// 优惠类型数据

							int discount = (Integer) disMap.get("discount");// 折扣等级

							/*float onlyFreight = Float.valueOf(params
									.get("freight")) * discount / 100f;*/
							
							float onlyFreight = Float.valueOf(params
									.get("freight")) ;                //不折扣
							float reality = onlyFreight + lvpay; // 实际扣除金额

							if (v < reality) {
								render(JSON_TYPE,	CommonResponse.respFailJson("9031",
										"会员号余额不足", params.get("reqNo")),						response);
								return;
							}

							System.out.println("HUIYUAN,reality======================"+ reality);

							balance = balance - reality;

							System.out.println("HUIYUAN,扣除后balance余额======================"+ balance);

							userService.minusBalance(reality,DateUtils.formatDate(nowDate,"yyyy-MM-dd HH:mm:ss"), String	.valueOf(uid));// 更新会员余额表

							Map<String, Object> insertVipHistory = new HashMap<String, Object>();
							insertVipHistory.put("dis_user_no",params.get("vipNo"));
							insertVipHistory.put("rmoney", reality);
							insertVipHistory.put("omoney", npay);
							insertVipHistory.put("af_balance", balance);
							insertVipHistory.put("status", "SUCCESS");
							insertVipHistory.put("order_no", order.getOrderNo());
							insertVipHistory.put("discount_text",disMap.get("discount_text"));
							insertVipHistory.put("lied", "N");
							insertVipHistory.put("courier_no", userNo);
							insertVipHistory.put("operator",loginUser.getRealName());
							insertVipHistory.put("create_time",DateUtils.formatDate(new Date()));
							insertVipHistory.put("last_update_time",DateUtils.formatDate(new Date()));
							insertVipHistory.put("note", "快递员取件扣费");
							insertVipHistory.put("source", "COURIER_TAKE");
							userService.vipExpense(insertVipHistory);// VIP消费记录插入
							order.setFpayStatus("SUCCESS");
							model.put("omoney", npay);
							model.put("rmoney", reality);
							model.put("balance", balance);
							lpayAcount = lpayAcount - freight + onlyFreight;
							order.setDisRealityFreight(onlyFreight);// 运费
							order.setDisDiscount(String.valueOf(disMap
									.get("discount_text")));
							order.setPayAcount(lpayAcount);
							order.setTnpay(reality);// 取件收总额
							order.setDisUserNo(params.get("vipNo"));
							order.setDisUserNo(params.get("vipNo"));
						}

						// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
						// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

						// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
						// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

						// 判断支付方式
						if (payType.equals(PayType.POS.toString())) {
							// 刷卡支付
							ret = ValidateUtil.validateRequest(request,	reqParams(new String[] { "merchantName","merchantNo", "terminalNo",	"encPinblock" }), false, null,
									false, null, dynamicDataSourceHolder);
							if (!"TRUE".equals(ret.get("isSuccess"))) {
								render(JSON_TYPE,CommonResponse.respFailJson(ret.get("respCode"),ret.get("respMsg"),params.get("reqNo")), response);
								return;
							}
							// encPinblock
							{
								if (StringUtils.isEmptyWithTrim(params.get("encTracks2"))&& StringUtils.isEmptyWithTrim(params
										.get("icData"))) {render(JSON_TYPE,CommonResponse.respFailJson("9001","缺少参数：encTracks2或icData",params.get("reqNo")),response);
										return;
								}

								String merchantName = params	.get("merchantName"); // 保存用户打印
								String merchantNo = params.get("merchantNo");
								String terminalNo = params.get("terminalNo");
								String encTracks2 = params.get("encTracks2");
								String encTracks3 = params.get("encTracks3");
								String encPinblock = params.get("encPinblock");
								String icData = params.get("icData");
								String mac = params.get("mac");

								String localMac = SHA.SHA1Encode(params.get("reqTime") + merchantNo + freight);


								Map<String, String> posParams = new HashMap<String, String>();
								posParams.put("merchantNo", merchantNo);
								posParams.put("terminalNo", terminalNo);
								posParams.put("amount",String.format("%.2f", npay));
								posParams.put("encryptPin", encPinblock);
								posParams.put("track2", encTracks2);
								posParams.put("track3", encTracks3);
								posParams.put("icData", icData);


								Map<String, Object> saleMap = PosUtil.sendAndRev(configInfo,posParams, BizType.SALE);
								if (saleMap != null&& "SUCCESS".equals(saleMap.get("respCode"))) {
									// 交易成功
									order.setPayStatus(PayStatus.FREIGTH.getValue());
									model.put("amount", npay);
									model.put("currency", "CNY");
									model.put("issuer", saleMap.get("issuer"));
									model.put("cardNo", saleMap.get("cardNo"));
									model.put("opType", "消费");
									model.put("batchNo", saleMap.get("batchNo"));
									model.put("voucherNo",saleMap.get("voucherNo"));
									model.put("authNo", saleMap.get("authNo"));
									model.put("refNo", saleMap.get("refNo"));
									model.put("operatorNo",saleMap.get("operatorNo"));
									model.put("icData", saleMap.get("icData"));
									model.put("transTime",saleMap.get("transTime"));

									Map<String, Object> posInfo = new HashMap<String, Object>();
									posInfo.putAll(model);
									posInfo.put("merchantName", merchantName); //
									posInfo.put("merchantNo", merchantNo);
									posInfo.put("terminalNo", terminalNo);
									posInfo.put("orderNo", order.getOrderNo());
									posInfo.put("courierNo", loginUserNo);
									posInfo.put("bizType", "FREIGHT");
									posInfoService.save(posInfo);

								} else {
									// order.setPayStatus(0);
									String msg = "未知错误";
									if (saleMap != null&& saleMap.get("respCode") != null) {
										msg = saleMap.get("respCode")
												.toString()+ saleMap.get("respMsg").toString();
									}
									log.info("******************************");
									log.info(msg);
									render(JSON_TYPE,CommonResponse.respFailJson("9022",
											"支付失败:" + msg,params.get("reqNo")),response);
									return;
								}

							}

						} else {
							// 其他，默认现金支付
							order.setPayStatus(PayStatus.FREIGTH.getValue());
							log.info("***********cash_pay************");
						}
					} else {
						order.setPayStatus(PayStatus.NOPAY.getValue());
					}

					/*
					 * if (order.getFreightType()!=3) { ret =
					 * ValidateUtil.validateRequest(request,reqParams(new
					 * String[] { "freight"}), false, null); if
					 * (!"TRUE".equals(ret.get("isSuccess"))) {
					 * render(JSON_TYPE,
					 * CommonResponse.respFailJson(ret.get("respCode"),
					 * ret.get("respMsg"), params.get("reqNo")),response);
					 * return ; }
					 * order.setFreight(Float.valueOf(params.get("freight"))); }
					 */

					List<String> mime = new ArrayList<String>();
					mime.add("image/jpeg");
					mime.add("image/pjpeg");
					mime.add("image/gif");
					mime.add("image/png");
					List<RequestFile> files = getFile(	request,null,configInfo.getFile_root(),
							"/order/"	+ DateUtils.formatDate(nowDate,"yyyyMMddHH"), mime);
					List<OrderPic> imagesList = new ArrayList<OrderPic>();
					ResizeImage resizeImage = new ResizeImage();
					float resizeTimes = 1;
					for (RequestFile image : files) {
						OrderPic orderPic = new OrderPic();
						orderPic.setOrderNo(order.getOrderNo());
						orderPic.setFileType(image.getFileType());
						orderPic.setFileName(image.getFileName());
						orderPic.setFileSize(image.getFileSize());
						orderPic.setFilePath(image.getFilePath());
						orderPic.setFileUri("/codfile/order/"+ DateUtils.formatDate(nowDate, "yyyyMMddHH")
								+ "/" + image.getFileName());
						orderPic.setOrginalName(image.getOrginalName());
						if (Integer.valueOf(image.getFileSize()) > 1024 * 800) { // 大于5mb压缩
							resizeTimes = (1.0f * 1024 * 800)
									/ Integer.valueOf(image.getFileSize());
							resizeImage.zoomImage(configInfo.getFile_root()
									+ image.getFilePath(), resizeTimes);
						}
						imagesList.add(orderPic);
					}

					if (StringUtils.isNotEmptyWithTrim(params.get("imageIds"))) {
						List<OrderPic> delImagesList = new ArrayList<OrderPic>();
						String[] imageIds = params.get("imageIds").split(",");
						for (String imageId : imageIds) {
							OrderPic orderPic = new OrderPic();
							orderPic.setId(Integer.parseInt(imageId));
							orderPic.setOrderNo(order.getOrderNo());
							delImagesList.add(orderPic);
						}
						orderPicService.delPics(delImagesList); // 批量删除图片
					}

					if (StringUtils.isNotEmptyWithTrim(params.get("itemName"))) {
						order.setItemName(params.get("itemName"));
					}
					if (StringUtils.isNotEmptyWithTrim(params.get("itemWeight"))) {
						order.setItemWeight(Float.valueOf(params.get("itemWeight")));
					}

					order.setStatus(2); // 已取件

					order.setLastUpdateTime(DateUtils.formatDate(nowDate));
					order.setTakeOrderTime(DateUtils.formatDate(nowDate));
					order.setItemStatus(params.get("itemStatus"));





					if ("1".equals(order.getFreightType())) {
						order.setFpayStatus("SUCCESS");
					} else {
						order.setFpayStatus("INIT");
					}

					order.setIdCard(params.get("idCard"));// 获取的身份证号码

					if (!StringUtils.isEmptyString(params.get("receNo"))) {
						order.setReceNo(params.get("receNo"));
						order.setReqRece("Y");
					} else {
						order.setReceNo("");
						order.setReqRece("N");
					}


					if (!StringUtils.isEmptyString(params.get("zidanOrder"))) {
						if (params.get("zidanOrder").length() > 255) {
							render(JSON_TYPE, CommonResponse.respFailJson("9022","回单号数量过多,单词最多输出20个", ""), response);
							return;
						}
					}
					if (StringUtils.isEmptyString(params.get("zidanNumber"))) {
						order.setZidanNumber("0");
						order.setItemCount("1");
					} else {
						order.setZidanNumber(params.get("zidanNumber"));
						order.setItemCount((1+Integer.valueOf(params.get("zidanNumber"))+""));
					}
					OrderInfo oInfo = (OrderInfo) order.clone() ;
					oInfo.setZidanOrder(null);
					oInfo.setZidanNumber("0");
					oInfo.setItemCount(1+"");
					oInfo.setZid(1);
					oInfo.setParentOrder(order.getOrderNo());
					oInfo.setCpay(0);
					oInfo.setVpay(0);
					oInfo.setMpay(0);
					oInfo.setGoodValuation(0);
					oInfo.setSnapy(0);
					oInfo.setGoodPrice(0);
					oInfo.setFreight(0);




					String[] zidanOrderStr = {} ;
					int zidanOrderStrLength = 0 ;
					List<String> orderNos =  new ArrayList<String>() ;//子单orderNo


					String uid = params.get("uid");
					uid = uid.substring(0, uid.indexOf("_"));

					String fix = "" ;
					/*if ("yx".equals(uid)) {
						fix = "yx" ;
					}*/


					boolean isExist = false ;
					String st = "" ;

					if (!StringUtils.isEmptyString(params.get("zidanOrder"))) {
						String zidanOrder = params.get("zidanOrder");

						if (zidanOrder.contains(",")) {
							int i = 1;
							zidanOrderStr = zidanOrder.split(",");
							zidanOrderStrLength = zidanOrderStr.length;
							String sumOrder = "";

							for (String strOrder : zidanOrderStr) {
								st = strOrder ;
								if (i != zidanOrderStrLength) {
									sumOrder = sumOrder + fix + strOrder + ",";
								} else {
									sumOrder = sumOrder + fix + strOrder;
								}
								if (orderInfoService.isExistLgcOrderNo(fix +strOrder, lgcNo)) {
									isExist = true ;
									break ;
								}

								i++;
							}

							order.setZidanOrder(sumOrder);
							order.setItemCount(i+"");
						} else {
							order.setZidanOrder(fix + zidanOrder);
							order.setItemCount(1+"");
							zidanOrderStr = new  String[1] ;
							zidanOrderStr[0] =  zidanOrder ;
							if (orderInfoService.isExistLgcOrderNo(fix +zidanOrder, lgcNo)) {
								isExist = true ;
							}
						}
					}


					if (isExist) {
						render(JSON_TYPE, CommonResponse.respFailJson("9030",	st+"子单号重复", params.get("reqNo")), response);
						return ;
					}		

					order.setForNo(params.get("forNo"));
					order.setZid(0);
					
					String isMessage = lgcService.getLgcConfig("MOBILE_CONFIG", "TAKE_SEND_MSG", "0") ;
					String message = "0" ;
					if ("1".equals(isMessage)) {
						if ("1".equals(params.get("message"))) {
							message = "1" ;
						}
					}
					order.setMessage(message);
					
					orderInfoService.yxTakeUpdate(order); // 更新订单信息 主单

					if (StringUtils.isNotEmptyWithTrim(params.get("orderNote"))) {
						System.out.println(params.get("orderNote"));
						order.setOrderNote(params.get("orderNote"));
						orderInfoService.insertOrderNote(order, userNo,	String.valueOf(order.getId()), "取件订单备注");
					}


					orderPicService.save(imagesList); // 保存订单图片

					orderInfoService.completeMsg(order); // 补全订单信息

					//					orderInfoService.updateCodBank(order);// 更新代收货款银行信息

					orderInfoService.changeOrderRegisterFirst(order);// 登记第一次录入信息


					//以下保存子单信息
					for (int i = 0; i < zidanOrderStr.length; i++) {
						String orderno = sequenceService.getNextVal("order_no") ;
						orderNos.add(orderno) ;

						oInfo.setLgcOrderNo(fix+zidanOrderStr[i]);
						oInfo.setOrderNo(orderno);
						long zid = orderInfoService.save(oInfo) ; 
						if (StringUtils.isNotEmptyWithTrim(order.getOrderNote())) {
							orderInfoService.insertOrderNote(oInfo, userNo,	String.valueOf(zid), "新建订单备注");
						}
						//						if ("yx".equals(uid)) {
						oInfo.setForNo(params.get("forNo"));
						orderInfoService.yxTakeUpdate(oInfo); // 更新订单信息 子单
						//						} else {
						//							orderInfoService.takeUpdate(oInfo); // 更新订单信息
						//						}
					}
					
			/*		if("kuaike".equals(uid)){
						if(StringUtils.isNotEmptyWithTrim(order.getRevPhone())&&order.getRevPhone().length()==11){
							String content ="您好！欢迎您使用快客同城速配，现有您一件同城货物已收件，单号"+lgcOrderNo+".请您电话保持畅通，我们将及时为您配送。下单、查询、投诉建议请关注微信公众号“快刻同城速配”，服务热线：0592－7127770【快递王子】";
							RunnableUtils ui = new RunnableUtils();
							ui.MessagePushClass(lgcNo, order.getRevPhone(),content);	
						}				
					}	*/					




					Map<String, Object> sMap = lgcService.getSubstationInfo(loginUser.getSubstationNo());
					OrderTrack track = new OrderTrack();
					track.setOrderNo(order.getOrderNo());
					track.setContext(sMap.get("substation_name") + ",快递员:"+ loginUser.getRealName() + ",已收件,联系方式："	+ loginUser.getPhone());
					track.setOrderTime(DateUtils.formatDate(nowDate));
					track.setCompleted("N");
					track.setCurNo(loginUserNo);
					track.setCurType("C");
					track.setNextNo(loginUser.getSubstationNo());
					track.setNextType("S");
					track.setOrderStatus("TAKEING");
					track.setParentId(0);
					track.setIsLast("Y"); //
					track.setOpname(loginUser.getRealName());
					orderTrackService.add(track);
					
					for (String sno:orderNos) {
						track.setOrderNo(sno);
						orderTrackService.add(track);
					}
					User userInfo = userService.getUserByNo(userNo);// 查询快递员信息
					/***
					 * 微信信息推送
					 */
					if (!StringUtil.isEmptyWithTrim(order.getWxOpenid())) {
						/*System.out.println("微信推送线程开始");
						RunnableUtils run1 = new RunnableUtils();
						run1.pushWEIXIN(order, userInfo, 4);*/
						Map<String, String> wxMap = new HashMap<String, String>() ;
					/*	wxMap.put("url",buildUrl(lgcNo, params.get("uid"), order.getLgcOrderNo()));
						wxMap.put("dskey", params.get("uid").substring(0,params.get("uid").indexOf("_"))) ;
						wxMap.put("t","2");
						wxMap.put("touser",order.getWxOpenid());
						wxMap.put("lgcOrderNo",order.getLgcOrderNo());
						wxMap.put("remark","\n快递已妥妥的打包完毕，即将出发");
						wxMap.put("first","\n尊敬的客户，您好,您的快件已被取走");
						WeiXinUtil.push(wxMap);*/
 
						WxxdConfig wxxdConfig = wxxdConfigService.getByDskey(params.get("uid").substring(0,params.get("uid").indexOf("_"))) ;
						String wxxd_url = wxxdConfig==null?"":wxxdConfig.getXdUrl() ;
						
						wxMap.put("url",buildUrl(wxxd_url,lgcNo, params.get("uid"),order.getOrderNo(), order.getLgcOrderNo()));
						wxMap.put("dskey", params.get("uid").substring(0,params.get("uid").indexOf("_"))) ;
						wxMap.put("tmplateId", "TM00071") ;
						wxMap.put("touser",order.getWxOpenid());
						Map<String, Map<String, String>> data = new HashMap<String, Map<String, String>>() ;
						 Map<String, String> lgcOrderNo1 = new HashMap<String, String>() ;
						 lgcOrderNo1.put("value", order.getLgcOrderNo());
						 
						 Map<String, String> company = new HashMap<String, String>() ;
						 company.put("value", "我们");
						 
						 Map<String, String> remark = new HashMap<String, String>() ;
						 remark.put("value", "\n快递已妥妥的打包完毕，即将出发");
						 remark.put("color", "#173177");
						 
						 data.put("orderNumber", lgcOrderNo1) ;
						 data.put("company", company) ;
						 data.put("remark", remark) ;
						 wxMap.put("data", JsonUtil.toJson(data)) ;
						
						 WeiXinUtil.push(configInfo,wxMap,2);
						
					
						Map<String, String> wxMap2 = new HashMap<String, String>(wxMap) ;
						wxMap2.put("url",buildWXUrl(wxxd_url,lgcNo, params.get("uid"), order.getOrderNo(),"0"));
						Map<String, Map<String, String>> data2 = new HashMap<String, Map<String, String>>(data) ;
						Map<String, String> remark2 = new HashMap<String, String>(remark) ;
						 remark2.put("value","\n快递已取走，立即进行评价");
						 data2.put("remark",remark2);
						 wxMap2.put("data", JsonUtil.toJson(data2)) ;
						WeiXinUtil.push(configInfo,wxMap2,2);
						
						
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
					 * 
					 * 
					 * 
					 * 
					 * 
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

					model.put("lgcOrderNo", lgcOrderNo);
					render(JSON_TYPE, CommonResponse.respSuccessJson("收件成功",
							model, params.get("reqNo")), response);
				}
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,
						CommonResponse.respFailJson(ret.get("respCode"),
								ret.get("respMsg"), params.get("reqNo")),
								response);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			render(JSON_TYPE,CommonResponse.respFailJson("9010", "数据解析错误",
					params.get("reqNo")), response);
		} catch (ConnectException e) {
			e.printStackTrace();
			render(JSON_TYPE,CommonResponse.respFailJson("9024", "连接已断开",
					params.get("reqNo")), response);
		} catch (SocketTimeoutException exception) {
			exception.printStackTrace();
			render(JSON_TYPE,CommonResponse.respFailJson("9023", "连接超时",
					params.get("reqNo")), response);
		} catch (FileUnknowTypeException e) {
			// e.printStackTrace();
			render(JSON_TYPE,CommonResponse.respFailJson("9012", "文件类型错误",
					params.get("reqNo")), response);
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,CommonResponse.respFailJson("9000", "服务器异常",
					params.get("reqNo")), response);
		}
	}

	// 派件扫描
	@RequestMapping(value = "/send")
	public void send(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] {}), true, userSessionService,
					checkVersion, appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				Date nowDate = new Date();
				String userNo = ret.get("userNo");
				params.put("userNo", userNo);
				params.put("createTime",
						DateUtils.formatDate(new Date(), "yyyy-MM-dd"));
				// 签到限制
				List<Map<String, Object>> signList = userSessionService	.signInfoAll(params);// 查询今日签到次数
				List<Map<String, Object>> signOutList = userSessionService.signOutInfoAll(params);// 查询今日签退次数
				int signTimes = signList.size();
				int signOutTimes = signOutList.size();
				System.out.println("signTimes=============" + signTimes
						+ "//////////signOutTimes=============" + signOutTimes);
				if (signTimes < 1 || signTimes == signOutTimes) {
					render(JSON_TYPE, CommonResponse.respFailJson("9047",
							"尚未签到或已签退", params.get("reqNo")), response);
					return;
				}
				User user = userService.getUserByNo(userNo);
				String orderNo = params.get("orderNo");
				String lgcNo = userService.getUserLgcNo(ret.get("userNo"));
				OrderInfo order = orderInfoService.getByLgcOrderNo(orderNo,lgcNo);

				orderInfoService.send(order.getOrderNo(), ret.get("userNo"),user.getSubstationNo());

				OrderTrack lastOrderTrack = orderTrackService.getLastOrderTrack(order.getOrderNo());
				String innerNo = user.getInnerNo();
				if (StringUtil.isEmptyWithTrim(innerNo)) {
					innerNo = "";
				} else {
					innerNo = "(" + innerNo + ")。";
				}
				Map<String, Object> sMap = lgcService.getSubstationInfo(user.getSubstationNo());
				OrderTrack orderTrack = new OrderTrack();
				orderTrack.setOrderNo(order.getOrderNo());
				orderTrack.setOrderTime(DateUtils.formatDate(new Date()));
				orderTrack.setPreNo(user.getSubstationNo());
				orderTrack.setPreType("S");
				orderTrack.setCompleted("N");
				orderTrack.setIsLast("Y");
				orderTrack.setScanIno(user.getCourierNo());
				orderTrack.setScanIname(user.getRealName());
				orderTrack.setOrderStatus("SIGNING");
				orderTrack.setCurNo(user.getCourierNo());
				orderTrack.setCurType("C");
				orderTrack.setContext(sMap.get("substation_name") + ",快递员:"
						+ user.getRealName() + innerNo + "将很快进行派送，请保持电话畅通："
						+ user.getPhone());
				orderTrack.setParentId(lastOrderTrack.getId());
				orderTrack.setOpname(user.getRealName());
				orderTrackService.add(orderTrack);
				orderTrackService.unLastTrack(lastOrderTrack);
				orderInfoService.updateRealSendTime(order.getOrderNo());
				User userInfo = userService.getUserByNo(userNo);// 查询快递员信息
				/**
				 * 微信信息推送
				 * 
				 */
				if (!StringUtil.isEmptyWithTrim(order.getWxOpenid())) {
					/*RunnableUtils run1 = new RunnableUtils();
					run1.pushWEIXIN(order, userInfo, 5);*/

					//微信推送
					WxxdConfig wxxdConfig = wxxdConfigService.getByDskey(params.get("uid").substring(0,params.get("uid").indexOf("_"))) ;
					String wxxd_url = wxxdConfig==null?"":wxxdConfig.getXdUrl() ;
					Map<String, String> wxMap = new HashMap<String, String>() ;
					wxMap.put("url",buildUrl(wxxd_url,lgcNo, params.get("uid"),order.getOrderNo(), order.getLgcOrderNo()));
					wxMap.put("dskey", params.get("uid").substring(0,params.get("uid").indexOf("_"))) ;
					wxMap.put("t","3");
					wxMap.put("touser",order.getWxOpenid());

					wxMap.put("first","尊敬的客户，您好,您的快件正在派送中");
					wxMap.put("lgcOrderNo",order.getLgcOrderNo());
					wxMap.put("contact",userInfo.getPhone());
					wxMap.put("remark","");
					WeiXinUtil.push(configInfo,wxMap,1);

				}
				String uid = params.get("uid") ;
				if (uid.contains("_")) {
					uid = uid.substring(0, uid.indexOf("_"));
				}
				
				
	// XXXXXXXXXXXXXXXXxxx,
				
					String isMessage = lgcService.getLgcConfig("MOBILE_CONFIG", "SEND_SEND_MSG", "0") ;
					if ("1".equals(isMessage)) {
						if(StringUtils.isNotEmptyWithTrim(order.getRevPhone())&&order.getRevPhone().length()==11){
							String content = user.getRealName()+"快递员正在派送中，电话："+user.getPhone()+"，请耐心等待。【快递王子】";
							if("kuaike".equals(uid)){
								content ="您好！欢迎您使用快客同城速配，现有您一件同城货物派送中，单号"+order.getLgcOrderNo()+".请您电话保持畅通，我们将及时为您配送。下单、查询、投诉建议请关注微信公众号“快刻同城速配”，服务热线：0592－7127770【快递王子】";
							}
							String channel = "szkyt" ;
							if("yx".equals(uid)){
								content ="【亿翔快递】尊敬的"+StringUtil.nullString(order.getRevName())+"先生/女士 ，您的快递："+order.getLgcOrderNo()+"已由"+user.getRealName()+"快递员即将配送，电话："+user.getPhone()+",请留意收件并保持电话畅通。如您家中有生活垃圾需要带出请告知快递员，感谢您使用亿翔快递，祝您生活愉快！";
							    channel = "yx" ;
							}
							RunnableUtils ui = new RunnableUtils();
							ui.MessagePushClass(channel,lgcNo, order.getRevPhone(),content,lgcService);	
						}	
					}
				
				
			/*	if("kuaike".equals(uid)){
					if(StringUtils.isNotEmptyWithTrim(order.getRevPhone())&&order.getRevPhone().length()==11){
						String content ="您好！欢迎您使用快客同城速配，现有您一件同城货物派送中，单号"+order.getLgcOrderNo()+".请您电话保持畅通，我们将及时为您配送。下单、查询、投诉建议请关注微信公众号“快刻同城速配”，服务热线：0592－7127770【快递王子】";
						RunnableUtils ui = new RunnableUtils();
						ui.MessagePushClass(lgcNo, order.getRevPhone(),content);	
					}				
				}	*/
				
				
				/**
				 * 一米鲜快递推送通知
				 */

				if ("YMX".equals(order.getSource())) {
					System.out.println("一米鲜快递推送通知");
					String info = "快递员" + userInfo.getRealName()
							+ "即将派件,请保持电话畅通";
					RunnableUtils ymx = new RunnableUtils();
					ymx.pushTMX(order.getOrderNote(), "1", params
							.get("orderNo"), userInfo.getRealName(), userInfo
							.getPhone(), info, DateUtils.formatDate(nowDate,
									"yyyy-MM-dd HH:mm:ss"), params.get("uid"));
				}
				/***
				 * 康美药业推送
				 */
				if ("康美".equals(order.getSource())) {
					RunnableUtils run1 = new RunnableUtils();
					run1.KangmeiPush(URL, company_num, userName,
							order.getLgcOrderNo(), sMap.get("substation_name")
							+ "快递员:" + user.getRealName() + innerNo
							+ "正在派件，联系方式：" + user.getPhone(), "0");
				}
				render(JSON_TYPE,
						CommonResponse.respSuccessJson("9040", "揽件成功",
								params.get("reqNo")), response);
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,
						CommonResponse.respFailJson(ret.get("respCode"),
								ret.get("respMsg"), params.get("reqNo")),
								response);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9000", "服务器异常",
							params.get("reqNo")), response);
		}
	}

	// 派件扫描
	@RequestMapping(value = "/sendEx")
	public void sendEx(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
																   reqParams(new String[]{}), true, userSessionService,
																   checkVersion, appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				Date nowDate = new Date();
				String userNo = ret.get("userNo");
				params.put("userNo", userNo);
				params.put("createTime",
						   DateUtils.formatDate(new Date(), "yyyy-MM-dd"));
				// 签到限制
				List<Map<String, Object>> signList = userSessionService
						.signInfoAll(params);// 查询今日签到次数
				List<Map<String, Object>> signOutList = userSessionService
						.signOutInfoAll(params);// 查询今日签退次数
				int signTimes = signList.size();
				int signOutTimes = signOutList.size();
				System.out.println("signTimes=============" + signTimes
						+ "//////////signOutTimes=============" + signOutTimes);
				if (signTimes < 1 || signTimes == signOutTimes) {
					render(JSON_TYPE, CommonResponse.respFailJson("9035",
																  "尚未签到或已签退", params.get("reqNo")), response);
					return;
				}
				String orderNo = params.get("orderNo");

				model = new HashMap<String, Object>();

				String lgcNo = userService.getUserLgcNo(ret.get("userNo"));
				OrderInfo order = orderInfoService.getByLgcOrderNo(orderNo, lgcNo);
				if (order == null) {
					User user = userService.getUserByNo(userNo);
					String substationNo = user.getSubstationNo();
					if (!warehouseService.SubStationISExist(substationNo)) {
						render(JSON_TYPE, CommonResponse.respFailJson("9000", "当前用户无分配分站,请联系客服人员分配分站", params.get("reqNo")), response);
						return;
					}
					orderInfoService.insertNotInfo("KDWZ.SEND.SCAN", nowDate, substationNo, lgcNo, orderNo);
					order = orderInfoService.getByLgcOrderNo(orderNo, lgcNo);
				}
				if (order == null) {
					render(JSON_TYPE, CommonResponse.respFailJson("9041", "运单不存在", params.get("reqNo")), response);
					return;
				}
				if (order.getStatus() != 7) {
					if (order.getStatus() == 3) {
						render(JSON_TYPE, CommonResponse.respFailJson(
							   "9048", "操作不合法,订单状态是已完成。",
							   params.get("reqNo")), response);
						return;
					}
					if (order.getStatus() == 1) {
						render(JSON_TYPE, CommonResponse.respFailJson(
							   "9048", "操作不合法,订单状态是未收件。",
							   params.get("reqNo")), response);
						return;
					}
				}
				if (StringUtils.isNotEmptyWithTrim(order.getLgcNo())) {
					if (!lgcNo.equals(order.getLgcNo())) {
						render(JSON_TYPE, CommonResponse.respFailJson(
							   "9048", "操作不合法，订单不是当前公司运单",
							   params.get("reqNo")), response);
						return;
					}
				}
				if (userNo.equals(order.getSendCourierNo())) {
					render(JSON_TYPE, CommonResponse.respFailJson("9051",
																  "已揽收", params.get("reqNo")), response);
					return;
				}
				if ("N".equals(order.getTakeMark())) {
					render(JSON_TYPE, CommonResponse.respFailJson("9050",
																  "已被" + order.getSendCourierNo() + "揽收",
																  params.get("reqNo")), response);
					return;
				}

				if (userNo.equals(order.getTakeCourierNo())) {
					render(JSON_TYPE, CommonResponse.respFailJson("9052",
																  "该件已在我的已收列表", params.get("reqNo")), response);
					return;
				}
				
				render(JSON_TYPE,
					   CommonResponse.respSuccessJson("", "可揽件",
													  params.get("reqNo")), response);
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,
					   CommonResponse.respFailJson(ret.get("respCode"),
												   ret.get("respMsg"), params.get("reqNo")),
					   response);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			render(JSON_TYPE,
				   CommonResponse.respFailJson("9000", "服务器异常",
											   params.get("reqNo")), response);
		}
	}

	// 派件签收
	@RequestMapping(value = "/sign")
	public void sign(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			model = new HashMap<String, Object>();
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "orderNo", "signStatus" }), true,
					userSessionService, checkVersion, appVersionService,
					dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String payType = params.get("payType");
				String loginUserNo = ret.get("userNo");
				String lgcNo = userService.getUserLgcNo(loginUserNo);
				Date nowDate = new Date();
				User loginUser = userService.getUserByNo(loginUserNo);
				OrderInfo order = orderInfoService.getByOrderNo(params.get("orderNo"), lgcNo);
				if (order == null) {
					render(JSON_TYPE,
							CommonResponse.respFailJson("9013", "订单不存在",
									params.get("reqNo")), response);
					return;
				}
				if (order.getStatus() != 8 && order.getStatus() != 2) {
					render(JSON_TYPE,
							CommonResponse.respFailJson("9015", "操作不合法",
									params.get("reqNo")), response);
					return;
				}
				if (!ret.get("userNo").equals(order.getSendCourierNo())) { //
					render(JSON_TYPE,
							CommonResponse.respFailJson("9015", "操作不合法",
									params.get("reqNo")), response);
					return;
				}
				
				if ("yx".equalsIgnoreCase(params.get("uid"))) {
					if (order.getTakeOrderTime() != null && order.getTakeCourierNo() != null) {
						render(JSON_TYPE, CommonResponse.respFailJson("9060", "此单未取件，请补充取件信息", params.get("reqNo")), response);
						return;
					}
					if (order.getFreight() == 0) {
						render(JSON_TYPE, CommonResponse.respFailJson("9060", "此单未取件，请补充取件信息", params.get("reqNo")), response);
						return;
					}
				}
				
				if (Integer.parseInt(params.get("signStatus")) == 0) { // 拒签或者二次投递
					ret = ValidateUtil.validateRequest(request,
							reqParams(new String[] { "reasonNo" }), false,
							null, false, null, dynamicDataSourceHolder);
					if (!"TRUE".equals(ret.get("isSuccess"))) {
						render(JSON_TYPE, CommonResponse.respFailJson(
								ret.get("respCode"), ret.get("respMsg"),
								params.get("reqNo")), response);
						return;
					}
					User userInfo = userService.getUserByNo(loginUserNo);// 查询快递员信息
					String reasonNo = params.get("reasonNo");
					String reasonContext = userService
							.queryReasonContext(reasonNo);// 获取问题原因

					String orderNo = order.getOrderNo();

					/**
					 * 物流扭转信息更新
					 * 
					 */
					String cur_no = "";
					String cur_type = "";
					int id = 0;
					Map<String, Object> firstTrackMap = orderTrackService
							.checkOrderTrack(orderNo);
					if (firstTrackMap != null) {
						id = (Integer) firstTrackMap.get("id");
						cur_no = (String) firstTrackMap.get("cur_no");// 当前流转编号（快递员编号或分站编号）
						cur_type = (String) firstTrackMap.get("cur_type");// 当前编号类型，C为快递员编号，S为分站编号
						orderTrackService.updateIsLast(id);
					}
					String innerNo = loginUser.getInnerNo();
					if (StringUtil.isEmptyWithTrim(innerNo)) {
						innerNo = "";
					} else {
						innerNo = "(" + innerNo + ")。";
					}

					Map<String, Object> sMap = lgcService
							.getSubstationInfo(loginUser.getSubstationNo());
					OrderTrack track1 = new OrderTrack();
					track1.setOrderNo(orderNo);
					track1.setContext(sMap.get("substation_name") + ",快递员:"
							+ loginUser.getRealName() + innerNo + "联系方式："
							+ loginUser.getPhone() + "!" + reasonContext);
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
					track1.setIsLast("Y"); //
					track1.setOpname(loginUser.getRealName());
					orderTrackService.add(track1);

					/**
					 * 信息推送
					 * */

					PushMsg msg = new PushMsg();
					msg.setUserNo(order.getUserNo());
					msg.setUserType(1);
					msg.setMsgCode(MsgType.REFUSE.getValue());
					msg.setMsgContent("您的快递在" + DateUtils.formatDate(nowDate)
							+ "!" + reasonContext);
					msg.setMsgData(orderNo);
					msg.setCreateTime(DateUtils.formatDate(nowDate));
					msg.setExpireTime(DateUtils.formatDate(DateUtils.addDate(
							nowDate, 0, 6, 0)));
					long msgId = msgService.save(msg);
					PushUtil.pushById(configInfo,String.valueOf(msgId), 1,
							params.get("uid"));

					/**
					 * 问题件处理
					 */
					Map<String, Object> refuseMap = new HashMap<String, Object>();
					refuseMap.put("order_no", orderNo);
					refuseMap.put("lgc_order_no", order.getLgcOrderNo());
					refuseMap.put("pro_type", reasonNo);// 问题编号
					refuseMap.put("descb", reasonContext);// 问题描述
					refuseMap.put("status", "1");// 处理状态
					refuseMap.put("check_name", userInfo.getRealName());// 登记人
					long proOrderID = 0l;
					Map<String, Object> questionCheck = orderInfoService
							.doubtCheck(orderNo);// 查询问题件是否存在
					if (questionCheck == null) {
						proOrderID = orderInfoService.refuse(refuseMap);
					} else {
						proOrderID = orderInfoService.updateQuestion(refuseMap);// 更新到最新的问题件信息
					}

					orderInfoService.refuse(orderNo, reasonContext,
							String.valueOf(proOrderID));
					/***
					 * 一米鲜订单信息更新推送
					 * 
					 */

					if ("YMX".equals(order.getSource())) {
						System.out.println("一米鲜快递推送通知");
						String info = "您的订单：" + orderNo + " 被客户拒签，或将二次投递：原因"
								+ reasonContext;
						RunnableUtils ymx = new RunnableUtils();
						ymx.pushTMX(order.getOrderNote(), "4", orderNo,
								userInfo.getRealName(), userInfo.getPhone(),
								info, DateUtils.formatDate(nowDate,
										"yyyy-MM-dd HH:mm:ss"), params
										.get("uid"));
					}
					/***
					 * 康美药业推送
					 */
					if ("康美".equals(order.getSource())) {
						RunnableUtils run1 = new RunnableUtils();
						run1.KangmeiPush(URL, company_num, userName,
								order.getLgcOrderNo(), "您的订单：" + orderNo
								+ " 被客户拒签，或将二次投递：原因" + reasonContext,
								"2");
					}
					/**
					 * 
					 * 微信推送
					 * 
					 * */
					if (!StringUtil.isEmptyWithTrim(order.getWxOpenid())) {
						/*System.out.println("微信推送线程开始");
						RunnableUtils run1 = new RunnableUtils();
						run1.pushWEIXIN(order, userInfo, 6);*/
						WxxdConfig wxxdConfig = wxxdConfigService.getByDskey(params.get("uid").substring(0,params.get("uid").indexOf("_"))) ;
						String wxxd_url = wxxdConfig==null?"":wxxdConfig.getXdUrl() ;
						//微信推送
						Map<String, String> wxMap = new HashMap<String, String>() ;
						wxMap.put("url",buildUrl(wxxd_url,lgcNo, params.get("uid"),order.getOrderNo(), order.getLgcOrderNo()));
						wxMap.put("dskey", params.get("uid").substring(0,params.get("uid").indexOf("_"))) ;
						wxMap.put("t","5");
						wxMap.put("touser",order.getWxOpenid());

						wxMap.put("first","尊敬的客户，您的快件已被拒签，或将二次投递：原因" + reasonContext);
						wxMap.put("keyword1",order.getLgcOrderNo());
						wxMap.put("keyword2",reasonContext);
						wxMap.put("remark","");
						WeiXinUtil.push(configInfo,wxMap,1);

					}
					render(JSON_TYPE,
							CommonResponse.respSuccessJson("", "状态更新成功",
									params.get("reqNo")), response);
					return;
				}
				/**
				 * 正常签收流程
				 * 
				 * 
				 */

				Integer zid = order.getZid() ;
				boolean pay = true ;
				if (zid==1) {
					pay = false ;
				}

				if (pay&&StringUtils.isEmptyWithTrim(params.get("signType"))) {
					render(JSON_TYPE, CommonResponse.respFailJson("9001",
							"缺少参数：signType", params.get("reqNo")), response);
					return;
				}
				String sign = "已签收";
				String signType = params.get("signType"); // 签收方式
				if (!pay) {
					signType = "本人签收" ;
				}

				float npay = Float.valueOf(params.get("npay"));
				Map<String, Object> payMap = orderInfoService.getNpay(order);
				float lnpay = (Float) payMap.get("npay");

				if (pay&&"2".equals(order.getFreightType())) { // 邮费
					if (StringUtils.isEmptyWithTrim(params.get("payType"))) {
						render(JSON_TYPE, CommonResponse.respFailJson("9001",
								"缺少参数：payType", params.get("reqNo")), response);
						return;
					}
					if (StringUtils.isEmptyWithTrim(params.get("npay"))) {
						render(JSON_TYPE, CommonResponse.respFailJson("9001",
								"缺少参数：npay", params.get("reqNo")), response);
						return;
					}

					if ("2".equals(order.getFreightType())) {
						order.setPayType(payType);
					}

					order.setPayAcount(order.getPayAcount());
					order.setSnapy(npay);
					if ("MONTH".equals(payType)) {	order.setMpay(order.getFreight() + order.getVpay());
					orderInfoService.updateMnay(order);
					}
					if (Math.abs(npay - lnpay) >= 0.01) {
						render(JSON_TYPE, CommonResponse.respFailJson("9026",
								"参数不正确：npay", params.get("reqNo")), response);
						return;
					}

					// 选择会员支付
					if ("HUIYUAN".equals(payType)
							&& "2".equals(order.getFreightType())) {// 支付方式为月结
						System.out.println("会员的支付方式");
						if (StringUtils.isEmptyWithTrim(params.get("vipNo"))) {
							render(JSON_TYPE, CommonResponse.respFailJson(
									"9001", "缺少参数：vipNo", params.get("reqNo")),
									response);
							return;
						}
						if (StringUtils.isEmptyWithTrim(params.get("vipPwd"))
								&& StringUtils.isEmptyWithTrim(params
										.get("monthSettleNo"))
										&& StringUtils.isEmptyWithTrim(params
												.get("monthSettleCard"))) {

							render(JSON_TYPE,
									CommonResponse.respFailJson("9001",
											"缺少参数：vipPwd", params.get("reqNo")),
											response);
							return;
						}
						Map<String, Object> map = userService
								.checkVipbInfo(params.get("vipNo"));
						if (map == null) {
							render(JSON_TYPE, CommonResponse.respFailJson(
									"9031", "不存在的会员号", params.get("reqNo")),
									response);
							return;
						}
						Map<String, Object> mapByPwd = userService
								.checkVipbInfoByPwd(params.get("vipNo"),
										Md5.md5Str(params.get("vipPwd")));
						if (mapByPwd == null) {
							render(JSON_TYPE, CommonResponse.respFailJson(
									"9031", "会员号密码错误", params.get("reqNo")),
									response);
							return;
						}
						if (!"1".equals(String.valueOf(mapByPwd.get("status")))) {
							render(JSON_TYPE, CommonResponse.respFailJson(
									"9031", "此会员号不是启用状态", params.get("reqNo")),
									response);
							return;
						}

						float v = Float.valueOf(((BigDecimal) mapByPwd
								.get("balance")).toString());

						int disType = (Integer) mapByPwd.get("disType");// 优惠类型

						int uid = (Integer) mapByPwd.get("uid");// 余额表ID

						float balance = Float.valueOf(((BigDecimal) mapByPwd
								.get("balance")).toString());// 余额

						System.out
						.println("HUIYUAN,扣除前balance余额======================"
								+ balance);
						Map<String, Object> disMap = userService
								.disType(disType);// 优惠类型数据

						int discount = (Integer) disMap.get("discount");// 折扣等级

						float freight1 = order.getFreight();// 运费
						float yingfu = freight1 + order.getVpay();// 应付
						//float shijikouchu = freight1 * discount / 100f;
						float shijikouchu = freight1 ;
						float reality = shijikouchu + order.getVpay(); // 实际扣除金额

						if (v < reality) {
							render(JSON_TYPE, CommonResponse.respFailJson(
									"9031", "会员号余额不足", params.get("reqNo")),
									response);
							return;
						}

						System.out
						.println("HUIYUAN,reality======================"
								+ reality);

						balance = balance - reality;

						System.out
						.println("HUIYUAN,扣除后balance余额======================"
								+ balance);

						userService.minusBalance(reality, DateUtils.formatDate(
								nowDate, "yyyy-MM-dd HH:mm:ss"), String
								.valueOf(uid));// 更新会员余额表

						Map<String, Object> insertVipHistory = new HashMap<String, Object>();
						insertVipHistory
						.put("dis_user_no", params.get("vipNo"));
						insertVipHistory.put("rmoney", reality);// 实扣
						insertVipHistory.put("omoney", yingfu);// 应扣
						insertVipHistory.put("af_balance", balance);
						insertVipHistory.put("status", "SUCCESS");
						insertVipHistory.put("order_no", order.getOrderNo());
						insertVipHistory.put("discount_text",
								disMap.get("discount_text"));
						insertVipHistory.put("lied", "N");
						insertVipHistory.put("courier_no", loginUserNo);
						insertVipHistory.put("operator",
								loginUser.getRealName());
						insertVipHistory.put("create_time",
								DateUtils.formatDate(new Date()));
						insertVipHistory.put("last_update_time",
								DateUtils.formatDate(new Date()));
						insertVipHistory.put("note", "快递员派件扣费");
						insertVipHistory.put("source", "COURIER_SEND");
						userService.vipExpense(insertVipHistory);// VIP消费记录插入
						order.setFpayStatus("SUCCESS");
						model.put("omoney", yingfu);// 应扣费
						model.put("rmoney", reality);// 实际扣费
						model.put("balance", balance);// 余额
						order.setDisRealityFreight(shijikouchu);
						order.setDisDiscount(String.valueOf(disMap
								.get("discount_text")));
						order.setPayAcount(reality);
						order.setSnapy(reality);
						order.setDisUserNo(params.get("vipNo"));
						if (order.getCod() == 1) {
							order.setPayAcount(reality + order.getGoodPrice());
							order.setSnapy(reality + order.getGoodPrice());
						}
					}
					System.out.println("********npay************" + npay);
					System.out.println("********lnpay***********" + lnpay);
					// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					if ("MONTH".equals(order.getPayType())&& "2".equals(order.getFreightType())) {// 支付方式为月结
						if (StringUtils.isEmptyWithTrim(params.get("monthSettleNo"))) {
							render(JSON_TYPE, CommonResponse.respFailJson("9001", "缺少参数：monthSettleNo",params.get("reqNo")), response);
							return;
						}
						Map<String, Object> map = muserService
								.selectMonthByFive(params.get("monthSettleNo"));
						if (map == null) {
							render(JSON_TYPE, CommonResponse.respFailJson(
									"9031", "不存在的月结号", params.get("reqNo")),
									response);
							return;
						}
						orderInfoService.updateMonthNo( (String)map.get("monthSettleNo"),
								order.getOrderNo());// 添加月结号
					}

					// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					if (payType.equals(PayType.POS.toString())) {
						ret = ValidateUtil.validateRequest(request,
								reqParams(new String[] { "merchantName",
										"merchantNo", "terminalNo",
								"encPinblock" }), false, null, false,
								null, dynamicDataSourceHolder);
						if (!"TRUE".equals(ret.get("isSuccess"))) {
							render(JSON_TYPE,
									CommonResponse.respFailJson(
											ret.get("respCode"),
											ret.get("respMsg"),
											params.get("reqNo")), response);
							return;
						}
						// encPinblock
						{
							if (StringUtils.isEmptyWithTrim(params
									.get("encTracks2"))
									&& StringUtils.isEmptyWithTrim(params
											.get("icData"))) {
								render(JSON_TYPE, CommonResponse.respFailJson(
										"9001", "缺少参数：encTracks2或icData",
										params.get("reqNo")), response);
								return;
							}

							String merchantName = params.get("merchantName"); // 保存用户打印
							String merchantNo = params.get("merchantNo");
							String terminalNo = params.get("terminalNo");
							String encTracks2 = params.get("encTracks2");
							String encTracks3 = params.get("encTracks3");
							String encPinblock = params.get("encPinblock");
							String icData = params.get("icData");
							String mac = params.get("mac");

							/*
							 * String localMac =
							 * SHA.SHA1Encode(params.get("reqTime"
							 * )+merchantNo+order.getPayAcount()) ; if
							 * (!mac.equals(localMac)) { log.info("mac:"+mac);
							 * log.info("localMac:"+localMac); render(JSON_TYPE,
							 * CommonResponse.respFailJson("9021", "非法参数，不允许交易",
							 * params.get("reqNo")),response); return ; }
							 */

							Map<String, String> posParams = new HashMap<String, String>();
							posParams.put("merchantNo", merchantNo);
							posParams.put("terminalNo", terminalNo);
							posParams
							.put("amount", String.format("%.2f", npay));
							posParams.put("encryptPin", encPinblock);
							posParams.put("track2", encTracks2);
							posParams.put("track3", encTracks3);
							posParams.put("icData", icData);

							log.info(posParams.toString());
							/*
							 * if (StringUtils.isEmptyWithTrim(icData)) { //磁条卡
							 * log.info("------------------------->磁条卡");
							 * posParams.put("icData", icData) ; }else { //ic卡
							 * log.info("------------------------->ic卡");
							 * posParams.put("encTracks2", encTracks2) ;
							 * posParams.put("encTracks3", encTracks3) ; }
							 */

							Map<String, Object> saleMap = PosUtil.sendAndRev(configInfo,
									posParams, BizType.SALE);
							if (saleMap != null
									&& "SUCCESS"
									.equals(saleMap.get("respCode"))) {
								// 交易成功
								if (order.getCod() == 1) {
									order.setPayStatus(PayStatus.FREIGHT_COD
											.getValue());
								} else {
									order.setPayStatus(PayStatus.FREIGTH
											.getValue());
								}
								model.put("amount", npay);
								model.put("currency", "CNY");
								model.put("issuer", saleMap.get("issuer"));
								model.put("cardNo", saleMap.get("cardNo"));
								model.put("opType", "消费");
								model.put("batchNo", saleMap.get("batchNo"));
								model.put("voucherNo", saleMap.get("voucherNo"));
								model.put("authNo", saleMap.get("authNo"));
								model.put("refNo", saleMap.get("refNo"));
								model.put("operatorNo",
										saleMap.get("operatorNo"));
								model.put("icData", saleMap.get("icData"));
								model.put("transTime", saleMap.get("transTime"));
								Map<String, Object> posInfo = new HashMap<String, Object>();
								posInfo.putAll(model);
								posInfo.put("orderNo", order.getOrderNo());
								posInfo.put("courierNo", loginUserNo);
								posInfo.put("merchantName", merchantName); //
								posInfo.put("merchantNo", merchantNo);
								posInfo.put("terminalNo", terminalNo);
								if (order.getCod() == 1) {
									posInfo.put("bizType", "COD");
								} else {
									posInfo.put("bizType", "FREIGHT");
								}
								posInfoService.save(posInfo);
							} else {
								String msg = saleMap.get("respCode").toString()
										+ saleMap.get("respMsg").toString();
								log.info("******************************");
								log.info(msg);
								render(JSON_TYPE, CommonResponse.respFailJson(
										"9022", "支付失败:" + msg,
										params.get("reqNo")), response);
								return;
							}

						}

					} else {
						if (order.getCod() == 1) {
							order.setPayStatus(PayStatus.FREIGHT_COD.getValue());
						} else {
							order.setPayStatus(PayStatus.FREIGTH.getValue());
						}
						log.info("***********cash_pay************");
					}
				} else {
					order.setSnapy(npay);
					if(!pay){
						order.setPayType("CASH");
					}		
				}

				String signature = "";
				List<String> mime = new ArrayList<String>();
				mime.add("image/jpeg");
				mime.add("image/pjpeg");
				mime.add("image/gif");
				mime.add("image/png");

				List<RequestFile> signImage = getFile(request, "sign",
						configInfo.getFile_root(), "/order/"
								+ DateUtils.formatDate(nowDate, "yyyyMMddHH"),
								mime);
				if (signImage.size() > 0) {
					signature = "/codfile/order/"
							+ DateUtils.formatDate(nowDate, "yyyyMMddHH") + "/"
							+ signImage.get(0).getFileName();
				}
				order.setSign(sign);
				order.setSignType(signType);
				order.setSignature(signature);
				order.setFpayStatus("SUCCESS");
				if (pay&&order.getCod() == 1) {
					System.out.println("包含代收货款");
					if (StringUtils.isEmptyWithTrim(params.get("cPayType"))) {
						render(JSON_TYPE, CommonResponse.respFailJson("9001",
								"请选择代收货款支付方式", params.get("reqNo")), response);
						return;
					}

					order.setCpayType(params.get("cPayType"));
					order.setCpayStatus("SUCCESS");
					System.out.println(order.getCpayType());
				}

				orderInfoService.sign(order);
				orderInfoService.updateQuestionStatus("6", order.getOrderNo()); // 更新问题件状态
				otherService.sinputInfo(order);// 签收录入

				PushMsg msg = new PushMsg();
				msg.setUserNo(order.getUserNo());
				msg.setUserType(1);
				msg.setMsgCode(MsgType.SIGN.getValue());
				msg.setMsgContent("您的快递在"
						+ DateUtils.formatDate(nowDate, "yyyy-MM-dd HH:mm:ss")
						+ "被签收！请确认是否为本人操作");
				msg.setMsgData(order.getOrderNo());
				msg.setCreateTime(DateUtils.formatDate(nowDate));
				msg.setExpireTime(DateUtils.formatDate(DateUtils.addDate(
						nowDate, 0, 6, 0)));
				long msgId = msgService.save(msg);
				PushUtil.pushById(configInfo,String.valueOf(msgId), 1, params.get("uid"));

				User userInfo = userService.getUserByNo(loginUserNo);// 查询快递员信息
				
				String uid = params.get("uid") ;
				if (uid.contains("_")) {
					uid = uid.substring(0, uid.indexOf("_"));
				}
				
				//【亿翔快递】尊敬的XX先生/女士，您的快递：xxxxxxxxxx已由xx先生/女士签收，请核实签收情况。感谢您使用亿翔快递，祝您生活愉快！

				if ("1".equals(order.getMessage())) {
					String isMessage = lgcService.getLgcConfig("MOBILE_CONFIG", "TAKE_SEND_MSG", "0") ;
					if ("1".equals(isMessage)) {   //。
						if(StringUtils.isNotEmptyWithTrim(order.getSendPhone())&&order.getSendPhone().length()==11){
							String content = "运单号："+order.getLgcOrderNo()+"，已签收完成，签收人："+order.getSignType()+"【快递王子】";
							String channel = "szkyt" ;
							if("yx".equals(uid)){
								content ="【亿翔快递】尊敬的"+StringUtil.nullString(order.getSendName())+"先生/女士 ，您的快递："+order.getLgcOrderNo()+"已由"+order.getSignType()+"先生/女士签收，请核实签收情况。感谢您使用亿翔快递，祝您生活愉快！";
							  channel = "yx" ;
							}
							RunnableUtils ui = new RunnableUtils();
							ui.MessagePushClass(channel,lgcNo, order.getSendPhone(),content,lgcService);	
						}	
					}
				}
				
				
				/**
				 * 微信下单信息tui'so
				 * 
				 * 
				 */
				if (!StringUtil.isEmptyWithTrim(order.getWxOpenid())) {
					/*System.out.println("微信推送线程开始");
					RunnableUtils run1 = new RunnableUtils();
					run1.pushWEIXIN(order, userInfo, 7);*/

					//微信推送
					WxxdConfig wxxdConfig = wxxdConfigService.getByDskey(params.get("uid").substring(0,params.get("uid").indexOf("_"))) ;
					String wxxd_url = wxxdConfig==null?"":wxxdConfig.getXdUrl() ;
					Map<String, String> wxMap = new HashMap<String, String>() ;
					wxMap.put("url",buildUrl(wxxd_url,lgcNo, params.get("uid"),order.getOrderNo(), order.getLgcOrderNo()));
					wxMap.put("dskey", params.get("uid").substring(0,params.get("uid").indexOf("_"))) ;
					wxMap.put("t","4");
					wxMap.put("touser",order.getWxOpenid());

					wxMap.put("first","尊敬的客户，您的快件已被签收");
					wxMap.put("lgcOrderNo",order.getLgcOrderNo());
					wxMap.put("stime",DateUtils.formatDate(nowDate));
					wxMap.put("remark","");
					WeiXinUtil.push(configInfo,wxMap,1);



					Map<String, String> wxMap2 = new HashMap<String, String>(wxMap) ;
					wxMap2.put("url",buildWXUrl(wxxd_url,lgcNo, params.get("uid"), order.getOrderNo(),"1"));
					wxMap2.put("remark","\n尊敬的客户，邀请您对本次服务进行评价");
					//wxMap2.put("first","\n尊敬的客户，邀请您对本次服务进行评价");
					//WeiXinUtil.push(wxMap2,1);
					
				}
				/***
				 * 一米鲜订单信息更新推送
				 * 
				 */

				if ("YMX".equals(order.getSource())) {

					System.out.println("一米鲜快递推送通知");
					String info = "您的快递已于"
							+ DateUtils.formatDate(nowDate,
									"yyyy-MM-dd HH:mm:ss") + "被签收";
					RunnableUtils ymx = new RunnableUtils();
					ymx.pushTMX(order.getOrderNote(), "2", params
							.get("orderNo"), userInfo.getRealName(), userInfo
							.getPhone(), info, DateUtils.formatDate(nowDate,
									"yyyy-MM-dd HH:mm:ss"), params.get("uid"));
				}
				/***
				 * 康美药业推送
				 */
				if ("康美".equals(order.getSource())) {
					RunnableUtils run1 = new RunnableUtils();
					run1.KangmeiPush(
							URL,
							company_num,
							userName,
							order.getLgcOrderNo(),
							"您的快递已于"
									+ DateUtils.formatDate(nowDate,
											"yyyy-MM-dd HH:mm:ss") + "被签收"
											+ "签收人姓名是:" + order.getSign() + "配送员姓名:"
											+ userInfo.getRealName() + "配送员电话:"
											+ userInfo.getPhone(), "1");
				}
				/**
				 * 物流扭转信息更新
				 * 
				 */
				Map<String, Object> firstTrackMap = orderTrackService
						.checkOrderTrack(params.get("orderNo"));
				String cur_no = "";
				String cur_type = "";
				int id = 0;
				if (firstTrackMap != null) {
					id = (Integer) firstTrackMap.get("id");
					cur_no = (String) firstTrackMap.get("cur_no");// 当前流转编号（快递员编号或分站编号）
					cur_type = (String) firstTrackMap.get("cur_type");// 当前编号类型，C为快递员编号，S为分站编号
					orderTrackService.updateIsLast(id);
				}

				String innerNo = loginUser.getInnerNo();
				if (StringUtil.isEmptyWithTrim(innerNo)) {
					innerNo = "";
				} else {
					innerNo = "(" + innerNo + ")。";
				}

				Map<String, Object> sMap = lgcService
						.getSubstationInfo(loginUser.getSubstationNo());
				OrderTrack track1 = new OrderTrack();
				track1.setOrderNo(params.get("orderNo"));
				track1.setContext(sMap.get("substation_name") + ",快递员:"
						+ loginUser.getRealName() + innerNo + " 派件已签收!"
						+ "签收人是:" + signType);
				track1.setOrderTime(DateUtils.formatDate(nowDate));
				track1.setPreNo(cur_no);
				track1.setPreType(cur_type);
				track1.setCompleted("Y");
				track1.setCurNo(loginUserNo);
				track1.setCurType("C");
				track1.setOrderStatus("SIGNED");
				track1.setParentId(id);
				track1.setIsLast("Y"); //
				track1.setOpname(loginUser.getRealName());
				orderTrackService.add(track1);

				render(JSON_TYPE,
						CommonResponse.respSuccessJson("状态更新成功", model,
								params.get("reqNo")), response);
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,
						CommonResponse.respFailJson(ret.get("respCode"),
								ret.get("respMsg"), params.get("reqNo")),
								response);
			}
		} catch (SocketTimeoutException exception) {
			exception.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9023", "连接超时",
							params.get("reqNo")), response);
		} catch (ConnectException e) {
			e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9024", "连接已断开",
							params.get("reqNo")), response);
		} catch (FileUnknowTypeException e) {
			// e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9012", "文件类型错误",
							params.get("reqNo")), response);
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9000", "服务器异常",
							params.get("reqNo")), response);
		}
	}

	// 获取pos交易记录
	@RequestMapping(value = "/pos_info")
	public void pos_info(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "orderNo" }), true,
					userSessionService, checkVersion, appVersionService,
					dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String loginNo = ret.get("userNo");
				List<PosInfoDto> posInfo = null;
				posInfo = posInfoService.getByOrderNoCourierNo(
						params.get("orderNo"), loginNo);
				model.put("posInfo", posInfo);
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

	// 上传面单
	@RequestMapping(value = "/orderUpload")
	public void orderUpload(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try { // 面单号，面单类型，1为收件面单 2为派件面单
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "lgcNo", "lgcType" }), true,
					userSessionService, checkVersion, appVersionService,
					dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String lgcNo = userService.getUserLgcNo(ret.get("userNo"));
				if ("1".equals(params.get("lgcType"))) {// 1为收件面单

					String lgcOrderNo = params.get("lgcNo");// 运单号
					OrderInfo orderInfo = orderInfoService.getByLgcOrderNo(
							lgcOrderNo, lgcNo);
					if (orderInfo == null) {
						render(JSON_TYPE,
								CommonResponse.respFailJson("9011", "运单"
										+ params.get("lgcNo") + "不存在",
										params.get("reqNo")), response);
						return;
					}
					List<String> mime = new ArrayList<String>();
					mime.add("image/jpeg");
					mime.add("image/pjpeg");
					mime.add("image/gif");
					mime.add("image/png");
					Date nowDate = new Date();// ConstantsLoader.getProperty("file_root")
					String nowDateTime = DateUtils.formatDate(nowDate,
							"yyyyMMddHH");
					List<RequestFile> files = getFile(lgcOrderNo, request,
							"uploadImage",
							configInfo.getFile_root(), "/order/"
									+ nowDateTime, mime);
					System.out
					.println(configInfo.getFile_root());
					if (files.size() <= 0) {
						render(JSON_TYPE, CommonResponse.respFailJson("9001",
								"缺少参数:uploadImage", params.get("reqNo")),
								response);
					} else {

						ResizeImage resizeImage = new ResizeImage();
						float resizeTimes = 1;
						if (Integer.valueOf(files.get(0).getFileSize()) > 1024 * 800) { // 大于5mb压缩
							resizeTimes = (1.0f * 1024 * 800)
									/ Integer.valueOf(files.get(0)
											.getFileSize());
							resizeImage.zoomImage(
									configInfo.getFile_root()
									+ files.get(0).getFilePath(),
									resizeTimes);
						}
						String loction = "/codfile/order/" + nowDateTime + "/"
								+ files.get(0).getFileName();
						System.out
						.println("takeloction=================================="
								+ loction);
						orderInfoService.uploadTakeOrder(lgcOrderNo, loction);
						render(JSON_TYPE,
								CommonResponse.respSuccessJson(
										ret.get("respCode"), "取件面单上传成功",
										params.get("reqNo")), response);
					}

				} else if ("2".equals(params.get("lgcType"))) {// 2为派件面单

					String lgcOrderNo = params.get("lgcNo");// 运单号
					OrderInfo orderInfo = orderInfoService.getByLgcOrderNo(
							lgcOrderNo, lgcNo);
					if (orderInfo == null) {
						render(JSON_TYPE,
								CommonResponse.respFailJson("9011", "运单"
										+ params.get("lgcNo") + "不存在",
										params.get("reqNo")), response);
						return;
					}
					List<String> mime = new ArrayList<String>();
					mime.add("image/jpeg");
					mime.add("image/pjpeg");
					mime.add("image/gif");
					mime.add("image/png");
					Date nowDate = new Date();
					String nowDateTime = DateUtils.formatDate(nowDate,
							"yyyyMMddHH");
					List<RequestFile> files = getFile(lgcOrderNo, request,
							"uploadImage",
							configInfo.getFile_root(), "/order/"
									+ nowDateTime, mime);
					if (files.size() <= 0) {
						render(JSON_TYPE, CommonResponse.respFailJson("9001",
								"缺少参数:uploadImage", params.get("reqNo")),
								response);
					} else {

						ResizeImage resizeImage = new ResizeImage();
						float resizeTimes = 1;
						if (Integer.valueOf(files.get(0).getFileSize()) > 1024 * 800) { // 大于5mb压缩
							resizeTimes = (1.0f * 1024 * 800)
									/ Integer.valueOf(files.get(0)
											.getFileSize());
							resizeImage.zoomImage(
									configInfo.getFile_root()
									+ files.get(0).getFilePath(),
									resizeTimes);
						}
						String loction = "/codfile/order/" + nowDateTime + "/"
								+ files.get(0).getFileName();
						System.out
						.println("sendloction=================================="
								+ loction);
						orderInfoService.uploadSendOrder(lgcOrderNo, loction);
						render(JSON_TYPE,
								CommonResponse.respSuccessJson(
										ret.get("respCode"), "派件面单上传成功",
										params.get("reqNo")), response);
					}
				} else {
					render(JSON_TYPE, CommonResponse.respFailJson("9011",
							"参数lgcType有误", params.get("reqNo")), response);
				}
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,
						CommonResponse.respFailJson(ret.get("respCode"),
								ret.get("respMsg"), params.get("reqNo")),
								response);
			}
		} catch (FileUnknowTypeException e) {
			// e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9012", "文件类型错误",
							params.get("reqNo")), response);
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9000", "服务器异常",
							params.get("reqNo")), response);
		}
	}

	// 查询是否为代派件
	@RequestMapping(value = "/queryInfo")
	public void queryInfo(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "lgcOrderNo" }), true,
					userSessionService, checkVersion, appVersionService,
					dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String loginNo = ret.get("userNo");// 快递员号
				String lgcOrderNo = params.get("lgcOrderNo");// 快递运单号
				String lgcNo = userService.getUserLgcNo(loginNo);// 快递公司号
				OrderDto orderInfo = orderInfoService
						.isExistLgcOrderNoBySendCourier(lgcOrderNo, lgcNo,
								loginNo);
				if (orderInfo == null) {
					render(JSON_TYPE, CommonResponse.respFailJson("9048",
							"订单状态有误", params.get("reqNo")), response);
					return;
				} else {
					OrderInfo orInfo = orderInfoService.getByOrderNo(
							orderInfo.getOrderNo(), lgcNo);
					// orderInfo.setNpay(orderInfoService.getNpay(orInfo,"2"));

					if (StringUtils.isNotEmptyWithTrim(orInfo
							.getTakeCourierNo())) {
						orderInfo.setTask(false);
					} else {
						orderInfo.setTask(true);
					}

					float vpay = orderInfo.getVpay();
					if (orderInfo.getGoodValuationRate() != null
							&& orderInfo.getGoodValuation() > 0 && vpay <= 0) {
						vpay = Math.round(orderInfo.getGoodValuation()
								* Float.valueOf(orderInfo
										.getGoodValuationRate()) * 100) / 100f;
					}
					orderInfo.setVpay(vpay);
					orderInfo.setTakeStatus(orInfo.getTakeMark());// 快件状态
					params.put("orderNo", orderInfo.getOrderNo());
					List<Map<String, Object>> imagesList = orderPicService
							.list(params);
					Map<String, Object> muser = null;
					if ("MONTH".equals(orderInfo.getPayType())) {
						if (StringUtils.isNotEmptyWithTrim(orderInfo
								.getMonthSettleNo())) {
							muser = muserService.selectMonthBy(orderInfo
									.getMonthSettleNo());
						}
					}
					model = new HashMap<String, Object>();
					model.put("orderInfo", orderInfo);
					model.put("muser", muser);
					model.put("imagesList", imagesList);
					render(JSON_TYPE,
							CommonResponse.respSuccessJson("", model,
									params.get("reqNo")), response);
				}
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

	/**
	 * 修改订单
	 * 
	 * @param params
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/changeOrder")
	public void changeOrder(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] {}), true, userSessionService,
					checkVersion, appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String userNo = ret.get("userNo");
				params.put("userNo", userNo);
				String order = params.get("orderNo");
				String lgcNo = userService.getUserLgcNo(ret.get("userNo"));
				OrderDto orderDto = orderInfoService.getOrderByOrderNo(order,	lgcNo);
				if (orderDto == null) {
					render(JSON_TYPE, CommonResponse.respFailJson("9013","订单号不存在", params.get("reqNo")), response);
					return;
				}
				if (orderDto.getStatus() != 2) {
					render(JSON_TYPE, CommonResponse.respFailJson("9048","订单状态有误,无法修改订单,请联系管理员", params.get("reqNo")),response);
					return;
				}
				Map<String, Object> map = warehouseService.getMapByOrder(orderDto.getLgcOrderNo(), "I", "Y");
				params.put("d", orderDto.getLgcOrderNo());
				if (map != null) {render(JSON_TYPE, CommonResponse.respFailJson("9042","当前订单已经入仓无法修改信息", params.get("reqNo")), response);
				return;
				}
				OrderInfo orderInfo = (OrderInfo) MapConverter.convertMap(	OrderInfo.class, params);


				params.put("payType", orderDto.getPayType());

				List<String> mime = new ArrayList<String>();
				mime.add("image/jpeg");
				mime.add("image/pjpeg");
				mime.add("image/gif");
				mime.add("image/png");
				List<RequestFile> files = getFile(	request,null,configInfo.getFile_root(),"/order/"
						+ DateUtils.formatDate(new Date(), "yyyyMMddHH"),mime);			
				List<OrderPic> imagesList = new ArrayList<OrderPic>();
				ResizeImage resizeImage = new ResizeImage();
				float resizeTimes = 1;
				for (RequestFile image : files) {
					OrderPic orderPic = new OrderPic();
					orderPic.setOrderNo(order);
					orderPic.setFileType(image.getFileType());
					orderPic.setFileName(image.getFileName());
					orderPic.setFileSize(image.getFileSize());
					orderPic.setFilePath(image.getFilePath());
					orderPic.setFileUri("/codfile/order/"	+ DateUtils.formatDate(new Date(), "yyyyMMddHH")
							+ "/" + image.getFileName());
					orderPic.setOrginalName(image.getOrginalName());
					if (Integer.valueOf(image.getFileSize()) > 1024 * 800) { // 大于5mb压缩
						resizeTimes = (1.0f * 1024 * 800)/ Integer.valueOf(image.getFileSize());
						resizeImage.zoomImage(configInfo.getFile_root()+ image.getFilePath(), resizeTimes);
					}
					imagesList.add(orderPic);
				}
				float freight = 0.00f;
				float vpay = 0.00f;
				float goodPrice = 0.00f;
				if (StringUtils.isNotEmptyWithTrim(params.get("freight"))) {
					freight = Float.valueOf(params.get("freight"));
				}

				if (StringUtils.isNotEmptyWithTrim(params.get("vpay"))) {
					vpay = Float.valueOf(params.get("vpay"));
				}
				if (StringUtils.isNotEmptyWithTrim(params.get("goodPrice"))) {
					goodPrice = Float.valueOf(params.get("goodPrice"));
				}
				float tnpay = (freight + vpay) * 100 / 100f;
				float payCount = (freight + vpay + goodPrice) * 100 / 100f;
				log.info("freight---------------------" + freight);
				log.info("vpay---------------------" + vpay);
				log.info("goodPrice---------------------" + goodPrice);
				log.info("tnpay---------------------" + tnpay);
				log.info("payCount---------------------" + payCount);
				if ("1".equals(params.get("freightType"))) {		
					params.put("tnpay", String.valueOf(tnpay));
				} else {
					params.put("tnpay", "0.00");
				}
				params.put("payCount", String.valueOf(payCount));
				params.put("vpay", String.valueOf(vpay));	
				orderInfoService.changeOrder(params);// 修改订单列表数据
				orderInfoService.changeOrderRegister(params);// 插入订单流水表信息

				orderPicService.save(imagesList); // 批量保存订单图片
				render(JSON_TYPE,CommonResponse.respSuccessJson("", "修改成功",params.get("reqNo")), response);
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,	CommonResponse.respFailJson(ret.get("respCode"),
						ret.get("respMsg"), params.get("reqNo")),response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,CommonResponse.respFailJson("9000", "服务器异常",
					params.get("reqNo")), response);
		}
	}

	/**
	 * 会员支付前调用查询
	 * 
	 * @param params
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/vipPay")
	public void vipPay(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "vipNo", "vipPwd", "freight" }),
					true, userSessionService, checkVersion, appVersionService,
					dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String userNo = ret.get("userNo");
				params.put("userNo", userNo);

				if (StringUtils.isEmptyWithTrim(params.get("vipNo"))) {
					render(JSON_TYPE, CommonResponse.respFailJson("9001",
							"缺少参数：vipNo", params.get("reqNo")), response);
					return;
				}
				if (StringUtils.isEmptyWithTrim(params.get("vipPwd"))
						&& StringUtils.isEmptyWithTrim(params	.get("monthSettleNo"))
						&& StringUtils.isEmptyWithTrim(params	.get("monthSettleCard"))) {

					render(JSON_TYPE, CommonResponse.respFailJson("9001","缺少参数：vipPwd", params.get("reqNo")), response);
					return;
				}
				Map<String, Object> map = userService.checkVipbInfo(params.get("vipNo"));
				if (map == null) {
					render(JSON_TYPE, CommonResponse.respFailJson("9031",	"不存在的会员号", params.get("reqNo")), response);
					return;
				}
				Map<String, Object> mapByPwd = userService.checkVipbInfoByPwd(params.get("vipNo"), Md5.md5Str(params.get("vipPwd")));
				if (mapByPwd == null) {
					render(JSON_TYPE, CommonResponse.respFailJson("9031","会员号密码错误", params.get("reqNo")), response);
					return;
				}
				if (!"1".equals(String.valueOf(mapByPwd.get("status")))) {
					render(JSON_TYPE, CommonResponse.respFailJson("9031","此会员号不是启用状态", params.get("reqNo")), response);
					return;
				}
				float freight = Math.round(Float.valueOf(params.get("freight")) * 100) / 100f;// 邮费

				float vpay = 0.00f;
				if (!StringUtils.isEmptyWithTrim(params.get("vpay"))) {
					vpay = Math.round(Float.valueOf(params.get("vpay")) * 100) / 100f; // 保价手续费
				}
				float v = Float.valueOf(((BigDecimal) mapByPwd.get("balance")).toString());// 余额

				int disType = (Integer) mapByPwd.get("disType");// 优惠类型

				int uid = (Integer) mapByPwd.get("uid");// 余额表ID

				float balance = Float.valueOf(((BigDecimal) mapByPwd	.get("balance")).toString());// 余额

				System.out.println("HUIYUAN,扣除前balance余额======================"+ balance);
				Map<String, Object> disMap = userService.disType(disType);// 优惠类型数据

				int discount = (Integer) disMap.get("discount");// 折扣等级

				//float onlyFreight = Float.valueOf(params.get("freight"))* discount / 100f;
				float onlyFreight = Float.valueOf(params.get("freight"));

				float reality = onlyFreight + vpay; // 实际扣除金额

				if (v < reality) {
					render(JSON_TYPE, CommonResponse.respFailJson("9031",
							"会员号余额不足", params.get("reqNo")), response);
					return;
				}

				System.out.println("HUIYUAN,reality======================"
						+ reality);

				balance = balance - reality;

				System.out.println("HUIYUAN,扣除后balance余额======================"
						+ balance);

				model = new HashMap<String, Object>();
				model.put("omoney", freight + vpay);
				model.put("rmoney", reality);
				model.put("balance", balance);

				render(JSON_TYPE,CommonResponse.respSuccessJson("", model,
						params.get("reqNo")), response);
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,CommonResponse.respFailJson(ret.get("respCode"),
						ret.get("respMsg"), params.get("reqNo")),
						response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,	CommonResponse.respFailJson("9000", "服务器异常",params.get("reqNo")), response);
		}
	}

	public static void main(String[] args) {
		//		 Scanner sc = new Scanner(System.in);
		//		  System.out.println("请输入菱形变长：");
		//		  int num =sc.nextInt();
		//		  for (int i=1;i<=num;i++)
		//		  {
		//		   for(int j=1;j<num-i+1;j++)
		//		   {
		//		    System.out.print(" ");
		//		   }
		//		   int count =2*i-1;
		//		   for(int k=0;k<count;k++)
		//		   {
		//		    if(0==k||count-1==k)
		//		     System.out.print("*");
		//		    else
		//		     System.out.print(" ");
		//		   } 
		//		   System.out.println("");
		//		  }
		//		  for(int i=1;i<num;i++)
		//		  {
		//		   for(int j=1;j<=i;j++)
		//		   {
		//		    System.out.print(" ");
		//		   }
		//		   int count = 2*(num-i)-1;
		//		   for(int k=0;k<count;k++)
		//		   {
		//		    if(k==0||k==count-1)
		//		     System.out.print("*");
		//		    else
		//		     System.out.print(" ");
		//		   }System.out.println("");
		//		  }
		//		String lengt = "1234567890A";
		//	String	signal =lengt.substring(lengt.length()-1, lengt.length());
		//		String numberNo = lengt.substring(0, lengt.length()-1);				
		//		System.out.println(signal);
		//		System.out.println(numberNo);
		//		Thread t  = new Thread();
	}


}