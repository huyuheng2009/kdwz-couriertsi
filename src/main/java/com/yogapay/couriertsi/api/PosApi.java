package com.yogapay.couriertsi.api;

import java.io.File;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jpos.core.CardHolder;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.tlv.TLVList;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.yogapay.couriertsi.enums.BizType;
import com.yogapay.couriertsi.services.LgcService;
import com.yogapay.couriertsi.services.PosInfoService;
import com.yogapay.couriertsi.utils.CommonResponse;
import com.yogapay.couriertsi.utils.Commons;
import com.yogapay.couriertsi.utils.ConstantsLoader;
import com.yogapay.couriertsi.utils.JCEHandler;
import com.yogapay.couriertsi.utils.PosUtil;
import com.yogapay.couriertsi.utils.SendMail2;
import com.yogapay.couriertsi.utils.SendMessage;
import com.yogapay.couriertsi.utils.StringUtils;
import com.yogapay.couriertsi.utils.ValidateUtil;

/**
 * 刷卡交易接口类
 * 
 * @author hhh
 */
@Controller
@RequestMapping(value = "/pos", method = RequestMethod.POST)
@Scope("prototype")
public class PosApi extends BaseApi {

	@Resource
	public PosInfoService posService;
	@Resource
	public LgcService lgcService;

