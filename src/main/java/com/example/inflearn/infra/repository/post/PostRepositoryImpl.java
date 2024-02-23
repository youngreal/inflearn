package com.example.inflearn.infra.repository.post;

import static com.example.inflearn.domain.comment.QComment.comment;
import static com.example.inflearn.domain.hashtag.QHashtag.hashtag;
import static com.example.inflearn.domain.like.QLike.like;
import static com.example.inflearn.domain.member.QMember.member;
import static com.example.inflearn.domain.post.domain.QPost.post;
import static com.example.inflearn.domain.post.domain.QPostHashtag.postHashtag;

import com.example.inflearn.domain.post.PostDto;
import com.example.inflearn.infra.repository.dto.projection.PopularPostDto;
import com.example.inflearn.infra.repository.dto.projection.PostCommentDto;
import com.example.inflearn.infra.repository.dto.projection.PostHashtagDto;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//todo 중복코드 리팩토링 해야함
//todo Impl클래스를 또 분리해야할수도?
@Slf4j
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final NumberPath<Long> likeCount = Expressions.numberPath(Long.class, "likeCount");
    private final NumberPath<Long> commentCount = Expressions.numberPath(Long.class, "commentCount");

    @Override
    public List<PostDto> getPostsPerPage(int page, int size, String sortCondition) {
        return jpaQueryFactory.select(Projections.fields(PostDto.class,
                        post.id.as("postId"),
                        member.nickname,
                        post.title,
                        post.contents,
                        post.viewCount,
                        ExpressionUtils.as(JPAExpressions
                                .select(like.id.count())
                                .from(like)
                                .where(post.id.eq(like.post.id)), likeCount),
                        ExpressionUtils.as(JPAExpressions
                                .select(comment.id.count())
                                .from(comment)
                                .where(post.id.eq(comment.post.id)), commentCount),
                        post.createdAt,
                        post.updatedAt,
                        post.postStatus)
                )
                .from(post)
                .join(post.member, member)
                .orderBy(sort(sortCondition))
                .limit(size)
                .offset(page)
                .fetch();
    }

    //todo Enum으로 개선?
    /*
    11.06 Enum으로 개선하려고 했는데 Enum에 querydsl에 의존적인 NumberPath같은 클래스가 의존된다(post.id, likeCount) 등을 반환해줘야하기때문에.. 이 경우 해당 Enum의 위치는 infra레이어에 있어야하거나 공통 DTO모듈에 있어야할것같은데.
    수정하는게 좋을지 모르겠다.
     */

    private OrderSpecifier<?> sort(String sortCondition) {
        if (sortCondition == null) {
            return post.id.desc();
        } else if (sortCondition.equalsIgnoreCase("like")) {
            return likeCount.desc();
        } else if (sortCondition.equalsIgnoreCase("comment")) {
            return commentCount.desc();
        }
        return post.id.desc();
    }

    /**
     * todo
     * SELECT
     *     post_id,
     *     post_content,
     *     (SELECT hashtag_name FROM Hashtags WHERE Hashtags.hashtag_id = Post_Hashtags.hashtag_id) AS hashtag_name
     * FROM
     *     Posts
     * WHERE
     *     post_id = [desired_post_id];
     *
     *     조인없이 서브쿼리로도 풀수있다. 나는 왜 조인을썼는가? 명확하게 설명할수있나? 어쩔때 서브쿼리를 쓸것인가?
     */
    @Override
    public List<PostHashtagDto> postHashtagsBy(PostDto postDto) {
        return jpaQueryFactory.select(
                        Projections.constructor(PostHashtagDto.class,
                                post.id,
                                hashtag.hashtagName))
                .from(postHashtag)
                .join(postHashtag.hashtag)
                .where(postHashtag.post.id.eq(postDto.getPostId()))
                .fetch();
    }

    //todo SELECT comment_id, comment_content, comment_date FROM Comments WHERE post_id = [desired_post_id]; 같은 쿼리로도 개선이 가능한데, join대신 테스트해보자
    @Override
    public List<PostCommentDto> commentsBy(PostDto postDto) {
        return jpaQueryFactory.select(
                        Projections.constructor(PostCommentDto.class,
                                comment.id,
                                comment.parentComment.id,
                                comment.contents
                        ))
                .from(comment)
                .join(comment.post)
                .where(comment.post.id.eq(postDto.getPostId()))
                .fetch();
    }

    @Override
    public List<PostDto> searchWithHashtag(String searchWord, int page, int size, String sortCondition, List<Long> postIds) {
        return jpaQueryFactory.select(Projections.fields(PostDto.class,
                        post.id.as("postId"),
                        member.nickname,
                        post.title,
                        post.contents,
                        post.viewCount,
                        ExpressionUtils.as(JPAExpressions
                                .select(like.id.count())
                                .from(like)
                                .where(post.id.eq(like.post.id)), likeCount),
                        ExpressionUtils.as(JPAExpressions
                                .select(comment.id.count())
                                .from(comment)
                                .where(post.id.eq(comment.post.id)), commentCount),
                        post.createdAt,
                        post.updatedAt,
                        post.postStatus)
                )
                .from(post)
                .join(post.member, member)
                .where(post.id.in(postIds))
                .orderBy(sort(sortCondition))
                .limit(size)
                .offset(page)
                .fetch();
    }

    @Override
    public List<Long> findPostIdsByHashtagSearchWord(String searchWord) {
        return jpaQueryFactory.select(postHashtag.post.id)
                .from(postHashtag)
                .join(postHashtag.hashtag)
                .where(hashtag.hashtagName.eq(searchWord))
                .fetch();
    }

    @Override
    public Long countPageWithHashtagSearchWord(String searchWord, int page, int size) {
        return jpaQueryFactory.select(postHashtag.post.id.count())
                .from(postHashtag)
                .join(postHashtag.hashtag, hashtag)
                .where(hashtag.hashtagName.eq(searchWord))
                .limit(size)
                .offset(page)
                .fetchOne();
    }

    @Override
    public List<PopularPostDto> findPopularPostByDate(LocalDate firstDay, LocalDate endDay) {
        return jpaQueryFactory.select(Projections.constructor(PopularPostDto.class,
                                post.id,
                                ExpressionUtils.as(JPAExpressions
                                        .select(like.id.count())
                                        .from(like)
                                        .where(post.id.eq(like.post.id)), likeCount)
                        )
                )
                .from(post)
                .where(post.createdAt.between(firstDay.atStartOfDay(), endDay.atStartOfDay()))
                .groupBy(post.id)
                .orderBy(likeCount.desc())
                .limit(5)
                .fetch();
    }

    @Override
    public List<PostHashtagDto> postHashtagsByPostDtos(List<PostDto> posts) {
        List<Long> postIds = posts.stream().map(PostDto::getPostId).toList();

        return jpaQueryFactory.select(
                        Projections.constructor(PostHashtagDto.class,
                                postHashtag.post.id,
                                hashtag.hashtagName))
                .from(postHashtag)
                .join(postHashtag.hashtag, hashtag)
                .where(postHashtag.post.id.in(postIds))
                .fetch();
    }

    @Override
    public PostDto postDetail(long postId) {
        return jpaQueryFactory.select(Projections.fields(PostDto.class,
                        post.id.as("postId"),
                        member.nickname,
                        post.title,
                        post.contents,
                        post.viewCount,
                        ExpressionUtils.as(JPAExpressions
                                .select(like.id.count())
                                .from(like)
                                .where(like.post.id.eq(postId)), "likeCount"),
                        ExpressionUtils.as(JPAExpressions
                                .select(comment.id.count())
                                .from(comment)
                                .where(comment.post.id.eq(postId)), "commentCount"),
                        post.createdAt,
                        post.updatedAt,
                        post.postStatus)
                )
                .from(post)
                .where(post.id.eq(postId))
                .join(post.member, member)
                .fetchOne();
    }

    @Override
    public PostDto postDetailWithoutCountQuery(long postId) {
        return jpaQueryFactory.select(Projections.fields(PostDto.class,
                                post.id.as("postId"),
                                member.nickname,
                                post.title,
                                post.contents,
                                post.viewCount,
                                post.createdAt,
                                post.updatedAt,
                                post.postStatus)
                )
                .from(post)
                .where(post.id.eq(postId))
                .join(post.member, member)
                .fetchOne();
    }

    @Override
    public Long likeCountWithScheduler(long postId) {
        return jpaQueryFactory.select(like.id.count())
                .from(like)
                .where(like.post.id.eq(postId))
                .fetchOne();
    }

    @Override
    public Long commentCountWithScheduler(long postId) {
        return jpaQueryFactory.select(comment.id.count())
                .from(comment)
                .where(comment.post.id.eq(postId))
                .fetchOne();
    }

    @Override
    public List<Long> getPageCount(int page, int size) {
        return jpaQueryFactory.select(post.id)
                .from(post)
                .orderBy(post.id.desc())
                .limit(size)
                .offset(page)
                .fetch();
    }
}
