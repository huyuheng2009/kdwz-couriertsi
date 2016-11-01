package com.yogapay.couriertsi.dto;

import java.util.Date;

public class OrderDto {
	private String id;
	private String orderNo ;
	private String sendArea ;
	private String sendAddr ;
	private String sendName ;
	private String sendPhone ;

	private String[] sendPhones ;
	private String sendLocation ;
	private String revArea ;
	private String revAddr ;
	private String revName ;
	private String revPhone ;
	private String[] revPhones ;
	private String revLocation ;
	private int itemType ;
	private String itemName ;
	private float itemWeight ;
	private int freightType ;
	private float freight ;         //邮费
	private String monthSettleName ;
	private String monthSettleNo ;
	private String monthSettleCard ;
	private int cod ;
	private float goodPrice ;
	private String codCardNo ;
	private String codName ;
	private String codCardCnapsNo ;
	private String codBank ;
	private float goodValuation ;   
	private String goodValuationRate ;
	private float payAcount ;         //支付总额
	private String payType ;        //邮费支付方式
	private String cpayType ;      //货物支付方式
	private String takeTime ;
	private Date takeTimeBegin ;
	private String takeAddr ;
	private String orderNote ;
	private int status ;
	private String sign ;
	private Date createTime ;
	private String lgcOrderNo ;
	private float npay ;    //此次应付金额
	private float vpay ;    //保价时的手续费
	private int posCount ; //当前刷卡次数
	private boolean task ; //是否可接
	private String source ; //订单来源
	private float cpay ;   //代收货款手续费
	private float dpay ;   //二次投递费用
	private String takeCourierNo;
	private String takeStatus;
	private String itemStatus;
	private String sendCourierNo;
	private Date takeOrderTime;
	private Date sendOrderTime;
	private String revLongitude;
	private String revLatitude;
	private String sendLongitude;
	private String sendLatitude;
	private String  fpayStatus;
	private String  cpayStatus;
	private String proOrder;//问题件标识
	private String idCard;
	private float disNpay;
	private String disUserNo;//支付会员号
	private String monthDiscount;//订单月结折扣，75,为7.5折
	private String codRate;
	private float disRealityFreight;
	private Object codSname;
	private String receNo;
	private String reqRece;
	private String zidanNumber;
	private String zidanOrder;
	private String proInfo;
	private String signName;
	private String forNo;
	private String zid;
	private String timeType;
	private String itemCount;
	private String message;
	
	
	
