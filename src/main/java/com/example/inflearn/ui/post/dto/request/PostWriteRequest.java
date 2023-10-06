package com.example.inflearn.ui.post.dto.request;

import com.example.inflearn.dto.PostDto;
import jakarta.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.EqualsAndHashCode;

@Builder
public record PostWriteRequest(
        @NotBlank
        String title,
        @NotBlank
        String contents,
        List<String> hashtags
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
                .contents(this.contents)
                .hashtags(hashtags)
                .build();
    }
}
