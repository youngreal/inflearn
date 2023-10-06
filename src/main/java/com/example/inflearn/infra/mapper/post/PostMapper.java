package com.example.inflearn.infra.mapper.post;

import com.example.inflearn.dto.PostDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/*
Mybatis 도입이유
1. mysql 의 fulltext search 를 사용하기위해선 QueryDsl로만으로 무리가있었고(버전호환문제)
2. 해당쿼리의 반환을 select count(*) 필드를 포함한 DTO로 해야하는 이유때문에 한계가 있어서 도입
 */
@Mapper
public interface PostMapper {

    List<PostDto> search(String searchWord, int offset, int limit);
}
