//package com.example.inflearn.service.post;
//
//import static java.lang.Boolean.FALSE;
//
//import com.example.inflearn.infra.redis.LikeCountRedisRepository;
//import com.example.inflearn.infra.redis.LettuceLockRepository;
//import java.time.LocalDateTime;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Slf4j
//@RequiredArgsConstructor
//@Service
//public class PostSchedulerService {
//
//    private static final int MINUTE = 60 * 1_000;
//    private final LettuceLockRepository lettuceLockRepository;
//    private final LikeCountRedisRepository likeCountRedisRepository;
//    private final PostQueryService postQueryService;
//    private final PostService postService;
//
//    //todo 로깅 AOP
//    @SchedulerLock(
//            name = "scheduler_lock", // 스케줄러 이름 같으면 락의 대상이 됨
//            lockAtLeastFor = "3s", // 락 유지시간(스케줄러 주기보다 약간 짧게 하는게 좋음)
//            lockAtMostFor = "3s" // 타임아웃 느낌, 정상적으로 스케줄러가 종료되지않는경우 잠금 유지시간 9초
//    )
//    @Scheduled(fixedDelay = 5000)
//    @Transactional
//    public void updatePopularPosts() {
//        log.info("1번 스케줄러 시작시간 ={}", LocalDateTime.now());
//        // select p.id from p , db
//        postQueryService.updatePopularPosts();
//        // insert to 인기글테이블
//    }
//
////    @Scheduled(fixedDelay = 5 * MINUTE)
////    public void updatePopularPosts() {
////        // 락 획득에 실패한다면 재시도를 시도하지않고 리턴한다
////        if (FALSE.equals(lettuceLockRepository.popularPostListUpdateLock())) {
////            log.info("The popularPostList lock has already been acquired from another server.");
////            return;
////        }
////
////        // 락 획득에 성공한다면 인기게시글을 업데이트한다.
////        try {
////            log.info("Get Lock : update PopularPostLists");
////            postQueryService.updatePopularPosts();
////        } finally {
////            lettuceLockRepository.popularPostListUpdateUnLock();
////        }
////    }
//
////    @Scheduled(fixedDelay = MINUTE)
////    public void updateViewCountToDatabase() {
////        if (FALSE.equals(lettuceLockRepository.updateViewCountLock())) {
////            log.info("The updateViewCount lock has already been acquired from another server.");
////            return;
////        }
////
////        try {
////            log.info("Get Lock : update viewCCount to Database.");
////            postService.updateViewCountForPopularPosts(likeCountRedisRepository.getPopularPostEntries());
////        } finally {
////            lettuceLockRepository.updateViewCountUnLock();
////        }
////    }
//}
