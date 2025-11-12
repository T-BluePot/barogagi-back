package com.barogagi.config;

import com.barogagi.member.info.dto.Member;
import com.barogagi.member.info.service.MemberService;
import com.barogagi.member.login.entity.UserMembership;
import com.barogagi.member.login.repository.UserMembershipRepository;
import com.barogagi.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.fasterxml.jackson.databind.type.LogicalType.Collection;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwt;
    private final UserMembershipRepository userRepo;
    private final MemberService memberService;

    public JwtAuthFilter(JwtUtil jwt, UserMembershipRepository userRepo, MemberService memberService) {
        this.jwt = jwt;
        this.userRepo = userRepo;
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
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
        } catch (JwtException | SecurityException e) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String p = request.getRequestURI();
        return p.startsWith("/auth/");
    }
}

