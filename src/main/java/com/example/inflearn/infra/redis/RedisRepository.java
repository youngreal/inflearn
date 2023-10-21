package com.example.inflearn.infra.redis;

import com.example.inflearn.domain.post.PostDto;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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
public class RedisRepository {

    private static final String POPULAR_POST_LIST_UPDATE_LOCK_KEY = "popularPostLock";
    private static final String LIKE_COUNT_KEY = "likeCount";
    private static final int POPULAR_POST_COUNT = 5;
    private static final String POPULAR_POST_LIST_UPDATE_LOCK_VALUE = "lock";
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, List<PostDto>> likeCountRedis;

    // key에는 인스턴스 ID, value에는 lock?
    // 락을 새로 세팅했으면 true, 락이 이미있었으면 false
    public Boolean popularPostListUpdateLock() {
        return redisTemplate.opsForValue()
                .setIfAbsent(POPULAR_POST_LIST_UPDATE_LOCK_KEY, POPULAR_POST_LIST_UPDATE_LOCK_VALUE, Duration.ofMillis(3_000));
        //todo 타임아웃은 정확하게 왜 설정하는거지?
    }

    public void popularPostListUpdateUnLock() {
        redisTemplate.delete(POPULAR_POST_LIST_UPDATE_LOCK_KEY);
    }

    public List<PostDto> getPopularPosts() {
        return likeCountRedis.opsForValue().get(LIKE_COUNT_KEY);
    }

    public void updatePopularPosts(List<PostDto> posts) {
        List<PostDto> popularPosts = likeCountRedis.opsForValue().get(LIKE_COUNT_KEY);
        if (popularPosts == null) {
            popularPosts = new ArrayList<>(posts);
        }

        popularPosts.addAll(posts);

        popularPosts.sort((post1, post2) -> {
            int likeCountComparison = Long.compare(post2.getLikeCount(), post1.getLikeCount());
            if (likeCountComparison == 0) {
                return Integer.compare(post2.getViewCount(), post1.getViewCount()); // Descending order
            } else {
                return likeCountComparison;
            }
        });

        if (popularPosts.size() > POPULAR_POST_COUNT) {
            popularPosts = popularPosts.subList(0, POPULAR_POST_COUNT);
        }

        log.info("redis posts = {}", redisTemplate.opsForValue().get(LIKE_COUNT_KEY));
        likeCountRedis.opsForValue().set(LIKE_COUNT_KEY, popularPosts);
        log.info("redis posts after = {}", redisTemplate.opsForValue().get(LIKE_COUNT_KEY));
    }

    public void setPopularPosts(List<PostDto> popularPosts) {
        likeCountRedis.opsForValue().set(LIKE_COUNT_KEY, popularPosts);
    }
}
