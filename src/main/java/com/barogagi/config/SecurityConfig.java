package com.barogagi.config;

import com.barogagi.member.oauth.join.service.CustomOidcUserService;
import com.barogagi.member.oauth.join.service.DelegatingOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
        logger.info("@@ jwtAuthFilter={}", jwtAuthFilter);
    }

    private static final String[] PERMIT_URL_ARRAY = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/login/oauth2/**",
            "/oauth2/**",
            "/login/**",
            "/membership/join/**",
            "/terms/**",
            "/main/page/popular/tag/list",
            "/main/page/popular/region/list",
            "/approval/tel/authCode/send",
            "/approval/tel/authCode/check",
            "/auth/**"
    };

    @Bean
    SecurityFilterChain filterChain(
            HttpSecurity http,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper,
            CustomOidcUserService customOidcUserService,    // 구글용 (OIDC)
            DelegatingOAuth2UserService delegatingOAuth2UserService   // 네이버, 카카오용 (OAuth2)
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                // API 서버 권장: 무상태
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // CORS preflight 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PERMIT_URL_ARRAY).permitAll()
                        // 그 외는 인증 필요
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(u -> u
                                .oidcUserService(customOidcUserService)   // Google
                                .userService(delegatingOAuth2UserService)      // Naver, Kakao
                        )
                        .successHandler(oAuth2LoginSuccessHandler)  // 로그인 성공 핸들러 (토큰 발급 등)
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // 브라우저 리다이렉트 대신 401 JSON
                .exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"error\":\"unauthorized\"}");
                }));

        return http.build();
    }
}