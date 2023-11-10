package com.example.inflearn.domain.post.domain;

import com.example.inflearn.domain.member.domain.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

//todo Index 컬럼, nullable 컬럼, @Table(uniqueConstraints = ) 조건을 사용해서 유니크컬럼도 지정해보자.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Getter
@Table(indexes = {
        @Index(columnList = "createdAt")
})
@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // 글 제목

    //todo fulltext 인덱스 인걸 어떻게 반영해야할까?
    @Column(nullable = false)
    @Lob
    private String contents; // 글내용

    private long viewCount; // 조회수
    private LocalDateTime createdAt; // 작성날짜
    private LocalDateTime updatedAt; // 수정날짜

    @Enumerated(EnumType.STRING)
    private PostStatus postStatus; // 글 해결 여부

    @ToString.Exclude
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostHashtag> postHashtags = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    private Post(Long id, String title, String contents, long viewCount, LocalDateTime createdAt,
            LocalDateTime updatedAt, PostStatus postStatus, List<PostHashtag> postHashtags,
            Member member) {
        this.id = id;
        this.title = title;
        this.contents = contents;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.postStatus = postStatus;
        this.postHashtags = postHashtags;
        this.member = member;
    }

    //todo 조금 애매하고 헷갈릴수있는 로직
    public void addPostHashtag(PostHashtag postHashtag) {
        this.postHashtags.add(postHashtag);
    }

    public void addMember(Member member) {
        this.member = member;
    }

    public void updateTitleAndContents(String title, String contents) {
        this.title = title;
        this.contents = contents;
        this.updatedAt = LocalDateTime.now();
    }

    public void plusViewCount() {
        this.viewCount += 1;
    }

    public void updateViewCountFromCache(long viewCount) {
        this.viewCount = viewCount;
    }
}
