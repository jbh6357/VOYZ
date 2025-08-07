package com.voiz.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // React 빌드 파일 서빙
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(0);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // React SPA 라우팅을 위한 설정
        // API 경로가 아닌 모든 경로를 index.html로 포워딩
        registry.addViewController("/")
                .setViewName("forward:/index.html");
        registry.addViewController("/{path:[^\\.]*}")
                .setViewName("forward:/index.html");
    }
}