package com.example.inflearn.common.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private final AsyncUncaughtExceptionHandler asyncUncaughtExceptionHandler;

    public AsyncConfig(AsyncUncaughtExceptionHandler asyncUncaughtExceptionHandler) {
        this.asyncUncaughtExceptionHandler = asyncUncaughtExceptionHandler;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return asyncUncaughtExceptionHandler;
    }
}
