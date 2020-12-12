package com.qiguliuxing.dts.core.notify;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

/**
 * HTML邮件发送帮助类 兼容云服务器屏蔽25端口的问题
 */
public class EmailHelper {

	/**
	 * 发送HTML格式的邮件
	 *
	 * @param host
	 *            Email服务器主机
	 * @param port
	 *            Email服务器端口
	 * @param userName
	 *            登录账号
	 * @param password
	 *            登录密码
	 * @param sslEnabled
	 *            是否启用SSL
	 * @param toAddress
	 *            邮件接受者地址
	 * @param fromAddress
	 *            邮件发送者地址
	 * @param fromName
	 *            邮件发送者名称
	 * @param subject
	 *            邮件主题
	 * @param htmlContent
	 *            邮件内容，HTML格式
	 * @return 邮件发送的MessageId，若为<code>null</code>，则发送失败
	 */
	public static String sendHtml(String host, int port, String userName, String password, boolean sslEnabled,
			String fromAddress, String fromName, String[] toAddress, String subject, String htmlContent) {
		HtmlEmail email = new HtmlEmail();
		email.setHostName(host);
		email.setSmtpPort(port);
		email.setAuthenticator(new DefaultAuthenticator(userName, password));
		email.setCharset("UTF-8");
		if (sslEnabled) {
			email.setSslSmtpPort(String.valueOf(port));
			email.setSSLOnConnect(true);
		}
		try {
			email.setFrom(fromAddress, fromName);
			email.addTo(toAddress);
			email.setSubject(subject);
			email.setHtmlMsg(htmlContent);
			return email.send();
		} catch (EmailException e) {
			return null;
		}
	}
}
