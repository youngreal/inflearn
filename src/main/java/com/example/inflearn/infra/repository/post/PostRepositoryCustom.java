package com.example.inflearn.infra.repository.post;

import com.example.inflearn.dto.PostDto;
import com.example.inflearn.infra.repository.dto.projection.PostCommentDto;
import com.example.inflearn.infra.repository.dto.projection.PostHashtagDto;
import java.util.List;

public interface PostRepositoryCustom {

    List<PostDto> getPostsPerPage(int page, int size);

    List<Long> getPageCount(int page, int size);

    List<PostHashtagDto> postHashtagsBy(PostDto postDto);

    List<PostHashtagDto> postHashtagsByPostDtos(List<PostDto> postDtos);

    PostDto postDetail(long postId);

    List<PostCommentDto> commentBy(PostDto postDto);
}
