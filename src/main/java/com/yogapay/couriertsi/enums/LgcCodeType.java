package com.yogapay.couriertsi.enums;

public enum LgcCodeType {
	AUTO_ALL("AUTO_ALL"),
	AUTO_AREA("AUTO_AREA"),
	MANUAL_STATION("MANUAL_STATION"),
	MANUAL_PERSON("MANUAL_PERSON");
	
	private String value;
	
	private LgcCodeType(String value){
		this.value = value;
	}
	public String gerVaue(){
		return value;
	}
}
