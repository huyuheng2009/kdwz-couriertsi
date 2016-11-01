package com.yogapay.couriertsi.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.util.Log;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.jpos.util.NameRegistrar.NotFoundException;

import com.yogapay.couriertsi.jpos.HEXChannel;
import com.yogapay.couriertsi.jpos.PackagerNavigator;

public class Commons {
    
	public static Log log = new Log((Logger) NameRegistrar.getIfExists("logger.Q2"), "Commons") ;
	
	public static final String ACQ_CHANNEL_KEY = "channel.ts_channel" ;
	
	public static final String ACQ_MUX_KEY = "mux.ts_mux" ;


	
	public static ISOMsg sendAndRecive(ISOMsg msg ,String clearTak) throws NotFoundException, ISOException, IOException {
		String ip = "127.0.0.1";
		int port = 4000;
		HEXChannel channel = new HEXChannel(ip, port,
				PackagerNavigator.COMMON_PACKAGER);
		channel.setHeader(ISOUtil.hex2byte("6000060000602200000000"));
		channel.setTimeout(50000);
		channel.connect();
		channel.setLogger(null, null);
		msg.setHeader(channel.getHeader());
		msg.setPackager(PackagerNavigator.COMMON_PACKAGER);
		msg.setDirection(ISOMsg.OUTGOING) ;
		//msg.set("64","1212121");
        //JCEHandler.genECBMAC(msg.pack(), clearTak) ;
		
		//发送
		System.out.println(dump(msg));
		channel.send(msg);
		ISOMsg resp = channel.receive();
		System.out.println(dump(resp));
		return resp ;
	}
	
	public static String decodeTracks2(String ksn,String bdk,String encTracks,String model){
		byte[] bdkByte = ISOUtil.hex2byte(bdk);
		String trackksn = ksn;
		byte[] trackksnByte = ISOUtil.hex2byte(trackksn);
		
		//第一步根据ksn和bdk获取到IPEK
		byte[] ipekByte = Commons.generateIPEK(trackksnByte, bdkByte);

		//第二步根据ksn和IPEK获取DUKPT Key
		byte[] dukptByte = Commons.getDUKPTKey(trackksnByte, ipekByte);

		//第三步根据DUKPT KEY得到Data Key Variant
		byte[] dataKeyVariantByte = Commons.getDataKeyVariant(dukptByte);


		int count = encTracks.length() / 16;
		String tracks2 = "";
		for(int i=0;i<count;i++){
		    String temp = encTracks.substring(i*16, (i+1)*16);
//		    System.out.println("第"+i+"组密文磁道="+temp);
//		    System.out.println("第"+i+"组解密之后的磁道="+ISOUtil.hexString(JCEHandler.decryptData(ISOUtil.hex2byte(temp), dataKeyVariantByte)));
		    tracks2 += ISOUtil.hexString(JCEHandler.decryptData(ISOUtil.hex2byte(temp), dataKeyVariantByte));
		}
		System.out.println("tracks2="+tracks2);
		return tracks2;
	}

	public static int char2Hex( char _char) throws IllegalAccessException{
		String str = String.valueOf(_char);
		char[] a = {'0','1','2','3','4','5','6','7','8','9'};
		boolean flag = false;
		for(int i=0;i<a.length;i++){
			if(_char == a[i]){
				return Integer.parseInt(String.valueOf(_char));
			}
		}
		if("a".equals(str.toLowerCase())){
			return 10;
		}else if("b".equals(str.toLowerCase())){
			return 11;
		}else if("c".equals(str.toLowerCase())){
			return 12;
		}else if("d".equals(str.toLowerCase())){
			return 13;
		}else if("e".equals(str.toLowerCase())){
			return 14;
		}else if("f".equals(str.toLowerCase())){
			return 15;
		}else{
			throw new IllegalAccessException("16进制数据有误"+str);
		}
	}

