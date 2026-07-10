package com.barogagi.member.info.service;

import com.barogagi.batch.entity.KorTourOrgLocalCode;
import com.barogagi.batch.repository.KorTourOrgLocalCodeRepository;
import com.barogagi.member.domain.UserMembershipInfo;
import com.barogagi.member.info.dto.InfoResponseDTO;
import com.barogagi.member.info.dto.MemberRequestDTO;
import com.barogagi.member.info.dto.UserInfoResponseDTO;
import com.barogagi.member.info.exception.MemberInfoException;
import com.barogagi.member.join.basic.dto.Gender;
import com.barogagi.member.repository.UserMembershipRepository;

import com.barogagi.push.service.PushService;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.EncryptUtil;
import com.barogagi.util.InputValidate;
import com.barogagi.util.MembershipUtil;
import com.barogagi.util.Validator;
import com.barogagi.util.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MembershipUtil membershipUtil;
    private final EncryptUtil encryptUtil;
    private final InputValidate inputValidate;
    private final UserMembershipRepository userMembershipRepository;
    private final KorTourOrgLocalCodeRepository korTourOrgLocalCodeRepository;
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

        // 결과값 세팅
        InfoResponseDTO infoResponseDTO = new InfoResponseDTO();
        infoResponseDTO.setMembershipNo(memberInfo.getMembershipNo());
        infoResponseDTO.setUserId(memberInfo.getUserId());
        infoResponseDTO.setPassword(memberInfo.getPassword());
        infoResponseDTO.setEmail(memberInfo.getEmail());
        infoResponseDTO.setBirth(memberInfo.getBirth());
        infoResponseDTO.setTel(memberInfo.getTel());
        infoResponseDTO.setGender(memberInfo.getGender());
        infoResponseDTO.setNickName(memberInfo.getNickName());
        infoResponseDTO.setJoinType(memberInfo.getJoinType());
        infoResponseDTO.setRegDate(memberInfo.getRegDate());
        infoResponseDTO.setUpdDate(memberInfo.getUpdDate());

        // 선호 지역 코드 번호가 있는 경우 번호 -> 지역 코드 & 시군구 코드로 변환
        if(null != memberInfo.getPreferredLocalCodeNo()) {
            KorTourOrgLocalCode localCode = korTourOrgLocalCodeRepository.findById(memberInfo.getPreferredLocalCodeNo())
                    .orElseThrow(() -> new MemberInfoException(ErrorCode.NOT_FOUND_LOCAL_CODE));

            infoResponseDTO.setAreaCd(localCode.getAreaCd());
            infoResponseDTO.setSigunguCd(localCode.getSigunguCd());
        }

        return ApiResponse.resultData(
                infoResponseDTO,
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
        UserMembershipInfo memberInfo = userMembershipRepository.findById(membershipNo).orElseThrow(() -> new MemberInfoException(ErrorCode.NOT_FOUND_USER_INFO));

        // 3. 데이터 처리 & update
        // 생년월일
        if(!inputValidate.isEmpty(memberRequestDTO.getBirth())) {

            memberRequestDTO.setBirth(memberRequestDTO.getBirth().replaceAll("[^0-9]", ""));

            // 8자리 숫자인지 확인
            if (!memberRequestDTO.getBirth().matches("^\\d{8}$")) {
                throw new MemberInfoException(ErrorCode.FAIL_INVALID_BIRTH_DATE_FORMAT);
            }

            // 실제 날짜인지 검증
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);
            try {
                LocalDate.parse(memberRequestDTO.getBirth(), formatter);
            } catch (DateTimeParseException e) {
                throw new MemberInfoException(ErrorCode.FAIL_INVALID_BIRTH_DATE_FORMAT);
            }

            memberTxService.updateBirth(memberInfo, memberRequestDTO.getBirth());

        }

        // 성별 (M : 남 / W : 여)
        if(!inputValidate.isEmpty(memberRequestDTO.getGender())) {
            if("M".equals(memberRequestDTO.getGender())) {
                memberTxService.updateGender(memberInfo, Gender.M);
            } else if("W".equals(memberRequestDTO.getGender())) {
                memberTxService.updateGender(memberInfo, Gender.W);
            } else {
                throw new MemberInfoException(ErrorCode.FAIL_INVALID_GENDER);
            }
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

        // 선호 지역
        if(!inputValidate.isEmpty(memberRequestDTO.getAreaCd()) && !inputValidate.isEmpty(memberRequestDTO.getSigunguCd())) {
            KorTourOrgLocalCode localCode = korTourOrgLocalCodeRepository.findLocalCodeInfo(memberRequestDTO.getAreaCd(), memberRequestDTO.getSigunguCd());

            if(null == localCode) {
                throw new MemberInfoException(ErrorCode.NOT_FOUND_LOCAL_CODE);
            }
            memberTxService.updateLocalCode(memberInfo, localCode.getLocalCodeNo());
        }

        return ApiResponse.result(ErrorCode.SUCCESS_UPDATE_USER_INFO);
    }
}
