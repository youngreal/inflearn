package com.example.musinsa.infra.mail;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"local","default"})
public class LocalMailService implements MailService{

    @Override
    public void send(EmailMessage emailMessage) {

    }
}
