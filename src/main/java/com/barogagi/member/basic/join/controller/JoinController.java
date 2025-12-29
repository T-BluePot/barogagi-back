package com.barogagi.member.basic.join.controller;

import com.barogagi.member.basic.join.service.BasicJoinService;
import com.barogagi.member.basic.join.dto.JoinRequestDTO;
import com.barogagi.member.login.dto.RefreshTokenRequestDTO;
import com.barogagi.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "일반 회원가입", description = "일반 회원가입 관련 API")
@RestController
@RequestMapping("/api/v1/users")
public class JoinController {

    private final BasicJoinService basicJoinService;

    @Autowired
    public JoinController(BasicJoinService basicJoinService) {
        this.basicJoinService = basicJoinService;
    }

    @Operation(summary = "아이디 중복 체크 기능", description = "아이디 중복 체크 기능입니다.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "U200", description = "해당 아이디 사용이 가능합니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A100", description = "API SECRET KEY 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "C101", description = "정보를 입력해주세요."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "U102", description = "적합한 아이디가 아닙니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "U300", description = "해당 아이디 사용이 불가능합니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @GetMapping("/userid/exists")
    public ApiResponse checkUserId(@RequestHeader("API-KEY") String apiSecretKey, @RequestParam String userId) {
        return basicJoinService.checkUserId(apiSecretKey, userId);
    }

    @Operation(summary = "회원가입 정보 저장 기능", description = "회원가입 정보 저장 기능입니다.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입에 성공하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "100", description = "API SECRET KEY 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "101", description = "정보를 입력해주세요."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "102", description = "적합한 아이디, 비밀번호, 닉네임이 아닙니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "103", description = "해당 아이디에 대한 회원 정보가 이미 존재합니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "300", description = "회원가입에 실패하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @PostMapping
    public ApiResponse signUp(@RequestBody JoinRequestDTO joinRequestDTO) {
        return basicJoinService.signUp(joinRequestDTO);
    }

    @Operation(summary = "닉네임 중복 체크 API", description = "닉네임 중복 체크 API",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "사용 가능한 닉네임입니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "100", description = "API SECRET KEY 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "101", description = "정보를 입력해주세요."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "102", description = "적합하지 않는 닉네임입니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "103", description = "해당 닉네임 사용이 불가능합니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @GetMapping("/nickname/exists")
    public ApiResponse checkDuplicateNickname(@RequestHeader("API-KEY") String apiSecretKey, @RequestParam String nickname){
        return basicJoinService.checkNickname(apiSecretKey, nickname);
    }

    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴 API",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "101", description = "정보를 입력해주세요."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 탈퇴되었습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "300", description = "회원 탈퇴 실패하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @DeleteMapping("/me")
    public ApiResponse deleteAccount(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) {
        return basicJoinService.deleteAccount(refreshTokenRequestDTO);
    }
}
