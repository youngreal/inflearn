package com.example.musinsa.ui.post.dto.request;

import com.example.musinsa.domain.HashTag;
import com.example.musinsa.domain.post.domain.Post;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Builder;

@Builder
public record PostUpdateRequest(
        @NotBlank
        String title,
        List<String> hashTags,
        @NotBlank
        String contents
) {
    public Post toEntity(PostUpdateRequest postUpdateRequest, long postId) {
        return Post.builder()
                .id(postId)
                .title(postUpdateRequest.title())
                .contents(postUpdateRequest.contents())
                .tags(postUpdateRequest.hashTags.stream()
                        .map(tags -> HashTag.builder()
                                .name(tags)
                                .build()).toList())
                .build();
    }
}
