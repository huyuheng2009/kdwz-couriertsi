package com.yogapay.couriertsi.enums;

public enum MsgType {

    LGC(500),      //快件消息
    OTHER(501),   //其他消息
    COME(502),  //上门取件
    TAKE(503),  //快件取走
    SEND(504),  //快件派送
    SIGN(505),  //快件签收
    REFUSE(506),  //快件拒签
    RETAKE(507),  //快件拒收
    OTTAKE(511),  //快件被别的快递员取走
    ASIGNTS(513),    //快件新单，可接单
    ACANCEL(514),    //取消当前分配
    ASIGNED(515);   //订单已被接下，除了接单快递员其他需从列表删除订单
    
	
	private  int value ;
    //构造器默认也只能是private, 从而保证构造函数只能在内部使用
	MsgType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
	
}
