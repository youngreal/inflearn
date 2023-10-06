package com.example.inflearn.domain.like.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.inflearn.common.exception.AlreadyLikeException;
import com.example.inflearn.common.exception.DoesNotLikeException;
import com.example.inflearn.domain.like.domain.PostLike;
import com.example.inflearn.domain.member.domain.Member;
import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.infra.repository.like.LikeRepository;
import com.example.inflearn.infra.repository.member.MemberRepository;
import com.example.inflearn.infra.repository.post.PostRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @InjectMocks
    private LikeService sut;

    @Mock
    private PostRepository postRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private LikeRepository likeRepository;

    @Test
    @DisplayName("게시글 좋아요 성공")
    void test() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("email@naver.com")
                .password("12345678")
                .build();

        Post post = Post.builder()
                .id(1L)
                .title("제목1234")
                .contents("본문1234")
                .postLikes(new ArrayList<>(List.of(PostLike.create(member, Post.builder()
                        .title("제목2")
                        .contents("본문2")
                        .build())
                )))
                .build();

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(post.getId())).willReturn(Optional.of(post));
        given(likeRepository.findByMemberAndPost(member, post)).willReturn(null);

        // when
        sut.likePost(member.getId(),post.getId());

        // then
        then(likeRepository).should().save(any(PostLike.class));
    }

    @Test
    @DisplayName("게시글 좋아요 실패")
    void like_fail_alreadyLikeException() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("email@naver.com")
                .password("12345678")
                .build();

        Post post = Post.builder()
                .id(1L)
                .title("제목1234")
                .contents("본문1234")
                .build();

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(post.getId())).willReturn(Optional.of(post));
        given(likeRepository.findByMemberAndPost(member, post)).willReturn(
                PostLike.create(member,post));

        // when
        assertThrows(AlreadyLikeException.class, () -> sut.likePost(member.getId(), post.getId()));

        // then
        then(likeRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("게시글 좋아요 취소 성공")
    void unlike_success() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("email@naver.com")
                .password("12345678")
                .build();

        Post post = Post.builder()
                .id(1L)
                .title("제목1234")
                .contents("본문1234")
                .build();

        PostLike postLike = PostLike.create(member, post);

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(post.getId())).willReturn(Optional.of(post));
        given(likeRepository.findByMemberAndPost(member, post)).willReturn(postLike);

        // when
        sut.unlikePost(member.getId(),post.getId());

        // then
        then(likeRepository).should().delete(postLike);
    }

    @Test
    @DisplayName("게시글 좋아요 취소 실패")
    void unlike_fail() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("email@naver.com")
                .password("12345678")
                .build();

        Post post = Post.builder()
                .id(1L)
                .title("제목1234")
                .contents("본문1234")
                .build();

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(post.getId())).willReturn(Optional.of(post));
        given(likeRepository.findByMemberAndPost(member, post)).willReturn(null);

        // when
        assertThrows(DoesNotLikeException.class, () -> sut.unlikePost(member.getId(), post.getId()));

        // then
        then(likeRepository).shouldHaveNoMoreInteractions();
    }

}