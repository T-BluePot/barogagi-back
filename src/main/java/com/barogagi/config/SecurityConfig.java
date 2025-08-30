package com.barogagi.config;

import com.barogagi.member.oauth.join.service.CustomOidcUserService;
import com.barogagi.member.oauth.join.service.DelegatingOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(
            HttpSecurity http,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper,
            CustomOidcUserService customOidcUserService,    // 구글용 (OIDC)
            DelegatingOAuth2UserService delegatingOAuth2UserService   // 네이버, 카카오용 (OAuth2)
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)) // OAuth 로그인은 세션 필요
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/auth/**", "/actuator/health").permitAll()
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
                );

        return http.build();
    }
}
