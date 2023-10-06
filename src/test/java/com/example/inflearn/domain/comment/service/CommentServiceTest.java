package com.example.inflearn.domain.comment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.inflearn.common.exception.DoesNotExistMemberException;
import com.example.inflearn.common.exception.DoesNotExistPostException;
import com.example.inflearn.domain.comment.domain.Comment;
import com.example.inflearn.domain.member.domain.Member;
import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.infra.repository.comment.CommentRepository;
import com.example.inflearn.infra.repository.member.MemberRepository;
import com.example.inflearn.infra.repository.post.PostRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService sut;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private CommentRepository commentRepository;

    @DisplayName("게시글 댓글 작성 성공")
    @Test
    void comment_save_success() {
        // given
        long memberId = 1L;
        long postId = 1L;
        Member member = Member.builder()
                .email("asdf1234@naver.com")
                .password("12345678")
                .build();

        Post post = Post.builder()
                .title("글제목1")
                .contents("글본문1")
                .build();

        Comment comment = Comment.create(member, post, "댓글내용1");

        given(memberRepository.findById(memberId)).willReturn(Optional.ofNullable(member));
        given(postRepository.findById(postId)).willReturn(Optional.ofNullable(post));

        // when
        sut.saveComment(memberId, postId, comment.getContents());

        // then
        then(commentRepository).should().save(any(Comment.class));
    }

    @DisplayName("게시글 댓글 작성 실패 : 게시글이 존재하지않음")
    @Test
    void comment_save_fail() {
        // given
        long memberId = 1L;
        long postId = 1L;
        Member member = Member.builder()
                .email("asdf1234@naver.com")
                .password("12345678")
                .build();

        Post post = Post.builder()
                .title("글제목1")
                .contents("글본문1")
                .build();

        Comment comment = Comment.create(member, post, "댓글내용1");

        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // when
        assertThrows(DoesNotExistMemberException.class,
                () -> sut.saveComment(memberId, postId, comment.getContents()));

        // then
        then(commentRepository).shouldHaveNoMoreInteractions();
    }

    @DisplayName("게시글 댓글 작성 실패 : 회원이 존재하지않음")
    @Test
    void comment_save_fail2() {
        // given
        long memberId = 1L;
        long postId = 1L;
        Member member = Member.builder()
                .email("asdf1234@naver.com")
                .password("12345678")
                .build();

        Post post = Post.builder()
                .title("글제목1")
                .contents("글본문1")
                .build();

        Comment comment = Comment.create(member, post, "댓글내용1");

        given(memberRepository.findById(memberId)).willReturn(Optional.ofNullable(member));
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // when
        assertThrows(DoesNotExistPostException.class,
                () -> sut.saveComment(memberId, postId, comment.getContents()));

        // then
        then(commentRepository).shouldHaveNoMoreInteractions();
    }

}