package com.barogagi.member.login.controller;

import com.barogagi.member.login.dto.*;
import com.barogagi.member.login.service.LoginService;
import com.barogagi.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "일반 로그인 & 토큰 재발급", description = "일반 로그인, 토큰 재발급 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
public class LoginController {

    private final LoginService loginService;

    @Autowired
    public LoginController(LoginService loginService){
        this.loginService = loginService;
    }

    @Operation(summary = "로그인 기능", description = "로그인 기능입니다.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "R200", description = "로그인에 성공하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "C101", description = "정보를 입력해주세요."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "L102", description = "회원 정보가 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "L103", description = "로그인에 실패하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A100", description = "API SECRET KEY 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @PostMapping("/login")
    public ApiResponse basicMemberLogin(@RequestHeader("API-KEY") String apiSecretKey, @RequestBody LoginDTO loginRequestDTO){
        return loginService.login(apiSecretKey, loginRequestDTO);
    }

    @Operation(summary = "아이디 찾기 기능", description = "아이디 찾기 기능입니다.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "F200", description = "해당 전화번호로 가입된 아이디가 존재합니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "C101", description = "정보를 입력해주세요."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "F201", description = "해당 전화번호로 가입된 계정이 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A100", description = "API SECRET KEY 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @PostMapping("/find-user")
    public ApiResponse findUserIdByTel(@RequestHeader("API-KEY") String apiSecretKey, @RequestParam String tel){
        return loginService.findUserIdByTel(apiSecretKey, tel);
    }

    @Operation(summary = "비밀번호 재설정 기능", description = "비밀번호 재설정 기능입니다.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "U200", description = "비밀번호 재설정에 성공하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "C101", description = "정보를 입력해주세요."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "U300", description = "비밀번호 재설정에 실패하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A100", description = "API SECRET KEY 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "U400", description = "해당 아이디의 정보가 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @PostMapping("/password-reset/confirm")
    public ApiResponse resetPassword(@RequestHeader("API-KEY") String apiSecretKey, @RequestBody LoginDTO vo){
        return loginService.resetPassword(apiSecretKey, vo);
    }

    @Operation(summary = "토큰 재발급 기능", description = "Access 토큰 만료 시, Refresh 토큰으로 Access 토큰 재발급",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "C101", description = "정보를 입력해주세요."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "R110", description = "로그인을 진행해주세요."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "R120", description = "로그인을 다시 진행해주세요."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "L102", description = "회원 정보가 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "R200", description = "토큰이 발급되었습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @PostMapping("/token/refresh")
    public ApiResponse refresh(@RequestHeader("REFRESH-TOKEN") String refreshToken) {
        return loginService.refreshToken(refreshToken);
    }

    @Operation(summary = "현재 기기 로그아웃 기능", description = "현재 기기 로그아웃: 전달된 refreshToken이 속한 (membershipNo, deviceId)의 VALID 토큰들을 REVOKE",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "C101", description = "정보를 입력해주세요."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "L200", description = "로그아웃 되었습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @PostMapping("/logout")
    public ApiResponse logout(@RequestHeader("REFRESH-TOKEN") String refreshToken) {
        return loginService.logout(refreshToken);
    }
}
