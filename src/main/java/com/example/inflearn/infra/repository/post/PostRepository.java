package com.example.inflearn.infra.repository.post;

import com.example.inflearn.domain.post.domain.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    //todo 엔티티 반환말고 DTO로 반환하는걸로 변경하기
    @Query(value = "select * from post where match(title,contents) against(?) limit ?,?", nativeQuery = true)
    List<Post> search(String searchWord, int offset, int limit);

    @Query(value = "select count(id) from(select id from post where match(title,contents) against(?) limit ?,?) as subquery ", nativeQuery = true)
    Long countPageWithSearchWord(String searchWord, int offset, int limit);
}
