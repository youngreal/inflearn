package com.example.inflearn.domain.hashtag.domain;

import com.example.inflearn.domain.post.domain.PostHashtag;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode(of = "hashtagName")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Getter
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "hashtag_name")
        }
)
@Entity
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hashtag_name", nullable = false)
    private String hashtagName;

    @ToString.Exclude
    @OneToMany(mappedBy = "hashtag")
    private List<PostHashtag> postHashtags = new ArrayList<>();

    @Builder
    private Hashtag(Long id, String hashtagName, List<PostHashtag> postHashtags) {
        this.id = id;
        this.hashtagName = hashtagName;
        this.postHashtags = postHashtags;
    }

    public void addPostHashtag(PostHashtag postHashtag) {
        this.postHashtags.add(postHashtag);
    }

    public static Hashtag createHashtag(String hashtagName) {
        return Hashtag.builder()
                .hashtagName(hashtagName)
                .postHashtags(new ArrayList<>())
                .build();
    }

    public boolean hasOnlyOnePostHashtag() {
        return this.postHashtags.size() == 1;
    }
}
