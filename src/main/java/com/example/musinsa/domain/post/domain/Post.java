package com.example.musinsa.domain.post.domain;

import com.example.musinsa.domain.PostHashtag;
import com.example.musinsa.domain.member.domain.Member;
import jakarta.persistence.CascadeType;
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
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Slf4j
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

    @ToString.Exclude
    @OneToMany(mappedBy = "post", cascade = CascadeType.PERSIST)
    private List<PostHashtag> postHashtags = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member member;

    @Builder
    private Post(Long id, String title, String contents, int viewCount, LocalDateTime createdAt,
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

    public void addPostHashtag(PostHashtag postHashtag) {
        this.postHashtags.add(postHashtag);
        postHashtag.addPost(this);
    }

    public void addMember(Member member) {
        this.member = member;
        member.getPosts().add(this);
    }

    public void updateTitleAndContents(String title, String contents) {
        this.title = title;
        this.contents = contents;
        this.updatedAt = LocalDateTime.now();
    }
}
