package com.example.musinsa.ui.member;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.musinsa.domain.member.domain.Member;
import com.example.musinsa.domain.member.service.MemberService;
import com.example.musinsa.infra.repository.member.MemberRepository;
import com.example.musinsa.ui.member.dto.request.MemberJoinRequest;
import com.example.musinsa.ui.member.dto.request.MemberLoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MemberRestController.class)
class MemberRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    @MockBean
    private MemberRepository memberRepository;

    private static final String SESSION_TOKEN_NAME = "SESSION";
    private static final int COOKIE_MAX_AGE = 30 * 24 * 60 * 60;
    private static final String RANDOM_UUID = "RandomUUID-12345678";

    @Test
    @DisplayName("회원가입 성공")
    void join_success() throws Exception {
        //given
        MemberJoinRequest request = joinRequest("asdf1234@naver.com", "12345678");
        given(memberService.signUp(any(Member.class))).willReturn(
                member("asdf1234@naver.com", "12345678"));

        //when
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(request.email()))
                .andDo(print());

        //then
        then(memberService).should().signUp(any(Member.class));
    }


    @DisplayName("회원가입 실패 : 잘못된 요청 형식")
    @MethodSource
    @ParameterizedTest(name = "[{index}] \"{0}\" => {2}")
    void 형식에_맞지않는_회원가입_요청시_회원가입에_실패한다(MemberJoinRequest request) throws Exception {
        //when
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        //then
        then(memberService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("이메일 토큰 확인 성공")
    void 이메일_토큰_유효성_확인_성공() throws Exception {
        //given
        String emailToken = "아무개";
        String email = "asdf1234@naver.com";

        Member member = Member.builder()
                .id(1L)
                .build();

        given(memberService.emailCheck(emailToken, email)).willReturn(member);
        given(memberService.login(member)).willReturn(RANDOM_UUID);

        //when
        mockMvc.perform(get("/check-email-token")
                        .param("emailToken", emailToken)
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(cookie().httpOnly(SESSION_TOKEN_NAME, true))
                .andExpect(cookie().secure(SESSION_TOKEN_NAME, true))
                .andExpect(cookie().sameSite(SESSION_TOKEN_NAME, "Strict"))
                .andExpect(cookie().domain(SESSION_TOKEN_NAME, "localhost"))
                .andExpect(cookie().value(SESSION_TOKEN_NAME, RANDOM_UUID))
                .andDo(print());

        //then
        then(memberService).should().emailCheck(emailToken,email);
        then(memberService).should().login(member);
    }

    @Test
    @DisplayName("이메일 토큰 유효성 검사 실패 : 잘못된 토큰, 이메일 파라미터입력")
    void 이메일토큰_유효성확인_실패_잘못된토큰이메일() throws Exception {
        //given
        String emailToken = "";
        String email = "";

        //when
        mockMvc.perform(get("/check-email-token")
                        .param("emailToken", emailToken)
                        .param("email", email))
                .andExpect(status().isBadRequest())
                .andDo(print());

        //then
        then(memberService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("로그인 성공시 Set-Cookie 헤더에 토큰을 발급한다")
    void 로그인_성공() throws Exception {
        //given
        MemberLoginRequest memberLoginRequest = MemberLoginRequest.builder()
                .email("asdf1234@naver.com")
                .password("12345678")
                .build();

        Member member = memberLoginRequest.toEntity(memberLoginRequest);
        given(memberService.login(member)).willReturn(RANDOM_UUID);

        //when & then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(cookie().httpOnly(SESSION_TOKEN_NAME, true))
                .andExpect(cookie().secure(SESSION_TOKEN_NAME, true))
                .andExpect(cookie().sameSite(SESSION_TOKEN_NAME, "Strict"))
                .andExpect(cookie().domain(SESSION_TOKEN_NAME, "localhost"))
                .andExpect(cookie().value(SESSION_TOKEN_NAME, RANDOM_UUID))
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 실패 : 잘못된 로그인 요청 폼")
    void 로그인_실패_잘못된_로그인요청_폼() throws Exception {
        //given
        MemberLoginRequest memberLoginRequest = MemberLoginRequest.builder()
                .email("asdf1234@naver.com")
                .password("")
                .build();

        //when & then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberLoginRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    //todo 로그아웃 테스트는 어떻게보면 ArgumentResolver의 테스트가 될수있을것같은데.. 여기있는것도 괜찮으려나?

    @Test
    @DisplayName("로그아웃 성공")
    void 로그아웃_성공() throws Exception {
        //given
        Member member = Member.builder()
                .id(1L)
                .build();

        Cookie cookie = makeCookie(SESSION_TOKEN_NAME);
        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.of(member));

        //when & then
        mockMvc.perform(post("/logout")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("로그아웃 실패 : 헤더에 쿠키를 포함하지않고 요청")
    void 로그아웃_실패_헤더에_쿠키를_포함하지않음() throws Exception {
        //given
        Member member = Member.builder()
                .id(1L)
                .build();

        given(memberRepository.findByLoginToken(RANDOM_UUID)).willReturn(Optional.of(member));

        //when & then
        mockMvc.perform(post("/logout"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("로그아웃 실패 : 헤더에 유효하지않은 형식의 쿠키 요청")
    void 로그아웃_실패_유효하지않은_쿠키() throws Exception {
        //given
        Member member = Member.builder()
                .id(1L)
                .build();

        Cookie cookie = makeCookie("wrongName");
        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.of(member));

        //when & then
        mockMvc.perform(post("/logout")
                        .cookie(cookie))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("로그아웃 실패 : 쿠키는 올바르지만 토큰에 일치하는 멤버 존재하지않음")
    void 로그아웃_실패_만료된_세션토큰() throws Exception {
        //given
        Cookie cookie = makeCookie(SESSION_TOKEN_NAME);
        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.empty());

        //when & then
        mockMvc.perform(post("/logout")
                        .cookie(cookie))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    private Cookie makeCookie(String sessionTokenName) {
        Cookie cookie = new Cookie(sessionTokenName, RANDOM_UUID);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(COOKIE_MAX_AGE);
        return cookie;
    }


    static Stream<Arguments> 형식에_맞지않는_회원가입_요청시_회원가입에_실패한다() {
        return Stream.of(
                arguments(joinRequest("", "12345678")),
                arguments(joinRequest(" ", "12345678")),
                arguments(joinRequest(null, "12345678")),
                arguments(joinRequest("asdfaf1234@naver.com", " ")),
                arguments(joinRequest("asdfaf1234@naver.com", null)));
    }

    private static MemberJoinRequest joinRequest(String email, String password) {
        return MemberJoinRequest.builder()
                .email(email)
                .password(password)
                .build();
    }

    private Member member(String email, String password) {
        return Member.builder()
                .email(email)
                .password(password)
                .build();
    }
}