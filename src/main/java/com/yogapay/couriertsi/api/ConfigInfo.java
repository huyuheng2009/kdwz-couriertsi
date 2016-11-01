package com.yogapay.couriertsi.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfigInfo {

	  
	  @Value("#{config['file_root']}")
	  private  String file_root ;
	  
	  @Value("#{config['yx_order_file']}")
	  private  String yx_order_file ;
	  
	  @Value("#{config['banner_file']}")
	  private  String banner_file ;
	  
	  @Value("#{config['push_host']}")
	  private  String push_host ;
	  
	  @Value("#{config['images']}")
	  private  String images ;
	  
	  @Value("#{config['install_file']}")
	  private  String install_file ;
	  
	  @Value("#{config['pos_host']}")
	  private  String pos_host ;
	  
	  @Value("#{config['wxpush_host']}")
	  private  String wxpush_host ;
	  
	  @Value("#{config['check_version']}")
	  private  String check_version ;
	  
	  @Value("#{config['message_host']}")
	  private  String message_host ;
	  
	  @Value("#{config['signature_images']}")
	  private  String signature_images ;
	  
	  @Value("#{config['salesSlip_images']}")
	  private  String salesSlip_images ;

	public String getFile_root() {
		return file_root;
	}

	public void setFile_root(String file_root) {
		this.file_root = file_root;
	}

	public String getYx_order_file() {
		return yx_order_file;
	}

	public void setYx_order_file(String yx_order_file) {
		this.yx_order_file = yx_order_file;
	}

	public String getBanner_file() {
		return banner_file;
	}

	public void setBanner_file(String banner_file) {
		this.banner_file = banner_file;
	}

	public String getPush_host() {
		return push_host;
	}

	public void setPush_host(String push_host) {
		this.push_host = push_host;
	}

	public String getImages() {
		return images;
	}

	public void setImages(String images) {
		this.images = images;
	}

	public String getInstall_file() {
		return install_file;
	}

	public void setInstall_file(String install_file) {
		this.install_file = install_file;
	}

	public String getPos_host() {
		return pos_host;
	}

	public void setPos_host(String pos_host) {
		this.pos_host = pos_host;
	}

	public String getWxpush_host() {
		return wxpush_host;
	}

	public void setWxpush_host(String wxpush_host) {
		this.wxpush_host = wxpush_host;
	}

	public String getCheck_version() {
		return check_version;
	}

	public void setCheck_version(String check_version) {
		this.check_version = check_version;
	}

	public String getMessage_host() {
		return message_host;
	}

	public void setMessage_host(String message_host) {
		this.message_host = message_host;
	}

	public String getSignature_images() {
		return signature_images;
	}

	public void setSignature_images(String signature_images) {
		this.signature_images = signature_images;
	}

	public String getSalesSlip_images() {
		return salesSlip_images;
	}

	public void setSalesSlip_images(String salesSlip_images) {
		this.salesSlip_images = salesSlip_images;
	}
	  
	  
	  
	  
}
