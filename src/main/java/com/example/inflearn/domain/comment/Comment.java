package com.example.inflearn.domain.comment;

import com.example.inflearn.domain.member.Member;
import com.example.inflearn.domain.post.domain.Post;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
/*
https://stackoverflow.com/questions/76490128/class-org-hibernate-mapping-basicvalue-cannot-be-cast-to-class-org-hibernate-map
hibernate final 버전이슈로 Entity간 관계설정시 단방향으로 mappedBy사용시 생기는 오류, final버전이후 6.3버전에선 수정되어 나온다고 하지만 현재 버전을 그대로 사용하기위해 댓글과 답글을 그냥 양방향으로 적용한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String contents;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parentComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.PERSIST)
    private List<Comment> childComments = new ArrayList<>();

    @Builder
    private Comment(Long id, String contents, LocalDateTime createdAt, Comment parentComment,
            Member member, Post post, List<Comment> childComments) {
        this.id = id;
        this.contents = contents;
        this.createdAt = createdAt;
        this.parentComment = parentComment;
        this.member = member;
        this.post = post;
        this.childComments = childComments;
    }

    public static Comment createComment(Member member, Post post, String contents) {
        return Comment.builder()
                .contents(contents)
                .createdAt(LocalDateTime.now())
                .parentComment(null)
                .member(member)
                .post(post)
                .childComments(new ArrayList<>())
                .build();
    }

    public void addReply(Comment reply) {
        reply.parentComment = this;
        this.childComments.add(reply);
    }
}
