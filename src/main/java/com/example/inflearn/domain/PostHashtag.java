package com.example.inflearn.domain;

import com.example.inflearn.domain.hashtag.domain.Hashtag;
import com.example.inflearn.domain.post.domain.Post;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

//todo 해당 엔티티는 어떤 패키지에 둬야할까?
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@Getter
//@Table(indexes = {
//        @Index(name = "idx_hashtag_post", columnList = "hashtag_id, post_id")
//})
@Entity
public class PostHashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "hashtag_id")
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
