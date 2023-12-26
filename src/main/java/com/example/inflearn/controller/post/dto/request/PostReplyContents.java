package com.example.inflearn.controller.post.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PostReplyContents(
        @NotBlank
        String contents
) {
}
