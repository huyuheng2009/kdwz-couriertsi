package com.yogapay.couriertsi.api;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yogapay.couriertsi.utils.ConstantsLoader;
import com.yogapay.couriertsi.utils.ResizeImage;



@Controller
public class FileApi extends BaseApi {
	
	@RequestMapping(value = "/order/images/*/*")
	public void  getImage(final ModelMap model,
			@RequestParam Map<String, String> params,HttpServletRequest request,HttpServletResponse response){
		 String path = request.getRequestURI().replace("//", "/").substring(14) ;
		 String userNo = path.substring(0,path.indexOf("/"));
		 String fileName = path.substring(path.indexOf("/")+1,path.lastIndexOf("."));
		 String suffix = path.substring(path.lastIndexOf(".")+1) ;
	   String mime = "image/jpeg" ;
	   if ("png".equals(suffix)) {
		mime = "image/png" ;
	   }
	   if ("gif".equals(suffix)) {
			mime = "image/gif" ;
		}
		response.setContentType(mime);//设置相应类型,告诉浏览器输出的内容为图片
        response.setHeader("Pragma", "No-cache");//设置响应头信息，告诉浏览器不要缓存此内容
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expire", 0);
        FileInputStream hFile = null ;
        OutputStream toClient = null ;
        boolean zoom = false ; 
        int width = 0 ;
        int height = 0 ;
        if (fileName.toLowerCase().endsWith("_small")) {
        	fileName = fileName.substring(0,fileName.lastIndexOf("_"))+"."+suffix ;
			zoom = true ;
			width = 300 ;
			height = 300 ;
		}else {
			fileName = fileName +"."+suffix ;
		}
        try{ 
        	ResizeImage resizeImage = new ResizeImage() ;
        	hFile = resizeImage.getImageFileInputStream(configInfo.getFile_root()
 		                .concat(configInfo.getImages()).concat("/"+userNo+"/"+fileName),fileName, zoom, width, height) ;
						int i=hFile.available(); //得到文件大小 
						byte data[]=new byte[i]; 
						hFile.read(data); //读数据 
						hFile.close(); 
						response.setContentType(mime); //设置返回的文件类型
						response.setContentLength(i);
					    toClient=response.getOutputStream(); //得到向客户端输出二进制数据的对象 
						toClient.write(data); //输出数据 
						toClient.close(); 
		} 
		catch(FileNotFoundException e) //错误处理 
		{   e.printStackTrace();
        }catch (IOException e) {
			
		} finally{
        	try {
        		hFile.close(); 
            	toClient.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
        	 
        }
        
	  }
	
	@RequestMapping(value = "/head/image/*/*")
	public void  getHeadImage(final ModelMap model,
			@RequestParam Map<String, String> params,HttpServletRequest request,HttpServletResponse response){
		 String path = request.getRequestURI().replace("//", "/").substring(12) ;
		 String userNo = path.substring(0,path.indexOf("/"));
		 String fileName = path.substring(path.indexOf("/")+1,path.lastIndexOf("."));
		 String suffix = path.substring(path.lastIndexOf(".")+1) ;
	   String mime = "image/jpeg" ;
	   if ("png".equals(suffix)) {
		mime = "image/png" ;
	   }
	   if ("gif".equals(suffix)) {
			mime = "image/gif" ;
		}
		response.setContentType(mime);//设置相应类型,告诉浏览器输出的内容为图片
        response.setHeader("Pragma", "No-cache");//设置响应头信息，告诉浏览器不要缓存此内容
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expire", 0);
        FileInputStream hFile = null ;
        OutputStream toClient = null ;
        boolean zoom = false ; 
        int width = 0 ;
        int height = 0 ;
        if (fileName.toLowerCase().endsWith("_small")) {
        	fileName = fileName.substring(0,fileName.lastIndexOf("_"))+"."+suffix ;
			zoom = true ;
			width = 600 ;
			height = 600 ;
		}else {
			fileName = fileName +"."+suffix ;
		}
        try{ 
        	ResizeImage resizeImage = new ResizeImage() ;
        	hFile = resizeImage.getImageFileInputStream(getRealPath(request)+"/images/users/"+userNo+"/"+fileName,fileName, zoom, width, height) ;
						int i=hFile.available(); //得到文件大小 
						byte data[]=new byte[i]; 
						hFile.read(data); //读数据 
						hFile.close(); 
						response.setContentType(mime); //设置返回的文件类型
						response.setContentLength(i);
					    toClient=response.getOutputStream(); //得到向客户端输出二进制数据的对象 
						toClient.write(data); //输出数据 
						toClient.close(); 
		} 
		catch(FileNotFoundException e) //错误处理 
		{   e.printStackTrace();
        }catch (IOException e) {
			//e.printStackTrace();
		} finally{
        	try {
        		hFile.close(); 
            	toClient.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
        	 
        }
        
	  }
	
	
	@RequestMapping(value = "/mobile/install_file")
	public void  install(final ModelMap model,
			@RequestParam Map<String, String> params,HttpServletRequest request,HttpServletResponse response)  {
	   String fileName = params.get("fileName") ;
		
	   FileInputStream hFile = null ;
       OutputStream toClient = null ;
	   try{ 
       response.setContentType("application/octet-stream");   
   /*    response.setHeader("Content-disposition", "attachment; filename="  
               + new String(fileName.getBytes("utf-8"), "ISO8859-1"));  */
       response.setHeader("Content-disposition", "attachment; filename="+fileName);  
        response.setHeader("Pragma", "No-cache");//设置响应头信息，告诉浏览器不要缓存此内容
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expire", 0);
     
			hFile = new FileInputStream(configInfo.getInstall_file().concat("/"+fileName)); // 以byte流的方式打开文件 d:\1.gif 
						int i=hFile.available(); //得到文件大小
						response.setContentLength(i);
						byte data[]=new byte[i]; 
						hFile.read(data); //读数据 
						hFile.close(); 
					    toClient=response.getOutputStream(); //得到向客户端输出二进制数据的对象 
						toClient.write(data); //输出数据 
						toClient.close(); 
						} 
		catch(FileNotFoundException e) //错误处理 
		{   e.printStackTrace();
        }catch (IOException e) {
			
		} finally{
        	try {
        		hFile.close(); 
            	toClient.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
        	 
        }
        
	  }
	

}
