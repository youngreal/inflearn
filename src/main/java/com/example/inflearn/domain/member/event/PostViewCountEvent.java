package com.example.inflearn.domain.member.event;

import com.example.inflearn.domain.post.domain.Post;
import lombok.Getter;

@Getter
public class PostViewCountEvent {

    private final long postId;

    public PostViewCountEvent(long postId) {
        this.postId = postId;
    }
}
