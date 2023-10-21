package com.example.inflearn.infra.repository.post;

import static com.example.inflearn.domain.QPostHashtag.postHashtag;
import static com.example.inflearn.domain.comment.domain.QComment.comment;
import static com.example.inflearn.domain.hashtag.domain.QHashtag.hashtag;
import static com.example.inflearn.domain.like.domain.QLike.like;
import static com.example.inflearn.domain.member.domain.QMember.member;
import static com.example.inflearn.domain.post.domain.QPost.post;

import com.example.inflearn.domain.post.PostDto;
import com.example.inflearn.infra.repository.dto.projection.PostCommentDto;
import com.example.inflearn.infra.repository.dto.projection.PostHashtagDto;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//todo 중복코드 리팩토링 해야함
//todo Impl클래스를 또 분리해야할수도?
@Slf4j
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<PostDto> getPostsPerPage(int page, int size, String sortCondition) {
        NumberPath<Long> likeCount = Expressions.numberPath(Long.class, "likeCount");
        NumberPath<Long> commentCount = Expressions.numberPath(Long.class, "commentCount");

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
                .orderBy(sort(sortCondition, likeCount, commentCount))
                .limit(size)
                .offset(page)
                .fetch();
    }

    //todo Enum으로 개선?
    private OrderSpecifier<?> sort(String sortCondition, NumberPath<Long> likeCount, NumberPath<Long> commentCount) {
        if (sortCondition == null) {
            return post.id.desc();
        } else if (sortCondition.equalsIgnoreCase("like")) {
            return likeCount.desc();
        } else if (sortCondition.equalsIgnoreCase("comment")) {
            return commentCount.desc();
        }
        return post.id.desc();
    }

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
        NumberPath<Long> likeCount = Expressions.numberPath(Long.class, "likeCount");
        NumberPath<Long> commentCount = Expressions.numberPath(Long.class, "commentCount");

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
                .orderBy(sort(sortCondition, likeCount, commentCount))
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
                .join(postHashtag.hashtag)
                .where(hashtag.hashtagName.eq(searchWord))
                .limit(size)
                .offset(page)
                .fetchOne();
    }

    @Override
    public List<PostDto> findPopularPostByDate(LocalDateTime firstDay, LocalDateTime endDay) {
        NumberPath<Long> likeCount = Expressions.numberPath(Long.class, "likeCount");
        return jpaQueryFactory.select(Projections.fields(PostDto.class,
                        post.id,
                        ExpressionUtils.as(JPAExpressions
                                .select(like.id.count())
                                .from(like)
                                .where(post.id.eq(like.post.id)), likeCount))
                )
                .from(post)
                .where(post.createdAt.between(firstDay, endDay))
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
                        post.id,
                                hashtag.hashtagName))
                .from(postHashtag)
                .join(postHashtag.hashtag)
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
                                .where(post.id.eq(comment.post.id)), "commentCount"),
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
    public List<Long> getPageCount(int page, int size) {
        return jpaQueryFactory.select(post.id)
                .from(post)
                .orderBy(post.id.desc())
                .limit(size)
                .offset(page)
                .fetch();
    }
}
