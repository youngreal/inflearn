package com.example.inflearn.service.like;

import com.example.inflearn.common.exception.AlreadyLikeException;
import com.example.inflearn.common.exception.DoesNotExistMemberException;
import com.example.inflearn.common.exception.DoesNotExistPostException;
import com.example.inflearn.common.exception.DoesNotLikeException;
import com.example.inflearn.domain.like.Like;
import com.example.inflearn.domain.member.Member;
import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.infra.repository.like.LikeRepository;
import com.example.inflearn.infra.repository.member.MemberRepository;
import com.example.inflearn.infra.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class LikeService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;

    //todo 동시성 문제
    public void saveLike(long memberId, long postId) {
        Member member = memberRepository.findById(memberId).orElseThrow(DoesNotExistMemberException::new);
        Post post = postRepository.findById(postId).orElseThrow(DoesNotExistPostException::new);
        likeRepository.findByMemberAndPost(member, post).ifPresent(like -> {
            throw new AlreadyLikeException();
        });
        likeRepository.save(Like.create(member, post));
    }

    public void unLike(long memberId, long postId) {
        Member member = memberRepository.findById(memberId).orElseThrow(DoesNotExistMemberException::new);
        Post post = postRepository.findById(postId).orElseThrow(DoesNotExistPostException::new);
        Like like = likeRepository.findByMemberAndPost(member, post).orElseThrow(DoesNotLikeException::new);
        likeRepository.delete(like);
    }
}
