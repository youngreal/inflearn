package com.example.inflearn.domain.member.event;

import org.springframework.context.ApplicationEventPublisher;

/**
 * 이벤트 디스패처(ApplicationEventPublisher)를 통해 이벤트를 발생시키기위한 클래스
 */
public class Events {
    private static ApplicationEventPublisher publisher;

    public static void setPublisher(ApplicationEventPublisher publisher) {
        Events.publisher = publisher;
    }

    public static void raise(Object event) {
        if (publisher != null) {
            publisher.publishEvent(event);
        }
    }
}
