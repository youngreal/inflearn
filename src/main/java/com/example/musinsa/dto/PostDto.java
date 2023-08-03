package com.example.musinsa.dto;

import com.example.musinsa.domain.PostHashtag;
import com.example.musinsa.domain.member.domain.Member;
import com.example.musinsa.domain.post.domain.Post;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;

@Builder
public record PostDto(
        String title,
        List<String> hashTags,
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
