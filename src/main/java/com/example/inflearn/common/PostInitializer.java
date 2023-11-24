//package com.example.inflearn.common;
//
//import com.example.inflearn.domain.comment.domain.Comment;
//import com.example.inflearn.domain.hashtag.domain.Hashtag;
//import com.example.inflearn.domain.like.domain.Like;
//import com.example.inflearn.domain.member.domain.Member;
//import com.example.inflearn.domain.post.domain.Post;
//import com.example.inflearn.domain.post.domain.PostHashtag;
//import com.example.inflearn.infra.repository.comment.CommentRepository;
//import com.example.inflearn.infra.repository.like.LikeRepository;
//import com.example.inflearn.infra.repository.member.MemberRepository;
//import com.example.inflearn.infra.repository.post.PostRepository;
//import jakarta.annotation.PostConstruct;
//import java.security.SecureRandom;
//import java.util.ArrayList;
//import java.util.List;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Component;
//
//@Profile("local")
//@RequiredArgsConstructor
//@Component
//public class PostInitializer {
//
//    private final MemberRepository memberRepository;
//    private final PostRepository postRepository;
//    private final PostHashtagRepository postHashtagRepository;
//    private final LikeRepository likeRepository;
//    private final CommentRepository commentRepository;
//    int hashtagCount = 0;
//    List<String> lists = List.of(
//            "java", "spring", "spring boot", "aws", "docker",
//            "elastic search", "백엔드", "로드맵","redis","kafka","쿠버네티스","msa","DB",
//            "프론트엔드", "ci", "cd", "배포", "무중단", "nest", "타입스크립트", "node", "git",
//            "서버", "데브옵스", "알고리즘", "자료구조", "운영체제", "스터디", "리액트", "이펙티브 자바",
//            "typescript", "nestjs", "rest", "python", "김영한", "백기선", "주니어", "신입", "C",
//            "C++", "바킹독", "코딩테스트", "Ruby", "코테", "온라인", "오프라인", "디스코드"
//            , "경력", "재직자", "이직준비", "디앱", "블록체인", "솔리디티", "각자도생", "sql", "데브옵스", "퍼블리셔",
//            "자바스크립트", "웹개발", "CS", "네트워크", "면접", "인터뷰","딥러닝","수학","1달","정처기","정보처리기사","기사시험",
//            "벌칙비","html","css","jenkins","github actions", "언리얼","tdd","클린코드","협업","프로젝트","백준","릿코드","자바의정석",
//            "기초","중급","고급","JPA","DBMS","real mysql","우테코","mvc","프리코스","이직러","모각코코","커피챗","비어챗","취준생위주",
//            "경력자위주","코테초보만","코테중수이상","코테고수들만","해외취업","배포자동화","유료스터디"
//    );
//
//    @PostConstruct
//    public void init() {
//        int batchSize = 1000;
////        int batchSize = 100;
//        int numberOfMemberToGenerate = 1_000_000;
////        int numberOfMemberToGenerate = 100;
//        int postsPerMember = 2;
//
//        for (int i = 1; i < numberOfMemberToGenerate; i+=batchSize) {
//            List<Member> members = generateMembers(i, batchSize);
//            memberRepository.saveAll(members);
//
//            List<Post> posts = new ArrayList<>();
//            for (Member member : members) {
//                posts.addAll(generatePostsForMember(postsPerMember, member)); // 1000 x 3
//            }
//            postRepository.saveAll(posts);
//
//            List<Like> likes = new ArrayList<>();
//            for (Post post : posts) {
//                likes.addAll(generateLikesForPost(post, members)); // 2000 x 3 = 6,000,000
//            }
//                likeRepository.saveAll(likes);
//
//            List<Comment> comments = new ArrayList<>();
//            for (Post post : posts) {
//                comments.addAll(generateCommentsForPost(post, members)); // 2000 x 3
//            }
//                commentRepository.saveAll(comments);
//
//
//            List<PostHashtag> postHashtags = new ArrayList<>();
//            for (Post post : posts) {
//                postHashtags.addAll(generatePostHashtagsForPost(post));
//            }
//
//            postHashtagRepository.saveAll(postHashtags);
//        }
//    }
//
//    private List<Like> generateLikesForPost(Post post, List<Member> members) {
//        List<Like> likes = new ArrayList<>();
//        SecureRandom secureRandom = new SecureRandom();
//        int randomIndex = secureRandom.nextInt(3);
//        int memberIndex = secureRandom.nextInt(999);
//        for (int i = 1; i <= randomIndex; i++) {
//            likes.add(Like.builder()
//                    .member(members.get(memberIndex))
//                    .post(post)
//                    .build());
//        }
//        return likes;
//    }
//
//    private List<Comment> generateCommentsForPost(Post post, List<Member> members) {
//        List<Comment> comments = new ArrayList<>();
//        SecureRandom secureRandom = new SecureRandom();
//        int randomCommentCount = secureRandom.nextInt(3);
//        int memberIndex = secureRandom.nextInt(999);
//        for (int i = 1; i <= randomCommentCount; i++) {
//            comments.add(Comment.builder()
//                    .member(members.get(memberIndex))
//                    .post(post)
//                    .contents("댓글 i")
//                    .build());
//        }
//        return comments;
//    }
//
//    private List<PostHashtag> generatePostHashtagsForPost(Post post) {
//        List<PostHashtag> postHashtags = new ArrayList<>();
//        Hashtag hashtag = Hashtag.createHashtag("java" + hashtagCount);
//        Hashtag hashtag2 = Hashtag.createHashtag("spring" + hashtagCount);
//        postHashtags.add(PostHashtag.createPostHashtag(post, hashtag));
//        postHashtags.add(PostHashtag.createPostHashtag(post, hashtag2));
//        hashtagCount++;
//        return postHashtags;
//    }
//
//    private List<Post> generatePostsForMember(int numberOfPosts, Member member) {
//        List<Post> posts = new ArrayList<>();
//        for (int i = 1; i <= numberOfPosts; i++) {
//            SecureRandom secureRandom = new SecureRandom();
//            int randomIndex = secureRandom.nextInt(lists.size());
//            String randomHashtag = lists.get(randomIndex);
//            String postTitle = randomHashtag + "_" + i;
//            String postContents = randomHashtag + "_" + i;
//            posts.add(Post.builder()
//                    .title(postTitle)
//                    .contents(postContents)
//                    .member(member)
//                    .build());
//        }
//        return posts;
//    }
//
//    private List<Member> generateMembers(int start, int count) {
//        List<Member> members = new ArrayList<>();
//        for (int i = start; i < start + count; i++) {
//            members.add(Member.builder()
//                    .email(i + "user@naver.com")
//                    .password("12345678")
//                    .build());
//        }
//        return members;
//    }
//}
