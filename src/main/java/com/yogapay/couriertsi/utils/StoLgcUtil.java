package com.yogapay.couriertsi.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

public class StoLgcUtil implements LgcUtil {

	@Override
	public List<Map<String, String>> track(String orderNo) throws Exception {
			URL url=null;
			HtmlPage page1=null;
			WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24 );
			//webClient.setTimeout(timeout_ms);
			webClient.getOptions().setTimeout(6000);
			webClient.getOptions().setCssEnabled(false);
			webClient.getOptions().setJavaScriptEnabled(true);
			webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
			webClient.getOptions().setThrowExceptionOnScriptError(false);
			webClient.setAjaxController(new NicelyResynchronizingAjaxController());
			String newUrlNmae = "";
				long begintime=0;
				begintime=System.currentTimeMillis();
				newUrlNmae = "http://q.sto.cn/track.aspx?wen="+orderNo;
				url = new URL(newUrlNmae);
				page1 = (HtmlPage)webClient.getPage(url);
				//获取表单  
				final HtmlForm form = page1.getForms().get(0);
				//获取提交按扭
				final HtmlSubmitInput button = form.getInputByName("btnQuery");
				//一会得输入的
				//final HtmlTextInput textField = form.getInputByName("userid");
				//textField.setValueAttribute("test");
				//点击提交表单
				final HtmlPage page = button.click();
				 List<Map<String, String>> ret = new ArrayList<Map<String,String>>() ;
				if(page != null){
					 List<DomElement> domNodeList = page.getElementsByTagName("table");
					for(int k = 0; k < domNodeList.size() ; k++){
						if(domNodeList.get(k).getAttribute("class").equals("tab_result")){
							DomNodeList<HtmlElement> trs = domNodeList.get(k).getElementsByTagName("tr");
							for (int i = 0; i < trs.size(); i++) {
								if (i==0) {
									continue ;
								}
								DomNodeList<HtmlElement> td = trs.get(i).getElementsByTagName("td");
								if (td.size()>1) {
									Map<String, String> map = new HashMap<String, String>() ;
									map.put("date", td.get(0).getTextContent()) ;
									map.put("context", td.get(1).getTextContent()) ;
									ret.add(map) ;
								}
							}
							break;
						}
					}
					if (ret.size()<=0) {
						Map<String, String> map = new HashMap<String, String>() ;
						map.put("date", "") ;
						map.put("context", "无此物流单号信息！") ;
						ret.add(map) ;
					}
				}
				webClient.closeAllWindows();
				return ret ;
	 }

}
