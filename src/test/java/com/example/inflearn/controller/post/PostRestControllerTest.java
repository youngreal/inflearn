package com.example.inflearn.controller.post;

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

import com.example.inflearn.service.comment.CommentService;
import com.example.inflearn.service.like.LikeService;
import com.example.inflearn.domain.member.Member;
import com.example.inflearn.service.member.MemberService;
import com.example.inflearn.domain.post.PostDto;
import com.example.inflearn.service.post.PostQueryService;
import com.example.inflearn.service.post.PostService;
import com.example.inflearn.infra.repository.member.MemberRepository;
import com.example.inflearn.controller.post.dto.request.PostCommentContents;
import com.example.inflearn.controller.post.dto.request.PostPaging;
import com.example.inflearn.controller.post.dto.request.PostReplyContents;
import com.example.inflearn.domain.post.PostSearch;
import com.example.inflearn.controller.post.dto.request.PostWriteRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
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
    void 중복_없는_해시태그_입력시_게시글_작성_성공한다() throws Exception {
        //given
        PostWriteRequest request = postRequest("글제목1", "글내용1", Set.of("Java","Spring"));
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
        then(postService).should().write(request.toDto(any()), eq(member.getId()));
    }

    @Test
    void 해시태그를_입력하지_않아도_게시글_작성_성공한다() throws Exception {
        //given
        PostWriteRequest request = postRequest("글제목1", "글내용1", Set.of());
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
    void 로그인_하지_않은_회원은_게시글_작성에_실패한다() throws Exception {
        //given
        PostWriteRequest request = postRequest("", "", Set.of("Java","Spring"));

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
    void 빈_제목이나_빈_내용을_입력한_게시글은_작성에_실패한다() throws Exception {
        //given
        PostWriteRequest request = postRequest("", "", Set.of("Java","Spring"));
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
    void 게시글_수정_성공() throws Exception {
        //given
        PostWriteRequest request = postRequest("글제목1", "글내용1", Set.of("Java","Spring"));
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
    void 비어있는_제목이나_본문으로_게시글을_수정요청하면_실패한다() throws Exception {
        //given
        PostWriteRequest request = postRequest("", "", Set.of("Java","Spring"));
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
    void 게시글_수정_실패_권한이_없는_유저() throws Exception {
        //given
        PostWriteRequest request = postRequest("", "", Set.of("Java","Spring"));
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
    void 게시글_조회_성공() throws Exception {
        //given
        PostDto postDto = createDto("게시글제목1", "게시글본문1");
        PostDto postDto2 = createDto("게시글제목2", "게시글본문2");
        PostDto postDto3 = createDto("게시글제목3", "게시글본문3");

        PostPaging paging = new PostPaging(1, 20, null);
        given(postQueryService.postsPerPage(eq(paging.page()), eq(paging.size()), any())).willReturn(List.of(postDto,postDto2,postDto3));
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
    void 게시글_조회_실패_page나_size가_1보다_적음() throws Exception {
        //when & then
        mockMvc.perform(get("/posts?page=-1&size=-1"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 게시글_검색_성공_특정_검색어_입력() throws Exception {
        //given
        PostDto postDto = createDto("게시글제목1", "게시글본문1");
        PostSearch postSearch = PostSearch.of(3,20,"자바");
        given(postQueryService.searchPosts(postSearch)).willReturn(List.of(postDto));
        given(postQueryService.pageCountWithSearchWord(postSearch)).willReturn(3L);

        //when & then
        mockMvc.perform(get("/posts/search?page=3&size=20&searchWord=자바"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].title").value("게시글제목1"))
                .andExpect(jsonPath("$.posts[0].contents").value("게시글본문1"))
                .andExpect(jsonPath("$.pageCount").value(3L))
                .andDo(print());
    }

    @Test
    void 포스트_검색_성공_특정_정렬조건_입력() throws Exception {
        //given
        PostDto postDto = createDto("게시글제목1", "게시글본문1");
        PostSearch postSearch = PostSearch.of(3,20,"자바", "like");
        given(postQueryService.searchPosts(any(PostSearch.class))).willReturn(List.of(postDto));
        given(postQueryService.pageCountWithSearchWord(any(PostSearch.class))).willReturn(3L);

        //when & then
        mockMvc.perform(get("/posts/search?page=3&size=20&searchWord=자바&sort=like"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].title").value("게시글제목1"))
                .andExpect(jsonPath("$.posts[0].contents").value("게시글본문1"))
                .andExpect(jsonPath("$.pageCount").value(3L))
                .andDo(print());
    }

    @Test
    void 포스트_검색_실패_존재하지_않는_정렬조건_입력() throws Exception {
        //given
        PostDto postDto = createDto("게시글제목1", "게시글본문1");
        PostSearch postSearch = PostSearch.of(3,20,"자바");
        given(postQueryService.searchPosts(any(PostSearch.class))).willReturn(List.of(postDto));
        given(postQueryService.pageCountWithSearchWord(any(PostSearch.class))).willReturn(3L);

        //when & then
        mockMvc.perform(get("/posts/search?page=3&size=20&searchWord=자바&sort=12345"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 포스트_검색_실패_page나_size값이_1보다_작음() throws Exception {
        //when & then
        mockMvc.perform(get("/posts/search?page=-1&size=-1&searchWord=자바"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 포스트_해시태그_검색_성공_해시태그_검색어_입력() throws Exception {
        //given
        PostDto postDto = createDto("게시글제목1", "게시글본문1");
        PostSearch postSearch = PostSearch.of(3,20,"aws");
        given(postQueryService.searchPostsWithHashtag(postSearch)).willReturn(List.of(postDto));
        given(postQueryService.pageCountWithHashtagSearchWord(postSearch)).willReturn(3L);

        //when & then
        mockMvc.perform(get("/posts/search-hashtag?page=3&size=20&searchWord=aws"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].title").value("게시글제목1"))
                .andExpect(jsonPath("$.posts[0].contents").value("게시글본문1"))
                .andExpect(jsonPath("$.pageCount").value(3L))
                .andDo(print());
    }

    @Test
    void 포스트_해시태그_검색_성공_해시태그_정렬조건_입력() throws Exception {
        //given
        PostDto postDto = createDto("게시글제목1", "게시글본문1");
        PostSearch postSearch = PostSearch.of(3,20,"aws", "like");
        given(postQueryService.searchPostsWithHashtag(postSearch)).willReturn(List.of(postDto));
        given(postQueryService.pageCountWithHashtagSearchWord(postSearch)).willReturn(3L);

        //when & then
        mockMvc.perform(get("/posts/search-hashtag?page=3&size=20&searchWord=aws&sort=like"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].title").value("게시글제목1"))
                .andExpect(jsonPath("$.posts[0].contents").value("게시글본문1"))
                .andExpect(jsonPath("$.pageCount").value(3L))
                .andDo(print());
    }

    @Test
    void 포스트_해시태그_검색_실패_존재하지_않는_정렬조건_입력() throws Exception {
        //when & then
        mockMvc.perform(get("/posts/search-hashtag?page=3&size=20&searchWord=자바&sort=12345"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 포스트_해시태그_검색_실패_page나_size값이_1보다_작음() throws Exception {
        //when & then
        mockMvc.perform(get("/posts/search-hashtag?page=-1&size=-1&searchWord=자바"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 게시글_좋아요_성공() throws Exception {
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
    void 게시글_좋아요_실패_로그인_하지_않은_회원() throws Exception {
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
    void 게시글_좋아요_취소_성공() throws Exception {
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
    void 게시글_좋아요_취소_실패_로그인_하지_않은_회원() throws Exception {
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
    void 게시글_댓글_작성_성공() throws Exception {
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
    void 게시글_댓글_작성_실패_로그인_하지_않은_회원의_댓글작성_요청() throws Exception {
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
    void 게시글_댓글_작성_실패_내용을_입력하지_않음() throws Exception {
        //given
        Member member = member(1L, "asdf1234@naver.com", "12345678");
        PostCommentContents postCommentContents = new PostCommentContents("");
        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.of(member));

        //when & then
        mockMvc.perform(post("/posts/1/comments")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postCommentContents))
                )
                .andExpect(status().isBadRequest())
                .andDo(print());

    }

    @Test
    void 게시글_답글_작성_성공() throws Exception {
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
    void 게시글_답글_작성_실패_로그인_하지않은_회원의_답글_작성_요청() throws Exception {
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
    void 게시글_답글_작성_실패_답글_내용을_입력하지_않음() throws Exception {
        //given
        Member member = member(1L, "asdf1234@naver.com", "12345678");
        PostCommentContents postCommentContents = new PostCommentContents("");
        given(memberRepository.findByLoginToken(cookie.getValue())).willReturn(Optional.of(member));

        //when & then
        mockMvc.perform(post("/comments/1/reply")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postCommentContents))
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

    private PostWriteRequest postRequest(String title, String contents, Set<String> hashtags) {
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