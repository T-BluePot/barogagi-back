package com.barogagi.config;

import com.barogagi.member.domain.MembershipStatus;
import com.barogagi.member.domain.UserMembershipInfo;
import com.barogagi.member.login.exception.InvalidRefreshTokenException;
import com.barogagi.member.repository.UserMembershipRepository;
import com.barogagi.util.JwtUtil;
import com.barogagi.util.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwt;
    private final UserMembershipRepository userMembershipRepository;

    public JwtAuthFilter(JwtUtil jwt, UserMembershipRepository userMembershipRepository) {
        this.jwt = jwt;
        this.userMembershipRepository = userMembershipRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException {

        try {
            String header = req.getHeader("Authorization");

            if (header != null && header.startsWith("Bearer ")) {

                String accessToken = header.substring(7);

                // 1. Access Token JWT 검증
                //    - 서명
                //    - issuer
                //    - expiration
                //    - typ = ACCESS
                Claims claims = jwt.parseToken(accessToken, "ACCESS");

                // 2. membershipNo 추출
                String membershipNo = jwt.getMembershipNo(claims);

                if (membershipNo == null || membershipNo.isBlank()) {
                    writeErrorResponse(ErrorCode.NOT_EXIST_ACCESS_AUTH);
                    return;
                }

                // 3. 회원 조회
                Optional<UserMembershipInfo> member = userMembershipRepository.findById(membershipNo);

                if (member.isEmpty()) {
                    writeErrorResponse(ErrorCode.NOT_EXIST_ACCESS_AUTH);
                    return;
                }

                // 4. 회원 상태 확인
                // 탈퇴/탈퇴 대기 상태라면 Access Token 차단
                if (member.get().getStatus() != MembershipStatus.ACTIVE) {
                    writeErrorResponse(ErrorCode.NOT_EXIST_ACCESS_AUTH);
                    return;
                }

                // 5. SecurityContext에 인증 정보 저장
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(member.get(), null, null);
                SecurityContextHolder.getContext().setAuthentication(auth);

                // 6. 요청 속성 저장
                req.setAttribute("membershipNo", membershipNo);
                req.setAttribute("member", member.get());
                req.setAttribute("deviceId", jwt.getDeviceId(claims));
            }
            chain.doFilter(req, res);
        } catch (ExpiredJwtException e) {
            writeErrorResponse(ErrorCode.EXPIRE_TOKEN);
        } catch (Exception e) {
            writeErrorResponse(ErrorCode.NOT_EXIST_ACCESS_AUTH);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String p = request.getRequestURI();
        return p.startsWith("/auth/")
                || p.startsWith("/login/basic/membership/userId/search")
                || p.startsWith("/oauth2")
                || p.startsWith("/login/oauth2");
    }

    private void writeErrorResponse(ErrorCode errorCode) throws IOException {
        throw new InvalidRefreshTokenException(errorCode);
    }

}

