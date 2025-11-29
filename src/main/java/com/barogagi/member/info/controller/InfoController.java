package com.barogagi.member.info.controller;

import com.barogagi.config.PasswordConfig;
import com.barogagi.member.basic.join.dto.NickNameDTO;
import com.barogagi.member.basic.join.service.JoinService;
import com.barogagi.member.info.exception.MemberInfoException;
import com.barogagi.member.info.dto.Member;
import com.barogagi.member.info.dto.MemberRequestDTO;
import com.barogagi.member.info.service.MemberService;
import com.barogagi.member.login.service.AccountService;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.EncryptUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원 정보", description = "회원 정보 관련 API")
@RestController
@RequestMapping("/info")
public class InfoController {

    private static final Logger logger = LoggerFactory.getLogger(InfoController.class);

    private final MemberService memberService;
    private final JoinService joinService;
    private final AccountService accountService;

    private final EncryptUtil encryptUtil;

    private final PasswordConfig passwordConfig;

    public InfoController(MemberService memberService,
                          JoinService joinService,
                          AccountService accountService,
                          EncryptUtil encryptUtil,
                          PasswordConfig passwordConfig) {

        this.memberService = memberService;
        this.joinService = joinService;
        this.accountService = accountService;
        this.encryptUtil = encryptUtil;
        this.passwordConfig = passwordConfig;
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

            Object membershipNoAttr = request.getAttribute("membershipNo");
            if(membershipNoAttr == null) {
                throw new MemberInfoException("401", "접근 권한이 존재하지 않습니다.");
            }

            String membershipNo = String.valueOf(membershipNoAttr);

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

            logger.info("param password={}", memberRequestDto.getPassword());
            logger.info("param email={}", memberRequestDto.getEmail());
            logger.info("param gender={}", memberRequestDto.getGender());
            logger.info("param nickName={}", memberRequestDto.getNickName());
            logger.info("param tel={}", memberRequestDto.getTel());

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

            String joinType = memberInfo.getJoinType();
            logger.info("@@ joinType={}", joinType);
            if(joinType.equals("BASIC")) {
                // 일반 회원가입으로 가입한 경우에만 비밀번호 수정 가능
                if(!memberRequestDto.getPassword().isEmpty()) {
                    String encodedPassword = passwordConfig.passwordEncoder().encode(memberRequestDto.getPassword());
                    memberInfo.setPassword(encodedPassword);
                }
            }

            // 이메일
            if(!memberRequestDto.getEmail().isEmpty()) {
                String encodedEmail = encryptUtil.encrypt(memberRequestDto.getEmail());
                memberInfo.setEmail(encodedEmail);
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

            // 프로필 이미지(저장 코드는 회의 진행 후 작업)

            // 전화번호
            if(!memberRequestDto.getTel().isEmpty()) {
                memberInfo.setTel(encryptUtil.encrypt(memberRequestDto.getTel().replaceAll("[^0-9]", "")));
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

    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴 API",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "100", description = "접근 권한이 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 탈퇴되었습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @PostMapping("/member/delete")
    public ApiResponse deleteMe(HttpServletRequest request) {

        logger.info("CALL /info/member/delete");

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {

            Object membershipNoAttr = request.getAttribute("membershipNo");
            logger.info("@@@ membershipNoAttr={}", membershipNoAttr);
            if(membershipNoAttr == null) {
                throw new MemberInfoException("100", "접근 권한이 존재하지 않습니다.");
            }

            String membershipNo = String.valueOf(membershipNoAttr);

            accountService.deleteMyAccount(membershipNo);

            resultCode = "200";
            message = "회원 탈퇴되었습니다.";

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
