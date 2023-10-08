package com.example.inflearn.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.inflearn.common.exception.CannotCreateReplyException;
import com.example.inflearn.common.exception.DoesNotExistCommentException;
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
import org.springframework.test.util.ReflectionTestUtils;

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

        Comment comment = Comment.createComment(member, post, "댓글내용1");

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

        Comment comment = Comment.createComment(member, post, "댓글내용1");

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

        Comment comment = Comment.createComment(member, post, "댓글내용1");

        given(memberRepository.findById(memberId)).willReturn(Optional.ofNullable(member));
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // when
        assertThrows(DoesNotExistPostException.class,
                () -> sut.saveComment(memberId, postId, comment.getContents()));

        // then
        then(commentRepository).shouldHaveNoMoreInteractions();
    }

    @DisplayName("게시글 답글 작성 성공")
    @Test
    void reply_save_success() {
        // given
        long memberId = 1L;
        long commentId = 1L;
        Member member = Member.builder()
                .email("asdf1234@naver.com")
                .password("12345678")
                .build();

        Post post = Post.builder()
                .title("글제목1")
                .contents("글본문1")
                .build();

        Comment parentComment = Comment.createComment(member, post, "댓글내용1");
        Comment reply = Comment.createComment(member, parentComment.getPost(), "답글내용1");

        given(memberRepository.findById(memberId)).willReturn(Optional.ofNullable(member));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(parentComment));

        // when
        sut.saveReply(memberId, commentId, reply.getContents());

        // then
        assertThat(parentComment.getChildComments().get(0).getContents()).isEqualTo(reply.getContents());
        assertThat(parentComment.getChildComments().get(0).getParentComment()).isEqualTo(parentComment);
    }

    @DisplayName("게시글 답글 작성 실패 : 부모댓글이 존재하지않음")
    @Test
    void reply_save_fail() {
        // given
        long memberId = 1L;
        long commentId = 1L;
        Member member = Member.builder()
                .email("asdf1234@naver.com")
                .password("12345678")
                .build();

        Post post = Post.builder()
                .title("글제목1")
                .contents("글본문1")
                .build();

        Comment comment = Comment.createComment(member, post, "댓글내용1");

        given(memberRepository.findById(memberId)).willReturn(Optional.ofNullable(member));
        given(commentRepository.findById(commentId)).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistCommentException.class,
                () -> sut.saveReply(memberId, commentId, comment.getContents()));
    }

    @DisplayName("게시글 답글 작성 실패 : 회원이 존재하지않음")
    @Test
    void reply_save_fail2() {
        // given
        long memberId = 1L;
        long commentId = 1L;
        Member member = Member.builder()
                .email("asdf1234@naver.com")
                .password("12345678")
                .build();

        Post post = Post.builder()
                .title("글제목1")
                .contents("글본문1")
                .build();

        Comment comment = Comment.createComment(member, post, "댓글내용1");
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistMemberException.class,
                () -> sut.saveReply(memberId, commentId, comment.getContents()));
    }

    @DisplayName("게시글 답글 작성 실패 : 답글에는 또다시 답글을 달수 없음")
    @Test
    void reply_save_fail3() {
        // given
        long memberId = 1L;
        long commentId = 1L;
        Member member = Member.builder()
                .email("asdf1234@naver.com")
                .password("12345678")
                .build();

        Post post = Post.builder()
                .title("글제목1")
                .contents("글본문1")
                .build();

        Comment comment = Comment.createComment(member, post, "댓글내용1");
        ReflectionTestUtils.setField(comment, "parentComment", comment);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when & then
        assertThrows(CannotCreateReplyException.class,
                () -> sut.saveReply(memberId, commentId, comment.getContents()));
    }

}