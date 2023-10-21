package com.example.inflearn.domain.like.service;

import com.example.inflearn.common.exception.AlreadyLikeException;
import com.example.inflearn.common.exception.DoesNotExistMemberException;
import com.example.inflearn.common.exception.DoesNotExistPostException;
import com.example.inflearn.common.exception.DoesNotLikeException;
import com.example.inflearn.domain.like.domain.Like;
import com.example.inflearn.domain.member.domain.Member;
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
        Like byMemberAndPost = likeRepository.findByMemberAndPost(member, post);
        if (byMemberAndPost != null) {
            throw new AlreadyLikeException();
        }

        Like like = Like.create(member, post);
        likeRepository.save(like);
    }

    public void unLike(long memberId, long postId) {
        Member member = memberRepository.findById(memberId).orElseThrow(DoesNotExistMemberException::new);
        Post post = postRepository.findById(postId).orElseThrow(DoesNotExistPostException::new);
        Like like = likeRepository.findByMemberAndPost(member, post);
        if (like == null) {
            throw new DoesNotLikeException();
        }

        likeRepository.delete(like);
    }
}
