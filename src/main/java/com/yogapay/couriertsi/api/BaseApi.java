package com.yogapay.couriertsi.api;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

import com.yogapay.couriertsi.dataSource.DynamicDataSourceHolder;
import com.yogapay.couriertsi.enums.RateType;
import com.yogapay.couriertsi.exception.FileUnknowTypeException;
import com.yogapay.couriertsi.services.AppVersionService;
import com.yogapay.couriertsi.services.SequenceService;
import com.yogapay.couriertsi.services.UserSessionService;
import com.yogapay.couriertsi.utils.ConstantsLoader;
import com.yogapay.couriertsi.utils.RequestFile;
import com.yogapay.couriertsi.utils.SendMail;
import com.yogapay.couriertsi.utils.StringUtils;
/**
 * 
 * @author hhh
 *
 */
public class BaseApi {

	// header 常量定义
	private static final String DEFAULT_ENCODING = "UTF-8";
	private static final boolean DEFAULT_NOCACHE = true;
	// Content Type 常量定义
	public static final String TEXT_TYPE = "text/plain";
	public static final String JSON_TYPE = "application/json";
	public static final String XML_TYPE = "text/xml";
	public static final String HTML_TYPE = "text/html";
	public static final String JS_TYPE = "text/javascript";
	public static final String EXCEL_TYPE = "application/vnd.ms-excel";
	public static int pageSize = 10;
	public static int pageNo = 1;
	public PageRequest pageRequest ;
	public Page<Map<String, Object>> pageList = null;
	static final Logger log = LoggerFactory.getLogger(BaseApi.class);
	@Resource public SequenceService sequenceService ;
	@Resource public UserSessionService userSessionService ;
	@Resource public AppVersionService appVersionService ;
	@Resource public DynamicDataSourceHolder dynamicDataSourceHolder ;
	public Map<String, Object> model = new HashMap<String, Object>() ;
	SendMail sendMail = new SendMail();
	@Value("#{config['check_version']}")
	public boolean checkVersion ;
    @Autowired
    public ConfigInfo configInfo ;
	
	
	private final String [] baseReqParams = {"reqTime","reqNo","appVersion"};

	public PageRequest getPageRequest(int cpage) {
		return new PageRequest(cpage - 1, pageSize);
	}
	
	public PageRequest getPageRequest(int cpage,int size) {
		if(size==0) size = pageSize;
		return new PageRequest(cpage - 1, size);
	}

