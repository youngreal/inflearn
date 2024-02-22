package com.example.inflearn.service.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.inflearn.domain.member.Member;
import com.example.inflearn.domain.post.PostDto;
import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.dto.CommentDto;
import com.example.inflearn.infra.repository.dto.projection.PostHashtagDto;
import com.example.inflearn.infra.mapper.post.PostMapper;
import com.example.inflearn.infra.repository.post.PostRepository;
import com.example.inflearn.domain.post.PostSearch;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
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

    @Test
    void 페이지_당_게시글_조회_성공 () {
        // given
        PostDto post = createDto("게시글제목1", "게시글본문1", new HashSet<>(), new ArrayList<>());
        PostDto post2 = createDto("게시글제목2", "게시글본문2", new HashSet<>(), new ArrayList<>());
        PostDto post3 = createDto("게시글제목3", "게시글본문3", new HashSet<>(), new ArrayList<>());
        int page = 1;
        int size = 20;
        String sort = "like";

        given(paginationService.calculateOffSet(page)).willReturn(0);
        given(postRepository.getPostsPerPage(paginationService.calculateOffSet(page),size,sort)).willReturn(List.of(post,post2,post3));

        // when
        List<PostDto> actual = sut.postsPerPage(page, size,sort);

        // then
        assertThat(actual).hasSize(3);
        assertThat(actual.get(0)).isEqualTo(post);
        assertThat(actual.get(1)).isEqualTo(post2);
        assertThat(actual.get(2)).isEqualTo(post3);
    }


    @Test
    void 검색어로_게시글을_검색하면_검색어를_포함하고있는_게시글목록을_반환한다() {
        // given
        PostSearch postSearch = PostSearch.of(1,20,"자바");
        PostDto postDto = createDto("자바스터디1", "자바스터디구합니다", Set.of(), new ArrayList<>());
        List<PostHashtagDto> postHashtagDtos = new ArrayList<>(List.of(new PostHashtagDto(1L, "자바")));
        List<PostDto> postDto1 = List.of(postDto);

        given(paginationService.calculateOffSet(postSearch.page())).willReturn(0);
        given(postMapper.search(eq(postSearch.searchWord()), anyInt(), eq(postSearch.size()), eq(postSearch.sort()))).willReturn(postDto1);
        given(postRepository.postHashtagsByPostDtos(any())).willReturn(postHashtagDtos);

        // when
        List<PostDto> actual = sut.searchPosts(postSearch);

        // then
        assertThat(actual).isEqualTo(List.of(postDto));
        assertThat(actual.get(0).getHashtags()).isNotNull();
        assertThat(actual).hasSize(1);
    }

    @Test
    void 검색어로_게시글을_검색하면_검색어를_포함하고있는_게시글_총_개수를_반환한다() {
        // given
        PostSearch postSearch = PostSearch.of(1,20,"자바");

        // when
        given(paginationService.offsetForTotalPageNumbers(postSearch.page())).willReturn(0);
        given(paginationService.sizeForTotalPageNumbers(postSearch.size())).willReturn(postSearch.size() * 10);
        given(postRepository.countPageWithSearchWord(postSearch.searchWord(),paginationService.offsetForTotalPageNumbers(postSearch.page()), paginationService.sizeForTotalPageNumbers(postSearch.size()))).willReturn(1L);

        // when
        Long actual = sut.pageCountWithSearchWord(postSearch);

        // then
        then(postRepository).should().countPageWithSearchWord(postSearch.searchWord(), paginationService.offsetForTotalPageNumbers(postSearch.page()), paginationService.sizeForTotalPageNumbers(postSearch.size()));
        assertThat(actual).isEqualTo(1L);
    }

    @Test
    void 해시태그_검색어로_게시글을_검색하면_검색어를_포함하고있는_게시글목록을_반환한다() {
        // given
        PostSearch postSearch = PostSearch.of(1,20,"자바");
        PostDto postDto = createDto("자바스터디1", "자바스터디구합니다", Set.of(), new ArrayList<>());
        List<Long> postIds = new ArrayList<>(List.of(1L, 2L, 3L));
        List<PostHashtagDto> postHashtagDtos = new ArrayList<>(List.of(new PostHashtagDto(1L, "자바")));
        List<PostDto> searchResults = List.of(postDto);
        given(postRepository.findPostIdsByHashtagSearchWord(postSearch.searchWord())).willReturn(postIds);
        given(postRepository.searchWithHashtag(eq(postSearch.searchWord()), anyInt(), eq(postSearch.size()), eq(postSearch.sort()), eq(postIds))).willReturn(searchResults);
        given(paginationService.calculateOffSet(postSearch.page())).willReturn(0);
        given(postRepository.postHashtagsByPostDtos(any())).willReturn(postHashtagDtos);

        // when
        List<PostDto> actual = sut.searchPostsWithHashtag(postSearch);

        // then
        assertThat(actual).isEqualTo(List.of(postDto));
        assertThat(actual.get(0).getHashtags()).isNotNull();
        assertThat(actual).hasSize(1);
    }

    @Test
    void 해시태그_검색어로_게시글을_검색하면_페이지_개수계산을_위한_total을_반환한다() {
        // given
        PostSearch postSearch = PostSearch.of(1,20,"자바");

        // when
        given(paginationService.offsetForTotalPageNumbers(postSearch.page())).willReturn(0);
        given(paginationService.sizeForTotalPageNumbers(postSearch.size())).willReturn(postSearch.size() * 10);
        given(postRepository.countPageWithHashtagSearchWord(postSearch.searchWord(),paginationService.offsetForTotalPageNumbers(postSearch.page()), paginationService.sizeForTotalPageNumbers(postSearch.size()))).willReturn(1L);

        // when
        Long actual = sut.pageCountWithHashtagSearchWord(postSearch);

        // then
        then(postRepository).should().countPageWithHashtagSearchWord(postSearch.searchWord(), paginationService.offsetForTotalPageNumbers(postSearch.page()), paginationService.sizeForTotalPageNumbers(postSearch.size()));
        assertThat(actual).isEqualTo(1L);
    }

    private PostDto createDto(String title, String contents, Set<String> hashtags, List<CommentDto> comments) {
        return PostDto.builder()
                .title(title)
                .contents(contents)
                .hashtags(hashtags)
                .comments(comments)
                .build();
    }

    private Post createPost(Member member, String title, String contents) {
        return Post.builder()
                .id(1L)
                .title(title)
                .contents(contents)
                .member(member)
                .postHashtags(new ArrayList<>())
                .build();
    }
}