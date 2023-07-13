package com.example.musinsa.infra.mail;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class DevMailService implements MailService{

    @Override
    public void send(EmailMessage emailMessage) {

    }
}
