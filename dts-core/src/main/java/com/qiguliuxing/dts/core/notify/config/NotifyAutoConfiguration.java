package com.qiguliuxing.dts.core.notify.config;

import com.github.qcloudsms.SmsSingleSender;
import com.qiguliuxing.dts.core.notify.NotifyService;
import com.qiguliuxing.dts.core.notify.SslMailSender;
import com.qiguliuxing.dts.core.notify.TencentSmsSender;
import com.qiguliuxing.dts.core.notify.WxTemplateSender;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@EnableConfigurationProperties(NotifyProperties.class)
public class NotifyAutoConfiguration {

	private final NotifyProperties properties;

	public NotifyAutoConfiguration(NotifyProperties properties) {
		this.properties = properties;
	}

	@Bean
	public NotifyService notifyService() {
		NotifyService notifyService = new NotifyService();

		NotifyProperties.Mail mailConfig = properties.getMail();
		if (mailConfig.isEnable()) {
			/*
			 * notifyService.setMailSender(mailSender());
			 * notifyService.setSendFrom(mailConfig.getSendfrom());
			 * notifyService.setSendTo(mailConfig.getSendto());
			 */
			notifyService.setSslMailSender(sslMailSender());
		}

		NotifyProperties.Sms smsConfig = properties.getSms();
		if (smsConfig.isEnable()) {
			notifyService.setSmsSender(tencentSmsSender());
			notifyService.setSmsTemplate(smsConfig.getTemplate());
		}

		NotifyProperties.Wx wxConfig = properties.getWx();
		if (wxConfig.isEnable()) {
			notifyService.setWxTemplateSender(wxTemplateSender());
			notifyService.setWxTemplate(wxConfig.getTemplate());
		}
		return notifyService;
	}

	@Bean
	public JavaMailSender mailSender() {
		NotifyProperties.Mail mailConfig = properties.getMail();
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(mailConfig.getHost());
		mailSender.setUsername(mailConfig.getUsername());
		mailSender.setPassword(mailConfig.getPassword());
		return mailSender;
	}

	@Bean
	public SslMailSender sslMailSender() {
		NotifyProperties.Mail mailConfig = properties.getMail();
		SslMailSender sslMailSender = new SslMailSender();

		sslMailSender.setHost(mailConfig.getHost());
		sslMailSender.setUserName(mailConfig.getUsername());
		sslMailSender.setPassword(mailConfig.getPassword());
		sslMailSender.setFromAddress(mailConfig.getSendfrom());
		sslMailSender.setToAddress(mailConfig.getSendto());
		sslMailSender.setFromName(mailConfig.getSendfrom());
		sslMailSender.setPort("465");// 默认465端口，25端口云服务器基本难以申请
		sslMailSender.setSslEnabled("true");

		return sslMailSender;
	}

	@Bean
	public WxTemplateSender wxTemplateSender() {
		WxTemplateSender wxTemplateSender = new WxTemplateSender();
		return wxTemplateSender;
	}

	@Bean
	public TencentSmsSender tencentSmsSender() {
		NotifyProperties.Sms smsConfig = properties.getSms();
		TencentSmsSender smsSender = new TencentSmsSender();
		smsSender.setSender(new SmsSingleSender(smsConfig.getAppid(), smsConfig.getAppkey()));
		return smsSender;
	}
}
