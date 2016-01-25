package com.dianping.cache.alarm.email;

import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Created by lvshiyun on 16/1/13.
 */
public class SpringMailSender {

    // Spring的邮件工具类，实现了MailSender和JavaMailSender接口
    private JavaMailSenderImpl mailSender;

    public JavaMailSenderImpl getMailSender() {
        return mailSender;
    }

    public void setMailSender(JavaMailSenderImpl mailSender) {
        this.mailSender = mailSender;
    }

    public SpringMailSender() {

        // 初始化JavaMailSenderImpl，当然推荐在spring配置文件中配置，这里是为了简单
        mailSender = new JavaMailSenderImpl();
        // 设置参数
        mailSender.setUsername("shiyun.lv@dianping.com");
        mailSender.setHost("mail.51ping.com");
        mailSender.setDefaultEncoding("utf-8");
        mailSender.setProtocol("smtp");
    }
}
