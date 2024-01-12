package com.example.inflearn.infra.redis;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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

    private static final String LIKE_COUNT_KEY = "likeCountKey";
    private final HyperLogLogOperations<Long, Long> viewCountOperationForTest; // postId, uniqueString
    private final RedisTemplate<String, Long> likeCountOperation; // postId, likeCount

    public Map<Object, Long> getPopularPostEntries() {
        log.info("call popularEntries");
        Set<Object> keys = likeCountOperation.opsForHash().keys(LIKE_COUNT_KEY);
        log.info("keys = {}", keys);
        Map<Object, Long> map = new HashMap<>();
        for (Object key : keys) {
            map.put(key, viewCountOperationForTest.size((Long) key));
        }
        log.info("map = {}", map);
        return map;
    }

    public Long getViewCount(Long postId) {
        return viewCountOperationForTest.size(postId);
    }


//    // currentTimeMillis 생성방식 성능테스트 필요
//    /*
//     같은 밀리세컨드에 들어온 요청은 중복 카운팅될수있지만 uuid생성비용보단 적을것이다.
//     */
    public void addViewCount(long postId) {
        log.info("addViewCount2");
        Long value = System.currentTimeMillis();
        viewCountOperationForTest.add(postId, value);
    }

    public void updatePopularPosts(Map<Long, Long> popularPostInDB) {
        log.info("posts In Db = {}", popularPostInDB);
        Map<Long, Long> result = updatePostsInCache(getPostsInCache(), popularPostInDB);
        log.info("update list = {}", result);
        likeCountOperation.opsForHash().putAll(LIKE_COUNT_KEY, result);
    }

    private Map<Long, Long> getPostsInCache() {
        Map<Object, Object> popularPostEntries = likeCountOperation.opsForHash().entries(LIKE_COUNT_KEY);
        return popularPostEntries.entrySet().stream()
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
