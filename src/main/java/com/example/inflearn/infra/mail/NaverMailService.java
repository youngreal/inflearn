package com.example.inflearn.infra.mail;

import com.example.inflearn.common.exception.CustomMessagingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NaverMailService implements MailService {

    private final JavaMailSender naverMailSender;

    public NaverMailService(@Qualifier("naverMailSender")JavaMailSender naverMailSender) {
        this.naverMailSender = naverMailSender;
    }

    @Override
    public void send(EmailMessage emailMessage) {
        MimeMessage mimeMessage = naverMailSender.createMimeMessage();
        try {
            log.info("sent email: {}", emailMessage.message());
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            setMessage(emailMessage, mimeMessageHelper);
            naverMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.warn("messaging exception 발생, retry 시작");
            throw new CustomMessagingException(e);
        }
    }

    private void setMessage(EmailMessage emailMessage, MimeMessageHelper mimeMessageHelper)
            throws MessagingException {
        mimeMessageHelper.setTo(emailMessage.to());
        mimeMessageHelper.setSubject(emailMessage.subject());
        mimeMessageHelper.setText(emailMessage.message(), true);
        mimeMessageHelper.setFrom("dudwls0505@naver.com");
    }
}
