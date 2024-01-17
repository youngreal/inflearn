package com.example.inflearn.infra.redis;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

//todo 개선필요, 퍼사드를 만들어야할까?
/**
 * 로직 실행전에 Key와 SetNx명령어를 활용해 Lock을하고 로직이 끝나면 unlock하는방식
 * 로직 실행전/후로 락 획득/해제를 수행해줘야하기떄문에 퍼사드를 쓴다
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LettuceLockRepository {

    private static final String POPULAR_POST_LIST_UPDATE_LOCK_KEY = "popularPostLock";
    private static final String POPULAR_POST_LIST_UPDATE_LOCK_VALUE = "popularPostLock";
    private static final String UPDATE_VIEW_LOCK_KEY = "updateViewLock";
    private static final String UPDATE_VIEW_LOCK_VALUE = "updateViewLock";
    private final RedisTemplate<String, String> redisTemplate;

    public Boolean popularPostListUpdateLock() {
        return redisTemplate.opsForValue()
                .setIfAbsent(POPULAR_POST_LIST_UPDATE_LOCK_KEY, POPULAR_POST_LIST_UPDATE_LOCK_VALUE, Duration.ofMillis(3_000));
    }

    public void popularPostListUpdateUnLock() {
        redisTemplate.delete(POPULAR_POST_LIST_UPDATE_LOCK_KEY);
    }

    public Boolean updateViewCountLock() {
        return redisTemplate.opsForValue()
                .setIfAbsent(UPDATE_VIEW_LOCK_KEY, UPDATE_VIEW_LOCK_VALUE, Duration.ofMillis(3_000));
    }

    public void updateViewCountUnLock() {
        redisTemplate.delete(UPDATE_VIEW_LOCK_KEY);
    }
}
