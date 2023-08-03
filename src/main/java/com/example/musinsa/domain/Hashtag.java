package com.example.musinsa.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hashtagName;

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
        postHashtag.addHashtag(this);
    }

    public static Hashtag createHashtag(String hashtagName) {
        return Hashtag.builder()
                .hashtagName(hashtagName)
                .postHashtags(new ArrayList<>())
                .build();
    }
}
