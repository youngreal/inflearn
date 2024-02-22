package com.example.inflearn.service.post;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import org.springframework.stereotype.Component;

// 각 서버에서 메모리에서 조회수를 카운팅 하기위한 클래스
@Component
@Getter
public class PostMemoryService {
    // postId, viewCount
    private final Map<Long, Long> viewCountStore = new ConcurrentHashMap<>();
    // postId, likeCount
    private final Map<Long, Long> likeCountStore = new HashMap<>();
    // postId, commentCount
    private final Map<Long, Long> commentCountStore = new HashMap<>();

    public void addViewCount(Long postId) {
        viewCountStore.compute(postId, (key, value) -> (value == null) ? 1 : value + 1);
    }

    public void initViewCount(Long postId) {
        viewCountStore.replace(postId, 0L);
    }

    public Long likeCount(Long postId) {
        return likeCountStore.getOrDefault(postId, 0L);
    }

    public Long commentCount(Long postId) {
        return commentCountStore.getOrDefault(postId, 0L);
    }

    public Set<Long> getEntry() {
        return viewCountStore.keySet();
    }

    public void saveLikeCount(Long popularPostKey, Long likeCount) {
        likeCountStore.put(popularPostKey, likeCount);
    }

    public void saveCommentCount(Long popularPostKey, Long commentCount) {
        commentCountStore.put(popularPostKey, commentCount);
    }
}
