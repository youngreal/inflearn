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
//todo 정렬 조건에 따른 동적쿼리 작성을 mybatis에서 하는게 맞을까? 아니면 서비스레이어에서 해줘야할까? 전자로 선택했는데 어떤단점이있을까?
    List<PostDto> search(String searchWord, int offset, int limit, String sort);
}
