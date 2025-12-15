package com.barogagi.member.info.controller;

import com.barogagi.mainPage.exception.MainPageException;
import com.barogagi.member.basic.join.dto.NickNameDTO;
import com.barogagi.member.basic.join.service.JoinService;
import com.barogagi.member.info.exception.MemberInfoException;
import com.barogagi.member.info.dto.Member;
import com.barogagi.member.info.dto.MemberRequestDTO;
import com.barogagi.member.info.service.MemberService;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.EncryptUtil;
import com.barogagi.util.MembershipUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "회원 정보", description = "회원 정보 관련 API")
@RestController
@RequestMapping("/info")
public class InfoController {

    private static final Logger logger = LoggerFactory.getLogger(InfoController.class);

    private final MemberService memberService;
    private final JoinService joinService;

    private final EncryptUtil encryptUtil;
    private final MembershipUtil membershipUtil;

    public InfoController(MemberService memberService,
                          JoinService joinService,
                          EncryptUtil encryptUtil,
                          MembershipUtil membershipUtil) {

        this.memberService = memberService;
        this.joinService = joinService;
        this.encryptUtil = encryptUtil;
        this.membershipUtil = membershipUtil;
    }

    @Operation(summary = "회원 정보 조회", description = "회원 정보 조회 기능입니다.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "접근 권한이 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "402", description = "해당 사용자에 대한 정보가 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 정보 조회가 완료되었습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @PostMapping("/member")
    public ApiResponse selectMemberInfo(HttpServletRequest request) {
        logger.info("CALL /info/member");

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {

            // 회원번호 구하기
            Map<String, Object> membershipNoInfo = membershipUtil.MembershipNoService(request);
            if(!membershipNoInfo.get("resultCode").equals("200")) {
                throw new MemberInfoException(String.valueOf(membershipNoInfo.get("resultCode")),
                        String.valueOf(membershipNoInfo.get("message")));
            }

            String membershipNo = String.valueOf(membershipNoInfo.get("membershipNo"));

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

    @Operation(summary = "회원 정보 수정", description = "회원 정보 조회 수정입니다.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "접근 권한이 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "402", description = "해당 사용자에 대한 정보가 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "이미 해당 닉네임이 존재합니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 정보 수정 실패하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "사용자 정보 수정 완료하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @PostMapping("/member/update")
    public ApiResponse updateMemberInfo(HttpServletRequest request, @RequestBody MemberRequestDTO memberRequestDto) {
        logger.info("CALL /info/member/update");

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {

            logger.info("param gender={}", memberRequestDto.getGender());
            logger.info("param nickName={}", memberRequestDto.getNickName());

            // 회원번호 구하기
            Map<String, Object> membershipNoInfo = membershipUtil.MembershipNoService(request);
            if(!membershipNoInfo.get("resultCode").equals("200")) {
                throw new MemberInfoException(String.valueOf(membershipNoInfo.get("resultCode")),
                        String.valueOf(membershipNoInfo.get("message")));
            }

            String membershipNo = String.valueOf(membershipNoInfo.get("membershipNo"));
            logger.info("@@ membershipNo={}", membershipNo);

            // 회원 정보 조회
            Member memberInfo = memberService.findByMembershipNo(membershipNo);
            if(null == memberInfo) {
                throw new MemberInfoException("402", "해당 사용자에 대한 정보가 존재하지 않습니다.");
            }

            // 생년월일
            if(!memberRequestDto.getBirth().isEmpty()) {
                memberInfo.setBirth(memberRequestDto.getBirth().replaceAll("[^0-9]", ""));
            }

            // 성별 (M : 남 / W : 여)
            if(!memberRequestDto.getGender().isEmpty()) {
                memberInfo.setGender(memberRequestDto.getGender());
            }

            // 닉네임(중복X)
            if(!memberRequestDto.getNickName().isEmpty()) {
                NickNameDTO nickNameRequest = new NickNameDTO();
                nickNameRequest.setNickName(memberRequestDto.getNickName());
                int nickNameCnt = joinService.checkNickName(nickNameRequest);

                logger.info("@@ nickNameCnt={}", nickNameCnt);
                if(nickNameCnt > 0) {
                    throw new MemberInfoException("403", "이미 해당 닉네임이 존재합니다.");
                }

                memberInfo.setNickName(memberRequestDto.getNickName());
            }

            int updateMemberInfo = memberService.updateMemberInfo(memberInfo);
            logger.info("@@ updateMemberInfo={}", updateMemberInfo);
            if(updateMemberInfo <= 0) {
                throw new MemberInfoException("404", "사용자 정보 수정 실패하였습니다.");
            }

            // 사용자 정보 수정 성공
            resultCode = "200";
            message = "사용자 정보 수정 완료하였습니다.";

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
