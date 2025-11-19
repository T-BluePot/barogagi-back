package com.barogagi.member.info.controller;

import com.barogagi.member.info.MemberInfoException;
import com.barogagi.member.info.dto.Member;
import com.barogagi.member.info.service.MemberService;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.EncryptUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원 정보", description = "회원 정보 관련 API")
@RestController
@RequestMapping("/info")
public class InfoController {

    private static final Logger logger = LoggerFactory.getLogger(InfoController.class);

    private final MemberService memberService;

    private final EncryptUtil encryptUtil;

    public InfoController(MemberService memberService,
                          EncryptUtil encryptUtil) {
        this.memberService = memberService;
        this.encryptUtil = encryptUtil;
    }

    @Operation(summary = "회원 정보 조회", description = "회원 정보 조회 기능입니다.")
    @GetMapping("/member")
    public ApiResponse selectMemberInfo(HttpServletRequest request) {
        logger.info("CALL /info/member");

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {

            String membershipNo = String.valueOf(request.getAttribute("membershipNo"));
            logger.info("@@ membershipNo.isEmpty()={}", membershipNo.isEmpty());
            if (membershipNo.isEmpty()) {
                throw new MemberInfoException("401", "접근 권한이 존재하지 않습니다.");
            }

            // 회원 정보 조회
            Member memberInfo = memberService.findByMembershipNo(membershipNo);
            if(null == memberInfo) {
                throw new MemberInfoException("402", "해당 사용자에 대한 정보가 존재하지 않습니다.");
            }

            // 이메일 복호화
            memberInfo.setEmail(encryptUtil.decrypt(memberInfo.getEmail()));

            // 전화번호 복호화
            memberInfo.setTel(encryptUtil.decrypt(memberInfo.getTel()));

            // 비밀번호는 보내주지 않는다
            memberInfo.setPassword("");

            resultCode = "200";
            message = "회원 정보 조회가 완료되었습니다.";
            apiResponse.setData(memberInfo);

        } catch (MemberInfoException ex) {
            resultCode = ex.getResultCode();
            message = ex.getMessage();

        } catch (Exception e) {
            logger.error("error", e);
            resultCode = "400";
            message = "오류가 발생하였습니다.";
        } finally {
            apiResponse.setResultCode(resultCode);
            apiResponse.setMessage(message);
        }

        return apiResponse;
    }
}
