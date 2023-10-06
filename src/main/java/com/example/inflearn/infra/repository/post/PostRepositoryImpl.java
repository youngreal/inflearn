package com.example.inflearn.infra.repository.post;

import static com.example.inflearn.domain.QHashtag.hashtag;
import static com.example.inflearn.domain.QPostHashtag.postHashtag;
import static com.example.inflearn.domain.like.domain.QPostLike.postLike;
import static com.example.inflearn.domain.member.domain.QMember.member;
import static com.example.inflearn.domain.post.domain.QPost.post;

import com.example.inflearn.dto.PostDto;
import com.example.inflearn.dto.PostHashtagDto;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<PostDto> getPostsPerPage(int page, int size) {
        return jpaQueryFactory.select(Projections.fields(PostDto.class,
                        post.id.as("postId"),
                        member.nickname,
                        post.title,
                        post.contents,
                        post.viewCount,
                        ExpressionUtils.as(JPAExpressions
                                .select(postLike.id.count())
                                .from(postLike)
                                .where(post.id.eq(postLike.post.id)), "likeCount"),
                        post.createdAt,
                        post.updatedAt,
                        post.postStatus)
                )
                .from(post)
                .join(post.member, member)
                .orderBy(post.id.desc())
                .limit(size)
                .offset(page)
                .fetch();
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
                                .select(postLike.id.count())
                                .from(postLike)
                                .where(postLike.post.id.eq(postId)), "likeCount"),
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
