package com.example.inflearn.controller.post.dto.request;

import com.example.inflearn.common.annotation.validation.sort.SortEnum;
import com.example.inflearn.domain.post.PostSort;
import jakarta.validation.constraints.Min;

public record PostPaging(
        @Min(1)
        int page,
        @Min(1)
        int size,
        @SortEnum(enumClass = PostSort.class, ignoreCase = true)
        String sort
) {
}
