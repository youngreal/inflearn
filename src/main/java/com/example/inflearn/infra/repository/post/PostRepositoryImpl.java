package com.example.inflearn.infra.repository.post;

import static com.example.inflearn.domain.member.domain.QMember.member;
import static com.example.inflearn.domain.post.domain.QPost.post;

import com.example.inflearn.domain.post.domain.Post;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    //todo fetch가 무엇인가
    //todo 몇초 걸리는지 확인.
    @Override
    public List<Post> getPostsPerPage(int page, int size) {
        return jpaQueryFactory.selectFrom(post)
                .join(post.member, member).fetchJoin()
                .orderBy(post.id.desc())
                .offset(page)
                .limit(size)
                .fetch();
    }

    @Override
    public List<Long> getPageCount(int page, int size) {
        return jpaQueryFactory.select(post.id)
                .from(post)
                .orderBy(post.id.desc())
                .offset(page)
                .limit(size)
                .fetch();
    }
}
