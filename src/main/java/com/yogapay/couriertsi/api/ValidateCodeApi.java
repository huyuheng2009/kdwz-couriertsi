package com.yogapay.couriertsi.api;

import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.yogapay.couriertsi.domain.RunnableUtils;
import com.yogapay.couriertsi.domain.ValidateCode;
import com.yogapay.couriertsi.enums.OperateType;
import com.yogapay.couriertsi.services.UserService;
import com.yogapay.couriertsi.services.ValidateCodeService;
import com.yogapay.couriertsi.utils.CommonResponse;
import com.yogapay.couriertsi.utils.DateUtils;
import com.yogapay.couriertsi.utils.StringUtils;
import com.yogapay.couriertsi.utils.ValidateUtil;
/**
 * 发送验证码
 * @author hhh
 *
 */
@Controller
@RequestMapping(value = "/vcode",method=RequestMethod.POST)
@Scope("prototype")
public class ValidateCodeApi extends BaseApi{

	@Resource
	private ValidateCodeService validateCodeService ;
	@Resource
	private UserService userService;

	@RequestMapping(value = "/send")
	public void regist(@RequestParam Map<String, String> params,HttpServletRequest request,HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(new String[]{"phone","businessType"}), false,null,checkVersion,appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				String userNo = ret.get("userNo");
				String lgcNo = userService.getUserLgcNo(userNo);
				String channel=params.get("channel");
				if(StringUtils.isEmptyWithTrim(channel)){
					channel="N";
				}     	
				System.out.println("发送的业务类型"+params.get("businessType"));
				ValidateCode validateCode = validateCodeService.getLastCode(params.get("phone"),Integer.valueOf(params.get("businessType"))) ;
				String operateType = "none" ;
				switch (Integer.valueOf(params.get("businessType"))) {
				case 11:
					operateType = OperateType.REGIST.getValue() ;
					break;
				case 12:
					operateType = OperateType.CHANGE_PASSWORD.getValue() ;
					break;
				case 13:
					operateType = OperateType.CHANGE_PHONE.getValue() ;
					break;
				default:
					operateType = "none" ;
					break;
				}
				if (operateType.equals("none")) {
					render(JSON_TYPE, CommonResponse.respFailJson("9018", "业务类型错误", params.get("reqNo")), response);
					return ;
				}
				Date nowDate = new Date() ;
				boolean flag = true ;
				String code = "" ;
				int repeatTime = 1 ;
				if (validateCode==null||nowDate.after(validateCode.getExpireTime())) {   //过期
					code = StringUtils.getRandomString(4, true) ;
				}else {
					if (validateCode.getRepeatTime()<4) {       //半小时只允许发四次
						code = validateCode.getCode() ;
						repeatTime = validateCode.getRepeatTime()+1 ;
					}else {
						flag = false ;
					}
				}
				String pix="【快递王子】";
				String c = "szkyt" ;
				String uid = params.get("uid") ;
				if (uid.contains("_")) {
					uid = uid.substring(0, uid.indexOf("_"));
				}
				if("yx".equals(uid)){
					channel = "yx" ;
					pix="【亿翔快递】";
				}
				String content = "你正在进行"+operateType+"操作,验证码为："+ code + "，切勿告知他人"+pix ;
				if (flag) {
					//发送短信通知
//					Map<String, String> values = new HashMap<String, String>();
//					Map<String, String> requestProperty = new HashMap<String, String>();
//					values.put("operation", "S");
//					values.put("target", params.get("phone"));
//					values.put("note.businessCode", "YYPT");
//					values.put("content", content);
//					values.put("channel",channel);//选择短信通道values.get("channel")+
//					values.put("check", SHA.SHA1Encode(values.get("target") + "yogapayHFT" + values.get("content") + values.get("note.businessCode")).toUpperCase());
//					CommonHttpConnection.proccess(new HttpConnectionParameters(ConstantsLoader.getProperty("message_host"), "POST", 22000, true, true, true, requestProperty), values);
					RunnableUtils ui = new RunnableUtils();
					ui.MessagePushClass(c,lgcNo, params.get("phone"),content,null);	
		
					
					ValidateCode newValidateCode = new ValidateCode() ;
					newValidateCode.setCode(code);
					newValidateCode.setCreateTime(nowDate);
					newValidateCode.setPhone(params.get("phone"));
					newValidateCode.setBusinessType(Integer.valueOf(params.get("businessType")));
					newValidateCode.setRepeatTime(repeatTime);
					newValidateCode.setExpireTime(DateUtils.addDate(nowDate, 0, 0, 30));
					validateCodeService.save(newValidateCode) ;
					render(JSON_TYPE, CommonResponse.respSuccessJson("","发送成功", params.get("reqNo")), response);     
				}else {
					render(JSON_TYPE, CommonResponse.respFailJson("9003", "请求频繁", params.get("reqNo")), response);
				}


			}else {
				render(JSON_TYPE, CommonResponse.respFailJson(ret.get("respCode"), ret.get("respMsg"), params.get("reqNo")), response);
			}
		}  catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE, CommonResponse.respFailJson("9000", "服务器异常", params.get("reqNo")), response);
		}
	}




}
