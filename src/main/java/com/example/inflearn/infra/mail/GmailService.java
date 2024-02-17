package com.example.inflearn.infra.mail;

import com.example.inflearn.common.exception.CustomMessagingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GmailService implements MailService {

    private final JavaMailSender gmailMailSender;

    public GmailService(@Qualifier("gmailMailSender") JavaMailSender gmailMailSender) {
        this.gmailMailSender = gmailMailSender;
    }

    @Override
    public void send(EmailMessage emailMessage) {
        MimeMessage mimeMessage = gmailMailSender.createMimeMessage();
        try {
            log.info("sent email: {}", emailMessage.message());
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            setMessage(emailMessage, mimeMessageHelper);
            gmailMailSender.send(mimeMessage);
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
    }
}