	// pos签到
	@RequestMapping(value = "/init")
	public void init(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			model = new HashMap<String, Object>();
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "sn", "isUpdate" }), false, null,
					checkVersion, appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				log.info("init...");
				String isUpdate = params.get("isUpdate");
				if (!"T".equals(isUpdate)) {
					isUpdate = "F";
				}
				Map<String, String> posParams = new HashMap<String, String>();
				posParams.put("sn", params.get("sn"));
				posParams.put("isUpdate", isUpdate);
				Map<String, Object> checkInMap = PosUtil.sendAndRev(configInfo,posParams,
						BizType.INIT);
				if (checkInMap != null
						&& "SUCCESS".equals(checkInMap.get("respCode"))) {
					checkInMap.remove("respTime");
					checkInMap.remove("isSuccess");
					checkInMap.remove("respCode");
					checkInMap.remove("respMsg");
					model.put("respMsg", "初始化成功");
					model.put("posInfo", checkInMap);
				} else {
					String msg = "初始化失败！";
					model.put("posInfo", "");
					if (checkInMap != null
							&& checkInMap.get("respCode") != null) {
						msg = "初始化失败:" + checkInMap.get("respCode")
								+ checkInMap.get("respMsg").toString();
						log.info("初始化失败:"
								+ checkInMap.get("respCode").toString()
								+ "--------->" + msg);
					}
					render(JSON_TYPE,
							CommonResponse.respFailJson("9020", msg,
									params.get("reqNo")), response);
					return;
				}
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
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9000", "服务器异常",
							params.get("reqNo")), response);

		}
	}

	@RequestMapping(value = "/sale")
	public void sale(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> pos = null;
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					new String[] { "uid", "ksn",
			"amount" }, true, userSessionService, checkVersion,
					appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String ksn = params.get("ksn");
				Map<String, Object> ksnInfo = posService.getKsnByKsnNo(ksn);
				// 校验ksn的有限性
				if (ksnInfo == null) {
					log.info("ksn false!!!!---->" + ret.get("respCode")
							+ "---------" + ret.get("respMsg"));
					render(JSON_TYPE, CommonResponse.respFailJson(
							ret.get("respCode"), ret.get("respMsg"),
							params.get("reqNo")), response);
					return;
				}
				// 根据uid查找快递公司
				Map<String, Object> lgc = lgcService.getLgcByUid(params
						.get("uid").split("_")[0]);
				// 判断快递公司是否绑定了收单商户
				Map<String, Object> lgcMerchant = posService
						.getLgcMerchantByLgcNo((String) lgc.get("lgc_no"));
				if (lgcMerchant == null) {
					log.info("lgc_merchant false!!!!---->"
							+ ret.get("respCode") + "---------"
							+ ret.get("respMsg"));
					render(JSON_TYPE, CommonResponse.respFailJson(
							ret.get("respCode"), ret.get("respMsg"),
							params.get("reqNo")), response);
					return;
				}
				// 判断收单pos
				pos = posService.getPosByMerchantNo((String) lgcMerchant
						.get("merchant_no"));
				if (pos == null) {
					log.info("pos false!!!!---->" + ret.get("respCode")
							+ "---------" + ret.get("respMsg"));
					render(JSON_TYPE, CommonResponse.respFailJson(
							ret.get("respCode"), ret.get("respMsg"),
							params.get("reqNo")), response);
					return;
				}
				// 判断签名文件是否上传并保存图片
				MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
				Map<String, MultipartFile> fileMap = multipartRequest
						.getFileMap();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				String fileName = sdf.format(new Date())
						+ pos.get("merchant_no") + pos.get("terminal_no")
						+ pos.get("batch_no") + pos.get("voucher_no") + ".png";
				log.info("fileName=================" + fileName);
				FileCopyUtils.copy(
						fileMap.get("signature").getBytes(),
						new File(configInfo.getSignature_images().concat(
										fileName)));
				String pinBlock = "";
				TLVList tlvList = null;
				TLVList trailler = null;
				byte[] tr = null;
				String pinKsn;
				String encPinblock;
				String region23 = "";
				CardHolder cardHolder = null;
				String clearTpk = null;
				String clearTak = null;
				String pan = null;
				// 校验刷卡数据
				try {
					clearTpk = JCEHandler.decryptData(
							(String) pos.get("tmk_tpk"),
							(String) pos.get("tmk"));
					clearTak = JCEHandler.decryptData(
							(String) pos.get("tmk_tak"),
							(String) pos.get("tmk"));

					Map<String, Object> key = posService.getKeyByKsnNo(ksn);
					String iccData = params.get("icData");
					System.out.println("iccData=============="+iccData);
					if (StringUtils.isNotEmptyWithTrim(iccData)) {
						tlvList = new TLVList();
						tlvList.unpack(ISOUtil.hex2byte(iccData));
						String tlv_C0 = tlvList.getString(0xC0);
						pinKsn = tlvList.getString(0xC1);
						String tlv_C2 = tlvList.getString(0xC2);
						encPinblock = tlvList.getString(0xC7);
						String decode_C2 = Commons.decodeTracks2(tlv_C0,
								(String) key.get("bdk"), tlv_C2,
								"mpos");
						System.out.println("C2:" + decode_C2);
						String hexLen = decode_C2.substring(4, 8);
						System.out.println("hexLen:" + hexLen);
						int length = Commons.char2Hex(hexLen.charAt(0))
								* (int) Math.pow(16, 3)
								+ Commons.char2Hex(hexLen.charAt(1))
								* (int) Math.pow(16, 2)
								+ Commons.char2Hex(hexLen.charAt(2))
								* (int) Math.pow(16, 1)
								+ Commons.char2Hex(hexLen.charAt(3))
								* (int) Math.pow(16, 0);
						System.out.println("length:" + length);
						// 去掉前面8位固定位和后面多余补位
						decode_C2 = decode_C2.substring(8, decode_C2.length())
								.substring(0, length * 2);
						tlvList.unpack(ISOUtil.hex2byte(decode_C2));
						String f0x57 = tlvList.getString(0x57);
						String track2;
						if (f0x57.indexOf("F") < 0) {
							track2 = tlvList.getString(0x57).replace("D", "=");
						} else {
							track2 = tlvList
									.getString(0x57)
									.substring(
											0,
											tlvList.getString(0x57)
													.indexOf("F"))
									.replace("D", "=");
						}
						cardHolder = new CardHolder(track2);
						pan = cardHolder.getPAN();
						System.out.println("track2:" + cardHolder.getTrack2());
						System.out.println("pan:" + cardHolder.getPAN());
						System.out.println("exp:" + cardHolder.getEXP());
						trailler = new TLVList();
						trailler.append(0x9F33, tlvList.getString(0x9F33));
						trailler.append(0x95, tlvList.getString(0x95));
						trailler.append(0x9F1E, tlvList.getString(0x9F1E));
						trailler.append(0x9F10, tlvList.getString(0x9F10));
						trailler.append(0x9F26, tlvList.getString(0x9F26));
						trailler.append(0x9F36, tlvList.getString(0x9F36));
						trailler.append(0x82, tlvList.getString(0x82));
						System.out.println("0x82:" + tlvList.getString(0x82));
						trailler.append(0x9C, tlvList.getString(0x9C));
						trailler.append(0x9F1A, tlvList.getString(0x9F1A));
						trailler.append(0x9A, tlvList.getString(0x9A));
						trailler.append(0x9F02, tlvList.getString(0x9F02));
						trailler.append(0x5F2A, tlvList.getString(0x5F2A));
						trailler.append(0x9F03, tlvList.getString(0x9F03));
						trailler.append(0x9F35, tlvList.getString(0x9F35));
						trailler.append(0x9F34, tlvList.getString(0x9F34));
						trailler.append(0x9F37, tlvList.getString(0x9F37));
						trailler.append(0x9F27, tlvList.getString(0x9F27));
						trailler.append(0x9F41, tlvList.getString(0x9F41));
						// trailler = trailler.pack();
						tr = trailler.pack();

						region23 = '0' + tlvList.getString(0x5F34);
						System.out.println("egion23:" + region23);
						System.out.println("trailler:" + trailler);
						pinBlock = Commons.decodePinBlock(pinKsn,
								(String) key.get("bdk"), cardHolder.getPAN(),
								encPinblock, clearTpk,
								(String) ksnInfo.get("model"));
					} else {// 磁条卡
						String track2 = Commons.decodeTracks2(
								params.get("trackKsn"),
								(String) key.get("bdk"),
								params.get("encTracks"), "mpos");
						if (track2.indexOf("F") < 0) {
							cardHolder = new CardHolder(track2);
						} else {
							String track2Data = track2.substring(0,
									track2.indexOf("F")).replace("D", "=");
							System.out.println("track2Data=" + track2Data);
							cardHolder = new CardHolder(track2Data);
						}
						pan = cardHolder.getPAN();
						pinBlock = Commons.decodePinBlock(params.get("pinKsn"),
								(String) key.get("bdk"), cardHolder.getPAN(),
								params.get("encryptPin"), clearTpk,
								(String) ksnInfo.get("model"));
					}
				} catch (Exception e) {
					e.printStackTrace();
					render(JSON_TYPE,
							CommonResponse.respFailJson("9022", "密码错误",
									params.get("reqNo")), response);
					return;
				}
				
				Map<String, Object> cardbin = posService.findCardbin(pan);

				// 向核心交易发送报文
				// send trans to ts
				ISOMsg msg = new ISOMsg();
				msg.set(0, "0200");
				msg.set(2, pan);
				msg.set(3, "000000");
				System.out.println("amount=============="+StringUtils.stringFillLeftZero(new BigDecimal(params.get("amount")).multiply(new BigDecimal(100)).intValue()+"",12));
				msg.set(4, StringUtils.stringFillLeftZero(new BigDecimal(params.get("amount")).multiply(new BigDecimal(100)).intValue()+"",12));
				msg.set(11, (String) pos.get("voucher_no"));
				if (params.containsKey("icData")) {
					msg.set(14, cardHolder.getEXP());
				}
				String f604;
				if (params.containsKey("encTracks")) {
					msg.set(22, StringUtils.isNotEmptyWithTrim(params
							.get("encPinblock")) ? "021" : "022");
					f604 = "0";
				} else {
					msg.set(22, "051");
					f604 = "5";
				}
				if (params.containsKey("icData")) {
					msg.set(23, region23);
				}
				msg.set(25, "00");
				if (params.containsKey("icData")) {
					msg.set(26, "12");
				} else {
					msg.set(26, "06");
				}
				System.out.println("cardHolder.track2="
						+ cardHolder.getTrack2());
				msg.set(35, cardHolder.getTrack2());
				msg.set(41, (String) pos.get("terminal_no"));
				msg.set(42, (String) pos.get("merchant_no"));
				msg.set(49, "156");
				if (StringUtils.isNotEmptyWithTrim(pinBlock)) {
					msg.set(52, ISOUtil.hex2byte(pinBlock));
				}
				msg.set(53, "2000000000000000");
				if (params.containsKey("icData")) {
					msg.set(55, tr);
				}
				msg.set(60, "22" + (String) pos.get("batch_no") + "000" + f604
						+ "0");
				msg.set(64, new byte[8]);

				ISOMsg r = Commons.sendAndRecive(msg, clearTak);
				if (r == null) {
					render(JSON_TYPE, CommonResponse.respFailJson("9023",
							"pos连接超时", params.get("reqNo")), response);
					return;
				}

				String code = r.getString(39);
				if ("00".equals(code)) {
					// 交易成功
					model.put("voucherNo", pos.get("voucher_no"));
					model.put("batchNo", pos.get("batch_no"));
					model.put("merchantNo", pos.get("merchant_no"));
					model.put("terminalNo", pos.get("terminal_no"));
					model.put("operatorNo", "01");
					model.put("merchantName", pos.get("merchant_name"));
					model.put(
							"cardNo",
							pan.substring(0, 5)
									+ "*****"
									+ pan.substring(pan.length() - 4,
											pan.length()));
					SimpleDateFormat s = new SimpleDateFormat("yyyy");
					model.put(
							"transTime",
							s.format(new Date()) + "-"+r.getString(13).substring(0, 2)+"-"+r.getString(13).substring(2, 4)+" "
									+ r.getString(12).substring(0, 2)+":"+r.getString(12).substring(2, 4)+":"+r.getString(12).substring(4, 6));
					model.put("refNo", r.getString(37));
					model.put("authNo", r.hasField(38) ? r.getString(38) : "");
					model.put("issuer", cardbin.get("bank_name"));
					model.put("icData", r.hasField(55) ? r.getString(55) : "");

					render(JSON_TYPE,
							CommonResponse.respSuccessJson("", model,
									params.get("reqNo")), response);
				} else {
					render(JSON_TYPE, CommonResponse.respFailJson("9051",
							"交易失败，联系发卡行", params.get("reqNo")), response);
					return;
				}

			} else {
				log.info("validate false!!!!---->" + ret.get("respCode")
						+ "---------" + ret.get("respMsg"));
				render(JSON_TYPE,
						CommonResponse.respFailJson(ret.get("respCode"),
								ret.get("respMsg"), params.get("reqNo")),
						response);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9000", "服务器异常",
							params.get("reqNo")), response);
			return;

		} finally {
			if (pos != null) {
				Integer voucherNo = Integer.parseInt((String) pos.get("voucher_no"));
				pos.put("voucher_no",
						StringUtils.stringFillLeftZero(voucherNo + 1 + "", 6));
				posService.updatePosVoucherNo(pos);
			}
		}
	}

	// 发送签购单
	@RequestMapping(value = "/sendSalesSlip")
	public void sendSalesSlip(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "cardNoTail", "transTime",
							"amount", "msgSign" }), false, null, checkVersion,
					appVersionService, dynamicDataSourceHolder);
			System.out.println("mobile=========="+params.get("mobile"));
			System.out.println("mail==========="+params.get("mail"));
			if ("TRUE".equals(ret.get("isSuccess"))) {
				if (StringUtils.isNotEmptyWithTrim(params.get("mobile"))) {
					// 根据uid查找快递公司
					Map<String, Object> lgc = lgcService.getLgcByUid(params
							.get("uid").split("_")[0]);
					if (lgc == null) {
						render(JSON_TYPE, CommonResponse.respFailJson("9001",
								"uid有误", params.get("reqNo")), response);
					} else {
						String content = "您尾号" + params.get("cardNoTail")
								+ "的银行卡于" + params.get("transTime") + "消费人民币"
								+ params.get("amount") + "元【"
								+ "快递王子】";
						System.out.println("content======"+content);
						System.out.println("lgc_no======"+lgc.get("lgc_no"));
						boolean flag = SendMessage.send(params.get("mobile"),
								content, (String) lgc.get("lgc_no"));
						if (!flag) {
							render(JSON_TYPE, CommonResponse.respFailJson(
									"9052", "发送签购单失败", params.get("reqNo")),
									response);
						}
					}
				}
				if (StringUtils.isNotEmptyWithTrim(params.get("mail"))) {
					// 判断签名文件是否上传并保存图片
					MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
					Map<String, MultipartFile> fileMap = multipartRequest
							.getFileMap();
					String fileName = params.get("cardNoTail")
							+ params.get("transTime").substring(0, 10) +params.get("reqNo")+ ".png";
					log.info("fileName=================" + fileName);
					File file = new File(configInfo.getSalesSlip_images().concat(fileName));
					FileCopyUtils.copy(fileMap.get("salesSlip").getBytes(),
							file);
					if (!file.exists()) {
						render(JSON_TYPE, CommonResponse.respFailJson("9053",
								"上传签购单失败", params.get("reqNo")), response);
					}
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("subject", "签购单");
					map.put("to", params.get("mail"));
					map.put("content", "您尾号" + params.get("cardNoTail")
							+ "的银行卡于" + params.get("transTime") + "消费人民币"
							+ params.get("amount") + "元" + ",附件中是签购单");
					List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
					Map<String, Object> f = new HashMap<String, Object>();
					f.put("name", "交易签购单.png");
					f.put("file", file);
					list.add(f);
					map.put("attachment", list);
					try {
						SendMail2.sendEamil(map);
					} catch (Exception e) {
						e.printStackTrace();
						render(JSON_TYPE, CommonResponse.respFailJson("9054",
								"服务器异常", params.get("reqNo")), response);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9000", "服务器异常",
							params.get("reqNo")), response);
		}
		render(JSON_TYPE,
				CommonResponse.respSuccessJson("", model, params.get("reqNo")),
				response);
	}

	// pos冲正接口
	@RequestMapping(value = "/rollback")
	public void rollback(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			model = new HashMap<String, Object>();
			Map<String, String> ret = ValidateUtil.validateRequest(request,
					reqParams(new String[] { "merchantNo", "terminalNo",
							"encPinblock", "amount" }), false, null,
					checkVersion, appVersionService, dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {

				if (StringUtils.isEmptyWithTrim(params.get("encTracks2"))
						&& StringUtils.isEmptyWithTrim(params.get("icData"))) {
					render(JSON_TYPE, CommonResponse.respFailJson("9001",
							"缺少参数：encTracks2或icData", params.get("reqNo")),
							response);
					return;
				}
				log.info("rollback...");
				Map<String, String> posParams = new HashMap<String, String>();
				posParams.put("merchantNo", params.get("merchantNo"));
				posParams.put("terminalNo", params.get("terminalNo"));
				posParams.put("amount", params.get("amount"));
				posParams.put("encryptPin", params.get("encPinblock"));
				posParams.put("track2", params.get("encTracks2"));
				posParams.put("track3", params.get("encTracks3"));
				posParams.put("icData", params.get("icData"));
				Map<String, Object> rollbackMap = PosUtil.sendAndRev(configInfo,posParams,
						BizType.ROLLBACK);
				if (rollbackMap != null
						&& "SUCCESS".equals(rollbackMap.get("respCode"))) {
					model.put("respMsg", "冲正成功");
				} else {
					String msg = "冲正失败！";
					if (rollbackMap != null
							&& rollbackMap.get("respCode") != null) {
						msg = "冲正失败:" + rollbackMap.get("respCode").toString()
								+ rollbackMap.get("respMsg").toString();
						log.info(msg);
					} else {
						msg = "冲正失败！未知错误";
					}
					render(JSON_TYPE,
							CommonResponse.respFailJson("9025", msg,
									params.get("reqNo")), response);
					return;
				}
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
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE,
					CommonResponse.respFailJson("9000", "服务器异常",
							params.get("reqNo")), response);

		}
	}

}
