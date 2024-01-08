package com.example.inflearn.service.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.inflearn.common.exception.DoesNotExistMemberException;
import com.example.inflearn.common.exception.DoesNotExistPostException;
import com.example.inflearn.common.exception.UnAuthorizationException;
import com.example.inflearn.service.hashtag.HashtagService;
import com.example.inflearn.domain.member.Member;
import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.domain.post.PostDto;
import com.example.inflearn.infra.repository.member.MemberRepository;
import com.example.inflearn.infra.repository.post.PostRepository;
import com.example.inflearn.controller.post.dto.request.PostUpdateRequest;
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

    private final Member member = createMember(1L, "asdf1234@naver.com","password12345678");

    @InjectMocks
    private PostService sut;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private HashtagService hashtagService;

    @Test
    void 게시글_작성_성공_DB에_없는_해시태그_입력하는_경우() {
        // given
        PostDto postDto = writeDto("글제목1", "글내용1", Set.of("새로운자바", "새로운스프링"));
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
        PostDto postDto = writeDto("글제목1", "글내용1", new HashSet<>());
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
        PostDto postDto = writeDto("글제목1", "글내용1", null);
        given(memberRepository.findById(member.getId())).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistMemberException.class, () -> sut.write(postDto, member.getId()));
        then(hashtagService).shouldHaveNoInteractions();
        then(postRepository).shouldHaveNoInteractions();
    }
    
    @MethodSource
    @ParameterizedTest
    void 게시글_수정_성공(Set<String> input) {
        // given
        long requestPostId = 1L;
        Post post = createPost(member, "글제목1", "글내용1");
        PostUpdateRequest dto = updateDto("수정제목1", "수정내용1", List.of("java", "spring"));
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(requestPostId)).willReturn(Optional.of(post));

        // when
        sut.update(dto.toDtoWithHashtag(input),member.getId(), requestPostId);

        // then
        then(hashtagService).should().saveHashtagsWhenPostUpdate(any(Post.class), anySet());
        then(hashtagService).should().deleteHashtags(any(),anySet());
        assertThat(post.getTitle()).isEqualTo(dto.title());
        assertThat(post.getContents()).isEqualTo(dto.contents());
    }

    /**
     * input, existingInDB, saveExpected, deleteExpected
     */
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
        PostUpdateRequest dto = updateDto("수정제목1", "수정내용1", new ArrayList<>());
        long requestPostId = 1L;

        given(memberRepository.findById(member.getId())).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistMemberException.class, () -> sut.update(dto.toDto(), member.getId(), requestPostId));
    }

    @Test
    void 게시글_수정_실패_존재하지_않는_게시글() {
        // given
        PostUpdateRequest dto = updateDto("수정제목1", "수정내용1", new ArrayList<>());
        long requestPostId = 1L;

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(requestPostId)).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistPostException.class, () -> sut.update(dto.toDto(), member.getId(), requestPostId));
    }

    @Test
    void 게시글_수정_실패_권한이_없는_유저의_수정요청() {
        // given
        Member member2 = createMember(2L, "qwer1234@naver.com","12345678");
        Post post = createPost(member2,"글제목1", "글내용1");
        PostUpdateRequest dto = updateDto("수정제목1", "수정내용1", new ArrayList<>());
        long requestPostId = 1L;

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(requestPostId)).willReturn(Optional.of(post));

        // when & then
        assertThrows(UnAuthorizationException.class, () -> sut.update(dto.toDto(), member.getId(), requestPostId));
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

    private Member createMember(long id, String email,String password) {
        return Member.builder()
                .id(id)
                .email(email)
                .password(password)
                .build();
    }

    private PostDto writeDto(String title, String contents, Set<String> hashtags) {
        return PostDto.builder()
                .title(title)
                .contents(contents)
                .hashtags(hashtags)
                .build();
    }

    private PostUpdateRequest updateDto(String title, String contents, List<String> hashtags) {
        return PostUpdateRequest.builder()
                .title(title)
                .contents(contents)
                .hashtags(hashtags)
                .build();
    }
}