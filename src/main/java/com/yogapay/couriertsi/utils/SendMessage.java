package com.yogapay.couriertsi.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import com.yogapay.couriertsi.utils.http.HttpUtils;

/**
 * Created by lei on 2016/4/26.
 */
public class SendMessage {
    public static boolean send(String phone, String content, String lgcNo) throws IOException {
    	System.out.println("==========sendMessage============");
        Date date = new Date();

        String check = SHA.SHA1Encode1(phone + "yogapayHFT" + content + "PTSD");

        StringBuffer bufStr = new StringBuffer();
        bufStr.append("target=" + phone);
        bufStr.append("&content=" + content);
        bufStr.append("&operation=S");
        bufStr.append("&note.businessCode=PTSD");
        bufStr.append("&note.usage=验证码");
        bufStr.append("&check=" + check);
        bufStr.append("&channel=szkyt");
        bufStr.append("&lgcNo=" + lgcNo);

        InputStream resultStr = HttpUtils.getSoapInputStream("http://message.yogapay.com/message/post", bufStr.toString());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int h = -1;

        while ((h = resultStr.read()) != -1) {
            baos.write(h);
        }
        String responseStr = baos.toString();

        System.out.println("response===========" + responseStr);
        Map<String, Object> map = JsonUtil.getMapFromJson(responseStr);
        System.out.println("--------------------------------" + map.get("rescode") + "---------------------------------------");
        if("01".equals(map.get("rescode"))){
            return true;
        }else{
            return false;
        }
    }
    public static void main(String[] args) throws IOException {
    	send("18589091930", "您尾号*287的银行卡于2016-05-03 10:54:03消费人民币1000.00元【快递王子】", "0000");
	}
}
