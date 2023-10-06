package com.example.inflearn.ui.post;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.inflearn.domain.like.service.LikeService;
import com.example.inflearn.domain.member.domain.Member;
import com.example.inflearn.domain.member.service.MemberService;
import com.example.inflearn.domain.post.service.PostQueryService;
import com.example.inflearn.domain.post.service.PostService;
import com.example.inflearn.dto.PostDto;
import com.example.inflearn.infra.repository.member.MemberRepository;
import com.example.inflearn.ui.post.dto.request.PostPaging;
import com.example.inflearn.ui.post.dto.request.PostSearch;
import com.example.inflearn.ui.post.dto.request.PostWriteRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
//todo 컨트롤러 코드는 코드라인수 대비 역할을 잘 모르겠다.. 추가하는게 좋을까?
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
    private MemberService memberService;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private LikeService likeService;

    private Cookie cookie;
    private static final int COOKIE_MAX_AGE = 30 * 24 * 60 * 60;
    private static final String RANDOM_UUID = "RandomUUID-12345678";
    private static final String SESSION_TOKEN_NAME = "SESSION";

    //todo 쿠키인증부분이 모든 테스트 클래스에서 반복되는 느낌이있는데 리팩토링이 필요해보인다. 반복작업이다.
    @BeforeEach
    void setup() {
        cookie = makeCookie(SESSION_TOKEN_NAME);
    }

    @Test
    @DisplayName("포스트 작성 성공: 중복없는 해시태그 존재")
    void post_write_success() throws Exception {
        //given
        PostWriteRequest request = postRequest("글제목1", "글내용1", List.of("Java","Spring"));
        Member member = member(1L, "asdf1234@naver.com", "12345678");

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
    @DisplayName("포스트 조회 성공")
    void post_view_success() throws Exception {
        //given
        PostDto postDto = createDto("게시글제목1", "게시글본문1");
        PostDto postDto2 = createDto("게시글제목2", "게시글본문2");
        PostDto postDto3 = createDto("게시글제목3", "게시글본문3");

        PostPaging paging = PostPaging.create(1, 20);
        given(postQueryService.getPostsPerPage(paging.page(), paging.size())).willReturn(List.of(postDto,postDto2,postDto3));
        given(postQueryService.getPageCount(paging.page(), paging.size())).willReturn(3L);

        //when & then
        mockMvc.perform(get("/posts?page=1&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].title").value("게시글제목1"))
                .andExpect(jsonPath("$.posts[1].title").value("게시글제목2"))
                .andExpect(jsonPath("$.posts[2].title").value("게시글제목3"))
                .andExpect(jsonPath("$.posts[0].contents").value("게시글본문1"))
                .andExpect(jsonPath("$.posts[1].contents").value("게시글본문2"))
                .andExpect(jsonPath("$.posts[2].contents").value("게시글본문3"))
                .andExpect(jsonPath("$.pageCount").value(3L))
                .andDo(print());
    }

    @Test
    @DisplayName("포스트 조회 실패 : page or size값이 1보다 적을때")
    void post_view_fail() throws Exception {
        //when & then
        mockMvc.perform(get("/posts?page=-1&size=-1"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }



    @Test
    @DisplayName("포스트 검색 성공 : 특정 검색어 입력")
    void post_search_success() throws Exception {
        //given
        PostDto postDto = createDto("게시글제목1", "게시글본문1");

        PostSearch postSearch = new PostSearch(3, 20, "자바");
        given(postQueryService.searchPost(postSearch.searchWord(), postSearch.page(), postSearch.size())).willReturn(List.of(postDto));
        given(postQueryService.getPageCountWithSearchWord(postSearch.searchWord(), postSearch.page(), postSearch.size())).willReturn(3L);

        //when & then
        mockMvc.perform(get("/posts/search?page=3&size=20&searchWord=자바"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].title").value("게시글제목1"))
                .andExpect(jsonPath("$.posts[0].contents").value("게시글본문1"))
                .andExpect(jsonPath("$.pageCount").value(3L))
                .andDo(print());
    }

    @Test
    @DisplayName("포스트 검색 실패 : page or size값이 1보다 적을때")
    void post_search_fail() throws Exception {
        //when & then
        mockMvc.perform(get("/posts/search?page=-1&size=-1&searchWord=자바"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("포스트 검색 실패 : 검색어 길이가 2보다 작을때")
    void post_search_fail2() throws Exception {
        //when & then
        mockMvc.perform(get("/posts/search?page=1&size=20&searchWord=자"))
                .andExpect(status().isBadRequest())
                .andDo(print());


    }

    @Test
    @DisplayName("게시글 좋아요 성공")
    void post_like_success() throws Exception {
        //given
        Member member = member(1L, "asdf1234@naver.com", "12345678");
        given(memberService.signUp(any(Member.class))).willReturn(member);
        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.of(member));

        //when & then
        mockMvc.perform(post("/posts/1/likes")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 좋아요 취소 성공")
    void post_unlike_success() throws Exception {
        //given
        Member member = member(1L, "asdf1234@naver.com", "12345678");
        given(memberService.signUp(any(Member.class))).willReturn(member);
        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.of(member));

        //when & then
        mockMvc.perform(delete("/posts/1/likes")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

    private PostDto createDto(String title, String contents) {
        return PostDto.builder()
                .title(title)
                .contents(contents)
                .build();
    }

    private PostWriteRequest postRequest(String title, String contents, List<String> hashtags) {
        return PostWriteRequest.builder()
                .title(title)
                .contents(contents)
                .hashtags(hashtags)
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