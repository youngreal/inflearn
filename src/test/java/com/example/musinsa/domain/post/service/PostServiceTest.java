package com.example.musinsa.domain.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.musinsa.common.exception.DoesNotExistMemberException;
import com.example.musinsa.common.exception.DoesNotExistPostException;
import com.example.musinsa.domain.member.domain.Member;
import com.example.musinsa.domain.post.domain.Post;
import com.example.musinsa.infra.repository.member.MemberRepository;
import com.example.musinsa.infra.repository.post.PostRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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


    @Test
    @DisplayName("포스트 작성 성공")
    void write_success() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("asdf1234@naver.com")
                .password("12345678")
                .build();

        Post post = Post.builder()
                .title("글제목1")
                .contents("글내용1")
                .build();

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));

        // when
        sut.write(post,member.getId());

        // then
        assertThat(post.getMember()).isEqualTo(member);
        then(postRepository).should().save(post);
    }

    @Test
    @DisplayName("포스트 작성 실패 : 존재하지 않는 유저")
    void write_fail() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("asdf1234@naver.com")
                .password("12345678")
                .build();

        Post post = Post.builder()
                .title("글제목1")
                .contents("글내용1")
                .build();

        given(memberRepository.findById(member.getId())).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistMemberException.class, () -> sut.write(post, member.getId()));
        assertThat(post.getMember()).isNotEqualTo(member);
        then(postRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("포스트 수정 성공")
    void update_success() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("asdf1234@naver.com")
                .password("12345678")
                .build();

        Post post = Post.builder()
                .id(1L)
                .title("글제목1")
                .contents("글내용1")
                .member(member)
                .build();

        Post updatePost = Post.builder()
                .id(1L)
                .title("수정제목1")
                .contents("수정내용1")
                .build();

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(updatePost.getId())).willReturn(Optional.of(post));

        // when
        sut.update(updatePost,member.getId());

        // then
        assertThat(post.getTitle()).isEqualTo("수정제목1");
        assertThat(post.getContents()).isEqualTo("수정내용1");
    }

    @Test
    @DisplayName("포스트 수정 실패 : 존재하지 않는 유저")
    void update_fail() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("asdf1234@naver.com")
                .password("12345678")
                .build();

        Post updatePost = Post.builder()
                .title("수정제목1")
                .contents("수정내용1")
                .build();

        given(memberRepository.findById(member.getId())).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistMemberException.class, () -> sut.update(updatePost, member.getId()));
    }

    @Test
    @DisplayName("포스트 수정 실패 : 존재하지 않는 게시글")
    void update_fail2() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("asdf1234@naver.com")
                .password("12345678")
                .build();

        Post updatePost = Post.builder()
                .title("수정제목1")
                .contents("수정내용1")
                .build();

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(updatePost.getId())).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistPostException.class, () -> sut.update(updatePost, member.getId()));
    }
}