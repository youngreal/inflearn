package com.example.musinsa.common.security;

import com.example.musinsa.common.exception.EmptyCookieRequestException;
import com.example.musinsa.common.exception.UnAuthorizationException;
import com.example.musinsa.common.exception.WrongServletRequestException;
import com.example.musinsa.domain.Member;
import com.example.musinsa.infra.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@RequiredArgsConstructor
public class CustomArgumentResolver implements HandlerMethodArgumentResolver {

    private final MemberRepository memberRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(CurrentMember.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);

        if (servletRequest == null) {
            throw new WrongServletRequestException("잘못된 서블릿 요청입니다");
        }

        if (servletRequest.getCookies() == null) {
            throw new EmptyCookieRequestException("잘못된 요청입니다");
        }

        Cookie[] cookies = servletRequest.getCookies();
        Cookie cookie = Arrays.stream(cookies)
                .filter(cookie1 -> cookie1.getName().equals("SESSION"))
                .findFirst()
                .orElseThrow(() -> new UnAuthorizationException("유효하지않은 세션토큰"));

        Member member = memberRepository.findByLoginToken(cookie.getValue()).orElseThrow(() -> new UnAuthorizationException("유효하지 않은 세션토큰"));
        return new CurrentMember(member.getId());
    }
}
