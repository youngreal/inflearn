package com.example.inflearn.controller.post.dto.request;

import com.example.inflearn.domain.post.PostDto;
import jakarta.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Builder;

@Builder
public record PostUpdateRequest(
        @NotBlank
        String title,
        List<String> hashtags,
        @NotBlank
        String contents
) {

    public PostDto toDto() {
        return PostDto.builder()
                .title(this.title)
                .hashtags(new HashSet<>())
                .contents(this.contents)
                .build();
    }

    public PostDto toDtoWithHashtag(Set<String> hashtags) {
        return PostDto.builder()
                .title(this.title)
                .hashtags(hashtags)
                .contents(this.contents)
                .build();
    }
}
