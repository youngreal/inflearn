package com.example.inflearn.ui.post.dto.response;

import java.util.List;

public record PostResponseWithPageCount(
        List<PostResponse> posts,
        Long pageCount
) {

}
