package com.example.inflearn.service.post;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// 각 서버에서 메모리에서 조회수를 카운팅 하기위한 클래스
@Slf4j
@Component
@Getter
public class PostMemoryService {

    private final Map<Long, Long> viewCountStore = new ConcurrentHashMap<>();

    public void addViewCount(long postId) {
        for (Entry<Long, Long> entry : viewCountStore.entrySet()) {
            log.info("before entry Key = {}", entry.getKey());
            log.info("after entry Value = {}", entry.getValue());
        }
        log.info("=====================================================");
        viewCountStore.compute(postId, (key, value) -> (value == null) ? 1 : value + 1);
        for (Entry<Long, Long> entry : viewCountStore.entrySet()) {
            log.info("entry Key = {}", entry.getKey());
            log.info("entry Value = {}", entry.getValue());
        }
    }

    public void initViewCount(long postId) {
        viewCountStore.replace(postId, 0L);
    }
}
