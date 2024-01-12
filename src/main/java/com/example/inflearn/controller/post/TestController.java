package com.example.inflearn.controller.post;

import com.example.inflearn.infra.redis.LikeCountRedisRepository;
import com.example.inflearn.infra.redis.LikeCountRedisRepositoryWithHash;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
public class TestController {

    //test
    private final LikeCountRedisRepository likeCountRedisRepository;
    private final LikeCountRedisRepositoryWithHash likeCountRedisRepositoryWithHash;

    @GetMapping("/redis/hash")
    public void redisTest0() {
        log.info("controller entry = {}", likeCountRedisRepositoryWithHash.getPopularPostEntries());
    }

    @GetMapping("/redis/entry")
    public void redisTest() {
        log.info("controller entry = {}", likeCountRedisRepository.getPopularPostEntries());
    }

    @GetMapping("/redis/{postId}")
    public void redisTest2(@PathVariable long postId) {
        log.info("controller viewCount = {}", likeCountRedisRepository.getViewCount(postId));
    }

    //hyperloglog v3 호환
    @GetMapping("/redis/v3/{postId}")
    public void redisTest4(@PathVariable long postId) {
        log.info("controller viewCount = {}", likeCountRedisRepository.getViewCount(postId));
    }

}
