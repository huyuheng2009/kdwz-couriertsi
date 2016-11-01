package com.yogapay.couriertsi.enums;


public enum ValidateCodeType {
	
	REGIST(11),
	CHANGE_PASSWORD(12),//改密码
	CHANGE_PHONE(13); //修改手机
	
	private  int value ;
    //构造器默认也只能是private, 从而保证构造函数只能在内部使用
	ValidateCodeType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
	
}
