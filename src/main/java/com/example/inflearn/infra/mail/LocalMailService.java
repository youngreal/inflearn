package com.example.inflearn.infra.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({"local","default"})
public class LocalMailService implements MailService{

    @Override
    public void send(EmailMessage emailMessage) {
        log.info("sent email: {}", emailMessage.message());
    }
}
