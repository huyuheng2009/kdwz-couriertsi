package com.yogapay.couriertsi.api;

import com.yogapay.couriertsi.SessionManager;
import com.yogapay.couriertsi.api2.CourierSession;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.yogapay.couriertsi.domain.User;
import com.yogapay.couriertsi.domain.UserSession;
import com.yogapay.couriertsi.domain.ValidateCode;
import com.yogapay.couriertsi.enums.ValidateCodeType;
import com.yogapay.couriertsi.exception.FileUnknowTypeException;
import com.yogapay.couriertsi.services.LgcService;
import com.yogapay.couriertsi.services.MsgService;
import com.yogapay.couriertsi.services.MsgTypeService;
import com.yogapay.couriertsi.services.OrderInfoService;
import com.yogapay.couriertsi.services.UserService;
import com.yogapay.couriertsi.services.ValidateCodeService;
import com.yogapay.couriertsi.services.WarehouseService;
import com.yogapay.couriertsi.utils.CommonResponse;
import com.yogapay.couriertsi.utils.ConstantsLoader;
import com.yogapay.couriertsi.utils.DateUtils;
import com.yogapay.couriertsi.utils.Md5;
import com.yogapay.couriertsi.utils.RequestFile;
import com.yogapay.couriertsi.utils.ResizeImage;
import com.yogapay.couriertsi.utils.StringUtil;
import com.yogapay.couriertsi.utils.StringUtils;
import com.yogapay.couriertsi.utils.ValidateUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 用户业务接口类
 * 
 * @author hhh
 * 
 */
@Controller
@RequestMapping(value = "/user", method = RequestMethod.POST)
@Scope("prototype")
public class UserApi extends BaseApi {

