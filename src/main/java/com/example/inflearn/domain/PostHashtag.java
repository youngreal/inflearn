package com.example.inflearn.domain;

import com.example.inflearn.domain.hashtag.domain.Hashtag;
import com.example.inflearn.domain.post.domain.Post;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@Getter
@Entity
public class PostHashtag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "hashtag_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Hashtag hashtag;

    @Builder
    private PostHashtag(Long id, Post post, Hashtag hashtag) {
        this.id = id;
        this.post = post;
        this.hashtag = hashtag;
    }

    public static PostHashtag createPostHashtag(Post post, Hashtag hashtag) {
        return PostHashtag.builder()
                .post(post)
                .hashtag(hashtag)
                .build();
    }
}
