package com.example.inflearn.ui.post.dto.request;

import jakarta.validation.constraints.Min;

public record PostPaging(
        @Min(1)
        int page,
        @Min(1)
        int size
) {

    public static PostPaging create(int page, int size) {
        return new PostPaging(page, size);
    }
}
