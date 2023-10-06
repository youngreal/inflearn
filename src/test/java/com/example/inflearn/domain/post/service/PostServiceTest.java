package com.example.inflearn.domain.post.service;

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
import com.example.inflearn.domain.member.domain.Member;
import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.dto.PostDto;
import com.example.inflearn.infra.repository.member.MemberRepository;
import com.example.inflearn.infra.repository.post.PostRepository;
import com.example.inflearn.ui.post.dto.request.PostUpdateRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService sut;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private HashtagService hashtagService;

    @Test
    @DisplayName("포스트 작성 성공 : DB에 없는 새로운 해시태그를 입력받은경우")
    void write_success() {
        // given
        Member member = createMember(1L, "asdf1234@naver.com","password12345678");
        PostDto postDto = writeDto("글제목1", "글내용1", Set.of("새로운자바", "새로운스프링"));
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));

        // when
        sut.write(postDto,member.getId());

        // then
        then(hashtagService).should().saveNewHashtagsWhenPostWrite(any(Post.class), eq(postDto.getHashtags()));
        ArgumentCaptor<Post> savedPost = ArgumentCaptor.forClass(Post.class);
        then(postRepository).should().save(savedPost.capture());
        Post post = savedPost.getValue();
        assertThat(post.getMember()).isEqualTo(member);
    }

    @Test
    @DisplayName("포스트 작성 성공2 : 글 제목, 본문만 있는경우")
    void write_success2() {
        // given
        Member member = createMember(1L, "asdf1234@naver.com","12345678");
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
    @DisplayName("포스트 작성 실패 : 존재하지 않는 유저")
    void write_fail() {
        // given
        Member member = createMember(1L, "asdf1234@naver.com","12345678");
        PostDto postDto = writeDto("글제목1", "글내용1", null);
        given(memberRepository.findById(member.getId())).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistMemberException.class, () -> sut.write(postDto, member.getId()));
        then(hashtagService).shouldHaveNoInteractions();
        then(postRepository).shouldHaveNoInteractions();
    }
    
    @DisplayName("포스트 수정 성공: FindByHashtagIn 사용시")
    @MethodSource
    @ParameterizedTest
    void update_success5(Set<String> input) {
        // given
        long requestPostId = 1L;
        Member member = createMember(1L, "asdf1234@naver.com","12345678");
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
    static Stream<Arguments> update_success5() {
        return Stream.of(
                arguments(Set.of("java", "spring")),
                arguments(Set.of("java")),
                arguments(Set.of("java", "spring", "aws"))
        );
    }

    @Test
    @DisplayName("포스트 수정 실패 : 존재하지 않는 유저")
    void update_fail() {
        // given
        Member member = createMember(1L, "asdf1234@naver.com","12345678");
        PostUpdateRequest dto = updateDto("수정제목1", "수정내용1", new ArrayList<>());
        long requestPostId = 1L;

        given(memberRepository.findById(member.getId())).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistMemberException.class, () -> sut.update(dto.toDto(), member.getId(), requestPostId));
    }

    @Test
    @DisplayName("포스트 수정 실패 : 존재하지 않는 게시글")
    void update_fail2() {
        // given
        Member member = createMember(1L, "asdf1234@naver.com","12345678");
        PostUpdateRequest dto = updateDto("수정제목1", "수정내용1", new ArrayList<>());
        long requestPostId = 1L;

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(requestPostId)).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistPostException.class, () -> sut.update(dto.toDto(), member.getId(), requestPostId));
    }

    @Test
    @DisplayName("포스트 수정 실패 : 수정권한이 없는 유저")
    void update_fail3() {
        // given
        Member member = createMember(1L, "asdf1234@naver.com","12345678");
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