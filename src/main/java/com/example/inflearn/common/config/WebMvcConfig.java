package com.example.inflearn.common.config;

import com.example.inflearn.common.security.CustomArgumentResolver;
import com.example.inflearn.infra.repository.member.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final MemberRepository memberRepository;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CustomArgumentResolver(memberRepository));
    }
}
