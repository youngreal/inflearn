package com.example.inflearn.domain.comment.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class CommentService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public void saveComment(long memberId, long postId, String contents) {
        Member member = memberRepository.findById(memberId).orElseThrow(DoesNotExistMemberException::new);
        Post post = postRepository.findById(postId).orElseThrow(DoesNotExistPostException::new);

        Comment comment = Comment.createComment(member, post, contents);
        commentRepository.save(comment);
    }

    public void saveReply(long memberId, long commentId, String contents) {
        Member member = memberRepository.findById(memberId).orElseThrow(DoesNotExistMemberException::new);
        Comment parentComment = commentRepository.findById(commentId).orElseThrow(DoesNotExistCommentException::new);
        if (parentComment.getParentComment() != null) {
            throw new CannotCreateReplyException();
        }

        Comment reply = Comment.createComment(member, parentComment.getPost(), contents);
        parentComment.addReply(reply);
    }
}