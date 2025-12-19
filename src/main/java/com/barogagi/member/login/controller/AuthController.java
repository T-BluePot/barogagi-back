package com.barogagi.member.login.controller;

import com.barogagi.member.login.dto.*;
import com.barogagi.member.login.exception.InvalidRefreshTokenException;
import com.barogagi.member.login.service.AccountService;
import com.barogagi.member.login.service.AuthService;
import com.barogagi.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Tag(name = "TOKEN 재발급, 로그아웃, 탈퇴", description = "TOKEN 재발급, 로그아웃, 탈퇴 관련 API")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final AccountService accountService;

    public AuthController(AuthService authService,
                          AccountService accountService) {
        this.authService = authService;
        this.accountService = accountService;
    }
}


