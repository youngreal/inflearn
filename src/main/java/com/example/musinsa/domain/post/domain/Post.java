package com.example.musinsa.domain.post.domain;

import com.example.musinsa.domain.HashTag;
import com.example.musinsa.domain.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    //todo @Lob?
    @Column(nullable = false)
    private String contents; // 글내용

    private int viewCount; // 조회수

    private LocalDateTime createdAt; // 작성날짜
    private LocalDateTime updatedAt; // 수정날짜

    @Enumerated(EnumType.STRING)
    private PostStatus postStatus; // 글 해결 여부

//    @OneToMany
//    private List<Recommend> recommends = new ArrayList<>(); // 추천수
//
    @OneToMany
    private List<HashTag> tags = new ArrayList<>(); //해시태그
//
//    @OneToMany
//    private List<Comment> comments = new ArrayList<>();  // 댓글

    @ManyToOne
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member member;

    @Builder
    public Post(Long id, String title, String contents, int viewCount, LocalDateTime createdAt,
            LocalDateTime updatedAt, PostStatus postStatus,Member member, List<HashTag> tags) {
        this.id = id;
        this.title = title;
        this.contents = contents;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.postStatus = postStatus;
        this.member = member;
        this.tags = tags;
    }

    public void create(Member member) {
        this.member = member;
        this.createdAt = LocalDateTime.now();
    }

    public void update(Post post) {
        this.title = post.title;
        this.contents = post.contents;
        this.updatedAt = LocalDateTime.now();
    }
}
