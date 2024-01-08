package com.example.inflearn.service.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.inflearn.common.exception.CannotCreateReplyException;
import com.example.inflearn.common.exception.DoesNotExistCommentException;
import com.example.inflearn.common.exception.DoesNotExistMemberException;
import com.example.inflearn.common.exception.DoesNotExistPostException;
import com.example.inflearn.domain.comment.Comment;
import com.example.inflearn.domain.member.Member;
import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.infra.repository.comment.CommentRepository;
import com.example.inflearn.infra.repository.member.MemberRepository;
import com.example.inflearn.infra.repository.post.PostRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    private final Member member = createMember();
    private final Post post = createPost();
    private final Comment comment = Comment.createComment(member, post, "댓글내용1");

    @InjectMocks
    private CommentService sut;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private CommentRepository commentRepository;

    @Test
    void 게시글_댓글_작성_성공() {
        // given
        long memberId = 1L;
        long postId = 1L;
        given(memberRepository.findById(memberId)).willReturn(Optional.ofNullable(member));
        given(postRepository.findById(postId)).willReturn(Optional.ofNullable(post));

        // when
        sut.saveComment(memberId, postId, comment.getContents());

        // then
        then(commentRepository).should().save(any(Comment.class));
    }

    @Test
    void 게시글_댓글_작성_실패_존재하지_않는_게시글() {
        // given
        long memberId = 1L;
        long postId = 1L;
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // when
        assertThrows(DoesNotExistMemberException.class,
                () -> sut.saveComment(memberId, postId, comment.getContents()));

        // then
        then(commentRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void 게시글_댓글_작성_실패_존재하지_않는_유저() {
        // given
        long memberId = 1L;
        long postId = 1L;
        given(memberRepository.findById(memberId)).willReturn(Optional.ofNullable(member));
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // when
        assertThrows(DoesNotExistPostException.class,
                () -> sut.saveComment(memberId, postId, comment.getContents()));

        // then
        then(commentRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void 게시글_답글_작성_성공() {
        // given
        long memberId = 1L;
        long commentId = 1L;
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

    @Test
    void 게시글_답글_작성_실패_존재하지_않는_부모댓글() {
        // given
        long memberId = 1L;
        long commentId = 1L;
        given(memberRepository.findById(memberId)).willReturn(Optional.ofNullable(member));
        given(commentRepository.findById(commentId)).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistCommentException.class,
                () -> sut.saveReply(memberId, commentId, comment.getContents()));
    }

    @Test
    void 게시글_답글_작성_실패_존재하지_않는_회원() {
        // given
        long memberId = 1L;
        long commentId = 1L;
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistMemberException.class,
                () -> sut.saveReply(memberId, commentId, comment.getContents()));
    }

    @Test
    void 게시글_답글_작성_실패_중복_답글을_작성할수_없음() {
        // given
        long memberId = 1L;
        long commentId = 1L;
        ReflectionTestUtils.setField(comment, "parentComment", comment);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when & then
        assertThrows(CannotCreateReplyException.class,
                () -> sut.saveReply(memberId, commentId, comment.getContents()));
    }

    private Member createMember() {
        return Member.builder()
                .email("asdf1234@naver.com")
                .password("12345678")
                .build();
    }

    private Post createPost() {
        return Post.builder()
                .title("글제목1")
                .contents("글본문1")
                .build();
    }
}