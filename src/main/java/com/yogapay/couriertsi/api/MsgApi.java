package com.yogapay.couriertsi.api;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.yogapay.couriertsi.dto.MsgDto;
import com.yogapay.couriertsi.services.MsgService;
import com.yogapay.couriertsi.services.MsgTypeService;
import com.yogapay.couriertsi.utils.CommonResponse;
import com.yogapay.couriertsi.utils.DateUtils;
import com.yogapay.couriertsi.utils.StringUtils;
import com.yogapay.couriertsi.utils.ValidateUtil;
/**
 * 消息通知接口类
 * @author hhh
 *
 */
@Controller
@RequestMapping(value = "/msg",method=RequestMethod.POST)
@Scope("prototype")
public class MsgApi extends BaseApi{

	@Resource
	private MsgService msgService ;
	@Resource
	private MsgTypeService msgTypeService ;


	@RequestMapping(value = "/list")
	public void list(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(new String[] { "msgCode" }), true, userSessionService,checkVersion,appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {
				setPageInfo(params);
				params.put("userNo", ret.get("userNo")) ;
				params.put("userType", "2") ;
				Date nowDate = new Date() ;
				String beginTime = DateUtils.formatDate(DateUtils.addDate(nowDate, -365, 0, 0)) ;
				String endTime = DateUtils.formatDate(nowDate) ;
				if (!StringUtils.isEmptyWithTrim(params.get("beginTime"))) {
					beginTime = DateUtils.formatDate(params.get("beginTime"),null) ;
				}
				if (!StringUtils.isEmptyWithTrim(params.get("endTime"))) {
					endTime = DateUtils.formatDate(params.get("endTime"),null) ;
				}
				params.put("beginTime", beginTime) ;
				params.put("endTime", endTime) ;
				String msgCode = msgTypeService.getChildrenString(Integer.valueOf(params.get("msgCode"))) ;
				params.put("msgCode", msgCode) ;
				Page<MsgDto> msgList = msgService.list(params, pageRequest) ;
				model = new HashMap<String, Object>() ;
				model.put("msgList", msgList.getContent()) ;
				model.put("totalCount", msgList.getTotalElements()) ;
				model.put("cp", msgList.getNumber()+1) ;
				model.put("isLastPage", msgList.isLastPage()) ;
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


	//更新消息为已读
	@RequestMapping(value = "/read")
	public void read(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(new String[] { "msgId","type" }), true, userSessionService,checkVersion,appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {			

				if("ALL".equals(params.get("type"))){				
					String msgCode = msgTypeService.getChildrenString(Integer.valueOf(params.get("msgCode"))) ;
					msgService.readAll(ret.get("userNo"),msgCode) ;				
				}
				if("ONLY".equals(params.get("type"))){
					msgService.read(ret.get("userNo"),params.get("msgId")) ;
				}



				render(JSON_TYPE, CommonResponse.respSuccessJson("","更新成功", params.get("reqNo")), response);     
			} else {
				log.info("validate false!!!!");
				render(JSON_TYPE, CommonResponse.respFailJson(ret.get("respCode"), ret.get("respMsg"), params.get("reqNo")),response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			render(JSON_TYPE, CommonResponse.respFailJson("9000","服务器异常", params.get("reqNo")), response);
		}
	}


	@RequestMapping(value = "/del")
	public void del(@RequestParam Map<String, String> params,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> ret = ValidateUtil.validateRequest(request,reqParams(new String[] { "delAll","msgCode" }), true, userSessionService,checkVersion,appVersionService,dynamicDataSourceHolder);
			if ("TRUE".equals(ret.get("isSuccess"))) {	
				if ("1".equals(params.get("delAll"))) {
					String msgCode = msgTypeService.getChildrenString(Integer.valueOf(params.get("msgCode"))) ;
					msgService.delAll(ret.get("userNo"),msgCode) ;
				}else {
					if (StringUtils.isEmptyWithTrim(params.get("msgIds"))) {
						render(JSON_TYPE, CommonResponse.respFailJson("9001","缺少参数msgIds", params.get("reqNo")), response);
						return ;
					}
					String[] ids = params.get("msgIds").split(",") ;
					String idString = "0" ;
					for (int i = 0; i < ids.length; i++) {
						if (ids[i].length()>0) {
							idString+=","+ids[i] ;
						}
					}
					msgService.delById(ret.get("userNo"),idString) ;
				}
				render(JSON_TYPE, CommonResponse.respSuccessJson(""," 删除成功", params.get("reqNo")), response);     
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
