package com.example.inflearn.service.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import autoparams.AutoSource;
import com.example.inflearn.domain.member.Member;
import com.example.inflearn.domain.post.PostDto;
import com.example.inflearn.dto.CommentDto;
import com.example.inflearn.infra.repository.dto.projection.PostHashtagDto;
import com.example.inflearn.infra.mapper.post.PostMapper;
import com.example.inflearn.infra.repository.post.PostRepository;
import com.example.inflearn.domain.post.PostSearch;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
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

    private final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
            .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
            .build();

    @ParameterizedTest
    @AutoSource
    void 페이지_당_조회_시_Post에_해시태그를_추가한다 (int page, int size) {
        // given
        PostDto post = createDto("제목1", "본문1", new HashSet<>(), new ArrayList<>());
        String sort = "like";
        given(paginationService.calculateOffSet(page)).willReturn(0);
        given(postRepository.getPostsPerPage(paginationService.calculateOffSet(page),size,sort)).willReturn(List.of(post));
        given(postRepository.postHashtagsByPostDtos(List.of(post))).willReturn(List.of(new PostHashtagDto(post.getPostId(),"해시태그1"), new PostHashtagDto(post.getPostId(), "해시태그2")));

        // when
        sut.postsPerPage(page, size,sort);

        // then
        assertThat(post.getHashtags().size()).isPositive();
    }


    @ParameterizedTest
    @AutoSource
    void 검색어로_게시글을_검색하면_검색어를_포함하고있는_게시글목록을_반환한다(int page, int size) {
        // given
        PostSearch postSearch = PostSearch.of(page,size,"자바");
        PostDto postDto = createDto("자바스터디1", "자바스터디구합니다", Set.of(), new ArrayList<>());
        given(paginationService.calculateOffSet(postSearch.page())).willReturn(0);
        given(postMapper.search(eq(postSearch.searchWord()), anyInt(), eq(postSearch.size()), eq(postSearch.sort()))).willReturn(List.of(postDto));
        given(postRepository.postHashtagsByPostDtos(List.of(postDto))).willReturn(List.of(new PostHashtagDto(postDto.getPostId(),"자바")));

        // when
        sut.searchPosts(postSearch);

        // then
        assertThat(postDto.getHashtags().size()).isPositive();
    }

    @ParameterizedTest
    @AutoSource
    void 해시태그_검색어로_게시글을_검색하면_검색어를_포함하고있는_게시글목록을_반환한다(int page, int size) {
        // given
        PostSearch postSearch = PostSearch.of(page,size,"자바");
        PostDto postDto = createDto("자바스터디1", "자바스터디구합니다", Set.of(), new ArrayList<>());
        List<Long> postIds = new ArrayList<>(List.of(1L, 2L, 3L));
        given(postRepository.findPostIdsByHashtagSearchWord(postSearch.searchWord())).willReturn(postIds);
        given(postRepository.searchWithHashtag(eq("자바"), anyInt(), eq(postSearch.size()), eq(postSearch.sort()), eq(postIds))).willReturn(List.of(postDto));
        given(paginationService.calculateOffSet(postSearch.page())).willReturn(0);
        given(postRepository.postHashtagsByPostDtos(any())).willReturn(List.of(new PostHashtagDto(postDto.getPostId(), "자바")));

        // when
        sut.searchPostsWithHashtag(postSearch);

        // then
        assertThat(postDto.getHashtags().size()).isPositive();
    }

    private PostDto createDto(String title, String contents, Set<String> hashtags, List<CommentDto> comments) {
        return PostDto.builder()
                .postId(1L)
                .title(title)
                .contents(contents)
                .hashtags(hashtags)
                .comments(comments)
                .build();
    }
}