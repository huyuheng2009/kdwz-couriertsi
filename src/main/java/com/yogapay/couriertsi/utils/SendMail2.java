package com.yogapay.couriertsi.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;

import sun.misc.BASE64Encoder;

public class SendMail2 {
	private static ThreadPoolExecutor emailExecutor = new ThreadPoolExecutor(2,
			10, 5L, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(100));

	public static void sendEamil(final Map<String, Object> params) {
		emailExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					HtmlEmail email = new HtmlEmail();
					email.setHostName("smtp.exmail.qq.com");
					email.setSmtpPort(465);
					email.setAuthentication(
							"hl@yogapay.com",
							"123qwe");
					email.setSSLOnConnect(true);
					email.setFrom("hl@yogapay.com", "salesslip");
					email.setCharset("UTF-8");
					email.setSubject((String) params.get("subject"));
					email.setHtmlMsg((String) params.get("content"));

					if (params.get("to") instanceof String) {
						email.addTo((String) params.get("to"));
					} else {
						for (String it : (ArrayList<String>) params.get("to")) {
							email.addTo(it);
						}
					}
					if (params.get("cc") != null) {
						if (params.get("cc") instanceof String) {
							email.addCc((String) params.get("cc"));
						} else {
							for (String it : (ArrayList<String>) params
									.get("to")) {
								email.addCc(it);
							}
						}
					}
					if (params.get("bcc") != null) {
						if (params.get("bcc") instanceof String) {
							email.addBcc((String) params.get("bcc"));
						} else {
							for (String it : (ArrayList<String>) params
									.get("to")) {
								email.addBcc(it);
							}
						}
					}

					if (params.get("attachment") != null) {
						for (Map<String, Object> map : (List<Map<String, Object>>) params
								.get("attachment")) {
							EmailAttachment emailAttachment = new EmailAttachment();
							emailAttachment.setName("=?UTF-8?B?"
									+ new BASE64Encoder().encode(((String) map
											.get("name")).toString().getBytes())
									+ "?=");
							emailAttachment.setPath(((File) map.get("file"))
									.getPath());
							email.attach(emailAttachment);
						}
					}

					String r = email.send();
					System.out.println("email send result:" + r);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static void main(String[] args) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("subject", "签购单");
		map.put("to", "279689178@qq.com");
		map.put("content", "附件中是签购单");
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		Map<String, Object> f = new HashMap<String, Object>();
		f.put("name", "123.png");
		f.put("file", new File("C:\\Users\\lei\\Pictures\\风控\\POS风险交易.png"));
		list.add(f);
		map.put("attachment", list);
		sendEamil(map);
	}

}
