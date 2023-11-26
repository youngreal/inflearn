package com.example.inflearn.infra.redis;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LikeCountRedisRepository {

    private static final String VIEW_COUNT_KEY = "viewCountKey";
    private static final String LIKE_COUNT_KEY = "likeCountKey";
    private final RedisTemplate<String, Long> popularPostsWithViewCount; // postId, viewCount
    private final RedisTemplate<String, Long> popularPostsWithLikeCount; // postId, likeCount

    //todo Map<Object, Object> 파라미터로 넘겨주는게 좋을지? 아니면 DTO같은 객체를 생성해서 넘겨줄까?
    //todo 만약 Map으로 반환한다면, Long타입변환은 여기서 하는게 좋을까? 아니면 외부에서 하는게 좋을까?
    public Map<Object, Object> getPopularPostEntries() {
        log.info("PostHashEntries = {}", popularPostsWithViewCount.opsForHash().entries(VIEW_COUNT_KEY));
        return popularPostsWithViewCount.opsForHash().entries(VIEW_COUNT_KEY);
    }

    public Long getViewCount(Long postId) {
        log.info("redis 내 진입 2");
        return (Long) popularPostsWithViewCount.opsForHash().get(VIEW_COUNT_KEY, postId);
    }

    public void updateViewCountToCache(long postId) {
        popularPostsWithViewCount.opsForHash().increment(LIKE_COUNT_KEY, postId, 1L);
    }


    public void updatePopularPosts(Map<Long, Long> popularPostInDB) {
        popularPostsWithLikeCount.opsForHash().putAll(VIEW_COUNT_KEY, updatePostsInCache(getPostsInCache(), popularPostInDB));
    }

    private Map<Long, Long> getPostsInCache() {
        Map<Object, Object> popularPostObjectEntries = popularPostsWithLikeCount.opsForHash().entries(LIKE_COUNT_KEY);
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
