package com.example.musinsa.infra.repository;

import com.example.musinsa.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByEmailAndPassword(String email, String password);

    //todo 인덱스 관련해서 문제가 없으려나?
    Optional<Member> findByLoginToken(String loginToken);
}
