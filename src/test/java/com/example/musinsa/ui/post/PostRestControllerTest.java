package com.example.musinsa.ui.post;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.musinsa.domain.member.domain.Member;
import com.example.musinsa.domain.member.service.MemberService;
import com.example.musinsa.domain.post.service.PaginationService;
import com.example.musinsa.domain.post.service.PostQueryService;
import com.example.musinsa.domain.post.service.PostService;
import com.example.musinsa.dto.PostDto;
import com.example.musinsa.infra.repository.member.MemberRepository;
import com.example.musinsa.ui.post.dto.request.PostWriteRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PostRestController.class)
class PostRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @MockBean
    private PostQueryService postQueryService;

    @MockBean
    private PaginationService paginationService;

    @MockBean
    private MemberService memberService;

    @MockBean
    private MemberRepository memberRepository;

    private static final int COOKIE_MAX_AGE = 30 * 24 * 60 * 60;
    private static final String RANDOM_UUID = "RandomUUID-12345678";
    private static final String SESSION_TOKEN_NAME = "SESSION";

    //todo 쿠키인증부분이 모든 테스트 클래스에서 반복되는 느낌이있는데 리팩토링이 필요해보인다. 반복작업이다.

    @Test
    @DisplayName("포스트 작성 성공: 중복없는 해시태그 존재")
    void post_write_success() throws Exception {
        //given
        PostWriteRequest request = postRequest("글제목1", "글내용1", List.of("Java","Spring"));
        Member member = member(1L, "asdf1234@naver.com", "12345678");
        Cookie cookie = makeCookie(SESSION_TOKEN_NAME);

        given(memberService.signUp(any(Member.class))).willReturn(member);
        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.of(member));

        //when
        mockMvc.perform(post("/posts")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());

        //then
        then(postService).should().write(request.toDtoWithHashtag(any()), eq(member.getId()));
    }

    @Test
    @DisplayName("포스트 작성 성공: 해시태그 존재하지 않음")
    void post_write_success2() throws Exception {
        //given
        PostWriteRequest request = postRequest("글제목1", "글내용1", List.of());
        Member member = member(1L, "asdf1234@naver.com", "12345678");
        Cookie cookie = makeCookie(SESSION_TOKEN_NAME);

        given(memberService.signUp(any(Member.class))).willReturn(member);
        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.of(member));

        //when
        mockMvc.perform(post("/posts")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());

        //then
        then(postService).should().write(eq(request.toDto()), eq(member.getId()));
    }

    @Test
    @DisplayName("포스트 작성 실패: 빈 제목이나 빈 본문 작성")
    void post_write_fail() throws Exception {
        //given
        PostWriteRequest request = postRequest("", "", List.of("Java","Spring"));
        Member member = member(1L, "asdf1234@naver.com", "12345678");
        Cookie cookie = makeCookie(SESSION_TOKEN_NAME);

        given(memberService.signUp(any(Member.class))).willReturn(member);
        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.of(member));

        //when
        mockMvc.perform(post("/posts")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        //then
        then(postService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("포스트 작성 실패: 중복된 해시태그값 존재")
    void post_write_fail2() throws Exception {
        //given
        PostWriteRequest request = postRequest("글제목1", "글내용1", List.of("Java","Spring","Java"));
        Member member = member(1L, "asdf1234@naver.com", "12345678");
        Cookie cookie = makeCookie(SESSION_TOKEN_NAME);

        given(memberService.signUp(any(Member.class))).willReturn(member);
        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.of(member));

        //when
        mockMvc.perform(post("/posts")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        //then
        then(postService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("포스트 수정 성공")
    void post_update_success() throws Exception {
        //given
        PostWriteRequest request = postRequest("글제목1", "글내용1", List.of("Java","Spring"));
        Member member = member(1L, "asdf1234@naver.com", "12345678");
        Cookie cookie = makeCookie(SESSION_TOKEN_NAME);

        given(memberService.signUp(any(Member.class))).willReturn(member);
        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.of(member));

        //when
        mockMvc.perform(put("/posts/1")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());

        //then
        then(postService).should().update(any(PostDto.class), eq(member.getId()), anyLong());
    }

    @Test
    @DisplayName("포스트 수정 실패: 빈 제목이나 빈 본문 작성")
    void post_update_fail() throws Exception {
        //given
        PostWriteRequest request = postRequest("", "", List.of("Java","Spring"));
        Member member = member(1L, "asdf1234@naver.com", "12345678");
        Cookie cookie = makeCookie(SESSION_TOKEN_NAME);

        given(memberService.signUp(any(Member.class))).willReturn(member);
        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.of(member));

        //when
        mockMvc.perform(put("/posts/1")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        //then
        then(postService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("포스트 수정 실패: 중복된 해시태그값 존재")
    void post_update_fail2() throws Exception {
        //given
        PostWriteRequest request = postRequest("글제목1", "글내용1", List.of("Java","Spring","Java"));
        Member member = member(1L, "asdf1234@naver.com", "12345678");
        Cookie cookie = makeCookie(SESSION_TOKEN_NAME);

        given(memberService.signUp(any(Member.class))).willReturn(member);
        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.of(member));

        //when
        mockMvc.perform(put("/posts/1")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        //then
        then(postService).shouldHaveNoInteractions();
    }

    private PostWriteRequest postRequest(String title, String contents, List<String> hashtags) {
        return PostWriteRequest.builder()
                .title(title)
                .contents(contents)
                .hashTags(hashtags)
                .build();
    }

    private Member member(Long id, String email, String password) {
        return Member.builder()
                .id(id)
                .email(email)
                .password(password)
                .build();
    }

    private Cookie makeCookie(String sessionTokenName) {
        Cookie cookie = new Cookie(sessionTokenName, RANDOM_UUID);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(COOKIE_MAX_AGE);
        return cookie;
    }
}