package com.example.musinsa.ui.member;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.musinsa.domain.Member;
import com.example.musinsa.domain.service.MemberService;
import com.example.musinsa.ui.member.dto.request.MemberJoinRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Test
    @DisplayName("회원가입 성공")
    void join_success() throws Exception {
        //given
        MemberJoinRequest request = joinRequest("asdf1234@naver.com", "12345678");
        given(memberService.save(any(Member.class))).willReturn(
                member("asdf1234@naver.com", "12345678"));

        //when
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(request.email()))
                .andDo(print());

        //then
        then(memberService).should().save(any(Member.class));
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

        //when
        mockMvc.perform(get("/check-email-token")
                        .param("emailToken", emailToken)
                        .param("email", email))
                .andExpect(status().isOk())
                .andDo(print());

        //then
        then(memberService).should().emailCheck(emailToken,email);
    }

    @Test
    @DisplayName("이메일 토큰 확인 실패 : 잘못된 토큰, 이메일 파라미터입력")
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