package com.example.musinsa.ui.post.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

public record PostResponseWithPageNumbers(
        Page<PostResponse> posts,
        List<Integer> pageNumbers
) {

}
