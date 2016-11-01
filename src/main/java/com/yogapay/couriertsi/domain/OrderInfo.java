package com.yogapay.couriertsi.domain;

import com.yogapay.couriertsi.utils.Md5;

public class OrderInfo extends ToString implements Cloneable {

	private int id;
	private String orderNo;
	private String userNo;
	private String subStationNo;
	private String takeCourierNo;
	private String sendCourierNo;
	private String sendArea;
	private String sendAddr;
	private String sendName;
	private String sendPhone;
	private String sendLocation;
	private String revArea;
	private String revAddr;
	private String revName;
	private String revPhone;
	private String revLocation;
	private String itemType;
	private String itemName;
	private float itemWeight;
	private String itemVolume;
	private String freightType;
	private float freight;
	private String monthSettleName;
	private String monthSettleNo;
	private String monthSettleCard;
	private int cod;
	private float goodPrice;
	private String codCardNo;
	private String codName;
	private String codCardCnapsNo;
	private String codBank;
	private float goodValuation;
	private String goodValuationRate;
	private String takeTime;
	private String takeTimeBegin;
	private String takeTimeEnd;
	private String takeAddr;
	private String takeLocation;
	private float payAcount;
	private String payType;
	private String cpayType;
	private String payStatus;
	private String orderNote;
	private String createTime;
	private String lastUpdateTime;
	private String completeTime;
	private String lgcNo;
	private String lgcOrderNo;
	private int status;
	private String sign;
	private String kstatus;
	private String source;
	private String lgcName;
	private float cpay;
	private float vpay;
	private float dpay;
	private String signType;
	private String signature;
	private float tnpay;
	private float snapy;
	private String sendSubstationNo;
	private float mpay;//需月结费用
	private String itemStatus;//订单类型 如鲜花 ，牛粪
	private String takeOrderTime;//具体取件时间
	private String takeMark;//派件或者取件标志
	private String sendOrderTime;//集体派件时间
	private String revLongitude;
	private String revLatitude;
	private String sendLongitude;
	private String sendLatitude;
	private String wxOpenid;
	private String fpayStatus;//邮费支付状态
	private String cpayStatus;//代收货款支付状态，
	private String revMonthSettleNo;//收放月结帐号
	private String revPayType;//收放支付方式
	private int deled;
	private String disUserNo;//支付会员号
	private String proOrder;//问题件标识
	private String monthDiscount;//订单月结折扣，75,为7.5折
	private String codRate;
	private String idCard;
	private float disRealityFreight;
	private String disDiscount;
	private String receNo;
	private String reqRece;
	private String zidanNumber;
	private String zidanOrder;
	private String itemCount;
	private String forNo;
	private int zid; //是否为子单，0,1
	private String timeType;
	private String parentOrder;
	private String message ;

	public String getParentOrder() {
		return parentOrder;
	}

