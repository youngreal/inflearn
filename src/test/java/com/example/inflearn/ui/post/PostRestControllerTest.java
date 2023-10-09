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

import com.example.inflearn.domain.comment.service.CommentService;
import com.example.inflearn.domain.like.service.LikeService;
import com.example.inflearn.domain.member.domain.Member;
import com.example.inflearn.domain.member.service.MemberService;
import com.example.inflearn.domain.post.service.PostQueryService;
import com.example.inflearn.domain.post.service.PostService;
import com.example.inflearn.domain.post.PostDto;
import com.example.inflearn.infra.repository.member.MemberRepository;
import com.example.inflearn.ui.post.dto.request.PostCommentContents;
import com.example.inflearn.ui.post.dto.request.PostPaging;
import com.example.inflearn.ui.post.dto.request.PostReplyContents;
import com.example.inflearn.domain.post.PostSearch;
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

    @MockBean
    private CommentService commentService;


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

        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.of(member));

        //when
        mockMvc.perform(post("/posts")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());

        //then
        //todo 개선 필요해보이는데..
//        then(postService).should().write(eq(request.toDto()), eq(member.getId()));
    }

    @Test
    @DisplayName("포스트 작성 실패: 로그인 하지 않은 회원")
    void post_write_fail1() throws Exception {
        //given
        PostWriteRequest request = postRequest("", "", List.of("Java","Spring"));

        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.empty());

        //when
        mockMvc.perform(post("/posts")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andDo(print());

        //then
        then(postService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("포스트 작성 실패: 빈 제목이나 빈 본문 작성")
    void post_write_fail2() throws Exception {
        //given
        PostWriteRequest request = postRequest("", "", List.of("Java","Spring"));
        Member member = member(1L, "asdf1234@naver.com", "12345678");

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
    void post_write_fail3() throws Exception {
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
    @DisplayName("포스트 수정 실패: 로그인하지 않은 회원")
    void post_update_fail3() throws Exception {
        //given
        PostWriteRequest request = postRequest("", "", List.of("Java","Spring"));
        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.empty());

        //when
        mockMvc.perform(put("/posts/1")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
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

        PostPaging paging = new PostPaging(1, 20, null);
        given(postQueryService.getPostsPerPage(eq(paging.page()), eq(paging.size()), any())).willReturn(List.of(postDto,postDto2,postDto3));
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
        PostSearch postSearch = PostSearch.of(3,20,"자바");
        given(postQueryService.searchPost(postSearch)).willReturn(List.of(postDto));
        given(postQueryService.getPageCountWithSearchWord(postSearch)).willReturn(3L);

        //when & then
        mockMvc.perform(get("/posts/search?page=3&size=20&searchWord=자바"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].title").value("게시글제목1"))
                .andExpect(jsonPath("$.posts[0].contents").value("게시글본문1"))
                .andExpect(jsonPath("$.pageCount").value(3L))
                .andDo(print());
    }

    @Test
    @DisplayName("포스트 검색 성공 : 특정 정렬조건 입력")
    void post_search_success2() throws Exception {
        //given
        PostDto postDto = createDto("게시글제목1", "게시글본문1");
        PostSearch postSearch = PostSearch.of(3,20,"자바", "like");
        given(postQueryService.searchPost(any(PostSearch.class))).willReturn(List.of(postDto));
        given(postQueryService.getPageCountWithSearchWord(any(PostSearch.class))).willReturn(3L);

        //when & then
        mockMvc.perform(get("/posts/search?page=3&size=20&searchWord=자바&sort=like"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].title").value("게시글제목1"))
                .andExpect(jsonPath("$.posts[0].contents").value("게시글본문1"))
                .andExpect(jsonPath("$.pageCount").value(3L))
                .andDo(print());
    }

    @Test
    @DisplayName("포스트 검색 실패 : 존재하지 않는 정렬조건 입력")
    void post_search_fail() throws Exception {
        //given
        PostDto postDto = createDto("게시글제목1", "게시글본문1");
        PostSearch postSearch = PostSearch.of(3,20,"자바");
        given(postQueryService.searchPost(any(PostSearch.class))).willReturn(List.of(postDto));
        given(postQueryService.getPageCountWithSearchWord(any(PostSearch.class))).willReturn(3L);

        //when & then
        mockMvc.perform(get("/posts/search?page=3&size=20&searchWord=자바&sort=12345"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("포스트 검색 실패 : page or size값이 1보다 적을때")
    void post_search_fail2() throws Exception {
        //when & then
        mockMvc.perform(get("/posts/search?page=-1&size=-1&searchWord=자바"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("포스트 검색 실패 : 검색어 길이가 2보다 작을때")
    void post_search_fail3() throws Exception {
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
    @DisplayName("게시글 좋아요 실패: 로그인하지 않은 회원")
    void post_like_fail() throws Exception {
        //given
        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.empty());

        //when & then
        mockMvc.perform(post("/posts/1/likes")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 좋아요 취소 성공")
    void post_unlike_success() throws Exception {
        //given
        Member member = member(1L, "asdf1234@naver.com", "12345678");
        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.of(member));

        //when & then
        mockMvc.perform(delete("/posts/1/likes")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 좋아요 취소 실패 : 로그인 하지 않은 회원")
    void post_unlike_fail() throws Exception {
        //given
        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.empty());

        //when & then
        mockMvc.perform(delete("/posts/1/likes")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 댓글 작성 성공")
    void post_comment_success() throws Exception {
        //given
        Member member = member(1L, "asdf1234@naver.com", "12345678");
        PostCommentContents postCommentContents = new PostCommentContents("이 스터디 어떻게 진행되는건가요?");
        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.of(member));

        //when & then
        mockMvc.perform(post("/posts/1/comments")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postCommentContents))
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 댓글 작성 실패 : 로그인하지않은 회원의 댓글작성요청")
    void post_comment_fail() throws Exception {
        //given
        PostCommentContents postCommentContents = new PostCommentContents("이 스터디 어떻게 진행되는건가요?");
        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.empty());

        //when & then
        mockMvc.perform(post("/posts/1/comments")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postCommentContents))
                )
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 댓글 작성 실패 : 댓글 내용을 입력하지 않음")
    void post_comment_fail2() throws Exception {
        //given
        Member member = member(1L, "asdf1234@naver.com", "12345678");
        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.of(member));

        //when & then
        mockMvc.perform(post("/posts/1/comments")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 답글 작성 성공")
    void post_reply_success() throws Exception {
        //given
        Member member = member(1L, "asdf1234@naver.com", "12345678");
        PostReplyContents postReplyContents = new PostReplyContents("이 스터디 어떻게 진행되는건가요?");
        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.of(member));

        //when & then
        mockMvc.perform(post("/comments/1/reply")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postReplyContents))
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 답글 작성 실패 : 로그인하지않은 회원의 답글작성요청")
    void post_reply_fail() throws Exception {
        //given
        PostReplyContents postReplyContents = new PostReplyContents("이 스터디 어떻게 진행되는건가요?");
        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.empty());

        //when & then
        mockMvc.perform(post("/comments/1/reply")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postReplyContents))
                )
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 답글 작성 실패 : 댓글 내용을 입력하지 않음")
    void post_reply_fail2() throws Exception {
        //given
        Member member = member(1L, "asdf1234@naver.com", "12345678");
        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.of(member));

        //when & then
        mockMvc.perform(post("/comments/1/reply")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
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