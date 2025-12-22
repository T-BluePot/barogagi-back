package com.barogagi.config;

import com.barogagi.member.info.dto.Member;
import com.barogagi.member.info.service.MemberService;
import com.barogagi.member.login.exception.InvalidRefreshTokenException;
import com.barogagi.util.JwtUtil;
import com.barogagi.config.resultCode.ResultCode;
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

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwt;
    private final MemberService memberService;

    public JwtAuthFilter(JwtUtil jwt, MemberService memberService) {
        this.jwt = jwt;
        this.memberService = memberService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        try {
            String header = req.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                Claims claims = jwt.parseToken(token, "ACCESS");

                String membershipNo = jwt.getMembershipNo(claims);
                // 회원 조회
                Member member = memberService.findByMembershipNo(membershipNo);

                // 인증 컨텍스트 설정(권한 필요 없으면 빈 리스트)
                var auth = new UsernamePasswordAuthenticationToken(member, null, null);
                SecurityContextHolder.getContext().setAuthentication(auth);

                // 필요 시 요청 속성에도 실어두기
                req.setAttribute("membershipNo", membershipNo);
                req.setAttribute("member", member);
            }
            chain.doFilter(req, res);
        } catch (ExpiredJwtException e) {
            // 유효기간이 지나서 만료된 경우
            writeErrorResponse(
                    ResultCode.EXPIRE_TOKEN.getResultCode(),
                    ResultCode.EXPIRE_TOKEN.getMessage()
            );
        } catch (JwtException | SecurityException e) {
            // 위조되었거나 변조되었거나 구조가 잘못되었을 경우
            writeErrorResponse(
                    ResultCode.NOT_EXIST_ACCESS_AUTH.getResultCode(),
                    ResultCode.NOT_EXIST_ACCESS_AUTH.getMessage()
            );
        } catch (Exception e) {
            writeErrorResponse(
                    ResultCode.NOT_EXIST_ACCESS_AUTH.getResultCode(),
                    ResultCode.NOT_EXIST_ACCESS_AUTH.getMessage()
            );
        }

    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String p = request.getRequestURI();
        return p.startsWith("/auth/") || p.startsWith("/login/basic/membership/userId/search");
    }

    private void writeErrorResponse(String resultCode, String message) throws IOException {
        throw new InvalidRefreshTokenException(resultCode, message);
    }

}

