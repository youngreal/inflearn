package com.example.inflearn.domain.comment.domain;

import com.example.inflearn.domain.member.domain.Member;
import com.example.inflearn.domain.post.domain.Post;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
/*
member 1: M comment
post 1 : M comment
comment 1: M reply
comment 1: M like
=> 관계를 안맺으면 하나의 댓글에 여러 좋아요를 누를수있기때문에 관계를 맺자.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contents;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Post post;

    @Builder
    private Comment(Long id, String contents, LocalDateTime createdAt, Member member, Post post) {
        this.id = id;
        this.contents = contents;
        this.createdAt = createdAt;
        this.member = member;
        this.post = post;
    }

    public static Comment create(Member member, Post post, String contents) {
        return Comment.builder()
                .contents(contents)
                .createdAt(LocalDateTime.now())
                .member(member)
                .post(post)
                .build();
    }
}
