package com.example.inflearn.infra.repository.post;

import static com.example.inflearn.domain.post.domain.QPost.post;

import com.example.inflearn.domain.post.domain.Post;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    //todo fetch가 무엇인가
    //todo 커버링 인덱스로 변환?
    @Override
    public List<Post> getPostsPerPage(int page, int size) {
        return jpaQueryFactory.selectFrom(post)
                .orderBy(post.id.desc())
                .offset((long) (page - 1) * size)
                .limit(size)
                .fetch();
    }
}
