package com.example.inflearn.infra.repository.like;

import com.example.inflearn.domain.like.domain.PostLike;
import com.example.inflearn.domain.member.domain.Member;
import com.example.inflearn.domain.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<PostLike, Long> {

    PostLike findByMemberAndPost(Member member, Post post);
}
