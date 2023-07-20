package com.example.musinsa.ui.post.dto.request;

import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableSet;

import com.example.musinsa.domain.HashTag;
import com.example.musinsa.domain.post.domain.Post;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Builder;

@Builder
public record PostWriteRequest(
        @NotBlank
        String title,
        List<String> hashTags,
        @NotBlank
        String contents
) {

    public Post toEntity(PostWriteRequest postWriteRequest) {
        return Post.builder()
                .title(postWriteRequest.title())
                .contents(postWriteRequest.contents())
                .tags(postWriteRequest.hashTags.stream()
                        .map(tags -> HashTag.builder()
                                .name(tags)
                                .build()).toList())
                .build();
    }
}
