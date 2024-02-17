package com.example.inflearn.common.config;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

    @Value("${google.mail.host}")
    private String gmailHost;
    @Value("${google.mail.port}")
    private int gmailPort;
    @Value("${google.mail.username}")
    private String gmailUsername;
    @Value("${google.mail.password}")
    private String gmailPassword;

    @Value("${naver.mail.host}")
    private String naverHost;
    @Value("${naver.mail.port}")
    private int naverPort;
    @Value("${naver.mail.username}")
    private String naverUsername;
    @Value("${naver.mail.password}")
    private String naverPassword;

    @Bean
    public JavaMailSender gmailMailSender() {
        JavaMailSenderImpl gmailMailSender = new JavaMailSenderImpl();
        gmailMailSender.setHost(gmailHost);
        gmailMailSender.setPort(gmailPort);
        gmailMailSender.setUsername(gmailUsername);
        gmailMailSender.setPassword(gmailPassword);
        gmailMailSender.setJavaMailProperties(getGoogleMailProperties());
        return gmailMailSender;
    }

    private Properties getGoogleMailProperties() {
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.timeout", "5000");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.smtp.ssl.trust","smtp.gmail.com"); // ssl 인증 서버는 smtp.naver.com
        properties.setProperty("mail.smtp.ssl.enable","true");
        properties.setProperty("mail.smtp.socketFactory.port", "465");
        properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        return properties;
    }

    private Properties getNaverMailProperties() {
        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.timeout", "5000");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.smtp.ssl.trust","smtp.naver.com");
        properties.setProperty("mail.smtp.ssl.enable","true");
        return properties;
    }

    @Bean
    public JavaMailSender naverMailSender() {
        JavaMailSenderImpl naverMailSender = new JavaMailSenderImpl();
        naverMailSender.setHost(naverHost);
        naverMailSender.setPort(naverPort);
        naverMailSender.setUsername(naverUsername);
        naverMailSender.setPassword(naverPassword);
        naverMailSender.setJavaMailProperties(getNaverMailProperties());
        return naverMailSender;
    }
}
