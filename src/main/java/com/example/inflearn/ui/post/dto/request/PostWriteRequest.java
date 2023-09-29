package com.example.inflearn.ui.post.dto.request;

import com.example.inflearn.dto.PostDto;
import jakarta.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Builder;

@Builder
public record PostWriteRequest(
        @NotBlank
        String title,
        List<String> hashtags,
        @NotBlank
        String contents
) {

    public PostDto toDto() {
        return PostDto.builder()
                .title(this.title)
                .contents(this.contents)
                .hashtags(new HashSet<>())
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