	@Resource
	private UserService userService;
	@Resource
	private ValidateCodeService validateCodeService;
	@Resource
	private MsgService msgService;
	@Resource
	private OrderInfoService orderInfoService;
	@Resource
	private LgcService lgcService;
	@Resource
	private MsgTypeService msgTypeService;
	@Resource
	private WarehouseService warehouseService;
	@Autowired
	private SessionManager sessionManager;
	
	
	// 登陆
	@RequestMapping(value = "/loginBegin")
	public void loginBegin(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "userName", "passWord",
					"androidId" }), false, null, checkVersion,	appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				User user = userService.getUserByPwd(params.get("userName"),Md5.md5Str(params.get("passWord")));

				Map<String, Object> userMap = warehouseService.getManagerGroup(params.get("userName"),
						Md5.md5Str(params.get("passWord")));// 查询管理员权限
				if (user == null && userMap == null) {
					render(JSON_TYPE, CommonResponse.respFailJson("9002","用户名或密码错误", params.get("reqNo")), response);
					return;
				}
				String role = "";
				if (user != null && userMap == null) {
					role = "COURIER";
				}
				if (user == null && userMap != null) {
					role = "MANAGER";
				}
				if (user != null && userMap != null) {
					role = "SYNTHESIZE";
				}

				model = new HashMap<String, Object>();
				model.put("role", role);
				render(JSON_TYPE,CommonResponse.respSuccessJson("", model,params.get("reqNo")), response);

			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,
						CommonResponse.respFailJson(ret.get("respCode"),
								ret.get("respMsg"), params.get("reqNo")),
								response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,CommonResponse.respFailJson("", "服务器异常",params.get("reqNo")), response);

		}
	}

	// 登陆
	@RequestMapping(value = "/userLogin")
	public void userLogin(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			model = new HashMap<String, Object>();
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "userName", "passWord",
					"androidId" }), false, null, checkVersion,
					appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				User user = userService.getUserByPwd(params.get("userName"),	Md5.md5Str(params.get("passWord")));

				if (user == null) {
					render(JSON_TYPE, CommonResponse.respFailJson("9002","用户名或密码错误", params.get("reqNo")), response);
				} else if("0".equals(user.getStatus())){
					render(JSON_TYPE, CommonResponse.respFailJson("9002","当前用户登录权限尚未启用,请联系客服开启登录权限！", params.get("reqNo")), response);	
					return;
				}else{
					
					log.info("login...");
					String session = UUID.randomUUID().toString().replace("-", "");
					UserSession userSession = new UserSession();
					userSession.setUserNo(user.getCourierNo());
					userSession.setSessionNo(session);
					userSession.setAppVersion(params.get("appVersion"));
					userSession.setAndroidId(params.get("androidId"));
					Date nowDate = new Date();
					userSession.setCreateTime(nowDate);
					userSession.setLastUpdateTime(nowDate);
					userSession.setExpiryTime(DateUtils.addDate(nowDate, 30,12, 0)); // 有效期12小时
					userSession.setIp(getClientIP(request));
					userSessionService.deleteByUserNo(user.getCourierNo());
					model.put("respMsg", "登陆成功");
					Map<String, Object> userInfo = userService.getUserInfo(user.getCourierNo());
					if (userInfo == null) {
						render(JSON_TYPE, CommonResponse.respFailJson("9046",
								"当前用户不存在", params.get("reqNo")), response);
						return;
					}
					if (!ValidateUtil.checkSubstation(userSessionService, ret, user.getCourierNo())) {
						render(JSON_TYPE,
							   CommonResponse.respFailJson(ret.get("respCode"), ret.get("respMsg"), params.get("reqNo")), response);
						return;
					}
					String lgcNo = userService.getUserLgcNo(user.getCourierNo());
					params.put("lgcNo", lgcNo);
					sessionManager.setCurrent(request, session, new CourierSession(user, lgcNo));
					// 签到数据表更新
					Date newDate = new Date();
					params.put("userNo", user.getCourierNo());
					params.put("createTime",DateUtils.formatDate(newDate, "yyyy-MM-dd"));
					params.put("beginTime",	DateUtils.formatDate(newDate, "yyyy-MM-dd"));
					String courierNewDate =DateUtils.formatDate(newDate,"yyyy-MM-dd");
					BigDecimal sumCashMoney = new BigDecimal("0.00");
					if (StringUtils.isNotEmptyWithTrim(courierNewDate)) {
						params.put("beginTime", DateUtils.formatDate(courierNewDate, "yyyy-MM-dd"));
						params.put("endTime", DateUtils.formatDate(courierNewDate, "yyyy-MM-dd"));
						BigDecimal tnpayCash = orderInfoService.detailTnpaySumCash(params);// 收件收总金额
						BigDecimal snpayCash = orderInfoService.detailSnpaySumCash(params);
						BigDecimal goodPrice = orderInfoService	.detailSnpaySumCodPay(params);
						sumCashMoney = sumCashMoney.add(tnpayCash).add(snpayCash).add(goodPrice);
					}
					List<Map<String, Object>> signList = userSessionService.signDetailTimes(user.getCourierNo());
					String sendTimes = orderInfoService.sendTimes(params);
					String takeTimes = orderInfoService.takeTimes(params);
					userInfo.put("sendTimes", sendTimes);
					userInfo.put("takeTimes", takeTimes);
					userInfo.put("signTimes", signList.size());// 本月签到次数
					userInfo.put("sumMoney", sumCashMoney.toString());// 总金额
					params.put("userNo", user.getCourierNo());
					String beginTime = DateUtils.formatDate(DateUtils.addDate(nowDate, -365, 0, 0));
					String endTime = DateUtils.formatDate(nowDate);
					params.put("beginTime", beginTime);
					params.put("endTime", endTime);

					/**
					 * 返回签到状态
					 * */
					// 签到限制
					List<Map<String, Object>> sign2List = userSessionService.signInfoAll(params);// 查询今日签到次数
					List<Map<String, Object>> signOutList = userSessionService.signOutInfoAll(params);// 查询今日签退次数
					int signTimes = sign2List.size();
					int signOutTimes = signOutList.size();
					System.out.println("signTimes=============" + signTimes			+ "//////////signOutTimes============="
							+ signOutTimes);
					if (signTimes < 1 || signTimes == signOutTimes) {		
						userInfo.put("signStatus", "N");
					} else {
						userInfo.put("signStatus", "Y");
					}

					String exchange = StringUtil.nullString(userInfo.get("exchange")) ;
					String queueName = StringUtil.nullString(userInfo.get("queueName")) ;
					userInfo.put("exchange", params.get("uid")+exchange) ;
					userInfo.put("queueName", params.get("uid")+queueName) ;
					
					Map<String, String> params1 = new HashMap<String, String>();
					params1.put("userNo", user.getCourierNo());
					String msgCode1 = msgTypeService.getChildrenString(500);
					String msgCode2 = msgTypeService.getChildrenString(501);
					String msgCode = msgCode1 + "," + msgCode2;

					params1.put("msgCode", msgCode);
					int allUreadCount = msgService.ureadCount(params1);
					params.put("status", "1");
					int tUreadCount = orderInfoService.unreadCount(params);
					userSessionService.saveCourier(userSession);
					List<Map<String, Object>> payType = appVersionService
							.getPayType(lgcNo);// 返支付方式
					Map<String, Object> valuationRate = lgcService.getLgcVrate();// 返回保价费率
					Map<String, Object> codRate = lgcService.getLgcCodrate();// 返回代收货款费率
					List<Map<String, Object>> list = userService.getItemType();// 返回物品类型
					List<Map<String, Object>> timeList = userService.getAgingType();// 返回时效类型
					List<Map<String, Object>> listRequire = userService.getRequired();// 获取必输项列表
					if ("1".equals(params.get("allRequire"))) {
						 listRequire = userService.getAllRequired() ;
					}
					Map<String, Object> mapRequire = new HashMap<String, Object>();
					for (Map<String, Object> mp : listRequire) {
						mapRequire.put((String) mp.get("name"),mp.get("required"));
					}
					Map<String, Object> roomMap = userService.lgcMode();
					String isRoom = (String) roomMap.get("orderRoom");
					String isMessage = lgcService.getLgcConfig("MOBILE_CONFIG", "TAKE_SEND_MSG", "0") ;
					model = new HashMap<String, Object>();
					model.put("isRoom", isRoom);
					model.put("isMessage", "1".equals(isMessage));
					model.put("list", list);
					model.put("timeList", timeList);
					model.put("mapRequire", mapRequire);
					model.put("valuationRate", valuationRate);
					model.put("codRate", codRate);
					model.put("userInfo", userInfo);
					model.put("payType", payType);
					model.put("allUreadCount", allUreadCount);
					model.put("tUreadCount", tUreadCount);
					String defaultItemType = orderInfoService.defaultItemStatus();
					model.put("defaultItemType", defaultItemType);
					response.addHeader("sessionNo", userSession.getSessionNo());
					render(JSON_TYPE,CommonResponse.respSuccessJson("", model,params.get("reqNo")), response);
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
					CommonResponse.respFailJson("", "服务器异常",
							params.get("reqNo")), response);

		}
	}

	// 登陆
	@RequestMapping(value = "/managerLogin")
	public void mangerLogin(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			model = new HashMap<String, Object>();
			Map<String, String> ret = ValidateUtil.managerValidateRequest(
					request, reqParams(new String[] { "userName", "passWord",
					"androidId" }), false, null, checkVersion,
					appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				Map<String, Object> userMap = warehouseService.getManagerGroup(
						params.get("userName"),
						Md5.md5Str(params.get("passWord")));// 查询管理员权限

				if (userMap == null) {
					render(JSON_TYPE, CommonResponse.respFailJson("9002",
							"用户名或密码错误", params.get("reqNo")), response);
				}else if("0".equals(userMap.get("status"))){
					render(JSON_TYPE, CommonResponse.respFailJson("9002","当前用户登录权限尚未启用,请联系客服开启登录权限！", params.get("reqNo")), response);	
					return;
				} else {
					if (!ValidateUtil.checkManager(userSessionService, ret, (String) userMap.get("user_name"))) {
						render(JSON_TYPE,
							   CommonResponse.respFailJson(ret.get("respCode"), ret.get("respMsg"), params.get("reqNo")), response);
						return;
					}
						
					log.info("login...");
					String session = UUID.randomUUID().toString()
							.replace("-", "");
					UserSession userSession = new UserSession();
					userSession.setUserNo((String) userMap.get("user_name"));
					userSession.setSessionNo(session);
					userSession.setAppVersion(params.get("appVersion"));
					userSession.setAndroidId(params.get("androidId"));
					Date nowDate = new Date();
					userSession.setCreateTime(nowDate);
					userSession.setLastUpdateTime(nowDate);
					userSession.setExpiryTime(DateUtils.addDate(nowDate, 30, 12,
							0)); // 有效期12小时
					userSession.setIp(getClientIP(request));
					userSessionService.deleteByManagerNo((String) userMap
							.get("user_name"));

					userSessionService.saveManager(userSession);
					model = new HashMap<String, Object>();
					model.put("respMsg", "登陆成功");
					response.addHeader("sessionNo", userSession.getSessionNo());
					render(JSON_TYPE,
							CommonResponse.respSuccessJson("", "登录成功",
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
					CommonResponse.respFailJson("", "服务器异常",
							params.get("reqNo")), response);

		}
	}

	// 注册
	@RequestMapping(value = "/regist")
	public void regist(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "userName", "passWord",
							"realName", "validateCode", "idCard", "lgcNo" }),
							false, null, checkVersion, appVersionService,
							dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				User user = userService.getUserByUserName(params
						.get("userName"));
				if (user != null) {
					render(JSON_TYPE, CommonResponse.respFailJson("9005",
							"用户名已经存在", params.get("reqNo")), response);
				} else {
					ValidateCode validateCode = validateCodeService
							.getLastCode(params.get("userName"),
									ValidateCodeType.REGIST.getValue());
					if (validateCode == null) {
						render(JSON_TYPE, CommonResponse.respFailJson("9006 ",
								"验证码未发送", params.get("reqNo")), response);
					} else {
						if (!validateCode.getCode().equals(
								params.get("validateCode"))) {
							render(JSON_TYPE, CommonResponse.respFailJson(
									"9007 ", "验证码错误", params.get("reqNo")),
									response);
						} else {
							if (DateUtils.bofore(validateCode.getExpireTime())) {
								List<String> mime = new ArrayList<String>();
								mime.add("image/jpeg");
								mime.add("image/pjpeg");
								mime.add("image/gif");
								mime.add("image/png");

								if (lgcService.getLgc(params.get("lgcNo")) == null) {
									render(JSON_TYPE,
											CommonResponse.respFailJson("9020",
													"不存在此快递公司",
													params.get("reqNo")),
													response);
									return;
								}
								String substationNo = lgcService
										.getDefaultSubstation(params
												.get("lgcNo"));
								User newUser = new User();
								String userNo = sequenceService.getNextVal("courier_no");
								newUser.setCourierNo(userNo);
								newUser.setUserName(params.get("userName"));
								newUser.setPassWord(Md5.md5Str(params.get("passWord")));
								newUser.setRealName(params.get("realName"));
								newUser.setPhone(params.get("userName"));
								newUser.setIdCard(params.get("idCard"));
								newUser.setSubstationNo(substationNo);

								// newUser.setSubstationNo(params.get("substationNo"));
								Date nowDate = new Date();
								List<RequestFile> headImage = getFile(request,	"headImage",configInfo.getFile_root(),"/user/"+ DateUtils.formatDate(nowDate,
														"yyyyMMddHH"), mime);
								if (headImage.size() > 0) {
									ResizeImage resizeImage = new ResizeImage();
									float resizeTimes = 1;
									if (Integer.valueOf(headImage.get(0)
											.getFileSize()) > 1024 * 1024 * 5) { // 大于5mb压缩
										resizeTimes = (1024 * 1024 * 5)
												/ Integer.valueOf(headImage
														.get(0).getFileSize());
										resizeImage
										.zoomImage(configInfo.getFile_root()
												+ headImage	
												
												.get(0)
												.getFilePath(),
												resizeTimes);
									}
									newUser.setHeadImage("/codfile/user/"
											+ DateUtils.formatDate(nowDate,
													"yyyyMMddHH") + "/"
													+ headImage.get(0).getFileName());
								} else {
									// newUser.setHeadImage("/images/users/default.png");
									newUser.setHeadImage("");
								}
								newUser.setRegistTime(new Date());
								newUser.setQueueName("K" + userNo);
								userService.save(newUser);
								render(JSON_TYPE,
										CommonResponse.respSuccessJson("",
												"注册成功", params.get("reqNo")),
												response);
							} else {
								render(JSON_TYPE, CommonResponse.respFailJson(
										"9008 ", "验证码过期", params.get("reqNo")),
										response);
							}

						}
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

	// 退出登陆
	@RequestMapping(value = "/layout")
	public void layout(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(null), false, null, false, null,
					dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				if (!StringUtils
						.isEmptyWithTrim(request.getHeader("sessionNO"))) {
					// userSessionService.deleteBySessionNO(request.getHeader("sessionNO"));
					userSessionService.expirySession(request
							.getHeader("sessionNO"));
					render(JSON_TYPE,
							CommonResponse.respSuccessJson("", "退出成功",
									params.get("reqNo")), response);
				} else {
					render(JSON_TYPE, CommonResponse.respFailJson("9001",
							"缺少参数:sessionNO", params.get("reqNo")), response);
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

	// 仓管员退出登陆
	@RequestMapping(value = "/managerLayout")
	public void managerLayout(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(null), false, null, false, null,
					dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				if (!StringUtils.isEmptyWithTrim(request.getHeader("sessionNO"))) {
					userSessionService.managerExpirySession(request	.getHeader("sessionNO"));
					render(JSON_TYPE,CommonResponse.respSuccessJson("", "退出成功",
									params.get("reqNo")), response);
				} else {
					render(JSON_TYPE, CommonResponse.respFailJson("9001",
							"缺少参数:sessionNO", params.get("reqNo")), response);
				}
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,CommonResponse.respFailJson(ret.get("respCode"),
								ret.get("respMsg"), params.get("reqNo")),
								response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,CommonResponse.respFailJson("9000", "服务器异常",
							params.get("reqNo")), response);
		}
	}

	// 修改用户头像
	@RequestMapping(value = "/head_image")
	public void headImage(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "" }), true, userSessionService,
					checkVersion, appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				User user = userService.getUserByUserNo(ret.get("userNo"));
				List<String> mime = new ArrayList<String>();
				mime.add("image/jpeg");
				mime.add("image/pjpeg");
				mime.add("image/gif");
				mime.add("image/png");
				Date nowDate = new Date();
				List<RequestFile> files = getFile(request, "headImage",
						configInfo.getFile_root(), "/user/"
								+ DateUtils.formatDate(nowDate, "yyyyMMddHH"),
								mime);
				if (files.size() <= 0) {
					render(JSON_TYPE, CommonResponse.respFailJson("9001",
							"缺少参数:headImage", params.get("reqNo")), response);
				} else {
					if (!StringUtils.isEmptyWithTrim(user.getHeadImage())
							&& !"/codfile/user/default.png".equals(user
									.getHeadImage())) {
						String path = user.getHeadImage().substring(8);
						// FileUtil.deleteFile(
						// ConstantsLoader.getProperty("file_root")+path) ;
					}
					ResizeImage resizeImage = new ResizeImage();
					float resizeTimes = 1;
					if (Integer.valueOf(files.get(0).getFileSize()) > 1024 * 1024 * 5) { // 大于5mb压缩
						resizeTimes = (1024 * 1024 * 5)
								/ Integer.valueOf(files.get(0).getFileSize());
						resizeImage.zoomImage(
								configInfo.getFile_root()
								+ files.get(0).getFilePath(),
								resizeTimes);
					}
					user.setHeadImage("/codfile/user/"
							+ DateUtils.formatDate(nowDate, "yyyyMMddHH") + "/"
							+ files.get(0).getFileName());
					userService.update(user);
					model.put("headImage", user.getHeadImage());
					render(JSON_TYPE,	CommonResponse.respSuccessJson("", model,
									params.get("reqNo")), response);
				}
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,	CommonResponse.respFailJson(ret.get("respCode"),
								ret.get("respMsg"), params.get("reqNo")),
								response);
			}
		} catch (FileUnknowTypeException e) {
			// e.printStackTrace();
			render(JSON_TYPE,	CommonResponse.respFailJson("9012", "文件类型错误",
							params.get("reqNo")), response);
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,	CommonResponse.respFailJson("9000", "服务器异常",
							params.get("reqNo")), response);
		}
	}

	// 修改密码
	@RequestMapping(value = "/cpwd")
	public void cpwd(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "oldPassWord", "nwPassWord" }),
					true, userSessionService, checkVersion, appVersionService,
					dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String loginNo = ret.get("userNo");
				User user = userService.getUserByNoPwd(loginNo,Md5.md5Str(params.get("oldPassWord")));
				if (user != null) {
					userService.cpwd(user.getCourierNo(),	Md5.md5Str(params.get("nwPassWord")));
					render(JSON_TYPE,		CommonResponse.respSuccessJson("", "修改成功",	params.get("reqNo")), response);
				} else {
					render(JSON_TYPE,
							CommonResponse.respFailJson("9002", "密码错误",params.get("reqNo")), response);
				}
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,	CommonResponse.respFailJson(ret.get("respCode"),	ret.get("respMsg"), params.get("reqNo")),
						response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9000", "服务器异常",
							params.get("reqNo")), response);
		}
	}
	
	
	// 仓管员修改密码
	@RequestMapping(value = "/mcpwd")
	public void mcpwd(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.managerValidateRequest(
					request, reqParams(null), true, userSessionService,
					checkVersion, appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String loginNo = ret.get("userNo");
				Map<String, Object> userMap = warehouseService.getManagerGroup(loginNo,
						Md5.md5Str(params.get("oldPassWord")));// 查询管理员权限
				if (userMap != null) {
					warehouseService.cpwd(loginNo,	Md5.md5Str(params.get("nwPassWord")));
					render(JSON_TYPE,		CommonResponse.respSuccessJson("", "修改成功",	params.get("reqNo")), response);
				} else {
					render(JSON_TYPE,
							CommonResponse.respFailJson("9002", "密码错误",params.get("reqNo")), response);
				}
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,	CommonResponse.respFailJson(ret.get("respCode"),	ret.get("respMsg"), params.get("reqNo")),
						response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9000", "服务器异常",
							params.get("reqNo")), response);
		}
	}

	// 忘记秘密
	@RequestMapping(value = "/fpwd")
	public void fpwd(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "phone", "nwPassWord" }), false,
					null, checkVersion, appVersionService,
					dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				User user = userService.getUserByUserName(params.get("phone"));
				userService.cpwd(user.getCourierNo(),
						Md5.md5Str(params.get("nwPassWord")));
				render(JSON_TYPE,	CommonResponse.respSuccessJson("", "修改成功",
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

	// 忘记秘密
	@RequestMapping(value = "/validate")
	public void validate(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "phone", "validateCode" }), false,
					null, checkVersion, appVersionService,
					dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				User user = userService.getUserByUserName(params.get("phone"));
				if (user == null) {
					render(JSON_TYPE, CommonResponse.respFailJson("9019",
							"不存在此用户", params.get("reqNo")), response);
				} else {
					ValidateCode validateCode = validateCodeService
							.getLastCode(params.get("phone"),
									ValidateCodeType.CHANGE_PASSWORD.getValue());
					if (validateCode == null) {
						render(JSON_TYPE, CommonResponse.respFailJson("9006 ",
								"验证码未发送", params.get("reqNo")), response);
					} else {
						if (!validateCode.getCode().equals(
								params.get("validateCode"))) {render(JSON_TYPE, CommonResponse.respFailJson(
									"9007 ", "验证码错误", params.get("reqNo")),	response);
						} else {
							if (DateUtils.bofore(validateCode.getExpireTime())) {
								render(JSON_TYPE,	CommonResponse.respSuccessJson("","验证成功", params.get("reqNo")),
												response);
							} else {
								render(JSON_TYPE, CommonResponse.respFailJson(
										"9008 ", "验证码过期", params.get("reqNo")),	response);
							}
						}
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

	// 修改用户信息
	@RequestMapping(value = "/cinfo")
	public void cinfo(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "realName", "idCard" }), true,
					userSessionService, checkVersion, appVersionService,
					dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				User user = new User();
				user.setCourierNo(ret.get("userNo"));
				user.setRealName(params.get("realName"));
				user.setIdCard(params.get("idCard"));
				userService.updateInfo(user);
				render(JSON_TYPE,
						CommonResponse.respSuccessJson("", "修改成功",
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

	// 获取用户详情
	@RequestMapping(value = "/info")
	public void info(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(null), true, userSessionService, checkVersion,
					appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				Map<String, Object> userInfo = userService.getUserInfo(ret
						.get("userNo"));
				params.put("userNo", ret.get("userNo"));
				List<Map<String, Object>> signList = userSessionService
						.signDetailTimes(ret.get("userNo"));

		
				String takeCount = orderInfoService.takeTimes(params);
				String sendCount = orderInfoService.sendTimes(params);
				String lgcNo = userService.getUserLgcNo(ret.get("userNo"));
				params.put("lgcNo", lgcNo);

				String courierNewDate =DateUtils.formatDate(new Date(),"yyyy-MM-dd");
						//orderInfoService.getTime(ret.get("userNo"));
				BigDecimal sumCashMoney = new BigDecimal("0.00");
				if (StringUtils.isNotEmptyWithTrim(courierNewDate)) {
					params.put("beginTime",	DateUtils.formatDate(courierNewDate, "yyyy-MM-dd"));
					params.put("endTime",DateUtils.formatDate(courierNewDate, "yyyy-MM-dd"));
					BigDecimal tnpayCash = orderInfoService.detailTnpaySumCash(params);// 收件收总金额
					BigDecimal snpayCash = orderInfoService.detailSnpaySumCash(params);
					BigDecimal goodPrice = orderInfoService.detailSnpaySumCodPay(params);
					sumCashMoney = sumCashMoney.add(tnpayCash).add(snpayCash)
							.add(goodPrice);
				}
				userInfo.put("signTimes", signList.size());// 本月签到次数
				userInfo.put("takeCount", takeCount);
				userInfo.put("sendCount", sendCount);
				userInfo.put("sumMoney", sumCashMoney.toString());
				model.put("userInfo", userInfo);
				render(JSON_TYPE,CommonResponse.respSuccessJson("", model,params.get("reqNo")), response);
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,CommonResponse.respFailJson(ret.get("respCode"),ret.get("respMsg"), params.get("reqNo")),	response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9000", "服务器异常",
							params.get("reqNo")), response);
		}
	}

	// 获取仓管员用户详情
	@RequestMapping(value = "/managerInfo")
	public void mangagerInfo(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.managerValidateRequest(
					request, reqParams(null), true, userSessionService,
					checkVersion, appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String managerUser = ret.get("userNo");
				Map<String, Object> userInfo = warehouseService
						.getManagerInfoByNo(managerUser);

				model = new HashMap<String, Object>();
				model.put("userInfo", userInfo);
				render(JSON_TYPE,CommonResponse.respSuccessJson("", model,params.get("reqNo")), response);
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,CommonResponse.respFailJson(ret.get("respCode"),
								ret.get("respMsg"), params.get("reqNo")),	response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9000", "服务器异常",
							params.get("reqNo")), response);
		}
	}

	//
	/*
	 * @RequestMapping(value = "/send") public void send(@RequestParam
	 * Map<String, String> params, HttpServletRequest request,
	 * HttpServletResponse response) { try { Map<String, String> ret =
	 * ValidateUtil.validateRequest(request,reqParams(new String[] { "orderNo"
	 * }), true,
	 * userSessionService,checkVersion,appVersionService,dynamicDataSourceHolder
	 * ); if ("TRUE".equals(ret.get("isSuccess"))) {
	 * 
	 * 
	 * render(JSON_TYPE,CommonResponse.respSuccessJson("","揽件成功",params.get("reqNo"
	 * )), response); } else { log.info("validate false!!!!"); render(JSON_TYPE,
	 * CommonResponse.respFailJson(ret.get("respCode"), ret.get("respMsg"),
	 * params.get("reqNo")),response); } } catch (Exception e) {
	 * e.printStackTrace(); render(JSON_TYPE,
	 * CommonResponse.respFailJson("9000","服务器异常", params.get("reqNo")),
	 * response); } }
	 */

	/*
	 * @RequestMapping(value = "/cphone") public void cphone(@RequestParam
	 * Map<String, String> params, HttpServletRequest request,
	 * HttpServletResponse response) { try { Map<String, String> ret =
	 * ValidateUtil.validateRequest(request,reqParams(new String[] {
	 * "passWord","newPhone","validateCode" }), true, userSessionService); if
	 * ("TRUE".equals(ret.get("isSuccess"))) { User user =
	 * userService.getUserByNoPwd(ret.get("userNo"),
	 * Md5.md5Str(params.get("passWord"))) ; if (user==null) { render(JSON_TYPE,
	 * CommonResponse.respFailJson( "9002","密码错误",
	 * params.get("reqNo")),response); }else { if
	 * (userService.getUserByUserName(params.get("newPhone"))!=null) {
	 * render(JSON_TYPE, CommonResponse.respFailJson( "9014","手机已被注册",
	 * params.get("reqNo")),response); }else { ValidateCode validateCode =
	 * validateCodeService
	 * .getLastCode(params.get("newPhone"),ValidateCodeType.CHANGE_PHONE
	 * .getValue()); if (validateCode == null) { render(JSON_TYPE,
	 * CommonResponse.respFailJson("9006 ", "验证码未发送", params.get("reqNo")),
	 * response); } else { if
	 * (!validateCode.getCode().equals(params.get("validateCode"))) {
	 * render(JSON_TYPE, CommonResponse.respFailJson("9007 ", "验证码错误",
	 * params.get("reqNo")),response); } else { if
	 * (DateUtils.bofore(validateCode.getExpireTime())) {
	 * user.setPhone(params.get("newPhone"));
	 * userService.cphone(user.getCourierNo(), user.getPhone()) ;
	 * render(JSON_TYPE
	 * ,CommonResponse.respSuccessJson("修改成功",params.get("reqNo")), response); }
	 * else { render(JSON_TYPE, CommonResponse.respFailJson( "9008 ", "验证码过期",
	 * params.get("reqNo")),response); }
	 * 
	 * } } } } } else { log.info("validate false!!!!"); render(JSON_TYPE,
	 * CommonResponse.respFailJson(ret.get("respCode"), ret.get("respMsg"),
	 * params.get("reqNo")),response); } } catch (Exception e) {
	 * e.printStackTrace(); render(JSON_TYPE,
	 * CommonResponse.respFailJson("9000","服务器异常", params.get("reqNo")),
	 * response); } }
	 */

	// 签到
	@RequestMapping(value = "/sign")
	public void signRegist(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {// "userName", "passWord","realName",
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] {}), true, userSessionService,
					checkVersion, appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				Date newDate = new Date();
				params.put("userNo", ret.get("userNo"));
				params.put("createTime",
						DateUtils.formatDate(newDate, "yyyy-MM-dd"));
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("user_no", ret.get("userNo"));
				map.put("app_version", params.get("appVersion"));
				map.put("android_id", params.get("androidId"));
				map.put("create_time",DateUtils.formatDate(newDate, "yyyy-MM-dd HH:mm:ss"));
				map.put("expiry_time", DateUtils.formatDate(	DateUtils.addDate(newDate, 0, 0, 15),
						"yyyy-MM-dd HH:mm:ss"));			
				map.put("ip", getClientIP(request));
				map.put("sign_time", DateUtils.formatDate(newDate));
				userSessionService.sign(map); // 签到

				render(JSON_TYPE, CommonResponse.respSuccessJson(
						ret.get("respCode"), "成功签到", params.get("reqNo")),
						response);

			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,
						CommonResponse.respFailJson(ret.get("respCode"),
								ret.get("respMsg"), params.get("reqNo")),
								response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,CommonResponse.respFailJson("9000", "服务器异常",
							params.get("reqNo")), response);
		}
	}

	// 签退
	@RequestMapping(value = "/signOut")
	public void signOut(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {// "userName", "passWord",
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] {}), true, userSessionService,
					checkVersion, appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				Date newDate = new Date();
				params.put("userNo", ret.get("userNo"));
				params.put("createTime",DateUtils.formatDate(newDate, "yyyy-MM-dd"));
				Map<String, Object> signMap = userSessionService
						.signInfo(params); // 查询签到时间
				if (signMap == null) {
					render(JSON_TYPE,	CommonResponse.respFailJson("", "尚未签到",
									params.get("reqNo")), response);
					return;
				}
				if (newDate.after((Date) signMap.get("expiry_time"))) {
					Date signTime = (Date) signMap.get("sign_time");
					long signTimeLong = signTime.getTime();
					long nowTimeLong = newDate.getTime();
					long DVLong = nowTimeLong - signTimeLong; // 计算出工作毫秒
					signMap.put("sign_work_time", String.valueOf(DVLong));
					signMap.put("create_date", DateUtils.formatDate(newDate,"yyyy-MM-dd HH:mm:ss"));
					signMap.put("sign_out_time", DateUtils.formatDate(newDate,"yyyy-MM-dd HH:mm:ss"));
					userSessionService.signOut(signMap);
					render(JSON_TYPE, CommonResponse.respSuccessJson(ret.get("respCode"), "成功签退", params.get("reqNo")),
							response);
				} else {
					render(JSON_TYPE, CommonResponse.respFailJson("",
							"签到15分钟内无法签退", params.get("reqNo")), response);
					return;
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
					CommonResponse.respFailJson("9000", "服务器异常",params.get("reqNo")), response);
		}
	}

	// 签到状态
	@RequestMapping(value = "/signStatus")
	public void signStatus(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {// "userName", "passWord",
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] {}), true, userSessionService,
					checkVersion, appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				Date newDate = new Date();
				params.put("userNo", ret.get("userNo"));
				if (StringUtil.isEmptyWithTrim(params.get("createTime"))) {
					params.put("createTime",
							DateUtils.formatDate(newDate, "yyyy-MM-dd"));
				}
				List<Map<String, Object>> signList = userSessionService.signInfoAll(params);// 查询今日签到次数
				List<Map<String, Object>> signOutList = userSessionService.signOutInfoAll(params);// 查询今日签退次数
				int signTimes = signList.size();
				int signOutTimes = signOutList.size();
				System.out.println("signTimes=============" + signTimes
						+ "//////////signOutTimes=============" + signOutTimes);
				String signStatus = "";
				if (signTimes == signOutTimes) {
					signStatus = "N";
				} else {
					signStatus = "Y";
				}
				List<Map<String, Object>> allSignTime = userSessionService
						.signAllTimeCheck(params);
				List<Map<String, Object>> newTimeList = new ArrayList<Map<String, Object>>();
				if (allSignTime.size() > 0) {
					int i = 1;
					for (Map<String, Object> map : allSignTime) {
						Map<String, Object> timeMap = new HashMap<String, Object>();
						if (i % 2 == 1) {
							timeMap.put("signTime", DateUtils.formatDate(
									(Date) map.get("signTime"), "HH:mm"));
							timeMap.put("signStatus", "已签到");
							newTimeList.add(timeMap);
						} else {
							timeMap.put("signTime", DateUtils.formatDate(
									(Date) map.get("signTime"), "HH:mm"));
							timeMap.put("signStatus", "已签退");
							newTimeList.add(timeMap);
						}
						++i;
					}
				}
				model.put("signList", newTimeList);
				model.put("signStatus", signStatus);
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

	// 签到查询
	@RequestMapping(value = "/signCheck")
	public void signCheck(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {// "userName", "passWord",
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] {}), true, userSessionService,
					checkVersion, appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				setPageInfo(params);
				params.put("userNo", ret.get("userNo"));

				Page<Map<String, Object>> list = userSessionService
						.monthWorkTime(params, pageRequest);

				model = new HashMap<String, Object>();
				model.put("signList", list.getContent());
				model.put("totalCount", list.getTotalElements());
				model.put("cp", list.getNumber() + 1);
				model.put("isLastPage", list.isLastPage());
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

	// 轨迹记录
	@RequestMapping(value = "/track")
	public void trackLocation(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {// "userName", "passWord","realName",
			Map<String, String> ret = ValidateUtil
					.validateRequest(request, reqParams(new String[] {
							"trackLongitude", "trackLatitude" }), true,
							userSessionService, checkVersion,
							appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				Date newDate = new Date();
				params.put("userNo", ret.get("userNo"));
				params.put("createTime",DateUtils.formatDate(newDate, "yyyy-MM-dd"));
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("user_no", ret.get("userNo"));
				map.put("app_version", params.get("appVersion"));
				map.put("android_id", params.get("androidId"));
				map.put("create_time", DateUtils.formatDate(newDate));
				map.put("ip", getClientIP(request));
				map.put("track_longitude", params.get("trackLongitude"));
				map.put("track_latitude", params.get("trackLatitude"));
				userService.saveTrack(map);// 保存轨迹
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

	// 地址同步
	@RequestMapping(value = "/addrSyn")
	public void addrSyn(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {// "userName", "passWord","realName",
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] {}), true, userSessionService,
					checkVersion, appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				params.put("courierNo", ret.get("userNo"));
				params.put("createTime", params.get("createTime"));
				List<Map<String, Object>> addrList = userService.addrList(params);
				model = new HashMap<String, Object>();
				model.put("addrList", addrList);
				render(JSON_TYPE,CommonResponse.respSuccessJson("", model,
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
					CommonResponse.respFailJson("9000", "服务器异常",params.get("reqNo")), response);
		}
	}

	// 新增会员
	@RequestMapping(value = "/addVip")
	public void addVip(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {//
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "vipPhone", "privilegeType","money", "passWord", "payType" }), true,
							userSessionService, checkVersion, appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				Date nowDate = new Date();
				String courierNo = ret.get("userNo");
				String payType = params.get("payType");
				if ("CASH".equals(payType.trim())) {
					String vipPhone = params.get("vipPhone");
					String privilegeType = params.get("privilegeType");
					Map<String, Object> privilegeMap = userService
							.checkPrivilegeType(privilegeType);// 查询优惠类型
					Map<String, Object> vipMap = userService
							.checkVipbInfo(vipPhone);
					if (vipMap != null) {
						render(JSON_TYPE,	CommonResponse.respFailJson("9045", vipPhone
										+ "已被注册", params.get("reqNo")),response);	
					return;
					}
					User user = userService.getUserByNo(courierNo);
					Map<String, Object> paramMap = new HashMap<String, Object>();
					paramMap.put("substation_no", user.getSubstationNo());
					paramMap.put("dis_user_no", params.get("vipPhone"));
					paramMap.put("dis_type", params.get("privilegeType"));
					paramMap.put("dis_user_name", params.get("vipName"));
					paramMap.put("contact_name", params.get("contactName"));
					paramMap.put("contact_phone", params.get("contactPhone"));
					paramMap.put("email", params.get("email"));
					paramMap.put("pwd", Md5.md5Str(params.get("passWord")));
					paramMap.put("operator", user.getRealName());
					paramMap.put("create_time", DateUtils.formatDate(nowDate));
					paramMap.put("note", params.get("note"));
					paramMap.put("status", "1");

					// 插入后获取ID号
					long id = userService.addVip(paramMap);
					paramMap.clear();

					paramMap.put("uid", id);
					paramMap.put("balance", params.get("money"));
					paramMap.put("last_update_time",
							DateUtils.formatDate(nowDate));
					userService.addVipMoney(paramMap);
					// 新增记录

					paramMap.clear();
					paramMap.put("dis_user_no", params.get("vipPhone"));
					paramMap.put("rmoney", params.get("money"));
					paramMap.put("omoney", params.get("omoney"));
					paramMap.put("af_balance", params.get("money"));
					paramMap.put("status", "SUCCESS");
					paramMap.put("discount_text",
							privilegeMap.get("discount_text"));
					paramMap.put("lied", "N");
					paramMap.put("gather_no", courierNo);
					paramMap.put("gather_no_type", "C");
					paramMap.put("operator", user.getRealName());
					paramMap.put("create_time", DateUtils.formatDate(nowDate));
					paramMap.put("last_update_time",
							DateUtils.formatDate(nowDate));
					paramMap.put("note", "首次注册充值");
					paramMap.put("source", "COURIER");
					paramMap.put("pay_type", "CASH");
					paramMap.put("acount_number", null);
					userService.addHistory(paramMap);
					// 新增充值记录
					render(JSON_TYPE,CommonResponse.respSuccessJson("", "新增会员成功",
									params.get("reqNo")), response);
					return;
				}
				if ("WEIXIN".equals(payType.trim())) {
					ret = ValidateUtil.validateRequest(request,
							reqParams(new String[] {}), true,
							userSessionService, checkVersion,
							appVersionService, dynamicDataSourceHolder);
					String acountNumber = sequenceService
							.getNextVal("acount_number");// 获取会员微信支付流水号

					// 新增记录
					String privilegeType = params.get("privilegeType");
					User user = userService.getUserByNo(courierNo);
					Map<String, Object> privilegeMap = userService
							.checkPrivilegeType(privilegeType);// 查询优惠类型
					Map<String, Object> paramMap = new HashMap<String, Object>();
					paramMap.put("dis_user_no", params.get("vipPhone"));
					paramMap.put("rmoney", params.get("money"));
					paramMap.put("omoney", params.get("omoney"));
					paramMap.put("af_balance", params.get("money"));
					paramMap.put("status", "FAIL");
					paramMap.put("discount_text",
							privilegeMap.get("discount_text"));
					paramMap.put("lied", "N");
					paramMap.put("gather_no", courierNo);
					paramMap.put("gather_no_type", "C");
					paramMap.put("operator", user.getRealName());
					paramMap.put("create_time", DateUtils.formatDate(nowDate));
					paramMap.put("last_update_time",
							DateUtils.formatDate(nowDate));
					paramMap.put("note", "首次注册充值");
					paramMap.put("source", "COURIER");
					paramMap.put("pay_type", "WEIXIN");
					paramMap.put("acount_number", acountNumber);
					userService.addHistory(paramMap);
					// 新增充值记录
					model = new HashMap<String, Object>();
					model.put("acountNumber", acountNumber);
					render(JSON_TYPE,
							CommonResponse.respSuccessJson("", model,
									params.get("reqNo")), response);
					return;
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

	// 会员充值
	@RequestMapping(value = "/vipRec")
	public void vipRec(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {//
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "vipPhone", "privilegeType",
							"money", "payType" }), true, userSessionService,
							checkVersion, appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String payType = params.get("payType");
				if ("CASH".equals(payType.trim())) {
					Date nowDate = new Date();
					String courierNo = ret.get("userNo");
					String vipPhone = params.get("vipPhone");
					User user = userService.getUserByNo(courierNo);
					Map<String, Object> vipMap = userService
							.checkVipbInfo(vipPhone);
					String privilegeType = params.get("privilegeType");
					Map<String, Object> privilegeMap = userService
							.checkPrivilegeType(privilegeType);// 查询优惠类型
					if (vipMap == null) {
						render(JSON_TYPE, CommonResponse.respFailJson("9044",
								"会员" + vipPhone + "不存在", params.get("reqNo")),
								response);
						return;
					}
					System.out.println("bgBalance============================"
							+ vipMap.get("balance"));
					System.out.println("money============================"
							+ params.get("money"));
					float sumBalance = Float.valueOf(params.get("money"));// 新加金额
					userService.addBalance(sumBalance, DateUtils.formatDate(
							nowDate, "yyyy-MM-dd HH:mm:ss"), String
							.valueOf(vipMap.get("uid")));// 保存充值金额

					BigDecimal balance = (BigDecimal) vipMap.get("balance");// 原有
					BigDecimal balance2 = new BigDecimal(sumBalance);
					balance = balance.add(balance2);
					System.out.println("充值金额================="
							+ balance2.toString());
					System.out.println("充值后金额=================="
							+ balance.toString());

					Map<String, Object> paramMap = new HashMap<String, Object>();
					paramMap.put("dis_user_no", params.get("vipPhone"));
					paramMap.put("rmoney", params.get("money"));
					paramMap.put("omoney", params.get("omoney"));
					paramMap.put("af_balance", balance);
					paramMap.put("status", "SUCCESS");
					paramMap.put("discount_text",
							privilegeMap.get("discount_text"));
					paramMap.put("lied", "N");
					paramMap.put("gather_no", courierNo);
					paramMap.put("gather_no_type", "C");
					paramMap.put("operator", user.getRealName());
					paramMap.put("create_time", DateUtils.formatDate(nowDate,
							"yyyy-MM-dd HH:mm:ss"));
					paramMap.put("last_update_time", DateUtils.formatDate(
							nowDate, "yyyy-MM-dd HH:mm:ss"));
					paramMap.put("note", params.get("note"));
					paramMap.put("source", "COURIER");
					paramMap.put("pay_type", "CASH");
					paramMap.put("acount_number", null);
					userService.addHistory(paramMap);
					// 新增充值记录
					userService.updateVipType(vipPhone, privilegeType);// 更改会员等级

					Map<String, Object> vipInfo = userService
							.checkVipbInfo(vipPhone);// 返回会员数据
					model = new HashMap<String, Object>();
					model.put("vipInfo", vipInfo);
					render(JSON_TYPE,
							CommonResponse.respSuccessJson("", model,
									params.get("reqNo")), response);
					return;
				}
				if ("WEIXIN".equals(payType.trim())) {

					String acountNumber = sequenceService
							.getNextVal("acount_number");// 获取会员微信支付流水号
					Date nowDate = new Date();
					String courierNo = ret.get("userNo");
					String vipPhone = params.get("vipPhone");
					User user = userService.getUserByNo(courierNo);
					Map<String, Object> vipMap = userService
							.checkVipbInfo(vipPhone);
					// String privilegeType =params.get("privilegeType");
					// Map<String,Object> privilegeMap =
					// userService.checkPrivilegeType(privilegeType);//查询优惠类型
					if (vipMap == null) {
						render(JSON_TYPE, CommonResponse.respFailJson("9044",
								"会员" + vipPhone + "不存在", params.get("reqNo")),
								response);
						return;
					}

					float sumBalance = Float.valueOf(params.get("money"));// 新加金额
					// userService.addBalance(
					// sumBalance,DateUtils.formatDate(nowDate,"yyyy-MM-dd HH:mm:ss"),
					// String.valueOf(vipMap.get("uid")));//保存充值金额
					//
					// BigDecimal balance =
					// (BigDecimal)vipMap.get("balance");//原有
					// BigDecimal balance2 = new BigDecimal(sumBalance);
					// balance= balance.add(balance2);
					// System.out.println("充值金额================="+balance2.toString());
					// System.out.println("充值后金额=================="+balance.toString());

					Map<String, Object> paramMap = new HashMap<String, Object>();
					paramMap.put("dis_user_no", params.get("vipPhone"));
					paramMap.put("rmoney", null);
					paramMap.put("omoney", params.get("omoney"));
					paramMap.put("af_balance", null);
					paramMap.put("status", "FAIL");
					paramMap.put("discount_text", null);
					paramMap.put("lied", "N");
					paramMap.put("gather_no", courierNo);
					paramMap.put("gather_no_type", "C");
					paramMap.put("operator", user.getRealName());
					paramMap.put("create_time", DateUtils.formatDate(nowDate,
							"yyyy-MM-dd HH:mm:ss"));
					paramMap.put("last_update_time", DateUtils.formatDate(
							nowDate, "yyyy-MM-dd HH:mm:ss"));
					paramMap.put("note", params.get("note"));
					paramMap.put("source", "COURIER");
					paramMap.put("pay_type", "WEIXIN");
					paramMap.put("acount_number", acountNumber);
					userService.addHistory(paramMap);
					// 新增充值记录
					model = new HashMap<String, Object>();
					model.put("acountNumber", acountNumber);
					render(JSON_TYPE,
							CommonResponse.respSuccessJson("", model,
									params.get("reqNo")), response);
					return;
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

	// 会员返回显示
	@RequestMapping(value = "/vipCheck")
	public void vipCheck(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {//
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "vipPhone" }), true,
					userSessionService, checkVersion, appVersionService,
					dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {

				String vipPhone = params.get("vipPhone");
				Map<String, Object> vipMap = userService
						.checkVipbInfo(vipPhone);
				if (vipMap == null) {
					render(JSON_TYPE, CommonResponse.respFailJson("9044",
							"会员号不存在", params.get("reqNo")), response);
					return;
				}

				model = new HashMap<String, Object>();
				model.put("vipMap", vipMap);
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

	// 会员充值记录查询
	@RequestMapping(value = "/checkVipHistory")
	public void checVipkHistory(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {//
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] {}), true, userSessionService,
					checkVersion, appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				setPageInfo(params);
				Date nowDate = new Date();
				String courierNo = ret.get("userNo");
				Page<Map<String, Object>> list = userService.checkVipHistory(
						courierNo, pageRequest);
				String SUMMoney = userService.checkVipHisMoney(courierNo);
				model = new HashMap<String, Object>();
				model.put("vipHisList", list.getContent());
				model.put("totalCount", list.getTotalElements());
				model.put("cp", list.getNumber() + 1);
				model.put("isLastPage", list.isLastPage());
				model.put("sumMoney", SUMMoney);
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

	// 会员优惠类型
	@RequestMapping(value = "/vipType")
	public void vipType(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {//
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] {}), true, userSessionService,
					checkVersion, appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				List<Map<String, Object>> list = userService.vipType();
				model = new HashMap<String, Object>();
				model.put("list", list);
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

	// 问题件原因
	@RequestMapping(value = "/error")
	public void error(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {//
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] {}), true, userSessionService,
					checkVersion, appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				List<Map<String, Object>> list = userService.getProOrderReason();
				model = new HashMap<String, Object>();
				model.put("list", list);
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

	// 获取物品类型
	@RequestMapping(value = "/itemType")
	public void itemType(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {//
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] {}), true, userSessionService,
					checkVersion, appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				List<Map<String, Object>> list = userService.getItemType();
				model = new HashMap<String, Object>();
				model.put("list", list);
				render(JSON_TYPE,CommonResponse.respSuccessJson("", model,params.get("reqNo")), response);
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,CommonResponse.respFailJson(ret.get("respCode"),
								ret.get("respMsg"), params.get("reqNo")),response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9000", "服务器异常",
							params.get("reqNo")), response);
		}
	}
	/**
	 * 查询快递员信息
	 * @param object
	 * @return
	 * @throws SQLException
	 */
	@RequestMapping(value = "/queryC")
	public void queryC(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "message" }), true,	userSessionService, checkVersion, appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {

				List<Map<String, Object>> list = userService.queryC(params.get("message"));
				model = new HashMap<String, Object>();
				model.put("list", list);		
				render(JSON_TYPE,	CommonResponse.respSuccessJson("", model,params.get("reqNo")), response);	

			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,	CommonResponse.respFailJson(ret.get("respCode"),	ret.get("respMsg"), params.get("reqNo")),
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
	 * 效验密码
	 * @param params
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/checkPwd")
	public void checkPwd(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "oldPwd" }), true,	userSessionService, checkVersion, appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				User user=userService.getUserByNo( ret.get("userNo"));
				String oldPwd = params.get("oldPwd");
				if(	!Md5.md5Str(oldPwd).equals(user.getPassWord())){		
					model = new HashMap<String, Object>();
					model.put("isTrue", "false");	
					render(JSON_TYPE,	CommonResponse.respFailJson(ret.get("respCode"),"输入的密码有误", params.get("reqNo")),
							response);
					return;
				}
				model = new HashMap<String, Object>();
				model.put("isTrue", "true");		
				render(JSON_TYPE,	CommonResponse.respSuccessJson("", model,params.get("reqNo")), response);						
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,	CommonResponse.respFailJson(ret.get("respCode"),	ret.get("respMsg"), params.get("reqNo")),
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
	 * 修改密码
	 * @param params
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/changePwd")
	public void changePwd(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "newPwd" }), true,	userSessionService, checkVersion, appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String newPwd = Md5.md5Str(params.get("newPwd"));
				userService.changePwd(ret.get("userNo"), newPwd);	
				render(JSON_TYPE,	CommonResponse.respSuccessJson("", "修改成功",params.get("reqNo")), response);						
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE,	CommonResponse.respFailJson(ret.get("respCode"),	ret.get("respMsg"), params.get("reqNo")),
						response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,CommonResponse.respFailJson("9000", "服务器异常",
							params.get("reqNo")), response);
		}
	}
}