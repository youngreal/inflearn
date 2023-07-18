package com.example.musinsa.domain.post.domain;

import com.example.musinsa.domain.Comment;
import com.example.musinsa.domain.HashTag;
import com.example.musinsa.domain.Recommend;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Post {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // 글 제목

    @Column(nullable = false) //todo @Lob?
    private String contents; // 글내용

    private int viewCount; // 조회수

    private LocalDateTime createdAt; // 작성날짜
    private LocalDateTime updatedAt; // 수정날짜

    @Enumerated(EnumType.STRING)
    private PostStatus postStatus; // 글 해결 여부

    @OneToMany
    private List<Recommend> recommends = new ArrayList<>(); // 추천수

    @OneToMany
    private List<HashTag> tags = new ArrayList<>(); //해시태그

    @OneToMany
    private List<Comment> comments = new ArrayList<>();  // 댓글

    @Builder
    public Post(Long id, String title, String contents, int viewCount, LocalDateTime createdAt,
            LocalDateTime updatedAt, PostStatus postStatus, List<Recommend> recommends,
            List<HashTag> tags, List<Comment> comments) {
        this.id = id;
        this.title = title;
        this.contents = contents;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.postStatus = postStatus;
        this.recommends = recommends;
        this.tags = tags;
        this.comments = comments;
    }
}
