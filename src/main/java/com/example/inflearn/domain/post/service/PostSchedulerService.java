package com.example.inflearn.domain.post.service;

import static java.lang.Boolean.FALSE;

import com.example.inflearn.domain.post.PostDto;
import com.example.inflearn.infra.redis.RedisRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 다중 서버 환경에서 동일한 스케줄링 서비스가 실행되는것 자체가 비효율적이며, race condition으로 인해 업데이트가 잘못되거나 중복될수있는문제 등이 발생할수있어서
 * redis의 Lettuce락을통해 제어하기위한 클래스
 * 하나의 서버에서만 실행한다면 나머지 서버에선 실행하지않아도 되기때문에(retry가 필요없기때문에) Lettuce락을 사용했다.
 *
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PostSchedulerService {

    private static final int HOURS = 60 * 60 * 1_000;
    private static final int THREE_HOURS = 3 * HOURS;
    private final RedisRepository redisRepository;
    private final PostQueryService postQueryService;
    private final PostService postService;

    @Scheduled(fixedDelay = THREE_HOURS)
    public void updatePopularPosts() {
        // 락 획득에 실패한다면 재시도를 시도하지않고 리턴한다
        if (FALSE.equals(redisRepository.popularPostListUpdateLock())) {
            log.info("The popularPostList lock has already been acquired from another server.");
            return;
        }

        // 락 획득에 성공한다면 인기게시글을 업데이트한다.
        try {
            log.info("Get Lock : update PopularPostLists");
            postQueryService.updatePopularPosts();
        } finally {
            redisRepository.popularPostListUpdateUnLock();
        }
    }
}
