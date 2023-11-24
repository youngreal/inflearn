package com.example.inflearn.domain.member.event;

import com.example.inflearn.common.exception.DoesNotExistPostException;
import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.infra.redis.LikeCountRedisRepository;
import com.example.inflearn.infra.redis.LikeCountRedisRepository2;
import com.example.inflearn.infra.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostViewCountEventHandler {

    private final PostRepository postRepository;
    private final LikeCountRedisRepository2 likeCountRedisRepository2;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PostViewCountEvent postViewCountEvent) {
        Post post = postRepository.findById(postViewCountEvent.getPostId()).orElseThrow();
        if (likeCountRedisRepository2.getViewCount(post.getId()) == null) {
            post.plusViewCount();
        } else {
            likeCountRedisRepository2.updateViewCountToCache(post.getId());
        }
    }
}
