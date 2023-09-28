package com.example.inflearn.ui.post.dto.response;

import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.domain.post.domain.PostStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record PostResponse(
        String title,
        String contents,
        int viewCount,
        List<String> hashtags,
        LocalDateTime createdAt,
        LocalDateTime updateAt,
        PostStatus postStatus
){

    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .title(post.getTitle())
                .contents(post.getContents())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updateAt(post.getUpdatedAt())
                .postStatus(post.getPostStatus())
                .build();
    }
}