	//QPOS解密密码块
	public static String decodePinBlock(String pinKsn, String bdk,  String cardNo, String encPinBlock, String clearTpk,String model) throws Exception{
		String pinBlock = null;
		String clearPin = "000000";
//        if (model == Constants.MPOS_MODEL) {
		System.out.println("pinKsn="+pinKsn+",encPinBlock="+encPinBlock);
		if(StringUtils.isNotEmptyWithTrim(pinKsn) && StringUtils.isNotEmptyWithTrim(encPinBlock)){
			byte[] pinKsnByte = ISOUtil.hex2byte(pinKsn);
			byte[] bdkByte = ISOUtil.hex2byte(bdk);
			//第一步根据ksn和bdk获取到IPEK
			byte[] ipekByte = generateIPEK(pinKsnByte, bdkByte);

			//第二步根据ksn和IPEK获取DUKPT Key
			byte[] dukptByte = getDUKPTKey(pinKsnByte, ipekByte);

			//第三步根据DUKPT KEY得到Data Key Variant
			byte[] pinKeyVariant = getPinKeyVariant(dukptByte);

			String clearPk = ISOUtil.hexString(pinKeyVariant);
			clearPin = unPinBlock(cardNo, encPinBlock,
					clearPk);
			System.out.println("clearPin=======" + clearPin);
		}
		if(clearPin.length() !=6){
			log.info("***密码长度不对***");
			throw new Exception();
		}
//        }
//        else{
//            println("encPinBlock=${encPinBlock},clearTpk=${clearTpk}")
//            clearPin = Des3.decode(encPinBlock, clearTpk)[0..5]
//        }

		cardNo = cardNo.substring(cardNo.length() - 13,
				cardNo.length() - 1);
		// 将卡号与密码进行异或
		byte[] x = ISOUtil.xor(ISOUtil.hex2byte("06" + clearPin + "FFFFFFFF"),
				ISOUtil.hex2byte("0000" + cardNo));
		System.out.println("clearTpk===="+clearTpk);
		pinBlock = JCEHandler.encryptData(ISOUtil.hexString(x), clearTpk);
		return pinBlock;
	}

	public static byte[] generateIPEK(byte[] ksn, byte[] bdk){
		byte[] ipekByte = new byte[16];
		byte[] temp = new byte[8];
		byte[] temp2 = new byte[8];
		byte[] keyTemp = new byte[16];
		System.arraycopy(bdk, 0, keyTemp, 0, 16);
		System.arraycopy(ksn, 0, temp, 0, 8);
		temp[7] &= 0xE0;
		//3des算法
		temp2 = JCEHandler.encryptData(temp, keyTemp);
		System.arraycopy(temp2, 0, ipekByte, 0, 8);
		keyTemp[0] ^= 0xC0;
		keyTemp[1] ^= 0xC0;
		keyTemp[2] ^= 0xC0;
		keyTemp[3] ^= 0xC0;
		keyTemp[8] ^= 0xC0;
		keyTemp[9] ^= 0xC0;
		keyTemp[10] ^= 0xC0;
		keyTemp[11] ^= 0xC0;
		//3des算法
		temp2 = JCEHandler.encryptData(temp, keyTemp);
		System.arraycopy(temp2, 0, ipekByte, 8, 8);
		System.out.println("IPEK="+ISOUtil.hexString(ipekByte));
		return ipekByte;
	}

	public static byte[] getDUKPTKey(byte[] ksn, byte[] ipek){
		byte[] dukptKeyByte = new byte[16];
		byte[] cnt = new byte[3];
		byte[] temp3 = new byte[8];
		int shift;
		System.arraycopy(ipek, 0, dukptKeyByte, 0, 16);
		cnt[0] = (byte) (ksn[7] & 0x1F);
		cnt[1] = ksn[8];
		cnt[2] = ksn[9];
		System.arraycopy(ksn, 2, temp3, 0, 6);
		temp3[5] &= 0xE0;
		shift = 0x10;
		while(shift > 0){
			if ((cnt[0] & shift) > 0){
				temp3[5] |= shift;
				NRKGP(dukptKeyByte, temp3);
			}
			shift >>= 1;
		}
		shift = 0x80;
		while(shift > 0){
			if ((cnt[1] & shift) > 0){
				temp3[6] |= shift;
				NRKGP(dukptKeyByte, temp3);
			}
			shift >>= 1;
		}
		shift = 0x80;
		while (shift > 0){
			if ((cnt[2] & shift) > 0)
			{
				temp3[7] |= shift;
				NRKGP(dukptKeyByte, temp3);
			}
			shift >>= 1;
		}
		System.out.println("dukptKeyByte="+ISOUtil.hexString(dukptKeyByte));
		return dukptKeyByte;
	}

