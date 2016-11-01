package com.yogapay.couriertsi.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;



public class SendMail {
	private String smtp = ""; // 邮件服务器主机名
	private String protocol = ""; // 邮件传输协议
	private String username = ""; // 登录用户名
	private String password = ""; // 登录密码
	private String from = ""; // 发件人地址
	private String to = ""; // 收件人地址
	private String subject = ""; // 邮件主题
	private String body = ""; // 邮件内容
	private String nick = ""; // 发件人昵称

	// 一个有规则的map，用作嵌入图片
	Map<String, String> map;
	// 存放附件
	List<String> list;

	public SendMail() {
	}
	
	
	public void send(String content) throws Exception {
		Map<String, String> mailMap = new HashMap<String, String>();
		mailMap.put("smtp", "smtp.exmail.qq.com");
		mailMap.put("protocol", "smtp");
		mailMap.put("username", "zentaopms@yogapay.com");
		mailMap.put("password","zentao0732");
		mailMap.put("from", "zentaopms@yogapay.com");
		mailMap.put("to", "609708791@qq.com");
		mailMap.put("subject", "BUG提示");
		mailMap.put("body", content);
		mailMap.put("nick", "支付界");
		SendMail sm = new SendMail(mailMap, null, null);
		// 执行发送
		sm.send();
	}
	public SendMail(Map<String, String> map, List<String> filelist,
			Map<String, String> image) {
		this.smtp = map.get("smtp");
		this.protocol = map.get("protocol");
		this.username = map.get("username");
		this.password = map.get("password");
		this.from = map.get("from");
		this.to = map.get("to");
		this.subject = map.get("subject");
		this.body = map.get("body");
		this.nick = map.get("nick");
		this.list = filelist;
		this.map = image;
	}



	public void send() throws Exception {
		Properties pros = new Properties();
		pros.setProperty("mail.transport.protocol", this.protocol);
		pros.setProperty("mail.host", this.smtp);
		pros.put("mail.smtp.auth", "true");
		MySendMailAuthenticator ma = new MySendMailAuthenticator(this.username,
				this.password);
		Session session = Session.getInstance(pros, ma);
		session.setDebug(false);

		MimeMessage msg = createMessage(session);

		Transport ts = session.getTransport();
		ts.connect();
		ts.sendMessage(msg, msg.getRecipients(Message.RecipientType.TO));
		ts.close();
	}

	public MimeMessage createMessage(Session session) throws Exception {

		MimeMessage message = new MimeMessage(session);
		message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(to));
		message.setSubject(this.subject);
		// 设置自定义发件人昵称
			nick = javax.mail.internet.MimeUtility.encodeText(nick);
		
		message.setFrom(new InternetAddress(nick + " <" + from + ">"));

		MimeMultipart allMultipart = new MimeMultipart();

		// 创建代表邮件正文和附件的各个MimeBodyPart对象
		MimeBodyPart contentpart = createContent(this.body);
		allMultipart.addBodyPart(contentpart);

		// 创建用于组合邮件正文和附件的MimeMultipart对象
		if (null != list)
			for (int i = 0; i < list.size(); i++) {
				allMultipart.addBodyPart(createAttachment(list.get(i)));
			}

		// 设置整个邮件内容为最终组合出的MimeMultipart对象
		message.setContent(allMultipart);
		message.saveChanges();
		return message;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MimeBodyPart createContent(String body) throws Exception {
		// 创建代表组合Mime消息的MimeMultipart对象，将该MimeMultipart对象保存到MimeBodyPart对象
		MimeBodyPart contentPart = new MimeBodyPart();
		MimeMultipart contentMultipart = new MimeMultipart("related");

		// 创建用于保存HTML正文的MimeBodyPart对象，并将它保存到MimeMultipart中
		MimeBodyPart htmlbodypart = new MimeBodyPart();
		htmlbodypart.setContent(this.body, "text/html;charset=UTF-8");
		contentMultipart.addBodyPart(htmlbodypart);

		if (map != null && map.size() > 0) {
			Set<Entry<String, String>> set = map.entrySet();
			for (Iterator iterator = set.iterator(); iterator.hasNext();) {
				Entry<String, String> entry = (Entry<String, String>) iterator
						.next();

				// 创建用于保存图片的MimeBodyPart对象，并将它保存到MimeMultipart中
				MimeBodyPart gifBodyPart = new MimeBodyPart();
				FileDataSource fds = new FileDataSource(entry.getValue());// 图片所在的目录的绝对路径

				gifBodyPart.setDataHandler(new DataHandler(fds));
				gifBodyPart.setContentID(entry.getKey()); // cid的值
				contentMultipart.addBodyPart(gifBodyPart);
			}
		}

		// 将MimeMultipart对象保存到MimeBodyPart对象
		contentPart.setContent(contentMultipart);
		return contentPart;
	}

	public MimeBodyPart createAttachment(String filename) throws Exception {
		// 创建保存附件的MimeBodyPart对象，并加入附件内容和相应的信息
		MimeBodyPart attachPart = new MimeBodyPart();
		FileDataSource fsd = new FileDataSource(filename);
		attachPart.setDataHandler(new DataHandler(fsd));
		attachPart.setFileName(MimeUtility.encodeText(fsd.getName()));
		return attachPart;
	}

	

// 向邮件服务器提交认证信息
class MySendMailAuthenticator extends Authenticator {
	String username = "";
	String password = "";

	public MySendMailAuthenticator(String user, String pass) {
		this.username = user;
		this.password = pass;
	}

	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(username, password);
	}
}
}