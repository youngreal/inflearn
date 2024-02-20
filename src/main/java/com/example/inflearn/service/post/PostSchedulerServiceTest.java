package com.example.inflearn.service.post;

import com.example.inflearn.domain.post.domain.PopularPost;
import com.example.inflearn.infra.repository.post.PopularPostRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostSchedulerServiceTest {

    private static final int POPULAR_POST_SIZE = 5;
    private final PostService postService;
    private final PostQueryService postQueryService;
    private final PopularPostRepository popularPostRepository;

    /**
     * param : id
     * param : likeCount
     */
    /**
     * 기존 인기글과 인기 테이블에 있던 인기글 비교해서 테이블에 남기기 (insert to 인기글테이블)
     */
    //todo 로깅 AOP
    @SchedulerLock(
            name = "scheduler_lock", // 스케줄러 이름 같으면 락의 대상이 됨
            lockAtLeastFor = "3s", // 락 유지시간(스케줄러 주기보다 약간 짧게 하는게 좋음)
            lockAtMostFor = "3s" // 타임아웃 느낌, 정상적으로 스케줄러가 종료되지않는경우 잠금 유지시간 9초
    )
    @Scheduled(fixedDelay = 15000)
    @Transactional
    public void updatePopularPosts() {
        log.info("1번 스케줄러 시작시간 ={}", LocalDateTime.now());
        // 7일간 좋아요 많은 5개 DB에서 가져오기
        Map<Long, Long> newPopularPosts = postQueryService.updatePopularPostsForSchedulerTest();
        for (Entry<Long, Long> entry : newPopularPosts.entrySet()) {
            log.info("new PopularPosts Key = {}", entry.getKey());
            log.info("new PopularPosts Value= {}", entry.getValue());
        }
        Map<Long, Long> beforePopularPosts = getBeforePopularPostMap();
        for (Entry<Long, Long> entry : beforePopularPosts.entrySet()) {
            log.info("before PopularPosts Key = {}", entry.getKey());
            log.info("before PopularPosts Value= {}", entry.getValue());
        }

        // 새롭게 맵을 만들지않고 두 Map을 add하게되면 서로의 Map의 변화에 서로 영향을 받기때문에 UnsupportedOperationException발생
        updatePosts(sortEntriesByValue(beforePopularPosts, newPopularPosts));
    }

    @Scheduled(fixedDelay = 10000)
    public void updateViewCountToDB() {
        //memory에있는 viewCount들을 post엔티티에 30초마다 반영해준다
        log.info("스케줄러 2 시작");
        postService.updateViewCountForPopularPosts();
    }


    private void updatePosts(List<Entry<Long, Long>> entries) {
        int popularPostId = 1;
        log.info("updatePosts entries = {}", entries);
        for (Entry<Long, Long> sortedEntry : entries) {
            popularPostRepository.updatePopularIds(sortedEntry.getKey(), popularPostId);
            popularPostRepository.updatePopularlikeCount(sortedEntry.getValue(), popularPostId);
            popularPostId++;
        }
    }

    private List<Entry<Long, Long>> sortEntriesByValue(Map<Long,Long> beforePopularPosts, Map<Long, Long> newPopularPosts) {
        Map<Long, Long> addMap = new HashMap<>();
        addMap.putAll(beforePopularPosts);
        addMap.putAll(newPopularPosts);

        return newPopularPosts.entrySet().stream()
                .sorted(Entry.<Long, Long>comparingByValue().reversed())
                .limit(POPULAR_POST_SIZE)
                .collect(Collectors.toList());
    }

    private Map<Long, Long> getBeforePopularPostMap() {
        return popularPostRepository.findAll().stream()
                .collect(Collectors.toMap(PopularPost::getPostId, PopularPost::getLikeCount,
                        (p1, p2) -> p1));
    }
}
