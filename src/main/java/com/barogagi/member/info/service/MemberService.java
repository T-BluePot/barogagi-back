package com.barogagi.member.info.service;

import com.barogagi.member.domain.UserMembershipInfo;
import com.barogagi.member.info.dto.MemberRequestDTO;
import com.barogagi.member.info.dto.UserInfoResponseDTO;
import com.barogagi.member.info.exception.MemberInfoException;
import com.barogagi.member.repository.UserMembershipRepository;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.EncryptUtil;
import com.barogagi.util.InputValidate;
import com.barogagi.util.MembershipUtil;
import com.barogagi.util.Validator;
import com.barogagi.util.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MembershipUtil membershipUtil;
    private final EncryptUtil encryptUtil;
    private final InputValidate inputValidate;
    private final UserMembershipRepository userMembershipRepository;
    private final MemberTxService memberTxService;
    private final Validator validator;

    public ApiResponse getUserInfo(HttpServletRequest request) {

        // 1. 회원번호 구하기
        Map<String, Object> membershipNoInfo = membershipUtil.membershipNoService(request);
        if(!membershipNoInfo.get("resultCode").equals("A200")) {
            return ApiResponse.error(
                    String.valueOf(membershipNoInfo.get("resultCode")),
                    String.valueOf(membershipNoInfo.get("message"))
            );
        }
        String membershipNo = String.valueOf(membershipNoInfo.get("membershipNo"));

        // 2. 회원 정보 조회
        UserInfoResponseDTO memberInfo = userMembershipRepository.findByMembershipNo(membershipNo);
        if(null == memberInfo) {
            throw new MemberInfoException(ErrorCode.NOT_FOUND_USER_INFO);
        }

        // 이메일 복호화
        memberInfo.setEmail(encryptUtil.decrypt(memberInfo.getEmail()));

        // 전화번호 복호화
        memberInfo.setTel(encryptUtil.decrypt(memberInfo.getTel()));

        // 비밀번호는 보내주지 않는다
        memberInfo.setPassword("");

        return ApiResponse.resultData(
                memberInfo,
                ErrorCode.FOUND_USER_INFO.getCode(),
                ErrorCode.FOUND_USER_INFO.getMessage()
        );
    }

    public ApiResponse updateUserInfo(HttpServletRequest request, MemberRequestDTO memberRequestDTO) {

        // 1. 회원번호 구하기
        Map<String, Object> membershipNoInfo = membershipUtil.membershipNoService(request);
        if(!membershipNoInfo.get("resultCode").equals("A200")) {
            return ApiResponse.error(
                    String.valueOf(membershipNoInfo.get("resultCode")),
                    String.valueOf(membershipNoInfo.get("message"))
            );
        }

        String membershipNo = String.valueOf(membershipNoInfo.get("membershipNo"));

        // 2. 회원 정보 조회
        UserMembershipInfo memberInfo = userMembershipRepository.findById(membershipNo)
                .orElseThrow(() -> new MemberInfoException(ErrorCode.NOT_FOUND_USER_INFO));


        // 3. 데이터 처리 & update
        // 생년월일
        if(!inputValidate.isEmpty(memberRequestDTO.getBirth())) {
            memberTxService.updateBirth(memberInfo, memberRequestDTO.getBirth().replaceAll("[^0-9]", ""));
        }

        // 성별 (M : 남 / W : 여)
        if(!inputValidate.isEmpty(memberRequestDTO.getGender())) {
            memberTxService.updateGender(memberInfo, memberRequestDTO.getGender());
        }

        // 닉네임(중복X)
        if(!inputValidate.isEmpty(memberRequestDTO.getNickName())) {

            if(!validator.isValidNickname(memberRequestDTO.getNickName())) {
                throw new MemberInfoException(ErrorCode.INVALID_NICKNAME);
            }

            boolean existsNickname = userMembershipRepository.existsByNickName(memberRequestDTO.getNickName());
            if(existsNickname) {
                throw new MemberInfoException(ErrorCode.UNAVAILABLE_NICKNAME);
            }
            memberTxService.updateNickName(memberInfo, memberRequestDTO.getNickName());
        }

        return ApiResponse.result(ErrorCode.SUCCESS_UPDATE_USER_INFO);
    }
}