	static void NRKGP(byte[] key, byte[] ksn){
		System.out.println("key="+ISOUtil.hexString(key));
		System.out.println("ksn="+ISOUtil.hexString(ksn));
		byte[] temp = new byte[8];
		byte[] key_l = new byte[8];
		byte[] key_r = new byte[8];
		byte[] key_temp = new byte[8];
		int i;
		System.arraycopy(key, 0, key_temp, 0, 8);
		for (i = 0; i < 8; i++){
			temp[i] = (byte)(ksn[i] ^ key[8 + i]);
		}
		//3des算法
		key_r = JCEHandler.encryptData(temp, key_temp);
		for (i = 0; i < 8; i++){
			key_r[i] ^= key[8 + i];
		}
		key_temp[0] ^= 0xC0;
		key_temp[1] ^= 0xC0;
		key_temp[2] ^= 0xC0;
		key_temp[3] ^= 0xC0;
		key[8] ^= 0xC0;
		key[9] ^= 0xC0;
		key[10] ^= 0xC0;
		key[11] ^= 0xC0;
		for (i = 0; i < 8; i++){
			temp[i] = (byte)(ksn[i] ^ key[8 + i]);
		}
		//3des算法
		key_l = JCEHandler.encryptData(temp, key_temp);
		for (i = 0; i < 8; i++)
			key[i] = (byte)(key_l[i] ^ key[8 + i]);
		System.arraycopy(key_r, 0, key, 8, 8);
	}

	public static byte[] getDataKeyVariant(byte[] dukpt){
		dukpt[5] ^= 0xFF;
		dukpt[13] ^= 0xFF;
		System.out.println("dataKeyVariant="+ISOUtil.hexString(dukpt));
		return dukpt;
	}

	public static byte[]  getPinKeyVariant(byte[] dukpt){
		dukpt[7] ^= 0xFF;
		dukpt[15] ^= 0xFF;
		System.out.println("dataKeyVariant="+ISOUtil.hexString(dukpt));
		return dukpt;
	}

	// 根据明文pk进行密码解密
	public static String unPinBlock(String accountNo, String pinBlock,
									String clearPk) {
		String clearPin = "000000";
		if (StringUtils.isNotEmptyWithTrim(pinBlock)) {
			String dest = JCEHandler.decryptData(pinBlock, clearPk);
			accountNo = accountNo.substring(accountNo.length() - 13,
					accountNo.length() - 1);

			byte[] x = ISOUtil.xor(ISOUtil.hex2byte(dest),
					ISOUtil.hex2byte("0000" + accountNo));
			clearPin = ISOUtil.hexString(x);
			if (clearPin.length() == 16) {
				System.out.println("clearPin====" + clearPin);
				clearPin = clearPin.substring(2, clearPin.indexOf("F"));
			}

		}
		return clearPin;
	}
	
	public static String dump(ISOMsg msg) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			msg.dump(new PrintStream(baos), "");
			baos.flush();
		} catch (IOException e) {
			// ignore
		} finally {
			try {
				baos.close();
			} catch (IOException e) {
				// ignore
			}
		}
		return new String(baos.toByteArray());
	}
	
	/** 
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     * @param params 需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
    public static String createLinkString(Map<String, String> params) {

        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        String prestr = "";

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);

            if (i == keys.size() - 1) {//拼接时，不包括最后一个&字符
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }

        return prestr;
    }
    
    public static String get(String url) {
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(url);
		try {
			client.executeMethod(method);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 打印服务器返回的状态
		System.out.println(method.getStatusLine());
		// 打印返回的信息
		String result = null;
		try {
			result = method.getResponseBodyAsString();
			System.out.println("result="+ result);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 释放连接
		method.releaseConnection();
		return result;
	}
	
}
