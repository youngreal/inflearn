package com.example.inflearn.common;

import com.example.inflearn.domain.hashtag.domain.Hashtag;
import com.example.inflearn.domain.member.domain.Member;
import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.domain.post.domain.PostHashtag;
import com.example.inflearn.infra.repository.member.MemberRepository;
import com.example.inflearn.infra.repository.post.PostRepository;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("dev")
@RequiredArgsConstructor
@Component
public class PostInitializer {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final PostHashtagRepository postHashtagRepository;
    int hashtagCount = 0;

    @PostConstruct
    public void init() {
        int batchSize = 1000;
//        int batchSize = 100;
        int numberOfMemberToGenerate = 1_000_000;
//        int numberOfMemberToGenerate = 100;
        int postsPerMember = 3;
        int postHashtagsPerPost = 2;

        for (int i = 1; i < numberOfMemberToGenerate; i+=batchSize) {
            List<Member> members = generateMembers(i, batchSize);
            memberRepository.saveAll(members);

            List<Post> posts = new ArrayList<>();
            for (Member member : members) {
                posts.addAll(generatePostsForMember(postsPerMember, member));
            }
            postRepository.saveAll(posts);

            List<PostHashtag> postHashtags = new ArrayList<>();
            for (Post post : posts) {
                postHashtags.addAll(generatePostHashtagsForPost(postHashtagsPerPost, post));
            }

            postHashtagRepository.saveAll(postHashtags);
        }
    }

    private List<PostHashtag> generatePostHashtagsForPost(int postHashtagsPerPost, Post post) {
        List<PostHashtag> postHashtags = new ArrayList<>();

        for (int i = 1; i <= postHashtagsPerPost; i++) {
            postHashtags.add(PostHashtag.createPostHashtag(post, Hashtag.createHashtag("java" + hashtagCount)));
        }
            hashtagCount++;
        return postHashtags;
    }

    private List<Post> generatePostsForMember(int numberOfPosts, Member member) {
        List<Post> posts = new ArrayList<>();
        for (int i = 1; i <= numberOfPosts; i++) {
            String postTitle = "게시글제목" + member.getEmail() + "_" + i;
            String postContents = "게시글내용" + member.getEmail() + "_" + i;
            posts.add(Post.builder()
                    .title(postTitle)
                    .contents(postContents)
                    .member(member)
                    .build());
        }
        return posts;
    }

    private List<Member> generateMembers(int start, int count) {
        List<Member> members = new ArrayList<>();
        for (int i = start; i < start + count; i++) {
            members.add(Member.builder()
                    .email(i + "testEmail@naver.com")
                    .password("12345678")
                    .build());
        }
        return members;
    }
}
