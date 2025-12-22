package com.barogagi.member.info.controller;

import com.barogagi.member.info.dto.MemberRequestDTO;
import com.barogagi.member.info.service.MemberService;
import com.barogagi.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원 정보", description = "회원 정보 관련 API")
@RestController
@RequestMapping("/api/v1/members")
public class InfoController {

    private final MemberService memberService;

    public InfoController(MemberService memberService) {
        this.memberService = memberService;
    }

    @Operation(summary = "회원 정보 조회", description = "회원 정보 조회 기능입니다.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "접근 권한이 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "102", description = "회원 정보가 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 정보 조회가 완료되었습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @GetMapping
    public ApiResponse selectMemberInfo(HttpServletRequest request) {
        return memberService.selectMemberInfo(request);
    }

    @Operation(summary = "회원 정보 수정", description = "회원 정보 조회 수정입니다.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "접근 권한이 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "402", description = "해당 사용자에 대한 정보가 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "103", description = "해당 닉네임 사용이 불가능합니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 정보 수정 실패하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "사용자 정보 수정 완료하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @PatchMapping
    public ApiResponse updateMemberInfo(HttpServletRequest request, @RequestBody MemberRequestDTO memberRequestDTO) {
        return memberService.updateMemberProcess(request, memberRequestDTO);
    }
}
