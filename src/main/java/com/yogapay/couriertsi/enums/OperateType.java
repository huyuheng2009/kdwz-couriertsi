package com.yogapay.couriertsi.enums;

public enum OperateType {

	REGIST("注册"), //注册
	CHANGE_PASSWORD("改密码"),//改密码
	CHANGE_PHONE("修改手机号"); //修改手机
	
	private  String value ;
    //构造器默认也只能是private, 从而保证构造函数只能在内部使用
	OperateType(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
	
	
}
