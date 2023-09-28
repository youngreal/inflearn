package com.example.inflearn.infra.repository.post;

import static com.example.inflearn.domain.post.domain.QPost.post;

import com.example.inflearn.domain.post.domain.Post;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hibernate.boot.model.FunctionContributor;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    //todo fetch가 무엇인가
    //todo 커버링 인덱스로 변환?
    @Override
    public List<Post> getPostsPerPage(int size, int page) {
        return jpaQueryFactory.selectFrom(post)
                .limit(size)
                .offset((long) (page - 1) * size)
                .orderBy(post.id.desc())
                .fetch();
    }

    //todo 게시글 수 변경?
    /**
     * DB에서 제공해줄수있는 최대 게시글의 개수
     * 현재는 12000개가 0.016~0.032초 정도 나오는데 조금더 많은 게시글을 가져올수도 있긴하다.
     */
    @Override
    public List<Post> totalCount() {
        return jpaQueryFactory.select(post)
                .orderBy(post.id.desc())
                .limit(60000)
                .fetch();
    }


//    @Override
//    public List<Post> test(String searchWord) {
//        BooleanExpression searchKeyword = Expressions.numberTemplate(Double.class,
//                "function('match',{0},{1},{2})", post.title, post.contents, searchWord).gt(0);
//
//        return jpaQueryFactory.selectFrom(post)
//                .where(searchKeyword)
//                .limit(20)
//                .fetch();
//    }
}
