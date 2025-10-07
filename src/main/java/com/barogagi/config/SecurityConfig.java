package com.barogagi.config;

import com.barogagi.member.oauth.join.service.CustomOidcUserService;
import com.barogagi.member.oauth.join.service.DelegatingOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PERMIT_URL_ARRAY = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/login/oauth2/**",
            "/oauth2/**",
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
                                .userService(delegatingOAuth2UserService)      // Naver
                        )
                        .successHandler((request, response, authentication) -> {
                            response.setContentType("application/json;charset=UTF-8");

                            var token = (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication;
                            var principal = token.getPrincipal();
                            var attrs = principal.getAttributes(); // Google: OIDC claims / Naver: 언래핑된 response

                            var body = new java.util.HashMap<String, Object>();
                            body.put("provider", token.getAuthorizedClientRegistrationId());
                            body.put("sub_or_id", attrs.getOrDefault("sub", attrs.get("id")));
                            body.put("email", attrs.get("email"));
                            body.put("name", attrs.get("name"));
                            body.put("picture", attrs.getOrDefault("picture", attrs.get("profile_image")));

                            objectMapper.writeValue(response.getWriter(), body);
                        })
                        .failureHandler((request, response, ex) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"error\":\"oauth_login_failed\",\"message\":\"" + ex.getMessage() + "\"}");
                        })
                )
                // 브라우저 리다이렉트 대신 401 JSON
                .exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"error\":\"unauthorized\"}");
                }));

        return http.build();
    }
}