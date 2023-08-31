package com.example.musinsa.event;

import com.example.musinsa.common.exception.CustomMessagingException;
import com.example.musinsa.infra.mail.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Service
public class MailSentEventHandler {

    private final MailService mailService;

    @Async
    @Retryable(
            retryFor = CustomMessagingException.class,
            maxAttempts = 5,
            backoff = @Backoff(
                    delay = 1000,
                    maxDelay = 20000,
                    multiplier = 2.0,
                    random = true // jitter
            )
    )
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 트랜잭션이 커밋된후에 이벤트가 실행된다.
    public void handle(MailSentEvent event) {
        log.info("event 실행");
        mailService.send(event.getMessage());
    }

    @Recover
    public void recoverMailSend(CustomMessagingException e) {
        log.info("recover start : exception msg = {}", e.getMessage());
    }
}
