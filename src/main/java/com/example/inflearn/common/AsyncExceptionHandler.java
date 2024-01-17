package com.example.inflearn.common;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        log.error("Throwable Exception message : {}", ex.getMessage());
        log.error("Exception cause : {}", ex.getCause());
    }
}
