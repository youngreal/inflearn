package com.example.inflearn.infra.repository.post;

import com.example.inflearn.domain.post.domain.PopularPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PopularPostRepository extends JpaRepository<PopularPost, Long> {

    PopularPost findByPostId(Long id);

    @Modifying
    @Query("update PopularPost p set p.postId=:postId where p.id=:popularPostId")
    void updatePopularIds(long postId, long popularPostId);

    @Modifying
    @Query("update PopularPost p set p.likeCount=:likeCount where p.id=:popularPostId")
    void updatePopularlikeCount(long likeCount, long popularPostId);
}
