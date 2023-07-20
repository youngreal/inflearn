package com.example.musinsa.domain.post.service;

import com.example.musinsa.common.exception.DoesNotExistMemberException;
import com.example.musinsa.common.exception.DoesNotExistPostException;
import com.example.musinsa.common.exception.UnAuthorizationException;
import com.example.musinsa.domain.member.domain.Member;
import com.example.musinsa.domain.post.domain.Post;
import com.example.musinsa.infra.repository.member.MemberRepository;
import com.example.musinsa.infra.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public void write(Post post, long id) {
        //todo 예외 메시지를 변경하는데 service를 확인해야할까? 리팩토링 해보자
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new DoesNotExistMemberException("존재하지 않는 유저입니다"));

        post.create(member);
        postRepository.save(post);
    }

    public void update(Post post, long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new DoesNotExistMemberException("존재하지 않는 유저입니다"));
        Post newPost = postRepository.findById(post.getId()).orElseThrow(() -> new DoesNotExistPostException("존재하지 않는 게시글입니다"));

        if (!member.getId().equals(newPost.getMember().getId())) {
            throw new UnAuthorizationException("글 수정 권한이 없습니다");
        }

        newPost.update(post);
    }
}