	public String getItemCount() {
		return itemCount;
	}
	public void setItemCount(String itemCount) {
		this.itemCount = itemCount;
	}
	public String getTimeType() {
		return timeType;
	}
	public void setTimeType(String timeType) {
		this.timeType = timeType;
	}
	public String getForNo() {
		return forNo;
	}
	public void setForNo(String forNo) {
		this.forNo = forNo;
	}
	public String getSignName() {
		return signName;
	}
	public void setSignName(String signName) {
		this.signName = signName;
	}
	public String getProInfo() {
		return proInfo;
	}
	public void setProInfo(String proInfo) {
		this.proInfo = proInfo;
	}
	public String getReceNo() {
		return receNo;
	}
	public void setReceNo(String receNo) {
		this.receNo = receNo;
	}
	public String getReqRece() {
		return reqRece;
	}
	public void setReqRece(String reqRece) {
		this.reqRece = reqRece;
	}
	public String getZidanNumber() {
		return zidanNumber;
	}
	public void setZidanNumber(String zidanNumber) {
		this.zidanNumber = zidanNumber;
	}
	public String getZidanOrder() {
		return zidanOrder;
	}
	public void setZidanOrder(String zidanOrder) {
		this.zidanOrder = zidanOrder;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Object getCodSname() {
		return codSname;
	}
	public void setCodSname(Object codSname) {
		this.codSname = codSname;
	}
	private String disDiscount;
	
	public String getDisUserNo() {
		return disUserNo;
	}
	public void setDisUserNo(String disUserNo) {
		this.disUserNo = disUserNo;
	}
	public String getMonthDiscount() {
		return monthDiscount;
	}
	public void setMonthDiscount(String monthDiscount) {
		this.monthDiscount = monthDiscount;
	}
	public String getCodRate() {
		return codRate;
	}
	public void setCodRate(String codRate) {
		this.codRate = codRate;
	}
	public float getDisRealityFreight() {
		return disRealityFreight;
	}
	public void setDisRealityFreight(float disRealityFreight) {
		this.disRealityFreight = disRealityFreight;
	}
	public String getDisDiscount() {
		return disDiscount;
	}
	public void setDisDiscount(String disDiscount) {
		this.disDiscount = disDiscount;
	}
	public float getDisNpay() {
		return disNpay;
	}
	public void setDisNpay(float disNpay) {
		this.disNpay = disNpay;
	}
	public  String getIdCard() {
		return idCard;
	}
	public  void setIdCard(String idCard) {
		this.idCard = idCard;
	}
	public String getProOrder() {
		return proOrder;
	}
	public void setProOrder(String proOrder) {
		this.proOrder = proOrder;
	}

	public String getFpayStatus() {
		return fpayStatus;
	}
	public void setFpayStatus(String fpayStatus) {
		this.fpayStatus = fpayStatus;
	}
	public String getCpayStatus() {
		return cpayStatus;
	}
	public void setCpayStatus(String cpayStatus) {
		this.cpayStatus = cpayStatus;
	}
	public String getRevLongitude() {
		return revLongitude;
	}
	public void setRevLongitude(String revLongitude) {
		this.revLongitude = revLongitude;
	}
	public String getRevLatitude() {
		return revLatitude;
	}
	public void setRevLatitude(String revLatitude) {
		this.revLatitude = revLatitude;
	}
	public String getSendLongitude() {
		return sendLongitude;
	}
	public void setSendLongitude(String sendLongitude) {
		this.sendLongitude = sendLongitude;
	}
	public String getSendLatitude() {
		return sendLatitude;
	}
	public void setSendLatitude(String sendLatitude) {
		this.sendLatitude = sendLatitude;
	}

	public Date getTakeOrderTime() {
		return takeOrderTime;
	}
	public void setTakeOrderTime(Date takeOrderTime) {
		this.takeOrderTime = takeOrderTime;
	}
	public Date getSendOrderTime() {
		return sendOrderTime;
	}
	public void setSendOrderTime(Date sendOrderTime) {
		this.sendOrderTime = sendOrderTime;
	}
	public String getSendCourierNo() {
		return sendCourierNo;
	}
	public void setSendCourierNo(String sendCourierNo) {
		this.sendCourierNo = sendCourierNo;
	}
	public String getSendPhone() {
		return sendPhone;
	}
	public String getRevPhone() {
		return revPhone;
	}
	public String getItemStatus() {
		return itemStatus;
	}
	public void setItemStatus(String itemStatus) {
		this.itemStatus = itemStatus;
	}
	public String getTakeStatus() {
		return takeStatus;
	}
	public void setTakeStatus(String takeStatus) {
		this.takeStatus = takeStatus;
	}
	public String getTakeCourierNo() {
		return takeCourierNo;
	}
	public void setTakeCourierNo(String takeCourierNo) {
		this.takeCourierNo = takeCourierNo;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getSendArea() {
		return sendArea;
	}
	public void setSendArea(String sendArea) {
		this.sendArea = sendArea;
	}
	public String getSendAddr() {
		return sendAddr;
	}
	public void setSendAddr(String sendAddr) {
		this.sendAddr = sendAddr;
	}
	public String getSendName() {
		return sendName;
	}
	public void setSendName(String sendName) {
		this.sendName = sendName;
	}
	
	public void setSendPhone(String sendPhone) {
		this.sendPhone = sendPhone;
		if (sendPhone!=null) {
			this.sendPhones = sendPhone.split(",") ;
		}
	}
	
	public String[] getSendPhones() {
		return sendPhones;
	}
	public void setSendPhones(String[] sendPhones) {
		this.sendPhones = sendPhones;
	}
	public String getSendLocation() {
		return sendLocation;
	}
	public void setSendLocation(String sendLocation) {
		this.sendLocation = sendLocation;
	}
	public String getRevArea() {
		return revArea;
	}
	public void setRevArea(String revArea) {
		this.revArea = revArea;
	}
	public String getRevAddr() {
		return revAddr;
	}
	public void setRevAddr(String revAddr) {
		this.revAddr = revAddr;
	}
	public String getRevName() {
		return revName;
	}
	public void setRevName(String revName) {
		this.revName = revName;
	}
	public void setRevPhone(String revPhone) {
		this.revPhone = revPhone;
		if (revPhone!=null) {
			this.revPhones = revPhone.split(",") ;
		}
	}
	public String[] getRevPhones() {
		return revPhones;
	}
	public void setRevPhones(String[] revPhones) {
		this.revPhones = revPhones;
	}
	public String getRevLocation() {
		return revLocation;
	}
	public void setRevLocation(String revLocation) {
		this.revLocation = revLocation;
	}
	public int getItemType() {
		return itemType;
	}
	public void setItemType(int itemType) {
		this.itemType = itemType;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public float getItemWeight() {
		return itemWeight;
	}
	public void setItemWeight(float itemWeight) {
		this.itemWeight = itemWeight;
	}
	public int getFreightType() {
		return freightType;
	}
	public void setFreightType(int freightType) {
		this.freightType = freightType;
	}
	public float getFreight() {
		return freight;
	}
	public void setFreight(float freight) {
		this.freight = freight;
	}
	public String getMonthSettleName() {
		return monthSettleName;
	}
	public void setMonthSettleName(String monthSettleName) {
		this.monthSettleName = monthSettleName;
	}
	public String getMonthSettleNo() {
		return monthSettleNo;
	}
	public void setMonthSettleNo(String monthSettleNo) {
		this.monthSettleNo = monthSettleNo;
	}
	public String getMonthSettleCard() {
		return monthSettleCard;
	}
	public void setMonthSettleCard(String monthSettleCard) {
		this.monthSettleCard = monthSettleCard;
	}
	public int getCod() {
		return cod;
	}
	public void setCod(int cod) {
		this.cod = cod;
	}
	public float getGoodPrice() {
		return goodPrice;
	}
	public void setGoodPrice(float goodPrice) {
		this.goodPrice = goodPrice;
	}
	public String getCodCardNo() {
		return codCardNo;
	}
	public void setCodCardNo(String codCardNo) {
		this.codCardNo = codCardNo;
	}
	public String getCodName() {
		return codName;
	}
	public void setCodName(String codName) {
		this.codName = codName;
	}
	public String getCodCardCnapsNo() {
		return codCardCnapsNo;
	}
	public void setCodCardCnapsNo(String codCardCnapsNo) {
		this.codCardCnapsNo = codCardCnapsNo;
	}
	public String getCodBank() {
		return codBank;
	}
	public void setCodBank(String codBank) {
		this.codBank = codBank;
	}
	public float getGoodValuation() {
		return goodValuation;
	}
	public void setGoodValuation(float goodValuation) {
		this.goodValuation = goodValuation;
	}
	public String getGoodValuationRate() {
		return goodValuationRate;
	}
	public void setGoodValuationRate(String goodValuationRate) {
		this.goodValuationRate = goodValuationRate;
	}
	public float getPayAcount() {
		return payAcount;
	}
	public void setPayAcount(float payAcount) {
		this.payAcount = payAcount;
	}
	public String getPayType() {
		return payType;
	}
	public void setPayType(String payType) {
		this.payType = payType;
	}
	public String getTakeTime() {
		return takeTime;
	}
	public void setTakeTime(String takeTime) {
		this.takeTime = takeTime;
	}
	public Date getTakeTimeBegin() {
		return takeTimeBegin;
	}
	public void setTakeTimeBegin(Date takeTimeBegin) {
		this.takeTimeBegin = takeTimeBegin;
	}
	public String getTakeAddr() {
		return takeAddr;
	}
	public void setTakeAddr(String takeAddr) {
		this.takeAddr = takeAddr;
	}
	public String getOrderNote() {
		return orderNote;
	}
	public void setOrderNote(String orderNote) {
		this.orderNote = orderNote;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public String getLgcOrderNo() {
		return lgcOrderNo;
	}
	public void setLgcOrderNo(String lgcOrderNo) {
		this.lgcOrderNo = lgcOrderNo;
	}
	public String getCpayType() {
		return cpayType;
	}
	public void setCpayType(String cpayType) {
		this.cpayType = cpayType;
	}
	public float getNpay() {
		return npay;
	}
	public void setNpay(float npay) {
		this.npay = npay;
	}
	public float getVpay() {
		return vpay;
	}
	public void setVpay(float vpay) {
		this.vpay = vpay;
	}
	public int getPosCount() {
		return posCount;
	}
	public void setPosCount(int posCount) {
		this.posCount = posCount;
	}
	public boolean isTask() {
		return task;
	}
	public void setTask(boolean task) {
		this.task = task;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}

	public float getCpay() {
		return cpay;
	}
	public void setCpay(float cpay) {
		this.cpay = cpay;
	}
	public float getDpay() {
		return dpay;
	}
	public void setDpay(float dpay) {
		this.dpay = dpay;
	}
	public String getZid() {
		return zid;
	}
	public void setZid(String zid) {
		this.zid = zid;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	
	
}
