package com.barogagi.config;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@WebFilter(urlPatterns = "/*")
public class XSSFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String uri = req.getRequestURI();

        // 1) OAuth 관련 경로는 그대로 통과 (절대 래핑 금지)
        if (uri.startsWith(req.getContextPath() + "/login/oauth2/")
                || uri.startsWith(req.getContextPath() + "/oauth2/authorization/")
                || uri.startsWith(req.getContextPath() + "/oauth2/callback/")) {
            chain.doFilter(request, response);  // ✅ 절대 래핑 금지
            return;
        }

        // 2) 그 외만 래핑
        XSSRequestWrapper wrapped = new XSSRequestWrapper(req);
        chain.doFilter(wrapped, response);
    }
}

