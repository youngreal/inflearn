package com.example.inflearn.service.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.inflearn.common.exception.DoesNotExistMemberException;
import com.example.inflearn.common.exception.DoesNotExistPostException;
import com.example.inflearn.common.exception.UnAuthorizationException;
import com.example.inflearn.domain.post.domain.PopularPost;
import com.example.inflearn.infra.repository.dto.projection.PostCommentDto;
import com.example.inflearn.infra.repository.dto.projection.PostHashtagDto;
import com.example.inflearn.infra.repository.post.PopularPostRepository;
import com.example.inflearn.service.hashtag.HashtagService;
import com.example.inflearn.domain.member.Member;
import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.domain.post.PostDto;
import com.example.inflearn.infra.repository.member.MemberRepository;
import com.example.inflearn.infra.repository.post.PostRepository;
import com.example.inflearn.controller.post.dto.request.PostUpdateRequest;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService sut;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PopularPostRepository popularPostRepository;

    @Mock
    private PostMemoryService postMemoryService;

    @Mock
    private HashtagService hashtagService;

    private final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
            .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
            .build();

    private final FixtureMonkey fixtureMonkeyForRecord = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .build();

    private final Member member = fixtureMonkey.giveMeBuilder(Member.class).set("id",1L).sample();
    private PostDto postDto;
    private Post post;

    @Test
    void 게시글_작성_성공_DB에_없는_해시태그_입력하는_경우() {
        // given
        postDto = fixtureMonkeyForRecord.giveMeBuilder(PostDto.class)
                .set("hashtags", Set.of("새로운자바", "새로운스프링"))
                .sample();
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));

        // when
        sut.write(postDto,member.getId());

        // then
        then(hashtagService).should().saveHashtags(any(Post.class), eq(postDto.getHashtags()));
        ArgumentCaptor<Post> savedPost = ArgumentCaptor.forClass(Post.class);
        then(postRepository).should().save(savedPost.capture());
        Post post = savedPost.getValue();
        assertThat(post.getMember()).isEqualTo(member);
    }

    @Test
    void 게시글_작성_성공_해시태그_없는경우() {
        // given
        PostDto postDto = writeDto("글제목1", "글내용1", null);
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));

        // when
        sut.write(postDto,member.getId());

        // then
        then(hashtagService).shouldHaveNoInteractions();
        ArgumentCaptor<Post> savedPost = ArgumentCaptor.forClass(Post.class);
        then(postRepository).should().save(savedPost.capture());
        Post post = savedPost.getValue();
        assertThat(post.getMember()).isEqualTo(member);
    }

    @Test
    void 게시글_작성_실패_존재하지_않는_유저() {
        // given
        postDto = fixtureMonkeyForRecord.giveMeBuilder(PostDto.class)
                .set("hashtags", null)
                .sample();
        given(memberRepository.findById(member.getId())).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistMemberException.class, () -> sut.write(postDto, member.getId()));
    }
    
    @MethodSource
    @ParameterizedTest
    void 게시글_수정_성공(Set<String> input) {
        // given
        post = fixtureMonkey.giveMeBuilder(Post.class).setNotNull("id").set("member", member).sample();
        PostUpdateRequest request = fixtureMonkeyForRecord.giveMeBuilder(PostUpdateRequest.class).set("hashtags", Set.of("java", "spring")).sample();
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(post.getId())).willReturn(Optional.of(post));

        // when
        sut.update(request.toDto(input),member.getId(), post.getId());

        // then
        assertThat(post.getTitle()).isEqualTo(request.title());
        assertThat(post.getContents()).isEqualTo(request.contents());
    }

    static Stream<Arguments> 게시글_수정_성공() {
        return Stream.of(
                arguments(Set.of("java", "spring")),
                arguments(Set.of("java")),
                arguments(Set.of("java", "spring", "aws"))
        );
    }

    @Test
    void 게시글_수정_실패_존재하지_않는_유저() {
        // given
        PostUpdateRequest request = fixtureMonkeyForRecord.giveMeBuilder(PostUpdateRequest.class).set("hashtags", new HashSet<>()).sample();
        long requestPostId = 1L;

        given(memberRepository.findById(member.getId())).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistMemberException.class, () -> sut.update(request.toDto(request.hashtags()), member.getId(), requestPostId));
    }

    @Test
    void 게시글_수정_실패_존재하지_않는_게시글() {
        // given
        PostUpdateRequest request = fixtureMonkeyForRecord.giveMeBuilder(PostUpdateRequest.class).set("hashtags", new HashSet<>()).sample();
        long requestPostId = 1L;

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(requestPostId)).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistPostException.class, () -> sut.update(request.toDto(request.hashtags()), member.getId(), requestPostId));
    }

    @Test
    void 게시글_수정_실패_권한이_없는_유저의_수정요청() {
        // given
        Member member2 = fixtureMonkey.giveMeBuilder(Member.class).set("id", 2L).sample();
        post = fixtureMonkey.giveMeBuilder(Post.class).set("member", member2).setNotNull("id").sample();
        PostUpdateRequest request = fixtureMonkeyForRecord.giveMeBuilder(PostUpdateRequest.class).set("hashtags", new HashSet<>()).sample();
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(post.getId())).willReturn(Optional.of(post));

        // when & then
        assertThrows(UnAuthorizationException.class, () -> sut.update(request.toDto(request.hashtags()), member.getId(), post.getId()));
    }

    @Test
    void 게시글_상세정보_조회시_인기글인_경우_메모리에_조회수를_카운팅한다() {
        // given
        post = fixtureMonkey.giveMeBuilder(Post.class)
                .set("member", null)
                .setNotNull("id")
                .sample();
        postDto = fixtureMonkeyForRecord.giveMeBuilder(PostDto.class)
                .set("hashtags", new HashSet<>())
                .set("comments",new ArrayList<>())
                .sample();

        List<PostHashtagDto> postHashtagDtos = new ArrayList<>(List.of(PostHashtagDto.create("자바"), PostHashtagDto.create("스프링")));
        List<PostCommentDto> postCommentDtos = new ArrayList<>(List.of(PostCommentDto.create("댓글1"), PostCommentDto.create("댓글2")));
        given(popularPostRepository.findByPostId(post.getId())).willReturn(PopularPost.of(post.getId()));
        given(postRepository.postDetailWithoutCountQuery(post.getId())).willReturn(postDto);
        given(postRepository.postHashtagsBy(postDto)).willReturn(postHashtagDtos);
        given(postRepository.commentsBy(postDto)).willReturn(postCommentDtos);

        // when
        PostDto actual = sut.postDetail(post.getId());

        // then
        then(postMemoryService).should().addViewCount(post.getId());
        assertThat(actual.getHashtags().size()).isEqualTo(postHashtagDtos.size());
        assertThat(actual.getComments().size()).isEqualTo(postCommentDtos.size());
    }

    @Test
    void 게시글_상세정보_조회시_게시글이_존재하지않으면_예외가_발생한다 () {
        // given
        long postId = 1;
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistPostException.class, () -> sut.postDetail(postId));
    }

    private PostDto writeDto(String title, String contents, Set<String> hashtags) {
        return PostDto.builder()
                .title(title)
                .contents(contents)
                .hashtags(hashtags)
                .build();
    }
}