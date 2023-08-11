package com.example.musinsa.ui.post.dto.request;

import com.example.musinsa.dto.PostDto;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Builder;

@Builder
public record PostUpdateRequest(
        @NotBlank
        String title,
        List<String> hashTags,
        @NotBlank
        String contents
) {

    public PostDto toDto() {
        return PostDto.builder()
                .title(this.title)
                .hashTags(new HashSet<>())
                .contents(this.contents)
                .build();
    }

    public PostDto toDtoWithHashtag(Set<String> hashtags) {
        return PostDto.builder()
                .title(this.title)
                .hashTags(hashtags)
                .contents(this.contents)
                .build();
    }
}
