package com.barogagi.config;

import com.barogagi.member.login.entity.UserMembership;
import com.barogagi.member.login.repository.UserMembershipRepository;
import com.barogagi.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwt;
    private final UserMembershipRepository userRepo;

    public JwtAuthFilter(JwtUtil jwt, UserMembershipRepository userRepo) {
        this.jwt = jwt;
        this.userRepo = userRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                if (!jwt.isExpired(token)) {
                    Long membershipNo = jwt.getMembershipNo(token);
                    Optional<UserMembership> opt = userRepo.findById(membershipNo);
                    if (opt.isPresent()) {
                        // roles 컬럼이 없으므로 기본 USER 권한 가정
                        var authorities = List.of(new SimpleGrantedAuthority("USER"));
                        // principal username 자리에 membershipNo 문자열을 넣어두면 나중에 바로 파싱 가능
                        var principal = new org.springframework.security.core.userdetails.User(
                                String.valueOf(membershipNo), "", authorities);

                        var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (Exception ignored) {}
        }
        chain.doFilter(req, res);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String p = request.getRequestURI();
        return p.startsWith("/auth/");
    }
}

