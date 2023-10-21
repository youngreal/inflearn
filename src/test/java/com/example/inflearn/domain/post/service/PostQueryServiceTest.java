package com.example.inflearn.domain.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.inflearn.common.exception.DoesNotExistPostException;
import com.example.inflearn.domain.post.PostDto;
import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.infra.repository.dto.projection.PostCommentDto;
import com.example.inflearn.infra.repository.dto.projection.PostHashtagDto;
import com.example.inflearn.infra.mapper.post.PostMapper;
import com.example.inflearn.infra.repository.post.PostRepository;
import com.example.inflearn.domain.post.PostSearch;
import java.util.ArrayList;
import java.util.HashSet;
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

    @Mock
    private PostMapper postMapper;

    @DisplayName("게시글 상세정보 조회시 조회수가 상승하고, 해시태그와 댓글이 객체에 입력된다")
    @Test
    void post_detail_success_input_hashtag_and_comment() {
        // given
        long postId = 1;
        PostDto postDto = PostDto.builder()
                .title("글제목")
                .contents("글내용")
                .hashtags(new HashSet<>())
                .comments(new ArrayList<>())
                .build();
        Post post = postDto.toEntityForWrite();

        List<PostHashtagDto> postHashtagDtos = new ArrayList<>(List.of(PostHashtagDto.create("자바"), PostHashtagDto.create("스프링")));
        List<PostCommentDto> postCommentDtos = new ArrayList<>(List.of(PostCommentDto.create("댓글1"), PostCommentDto.create("댓글2")));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(postRepository.postDetail(postId)).willReturn(postDto);
        given(postRepository.postHashtagsBy(postDto)).willReturn(postHashtagDtos);
        given(postRepository.commentsBy(postDto)).willReturn(postCommentDtos);

        // when
        PostDto actual = sut.postDetail(postId);

        // then
        assertThat(post.getViewCount()).isEqualTo(postDto.getViewCount() + 1);
        assertThat(actual.getHashtags().size()).isEqualTo(postHashtagDtos.size());
        assertThat(actual.getComments().size()).isEqualTo(postCommentDtos.size());
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


    @DisplayName("페이지당 게시글 조회 성공")
    @Test
    void post_view_per_page_success () {
        // given
        PostDto post = PostDto.builder()
                .title("게시글제목1")
                .contents("게시글본문1")
                .build();

        PostDto post2 = PostDto.builder()
                .title("게시글제목2")
                .contents("게시글본문2")
                .build();

        PostDto post3 = PostDto.builder()
                .title("게시글제목3")
                .contents("게시글본문3")
                .build();

        int page = 1;
        int size = 20;
        String sort = "like";

        given(paginationService.calculateOffSet(page)).willReturn(0);
        given(postRepository.getPostsPerPage(paginationService.calculateOffSet(page),size,sort)).willReturn(List.of(post,post2,post3));

        // when
        List<PostDto> actual = sut.getPostsPerPage(page, size,sort);

        // then
        assertThat(actual).hasSize(3);
        assertThat(actual.get(0)).isEqualTo(post);
        assertThat(actual.get(1)).isEqualTo(post2);
        assertThat(actual.get(2)).isEqualTo(post3);
    }


    @DisplayName("검색어를 포함해 게시글을 검색하면 해당 검색어를 포함하고있는 게시글들을 반환한다")
    @Test
    void 검색어로_게시글을_검색하면_검색어를_포함하고있는_게시글목록을_반환한다() {
        // given
        PostSearch postSearch = PostSearch.of(1,20,"자바");
        PostDto postDto = PostDto.builder()
                .title("자바스터디1")
                .contents("자바스터디구합니다")
                .hashtags(Set.of())
                .build();

        List<PostHashtagDto> postHashtagDtos = new ArrayList<>(List.of(new PostHashtagDto(1L, "자바")));
        List<PostDto> postDto1 = List.of(postDto);

        // when
        given(paginationService.calculateOffSet(postSearch.page())).willReturn(0);
        given(postMapper.search(eq(postSearch.searchWord()), anyInt(), eq(postSearch.size()), eq(postSearch.sort()))).willReturn(postDto1);
        given(postRepository.postHashtagsByPostDtos(any())).willReturn(postHashtagDtos);

        // when
        List<PostDto> actual = sut.searchPost(postSearch);

        // then
        assertThat(actual).isEqualTo(List.of(postDto));
        assertThat(actual.get(0).getHashtags()).isNotNull();
        assertThat(actual).hasSize(1);
    }

    @DisplayName("검색어를 포함해 게시글을 검색하면 해당 페이지에 표시될 페이지 개수를 계산할수있도록 total을 전해준다")
    @Test
    void page_count_success() {
        // given
        PostSearch postSearch = PostSearch.of(1,20,"자바");

        // when
        given(paginationService.offsetWhenGetPageNumbers(postSearch.page())).willReturn(0);
        given(paginationService.sizeWhenGetPageNumbers(postSearch.size())).willReturn(postSearch.size() * 10);
        given(postRepository.countPageWithSearchWord(postSearch.searchWord(),paginationService.offsetWhenGetPageNumbers(postSearch.page()), paginationService.sizeWhenGetPageNumbers(postSearch.size()))).willReturn(1L);

        // when
        Long actual = sut.getPageCountWithSearchWord(postSearch);

        // then
        then(postRepository).should().countPageWithSearchWord(postSearch.searchWord(), paginationService.offsetWhenGetPageNumbers(postSearch.page()), paginationService.sizeWhenGetPageNumbers(postSearch.size()));
        assertThat(actual).isEqualTo(1L);
    }

    @DisplayName("검색어를 포함해 게시글을 검색하면 해당 검색어를 포함하고있는 게시글들을 반환한다")
    @Test
    void 해시태그_검색어로_게시글을_검색하면_검색어를_포함하고있는_게시글목록을_반환한다() {
        // given
        PostSearch postSearch = PostSearch.of(1,20,"자바");
        PostDto postDto = PostDto.builder()
                .title("자바스터디1")
                .contents("자바스터디구합니다")
                .hashtags(Set.of())
                .build();

        List<Long> postIds = new ArrayList<>(List.of(1L, 2L, 3L));
        List<PostHashtagDto> postHashtagDtos = new ArrayList<>(List.of(new PostHashtagDto(1L, "자바")));
        List<PostDto> searchResults = List.of(postDto);
        given(postRepository.findPostIdsByHashtagSearchWord(postSearch.searchWord())).willReturn(postIds);
        given(postRepository.searchWithHashtag(eq(postSearch.searchWord()), anyInt(), eq(postSearch.size()), eq(postSearch.sort()), eq(postIds))).willReturn(searchResults);
        given(paginationService.calculateOffSet(postSearch.page())).willReturn(0);
        given(postRepository.postHashtagsByPostDtos(any())).willReturn(postHashtagDtos);

        // when
        List<PostDto> actual = sut.searchPostWithHashtag(postSearch);

        // then
        assertThat(actual).isEqualTo(List.of(postDto));
        assertThat(actual.get(0).getHashtags()).isNotNull();
        assertThat(actual).hasSize(1);
    }

    @DisplayName("해시태그 검색어를 포함해 게시글을 검색하면 해당 페이지에 표시될 페이지 개수를 계산할수있도록 total을 전해준다")
    @Test
    void page_count_success_with_hashtag_search() {
        // given
        PostSearch postSearch = PostSearch.of(1,20,"자바");

        // when
        given(paginationService.offsetWhenGetPageNumbers(postSearch.page())).willReturn(0);
        given(paginationService.sizeWhenGetPageNumbers(postSearch.size())).willReturn(postSearch.size() * 10);
        given(postRepository.countPageWithHashtagSearchWord(postSearch.searchWord(),paginationService.offsetWhenGetPageNumbers(postSearch.page()), paginationService.sizeWhenGetPageNumbers(postSearch.size()))).willReturn(1L);

        // when
        Long actual = sut.getPageCountWithHashtagSearchWord(postSearch);

        // then
        then(postRepository).should().countPageWithHashtagSearchWord(postSearch.searchWord(), paginationService.offsetWhenGetPageNumbers(postSearch.page()), paginationService.sizeWhenGetPageNumbers(postSearch.size()));
        assertThat(actual).isEqualTo(1L);
    }
}