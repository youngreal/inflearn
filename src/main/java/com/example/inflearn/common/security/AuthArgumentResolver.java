package com.example.inflearn.common.security;

import com.example.inflearn.common.exception.EmptyCookieRequestException;
import com.example.inflearn.common.exception.UnAuthorizationException;
import com.example.inflearn.common.exception.WrongServletRequestException;
import com.example.inflearn.domain.member.Member;
import com.example.inflearn.infra.repository.member.MemberRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
@RequiredArgsConstructor
public class AuthArgumentResolver implements HandlerMethodArgumentResolver {

    private final MemberRepository memberRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(LoginedMember.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);

        if (servletRequest == null) {
            throw new WrongServletRequestException();
        }

        if (servletRequest.getCookies() == null) {
            throw new EmptyCookieRequestException();
        }

        Cookie[] cookies = servletRequest.getCookies();
        Cookie cookie = Arrays.stream(cookies)
                .filter(cookie1 -> cookie1.getName().equals("SESSION"))
                .findFirst()
                .orElseThrow(() -> new UnAuthorizationException("유효하지 않은 세션토큰"));

        Member member = memberRepository.findByLoginToken(cookie.getValue()).orElseThrow(() -> new UnAuthorizationException("존재하지 않은 세션토큰"));
        return new LoginedMember(member.getId());
    }
}
