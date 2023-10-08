package com.example.inflearn.domain.like.domain;

import com.example.inflearn.domain.comment.domain.Comment;
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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "likes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Like {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Comment comment;

    @Builder
    private Like(Long id, Member member, Post post, Comment comment) {
        this.id = id;
        this.member = member;
        this.post = post;
        this.comment = comment;
    }

    public static Like create(Member member, Post post) {
        return Like.builder()
                .member(member)
                .post(post)
                .build();
    }

    public void addMemberAndPost() {
        member.getLikes().add(this);
        post.getLikes().add(this);
    }
}
