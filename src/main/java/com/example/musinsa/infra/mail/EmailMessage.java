package com.example.musinsa.infra.mail;

import lombok.Builder;

@Builder
public record EmailMessage(
        String to,
        String subject, // 이메일 제목
        String message // 이메일 본문
) {

}
