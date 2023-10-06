package com.example.inflearn.infra.repository.post;

import com.example.inflearn.domain.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
/*
네이티브 쿼리 사용이유
- mysql 의 fulltext search 와 querydsl 버전호환문제로 도입.
- 문제를 좀더 간단하게 해결하는 방법이라고 판단하여 선택
 */
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    @Query(value = "select count(id) from(select id from post where match(title,contents) against(?) limit ?,?) as subquery ", nativeQuery = true)
    Long countPageWithSearchWord(String searchWord, int offset, int limit);
}
