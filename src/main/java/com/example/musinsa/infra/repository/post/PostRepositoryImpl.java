package com.example.musinsa.infra.repository.post;

import static com.example.musinsa.domain.post.domain.QPost.post;

import com.example.musinsa.domain.post.domain.Post;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    //todo fetch가 무엇인가
    @Override
    public List<Post> getPostsPerPage(int size, int page) {
        return jpaQueryFactory.selectFrom(post)
                .limit(size)
                .offset((long) (page - 1) * size)
                .orderBy(post.id.desc())
                .fetch();
    }

    @Override
    public List<Post> totalCount() {
        return jpaQueryFactory.selectFrom(post)
                .orderBy(post.id.desc())
                .limit(12000)
                .fetch();
    }
}
