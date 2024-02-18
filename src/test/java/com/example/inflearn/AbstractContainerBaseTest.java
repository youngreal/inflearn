package com.example.inflearn;

import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.GenericContainer;

@SpringBootTest
public abstract class AbstractContainerBaseTest {

    static final GenericContainer REDIS_CONTAINER;

    static {
        REDIS_CONTAINER = new GenericContainer<>("redis:7")
                .withExposedPorts(6379);

        REDIS_CONTAINER.start();

        // 스프링이 레디스와 통신할수있도록 6379와 매핑된 호스트와 포트를 스프링서버에 전달한다.
        System.setProperty("spring.redis.host", REDIS_CONTAINER.getHost());
        System.setProperty("spring.redis.port", String.valueOf(REDIS_CONTAINER.getMappedPort(6379)));
    }
}
