package com.barogagi.config;

import com.barogagi.member.join.oauth.service.CustomOidcUserService;
import com.barogagi.member.join.oauth.service.DelegatingOAuth2UserService;
import com.barogagi.util.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
                          OAuth2LoginFailureHandler oAuth2LoginFailureHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
        this.oAuth2LoginFailureHandler = oAuth2LoginFailureHandler;
        log.info("@@ jwtAuthFilter={}", jwtAuthFilter);
    }

    private static final String[] PERMIT_URL_ARRAY = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/login/oauth2/**",
            "/oauth2/**",
            "/api/v1/auth/**",  // 일반 회원가입 관련
            "/api/v1/users/**",  // 로그인 관련
            "/api/v1/terms",  // 약관 조회 관련
            "/api/v1/home/tags/popular",  // 인기 태그 조회
            "/api/v1/home/regions/popular",  // 인기 지역 조회
            "/api/v1/verification-codes/**",  // 인증 번호 발송
            "/api/v1/withdrawal-reasons",  // 탈퇴 사유 조회
            "api/v1/schedule/image/proxy",  // 일정 이미지 프록시 (외부 이미지 허용)
            "/api/v1/oauth-link",
            "/auth/oauth/callback",  // oauth 로그인 성공 시 redirect
    };

    @Bean
    SecurityFilterChain filterChain(
            HttpSecurity http,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper,
            CustomOidcUserService customOidcUserService,    // 구글용 (OIDC)
            DelegatingOAuth2UserService delegatingOAuth2UserService   // 네이버, 카카오용 (OAuth2)
    ) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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
                                .userService(delegatingOAuth2UserService) // Naver, Kakao
                        )
                        .successHandler(oAuth2LoginSuccessHandler)  // 로그인 성공 핸들러 (토큰 발급 등)
                        .failureHandler(oAuth2LoginFailureHandler)  // 로그인 실패 핸들러
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // 브라우저 리다이렉트 대신 401 JSON
                .exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, e) -> {

                    String resultCode = ErrorCode.FAIL_OAUTH2_LOGIN.getCode();
                    String message = ErrorCode.FAIL_OAUTH2_LOGIN.getMessage();

                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json;charset=UTF-8");
                    String json = String.format(
                            "{\"resultCode\":\"%s\", \"message\":\"%s\"}",
                            resultCode, message
                    );
                    res.getWriter().write(json);
                }));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        List<String> origins = Arrays.asList(allowedOrigins.split(","));

        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}