	/**
	 * 直接输出内容的简便函数.
	 */
	protected void render(final String contentType, final String content,
			final HttpServletResponse response) {
		HttpServletResponse resp = initResponseHeader(contentType, response);
		try {
			resp.getWriter().write(content);
			resp.getWriter().flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 直接输出文本.
	 */
	protected void outText(final String text, final HttpServletResponse response) {
		render(TEXT_TYPE, text, response);
	}

	/**
	 * 直接输出HTML.
	 */
	protected void outHtml(final String html, final HttpServletResponse response) {
		render(HTML_TYPE, html, response);
	}

	/**
	 * 直接输出XML.
	 */
	protected void outXml(final String xml, final HttpServletResponse response) {
		render(XML_TYPE, xml, response);
	}

	/**
	 * 输出JSON,可以接收参数如： [{'name':'www'},{'name':'www'}],['a','b'],new
	 * String[]{'a','b'},合并如下：jsonString = "{TOTALCOUNT:" + totalCount + ",
	 * ROOT:" + jsonString + "}";
	 * 
	 * @param jsonString
	 *            json字符串.
	 * 
	 */
	protected void outJson(final String json, final HttpServletResponse response) {
		render(JSON_TYPE, json, response);
	}

	/**
	 * 设置让浏览器弹出下载对话框的Header.
	 * 
	 * @param fileName
	 *            下载后的文件名.
	 */
	protected void setFileDownloadHeader(HttpServletResponse response,
			String fileName) {
		try {
			// 中文文件名支持
			String encodedfileName = new String(fileName.getBytes(),
					"ISO8859-1");
			response.setHeader("Content-Disposition", "attachment; filename=\""
					+ encodedfileName + "\"");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 分析并设置contentType与headers.
	 */
	protected HttpServletResponse initResponseHeader(final String contentType,
			final HttpServletResponse response) {
		// 分析headers参数
		String encoding = DEFAULT_ENCODING;
		boolean noCache = DEFAULT_NOCACHE;
		// 设置headers参数
		String fullContentType = contentType + ";charset=" + encoding;
		response.setContentType(fullContentType);
		if (noCache) {
			setNoCacheHeader(response);
		}

		return response;
	}


	/**
	 * 设置客户端无缓存Header.
	 */
	protected void setNoCacheHeader(HttpServletResponse response) {
		// Http 1.0 header
		response.setDateHeader("Expires", 0);
		response.addHeader("Pragma", "no-cache");
		// Http 1.1 header
		response.setHeader("Cache-Control", "no-cache");
	}
	
	/**
	 * 获取参数值
	 */
	protected String getParameter(HttpServletRequest request,String name) {
		String paramString = request.getParameter(name);
		if (StringUtils.isEmptyWithTrim(paramString)) {
			paramString = "" ;
		}
		return paramString ;
	}
	

    
	/**
	 * 
	 * @param request   
	 * @param fileName   限定文件的字段名称
	 * @param rootPath   文件保存的根目录
	 * @param contextPath  文件保存的目录
	 * @param mime  限定文件的mime类型集合
	 * @return
	 * @throws Exception
	 */
	protected List<RequestFile> getFile(HttpServletRequest request,String fileName,String rootPath,String contextPath,List<String> mime) throws Exception{
		
		 RequestFile requestFile = null ;
		 List<RequestFile> requestFiles = new ArrayList<RequestFile>() ;
		 MultipartRequest multipartRequest ;
		 try {
		  multipartRequest = (MultipartRequest) request ;
		} catch (Exception e) {
		//	e.printStackTrace();
          return requestFiles ;
		}
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
     			{
     				if (StringUtils.isEmptyWithTrim(fileName)) {
     					for(String name:fileMap.keySet()){
     						if (fileMap.get(name).isEmpty()) {
								continue ;
							}else {
								boolean flag = false ;
								for (String type:mime) {
									if (type.equals(fileMap.get(name).getContentType())) {
										System.out.println(fileMap.get(name).getContentType()+"==========================================");
										flag = true ;
										break ;
									}
								}
								if (mime==null||mime.size()<=0) {
									flag = true ;
								}
								if (!flag) {
									throw new FileUnknowTypeException() ;
								}
     								requestFile = new RequestFile() ;
     								requestFile.setName(name);
     		     					String prefix = fileMap.get(name).getOriginalFilename().substring(fileMap.get(name).getOriginalFilename()
     		     							.indexOf("."));
     		     					requestFile.setFileName(System.currentTimeMillis()+StringUtils.getRandomString(6, true)+prefix);
     		     					requestFile.setOrginalName(fileMap.get(name).getOriginalFilename());
     		     					requestFile.setFileSize(fileMap.get(name).getSize()+"");
     		     					requestFile.setFileType(fileMap.get(name).getContentType());
     		     					requestFile.setFilePath(contextPath.concat("/").concat(requestFile.getFileName()));
     		     					File dir = new File(rootPath.concat("/").concat(contextPath));
     		     					if(!dir.isDirectory()){
     		     						dir.mkdirs();
     		     					}
     								FileCopyUtils.copy(fileMap.get(name).getBytes(),new File(rootPath.concat("/").concat(requestFile.getFilePath())));
     								requestFiles.add(requestFile) ;
							    }	
     	     				}//for
					}else {
						for(String name:fileMap.keySet()){
		     				  if (!name.equals(fileName)||fileMap.get(name).isEmpty()) {
									continue ;
								}else {
									boolean flag = false ;
									for (String type:mime) {
										if (type.equals(fileMap.get(name).getContentType())) {
											System.out.println(fileMap.get(name).getContentType()+"222222222222222222222222222222222");
											flag = true ;
											break ;
										}
									}
									if (mime==null||mime.size()<=0) {
										flag = true ;
									}
									if (!flag) {
										throw new FileUnknowTypeException() ;
									}
									requestFile = new RequestFile() ;
									requestFile.setName(name);
			     					String prefix = fileMap.get(name).getOriginalFilename().substring(fileMap.get(name).getOriginalFilename()
			     							.indexOf("."));
			     					requestFile.setFileName(System.currentTimeMillis()+prefix);
			     					requestFile.setOrginalName(fileMap.get(name).getOriginalFilename());
			     					requestFile.setFileSize(fileMap.get(name).getSize()+"");
			     					requestFile.setFileType(fileMap.get(name).getContentType());
			     					requestFile.setFilePath(contextPath.concat("/").concat(requestFile.getFileName()));
     		     					File dir = new File(rootPath.concat(contextPath));
			     					if(!dir.isDirectory()){
			     						dir.mkdirs();
			     					}
			     					FileCopyUtils.copy(fileMap.get(name).getBytes(),new File(rootPath.concat("/").concat(requestFile.getFilePath())));
									requestFiles.add(requestFile) ;
									break ;
								}
		     				}//for
					}
     		
     			}
		return requestFiles ;
	}
	
	
	/**
	 * 
	 * @param request   
	 * @param filePrefix    文件名前缀 防止重复
	 * @param fileName   限定文件的字段名称
	 * @param rootPath   文件保存的根目录
	 * @param contextPath  文件保存的目录
	 * @param mime  限定文件的mime类型集合
	 * @return
	 * @throws Exception
	 */
	protected List<RequestFile> getFile(String filePrefix,HttpServletRequest request,String fileName,String rootPath,String contextPath,List<String> mime) throws Exception{
		
		RequestFile requestFile = null ;
		List<RequestFile> requestFiles = new ArrayList<RequestFile>() ;
		MultipartRequest multipartRequest ;
		try {
			multipartRequest = (MultipartRequest) request ;
		} catch (Exception e) {
			//	e.printStackTrace();
			return requestFiles ;
		}
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		{
			if (StringUtils.isEmptyWithTrim(fileName)) {
				for(String name:fileMap.keySet()){
					if (fileMap.get(name).isEmpty()) {
						continue ;
					}else {
						boolean flag = false ;
						for (String type:mime) {
							if (type.equals(fileMap.get(name).getContentType())) {
								flag = true ;
								break ;
							}
						}
						if (mime==null||mime.size()<=0) {
							flag = true ;
						}
						if (!flag) {
							throw new FileUnknowTypeException() ;
						}
						requestFile = new RequestFile() ;
						requestFile.setName(name);
						String prefix = fileMap.get(name).getOriginalFilename().substring(fileMap.get(name).getOriginalFilename()
								.indexOf("."));
						requestFile.setFileName( filePrefix +System.currentTimeMillis()+StringUtils.getRandomString(6, true)+prefix);
						requestFile.setOrginalName(fileMap.get(name).getOriginalFilename());
						requestFile.setFileSize(fileMap.get(name).getSize()+"");
						requestFile.setFileType(fileMap.get(name).getContentType());
						requestFile.setFilePath(contextPath.concat("/").concat(requestFile.getFileName()));
						File dir = new File(rootPath.concat("/").concat(contextPath));
						if(!dir.isDirectory()){
							dir.mkdirs();
						}
						FileCopyUtils.copy(fileMap.get(name).getBytes(),new File(rootPath.concat("/").concat(requestFile.getFilePath())));
						requestFiles.add(requestFile) ;
					}	
				}//for
			}else {
				for(String name:fileMap.keySet()){
					System.out.println("name==========="+name);
					if (!name.equals(fileName)||fileMap.get(name).isEmpty()) {
						continue ;
					}else {
						boolean flag = false ;
						for (String type:mime) {
							if (type.equals(fileMap.get(name).getContentType())) {
								flag = true ;
								break ;
							}
						}
						if (mime==null||mime.size()<=0) {
							flag = true ;
						}
						if (!flag) {
							throw new FileUnknowTypeException() ;
						}
						requestFile = new RequestFile() ;
						requestFile.setName(name);
						String prefix = fileMap.get(name).getOriginalFilename().substring(fileMap.get(name).getOriginalFilename()
								.indexOf("."));
						requestFile.setFileName(filePrefix +System.currentTimeMillis()+prefix);
						requestFile.setOrginalName(fileMap.get(name).getOriginalFilename());
						requestFile.setFileSize(fileMap.get(name).getSize()+"");
						requestFile.setFileType(fileMap.get(name).getContentType());
						requestFile.setFilePath(contextPath.concat("/").concat(requestFile.getFileName()));
						File dir = new File(rootPath.concat(contextPath));
						if(!dir.isDirectory()){
							dir.mkdirs();
						}
						FileCopyUtils.copy(fileMap.get(name).getBytes(),new File(rootPath.concat("/").concat(requestFile.getFilePath())));
						requestFiles.add(requestFile) ;
						break ;
					}
				}//for
			}
			
		}
		return requestFiles ;
	}
	
	@SuppressWarnings("static-access")
	public void setPageInfo(Map<String, String> params) {
         if (!StringUtils.isEmptyWithTrim(params.get("pageNo"))) {
			this.pageNo = Integer.parseInt(params.get("pageNo")) ;
			if (this.pageNo<=0) {
				this.pageNo = 1 ;
			}
		 }else {
			 this.pageNo = 1 ;
		 }
         if (!StringUtils.isEmptyWithTrim(params.get("pageSize"))) {
        	 this.pageSize = Integer.parseInt(params.get("pageSize")) ;
        	 if (this.pageNo<=0) {
 				this.pageNo = 10 ;
 			}
		 }else {
			 this.pageSize = 10 ;
		}
         pageRequest = getPageRequest(pageNo, pageSize) ;
	}

	
	public String[] reqParams(String[] req) {
		if (req==null||req.length<1) {
			return baseReqParams ;
		}
        String[] ret = new String[baseReqParams.length + req.length];   
        System.arraycopy(baseReqParams, 0, ret, 0, baseReqParams.length); 
		System.arraycopy(req, 0, ret, baseReqParams.length, req.length);  
		return ret;
	}
	
	public  String getClientIP(HttpServletRequest request) {
		  String ip = request.getHeader("x-forwarded-for");
	        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	         ip = request.getHeader("Proxy-Client-IP");
	        }
	        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	         ip = request.getHeader("WL-Proxy-Client-IP");
	        }
	        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	         ip = request.getRemoteAddr();
	        }
	        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	         ip = request.getHeader("http_client_ip");
	        }
	        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	         ip = request.getHeader("HTTP_X_FORWARDED_FOR");
	        }
	        // 如果是多级代理，那么取第一个ip为客户ip
	        if (ip != null && ip.indexOf(",") != -1) {
	         ip = ip.substring(ip.lastIndexOf(",") + 1, ip.length()).trim();
	        }
	        if ("0:0:0:0:0:0:0:1".equals(ip)) {
			  ip = "127.0.0.1" ;
			}
	        return ip ;
	}
	
	public String getRealPath(HttpServletRequest request) {
		return request.getSession().getServletContext().getRealPath("/");
	}
	
	/**
	 * 根据手续费规则获取相应手续费
	 * @return
	 */
	public float getPayByRate(float value,Map<String, Object> rateMap) {
		String rateType = rateMap.get("rateType").toString() ;//费率类型
		float rate = Float.parseFloat(rateMap.get("rate").toString()) ;// 费率
		float minv = Float.parseFloat(rateMap.get("minv").toString()) ;//收取起点
		float maxv = Float.parseFloat(rateMap.get("maxv").toString()) ;//封顶费用
		float ret = 0 ;
		if (RateType.LR.toString().equals(rateType)) {
			if (value>=minv) {
				ret = value * rate ;
			}
		}else if (RateType.LTR.toString().equals(rateType)) {
			if (value>=minv) {
				ret = value * rate ;
			}
			if (ret>maxv) {
				ret = maxv ;
			}
		}
		return ret/1000f ;
	}
	public static String buildUrl(String wxxd_url,String lgcNo,String uid,String orderNo,String lgcOrderNo) {
		//return "http://gdt.pro-wxxd.yogapay.com/page/queryOrderList?lgcNo="+lgcNo+"&uid="+uid+"&orderNo="+orderNo ;
		return wxxd_url+"?lgcNo="+lgcNo+"&uid="+uid+"&goCall=query&orderNo="+orderNo+"&lgcOrderNo="+lgcOrderNo+"#!/page/query" ;
	}
	public  String buildWXUrl(String wxxd_url,String lgcNo,String uid,String orderNo,String ctype) {
		return wxxd_url+"?lgcNo="+lgcNo+"&uid="+uid+"&goCall=evaluate&ctype="+ctype+"&orderNo="+orderNo+"#!/page/evaluate" ;
	}
	
}
