package com.example.inflearn.infra.repository.post;

import com.example.inflearn.domain.post.PostDto;
import com.example.inflearn.infra.repository.dto.projection.PopularPostDto;
import com.example.inflearn.infra.repository.dto.projection.PostCommentDto;
import com.example.inflearn.infra.repository.dto.projection.PostHashtagDto;
import java.time.LocalDate;
import java.util.List;

public interface PostRepositoryCustom {

    List<PostDto> getPostsPerPage(int page, int size, String sort);

    List<Long> getPageCount(int page, int size);

    List<PostHashtagDto> postHashtagsBy(PostDto postDto);

    List<PostHashtagDto> postHashtagsByPostDtos(List<PostDto> postDtos);

    PostDto postDetail(long postId);

    PostDto postDetailWithoutCountQuery(long postId);

    Long likeCountWithScheduler(long postId);

    Long commentCountWithScheduler(long postId);
    List<PostCommentDto> commentsBy(PostDto postDto);

    List<PostDto> searchWithHashtag(String searchWord, int page, int size, String sort, List<Long> postIds);

    List<Long> findPostIdsByHashtagSearchWord(String searchWord);

    Long countPageWithHashtagSearchWord(String searchWord, int page, int size);

    List<PopularPostDto> findPopularPostByDate(LocalDate firstDay, LocalDate endDay);

}
