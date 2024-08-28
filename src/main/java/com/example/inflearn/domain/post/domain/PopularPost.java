package com.example.inflearn.domain.post.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long postId;

    private long likeCount;

    private PopularPost(long postId, long likeCount) {
        this.postId = postId;
        this.likeCount = likeCount;
    }

    public static PopularPost of(long postId) {
        return new PopularPost(postId, 0);
    }
}
