package com.example.inflearn.infra.redis;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HyperLogLogOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LikeCountRedisRepository2 {

    private final HyperLogLogOperations<Long, Long> viewCountOperation;
    private static final String VIEW_COUNT_KEY = "popularPosts";
    private static final String LIKE_COUNT_KEY = "likeCountKey";
    private final RedisTemplate<String, Long> likeCountOperation; // postId, likeCount

    /**
     * @return
     */

    public Long getViewCount(Long postId) {
        Long size = viewCountOperation.size(postId);
        log.info("size = {}", size);
        return size;
    }
    public void updateViewCountToCache(long postId) {
        viewCountOperation.add(postId, viewCountOperation.size(postId) + 1);
    }

    public void updatePopularPosts(Map<Long, Long> popularPostInDB) {
        likeCountOperation.opsForHash()
                .putAll(VIEW_COUNT_KEY, updatePostsInCache(getPostsInCache(), popularPostInDB));
    }

    private Map<Long, Long> getPostsInCache() {
        Map<Object, Object> popularPostObjectEntries = likeCountOperation.opsForHash().entries(LIKE_COUNT_KEY);
        return popularPostObjectEntries.entrySet().stream()
                .collect(Collectors.toMap(entry -> (Long) entry.getKey(), entry -> (Long) entry.getValue()));
    }

    private Map<Long, Long> updatePostsInCache(Map<Long, Long> popularPostInCache, Map<Long, Long> popularPostInDB) {
        // 새롭게 맵을 만들지않고 두 Map을 add하게되면 서로의 Map의 변화에 서로 영향을 받기때문에 UnsupportedOperationException발생
        Map<Long, Long> addMap = new HashMap<>();
        addMap.putAll(popularPostInCache);
        addMap.putAll(popularPostInDB);

        List<Entry<Long, Long>> sortedEntries = addMap.entrySet().stream()
                .sorted(Entry.<Long, Long>comparingByValue().reversed())
                .toList();

        return sortedEntries.stream()
                .limit(5)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a,b) -> a, LinkedHashMap::new));
    }
}
