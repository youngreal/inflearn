package com.example.inflearn.domain.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.inflearn.common.exception.DoesNotExistPostException;
import com.example.inflearn.dto.PostDto;
import com.example.inflearn.infra.repository.post.PostRepository;
import com.example.inflearn.ui.post.dto.request.PostSearch;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostQueryServiceTest {

    @InjectMocks
    private PostQueryService sut;

    @Mock
    private PaginationService paginationService;

    @Mock
    private PostRepository postRepository;

    @DisplayName("게시글 상세정보를 조회한다")
    @Test
    void 게시글_상세정보를_조회한다 () {
        // given
        long postId = 1;
        PostDto postDto = PostDto.builder()
                .title("글제목")
                .contents("글내용")
                .build();
        given(postRepository.findById(postId)).willReturn(Optional.of(postDto.toEntity()));

        // when
        PostDto actual = sut.postDetail(postId);

        // then
        assertThat(actual).isEqualTo(postDto);
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

    @DisplayName("검색어를 포함해 게시글을 검색하면 해당 검색어를 포함하고있는 게시글들을 반환한다")
    @Test
    void 검색어로_게시글을_검색하면_검색어를_포함하고있는_게시글목록을_반환한다() {
        // given
        PostSearch postSearch = new PostSearch(1, 20, "자바");
        PostDto postDto = PostDto.builder()
                .title("자바스터디1")
                .contents("자바스터디구합니다")
                .hashtags(Set.of())
                .build();

        // when
        given(paginationService.offSetWhenSearchPost(postSearch.page())).willReturn(0);
        given(postRepository.search(postSearch.searchWord(),paginationService.offSetWhenSearchPost(postSearch.page()), postSearch.size())).willReturn(List.of(postDto.toEntity()));

        // when
        List<PostDto> actual = sut.searchPost(postSearch.searchWord(), postSearch.page(), postSearch.size());

        // then
        assertThat(actual).isEqualTo(List.of(postDto));
        assertThat(actual).hasSize(1);
    }

    @DisplayName("검색어를 포함해 게시글을 검색하면 해당 페이지에 표시될 페이지 개수를 계산할수있도록 total을 전해준다")
    @Test
    void page_count_success() {
        // given
        PostSearch postSearch = new PostSearch(1, 20, "자바");

        // when
        given(paginationService.offsetWhenGetPageNumbers(postSearch.page())).willReturn(0);
        given(paginationService.sizeWhenGetPageNumbers(postSearch.size())).willReturn(postSearch.size() * 10);
        given(postRepository.countPage(postSearch.searchWord(),paginationService.offsetWhenGetPageNumbers(postSearch.page()), paginationService.sizeWhenGetPageNumbers(postSearch.size()))).willReturn(1L);

        // when
        Long actual = sut.getPageCount(postSearch.searchWord(), postSearch.page(), postSearch.size());

        // then
        then(postRepository).should().countPage(postSearch.searchWord(), paginationService.offsetWhenGetPageNumbers(postSearch.page()), paginationService.sizeWhenGetPageNumbers(postSearch.size()));
        assertThat(actual).isEqualTo(1L);
    }
}