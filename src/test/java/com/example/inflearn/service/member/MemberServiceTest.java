package com.example.inflearn.service.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.example.inflearn.common.exception.AlreadyExistMemberException;
import com.example.inflearn.common.exception.DoesNotExistEmailException;
import com.example.inflearn.common.exception.DoesNotExistMemberException;
import com.example.inflearn.common.exception.WrongEmailTokenException;
import com.example.inflearn.domain.member.Member;
import com.example.inflearn.infra.repository.member.MemberRepository;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import java.util.Optional;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService sut;

    @Mock
    private MemberRepository memberRepository;

    private final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
            .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
            .build();

    private Member member = fixtureMonkey.giveMeOne(Member.class);

    @Test
    void 회원가입_성공() {
        // given
        given(memberRepository.existsByEmail(member.getEmail())).willReturn(false);

        // when
        sut.signUp(member);

        // then
        assertThat(member.getEmailToken()).isNotNull();
    }

    @Test
    void 회원가입_실패_이미_존재하는_유저() {
        // given
        given(memberRepository.existsByEmail(member.getEmail())).willReturn(true);

        // when & then
        assertThrows(AlreadyExistMemberException.class, () -> sut.signUp(member));
    }

    @Test
    void 메일_체크_성공시_member의_isVerify필드_상태를_변경한다() {
        // given
        Member member = fixtureMonkey.giveMeBuilder(Member.class).set("isVerifiedEmail", false).sample();
        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.of(member));

        //when
        sut.checkEmail(member.getEmailToken(), member.getEmail());

        // then
        assertThat(member.isVerifiedEmail()).isTrue();
    }

    @Test
    void 메일_체크_실패_존재하지_않는_이메일() {
        // given
        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistEmailException.class, () -> sut.checkEmail(member.getEmailToken(), member.getEmail()));
    }

    @Test
    void 메일_체크_실패_유효하지_않은_이메일과_토큰번호() {
        // given
        Member member = fixtureMonkey.giveMeBuilder(Member.class).set("emailToken", null).sample();
        given(memberRepository.findByEmail(member.getEmail())).willReturn(
                Optional.of(fixtureMonkey.giveMeOne(Member.class)));

        // when & then
        assertThrows(WrongEmailTokenException.class,
                () -> sut.checkEmail(member.getEmailToken(), member.getEmail()));
    }

    @Test
    void 로그인_성공_후_토큰을_발급한다() {
        // given
        Member member = fixtureMonkey.giveMeBuilder(Member.class)
                .set("isVerifiedEmail", true)
                .set("loginToken", null)
                .sample();
        given(memberRepository.findByEmailAndPassword(member.getEmail(),
                member.getPassword())).willReturn(Optional.of(member));

        // when
        String loginToken = sut.login(member);

        // then
        assertThat(loginToken).isNotNull();
    }

    @Test
    void 로그인_실패_존재하지_않는_유저() {
        // given
        Member member = fixtureMonkey.giveMeBuilder(Member.class)
                .set("loginToken", null)
                .sample();
        given(memberRepository.findByEmailAndPassword(member.getEmail(),
                member.getPassword())).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistMemberException.class, () -> sut.login(member));
    }

    @Test
    void 로그아웃_후_토큰은_null이된다() {
        // given
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));

        // when
        sut.logout(member.getId());

        // then
        assertThat(member.getLoginToken()).isNull();
    }

    @Test
    void 로그아웃_실패_존재하지_않는_유저() {
        // given
        given(memberRepository.findById(member.getId())).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistMemberException.class, () -> sut.logout(member.getId()));
    }

    @Test
    void 이메일_재전송_실패_존재하지_않는_유저() {
        // given
        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.empty());

        // when && then
        assertThrows(DoesNotExistMemberException.class, () -> sut.resendEmail(member.getEmail()));
    }
}