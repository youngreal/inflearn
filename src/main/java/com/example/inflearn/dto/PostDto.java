package com.example.inflearn.dto;

import com.example.inflearn.domain.post.domain.Post;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;
import lombok.Builder;

@Builder
public record PostDto(
        String title,
        Set<String> hashtags,
        String contents,
        LocalDateTime createdAt
) {

    public Post toEntity() {
        return Post.builder()
                .title(this.title)
                .contents(this.contents)
                .postHashtags(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();
    }

}