	public void setParentOrder(String parentOrder) {
		this.parentOrder = parentOrder;
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

	public String getItemCount() {
		return itemCount;
	}

	public void setItemCount(String itemCount) {
		this.itemCount = itemCount;
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

	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
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

	public String getProOrder() {
		return proOrder;
	}

	public void setProOrder(String proOrder) {
		this.proOrder = proOrder;
	}

	public String getDisUserNo() {
		return disUserNo;
	}

	public void setDisUserNo(String disUserNo) {
		this.disUserNo = disUserNo;
	}

	public int getDeled() {
		return deled;
	}

	public void setDeled(int deled) {
		this.deled = deled;
	}

	public String getRevMonthSettleNo() {
		return revMonthSettleNo;
	}

	public void setRevMonthSettleNo(String revMonthSettleNo) {
		this.revMonthSettleNo = revMonthSettleNo;
	}

	public String getRevPayType() {
		return revPayType;
	}

	public void setRevPayType(String revPayType) {
		this.revPayType = revPayType;
	}

	public float getRevMpay() {
		return revMpay;
	}

	public void setRevMpay(float revMpay) {
		this.revMpay = revMpay;
	}
	private float revMpay;//收方月结费用

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

	public String getWxOpenid() {
		return wxOpenid;
	}

	public void setWxOpenid(String wxOpenid) {
		this.wxOpenid = wxOpenid;
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

	public String getTakeMark() {
		return takeMark;
	}

	public void setTakeMark(String takeMark) {
		this.takeMark = takeMark;
	}

	public String getTakeOrderTime() {
		return takeOrderTime;
	}

	public void setTakeOrderTime(String takeOrderTime) {
		this.takeOrderTime = takeOrderTime;
	}

	public String getSendOrderTime() {
		return sendOrderTime;
	}

	public void setSendOrderTime(String sendOrderTime) {
		this.sendOrderTime = sendOrderTime;
	}

	public String getItemStatus() {
		return itemStatus;
	}

	public void setItemStatus(String itemStatus) {
		this.itemStatus = itemStatus;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getUserNo() {
		return userNo;
	}

	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}

	public String getSubStationNo() {
		return subStationNo;
	}

	public void setSubStationNo(String subStationNo) {
		this.subStationNo = subStationNo;
	}

	public String getTakeCourierNo() {
		return takeCourierNo;
	}

	public void setTakeCourierNo(String takeCourierNo) {
		this.takeCourierNo = takeCourierNo;
	}

	public String getSendCourierNo() {
		return sendCourierNo;
	}

	public void setSendCourierNo(String sendCourierNo) {
		this.sendCourierNo = sendCourierNo;
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

	public String getSendPhone() {
		return sendPhone;
	}

	public void setSendPhone(String sendPhone) {
		this.sendPhone = sendPhone;
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

	public String getRevPhone() {
		return revPhone;
	}

	public void setRevPhone(String revPhone) {
		this.revPhone = revPhone;
	}

	public String getRevLocation() {
		return revLocation;
	}

	public void setRevLocation(String revLocation) {
		this.revLocation = revLocation;
	}

	public String getItemType() {
		return itemType;
	}

	public void setItemType(String itemType) {
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

	public String getItemVolume() {
		return itemVolume;
	}

	public void setItemVolume(String itemVolume) {
		this.itemVolume = itemVolume;
	}

	public String getFreightType() {
		return freightType;
	}

	public void setFreightType(String freightType) {
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

	public String getTakeTime() {
		return takeTime;
	}

	public void setTakeTime(String takeTime) {
		this.takeTime = takeTime;
	}

	public String getTakeTimeBegin() {
		return takeTimeBegin;
	}

	public void setTakeTimeBegin(String takeTimeBegin) {
		this.takeTimeBegin = takeTimeBegin;
	}

	public String getTakeTimeEnd() {
		return takeTimeEnd;
	}

	public void setTakeTimeEnd(String takeTimeEnd) {
		this.takeTimeEnd = takeTimeEnd;
	}

	public String getTakeAddr() {
		return takeAddr;
	}

	public void setTakeAddr(String takeAddr) {
		this.takeAddr = takeAddr;
	}

	public String getTakeLocation() {
		return takeLocation;
	}

	public void setTakeLocation(String takeLocation) {
		this.takeLocation = takeLocation;
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

	public String getPayStatus() {
		return payStatus;
	}

	public void setPayStatus(String payStatus) {
		this.payStatus = payStatus;
	}

	public String getOrderNote() {
		return orderNote;
	}

	public void setOrderNote(String orderNote) {
		this.orderNote = orderNote;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(String lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public String getCompleteTime() {
		return completeTime;
	}

	public void setCompleteTime(String completeTime) {
		this.completeTime = completeTime;
	}

	public String getLgcNo() {
		return lgcNo;
	}

	public void setLgcNo(String lgcNo) {
		this.lgcNo = lgcNo;
	}

	public String getLgcOrderNo() {
		return lgcOrderNo;
	}

	public void setLgcOrderNo(String lgcOrderNo) {
		this.lgcOrderNo = lgcOrderNo;
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

	public String getCpayType() {
		return cpayType;
	}

	public void setCpayType(String cpayType) {
		this.cpayType = cpayType;
	}

	public String getKstatus() {
		return kstatus;
	}

	public void setKstatus(String kstatus) {
		this.kstatus = kstatus;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getLgcName() {
		return lgcName;
	}

	public void setLgcName(String lgcName) {
		this.lgcName = lgcName;
	}

	public float getCpay() {
		return cpay;
	}

	public void setCpay(float cpay) {
		this.cpay = cpay;
	}

	public float getVpay() {
		return vpay;
	}

	public void setVpay(float vpay) {
		this.vpay = vpay;
	}

	public float getDpay() {
		return dpay;
	}

	public void setDpay(float dpay) {
		this.dpay = dpay;
	}

	public String getSignType() {
		return signType;
	}

	public void setSignType(String signType) {
		this.signType = signType;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public float getTnpay() {
		return tnpay;
	}

	public void setTnpay(float tnpay) {
		this.tnpay = tnpay;
	}

	public float getSnapy() {
		return snapy;
	}

	public void setSnapy(float snapy) {
		this.snapy = snapy;
	}

	public String getSendSubstationNo() {
		return sendSubstationNo;
	}

	public void setSendSubstationNo(String sendSubstationNo) {
		this.sendSubstationNo = sendSubstationNo;
	}

	public float getMpay() {
		return mpay;
	}

	public void setMpay(float mpay) {
		this.mpay = mpay;
	}

	public static void main(String[] args) {
		System.out.println(Md5.md5Str("1234"));
	}

	public synchronized float getDisRealityFreight() {
		return disRealityFreight;
	}

	public synchronized void setDisRealityFreight(float disRealityFreight) {
		this.disRealityFreight = disRealityFreight;
	}

	public synchronized String getDisDiscount() {
		return disDiscount;
	}

	public synchronized void setDisDiscount(String disDiscount) {
		this.disDiscount = disDiscount;
	}

	public int getZid() {
		return zid;
	}

	public void setZid(int zid) {
		this.zid = zid;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	

}
