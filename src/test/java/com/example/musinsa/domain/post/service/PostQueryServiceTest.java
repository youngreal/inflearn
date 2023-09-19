package com.example.musinsa.domain.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.musinsa.common.exception.DoesNotExistPostException;
import com.example.musinsa.domain.post.domain.Post;
import com.example.musinsa.infra.repository.post.PostRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class PostQueryServiceTest {

    @InjectMocks
    private PostQueryService sut;

    @Mock
    private PostRepository postRepository;

    @DisplayName("게시글 상세정보를 조회한다")
    @Test
    void 게시글_상세정보를_조회한다 () {
        // given
        long postId = 1;
        Post post = Post.builder()
                .title("글제목")
                .contents("글내용")
                .build();
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when
        Post actual = sut.postDetail(postId);

        // then
        assertThat(actual).isEqualTo(post);
    }

    @DisplayName("게시글 상세정보 조회 실패 : 게시글이 존재하지 않는다")
    @Test
    void 게시글_상세정보_조회시_게시글이_존재하지않으면_예외가_발생한다 () {
        // given
        long postId = 1;
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistPostException.class, () -> sut.postDetail(postId));
    }

    @DisplayName("검색어 없이 게시글을 검색하면 비어있는 게시글 목록을 반환한다")
    @Test
    void 검색어_없이_게시글을_조회하면_비어있는_게시글_목록을_반환한다() {
        // given
        String searchWord = "";
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdAt")));

        // when
        List<Post> actual = sut.searchPost(searchWord, pageable);

        // then
        assertThat(actual).isEmpty();
        then(postRepository).shouldHaveNoInteractions();
    }

    @DisplayName("검색어를 포함해 게시글을 검색하면 해당 검색어를 포함하고있는 게시글들을 반환한다")
    @Test
    void 검색어로_게시글을_검색하면_검색어를_포함하고있는_게시글목록을_반환한다() {
        // given
        String searchWord = "자바";
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdAt")));
        given(postRepository.findByTitleOrContentsContaining(searchWord, searchWord, pageable)).willReturn(List.of());

        // when
        List<Post> actual = sut.searchPost(searchWord, pageable);

        // then
        assertThat(actual).isEmpty();
        then(postRepository).should().findByTitleOrContentsContaining(searchWord, searchWord, pageable);
    }
}