package com.example.inflearn.service.post;

import static java.lang.Boolean.FALSE;

import com.example.inflearn.infra.redis.LikeCountRedisRepository;
import com.example.inflearn.infra.redis.RedisRepository;
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
    private static final int MINUTE = 60 * 1_000;
    private final RedisRepository redisRepository;
    private final LikeCountRedisRepository likeCountRedisRepository;
    private final PostQueryService postQueryService;
    private final PostService postService;

    //todo AOP로 개선할수있을것같다. 핵심로직과 락을거는 로직의 분리
    @Scheduled(fixedDelay = MINUTE)
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

    /*
    여러 분산 서버에서 동시실행 방지를 위한 분산락
     */
    @Scheduled(fixedDelay = MINUTE)
    public void updateViewCountToDatabase() {
        if (FALSE.equals(redisRepository.updateViewCountLock())) {
            log.info("The updateViewCount lock has already been acquired from another server.");
            return;
        }

        try {
            log.info("Get Lock : update viewCCount to Database.");
            postService.updateViewCountForPopularPosts(likeCountRedisRepository.getPopularPostEntries());
        } finally {
            redisRepository.updateViewCountUnLock();
        }
    }
}
