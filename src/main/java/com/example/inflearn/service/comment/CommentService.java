package com.example.inflearn.service.comment;

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
        commentRepository.save(Comment.createComment(member, post, contents));
    }

    public void saveReply(long memberId, long commentId, String contents) {
        Member member = memberRepository.findById(memberId).orElseThrow(DoesNotExistMemberException::new);
        Comment parentComment = commentRepository.findById(commentId).orElseThrow(DoesNotExistCommentException::new);
        if (parentComment.getParentComment() != null) {
            throw new CannotCreateReplyException();
        }

        parentComment.addReply(Comment.createComment(member, parentComment.getPost(), contents));
    }
}
