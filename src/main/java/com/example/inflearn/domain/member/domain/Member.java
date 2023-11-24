package com.example.inflearn.domain.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email")
        },
        indexes = {
                @Index(columnList = "loginToken"),
        })
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String nickname;

    private String emailToken;

    private String loginToken;

    private boolean isVerifiedEmail = false; // 이메일 인증여부

    @Builder
    private Member(Long id, String email, String password, String nickname, String emailToken,
            String loginToken, boolean isVerifiedEmail) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.emailToken = emailToken;
        this.loginToken = loginToken;
        this.isVerifiedEmail = isVerifiedEmail;
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

    public void invalidateToken() {
        this.loginToken = null;
    }

    public void completeEmailVerify() {
        this.isVerifiedEmail = true;
    }

    public boolean isLogined() {
        return this.loginToken != null;
    }
}
