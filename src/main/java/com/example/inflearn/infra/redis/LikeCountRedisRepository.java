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

//todo 인기글을 캐싱하고 DB에 조회수를 반영하는 역할과 조회수를 카운팅하는 2개의역할 둘다 존재해서 분리를 고려해야한다.
/**
 *
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LikeCountRedisRepository {

    private static final int POPULAR_POST_LIST_SIZE = 5;
    private static final String LIKE_COUNT_KEY = "likeCountKey";
    private final HyperLogLogOperations<Long, Long> viewCount; // postId, uniqueString
    private final RedisTemplate<String, Long> likeCount; // postId, likeCount

    public Map<Object, Long> getPopularPostEntries() {
        log.info("call popularEntries");

        Map<Object, Long> map = new HashMap<>();
        for (Object key : likeCount.opsForHash().keys(LIKE_COUNT_KEY)) {
            map.put(key, viewCount.size((Long) key));
        }
        return map;
    }

    public Long getViewCount(Long postId) {
        return viewCount.size(postId);
    }

    public void addViewCount(long postId) {
        viewCount.add(postId, System.currentTimeMillis());
    }

    public void updatePopularPosts(Map<Long, Long> popularPostInDB) {
        likeCount.opsForHash().putAll(LIKE_COUNT_KEY, updatePostsToCache(getPostsInCache(), popularPostInDB));
    }

    private Map<Long, Long> getPostsInCache() {
        Map<Object, Object> popularPostEntries = likeCount.opsForHash().entries(LIKE_COUNT_KEY);
        return popularPostEntries.entrySet().stream()
                .collect(Collectors.toMap(entry -> (Long) entry.getKey(), entry -> (Long) entry.getValue()));
    }

    private Map<Long, Long> updatePostsToCache(Map<Long, Long> popularPostInCache, Map<Long, Long> popularPostInDB) {
        // 새롭게 맵을 만들지않고 두 Map을 add하게되면 서로의 Map의 변화에 서로 영향을 받기때문에 UnsupportedOperationException발생
        Map<Long, Long> addMap = new HashMap<>();
        addMap.putAll(popularPostInCache);
        addMap.putAll(popularPostInDB);

        List<Entry<Long, Long>> sortedEntries = addMap.entrySet().stream()
                .sorted(Entry.<Long, Long>comparingByValue().reversed())
                .toList();

        return sortedEntries.stream()
                .limit(POPULAR_POST_LIST_SIZE)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a,b) -> a, LinkedHashMap::new));
    }
}
