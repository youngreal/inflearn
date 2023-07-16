package com.example.musinsa.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String nickname;

    private String emailToken;

    private String loginToken;

    @Builder
    public Member(Long id, String email, String password, String nickname, String emailToken, String loginToken) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.emailToken = emailToken;
        this.loginToken = loginToken;
    }


    public void generateEmailToken() {
        this.emailToken = UUID.randomUUID().toString();
    }

    public boolean isValidEmailToken(String emailToken) {
        return this.emailToken.equals(emailToken);
    }

    public void generateLoginToken() {
        this.loginToken = UUID.randomUUID().toString();
    }
}
