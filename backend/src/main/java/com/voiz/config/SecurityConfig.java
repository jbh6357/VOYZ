package com.voiz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()  // 모든 요청 허용
            )
            .csrf(csrf -> csrf.disable())  // CSRF 비활성화
            .httpBasic(httpBasic -> httpBasic.disable())  // HTTP Basic 인증 비활성화
            .formLogin(formLogin -> formLogin.disable());  // Form 로그인 비활성화
            
        return http.build();
    }
}