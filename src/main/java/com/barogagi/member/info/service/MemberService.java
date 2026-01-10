package com.barogagi.member.info.service;

import com.barogagi.member.basic.join.dto.NickNameDTO;
import com.barogagi.member.basic.join.service.JoinService;
import com.barogagi.member.info.dto.Member;
import com.barogagi.member.info.dto.MemberRequestDTO;
import com.barogagi.member.info.exception.MemberInfoException;
import com.barogagi.member.info.mapper.MemberMapper;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.EncryptUtil;
import com.barogagi.util.InputValidate;
import com.barogagi.util.MembershipUtil;
import com.barogagi.util.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MemberService {

    private final MemberMapper memberMapper;
    private final MembershipUtil membershipUtil;
    private final EncryptUtil encryptUtil;
    private final JoinService joinService;
    private final InputValidate inputValidate;

    @Autowired
    public MemberService(MemberMapper memberMapper,
                         MembershipUtil membershipUtil,
                         EncryptUtil encryptUtil,
                         JoinService joinService,
                         InputValidate inputValidate)
    {
        this.memberMapper = memberMapper;
        this.membershipUtil = membershipUtil;
        this.encryptUtil = encryptUtil;
        this.joinService = joinService;
        this.inputValidate = inputValidate;
    }

    public ApiResponse selectMemberInfo(HttpServletRequest request) {

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
        Member memberInfo = this.findByMembershipNo(membershipNo);
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

    public ApiResponse updateMemberProcess(HttpServletRequest request, MemberRequestDTO memberRequestDTO) {

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
        Member memberInfo = this.findByMembershipNo(membershipNo);
        if(null == memberInfo) {
            throw new MemberInfoException(ErrorCode.NOT_FOUND_USER_INFO);
        }

        // 3. 데이터 처리
        // 생년월일
        if(!inputValidate.isEmpty(memberRequestDTO.getBirth())) {
            memberInfo.setBirth(memberRequestDTO.getBirth().replaceAll("[^0-9]", ""));
        }

        // 성별 (M : 남 / W : 여)
        if(!inputValidate.isEmpty(memberRequestDTO.getGender())) {
            memberInfo.setGender(memberRequestDTO.getGender());
        }

        // 닉네임(중복X)
        if(!inputValidate.isEmpty(memberRequestDTO.getNickName())) {
            NickNameDTO nickNameRequest = new NickNameDTO();
            nickNameRequest.setNickName(memberRequestDTO.getNickName());
            int nickNameCnt = joinService.selectNicknameCnt(nickNameRequest);

            if(nickNameCnt > 0) {
                throw new MemberInfoException(ErrorCode.UNAVAILABLE_NICKNAME);
            }

            memberInfo.setNickName(memberRequestDTO.getNickName());
        }

        int updateMemberInfo = this.updateMemberInfo(memberInfo);
        if(updateMemberInfo <= 0) {
            throw new MemberInfoException(ErrorCode.FAIL_UPDATE_USER_INFO);
        }

        return ApiResponse.result(ErrorCode.SUCCESS_UPDATE_USER_INFO);
    }

    public Member findByMembershipNo(String membershipNo) {
        return memberMapper.findByMembershipNo(membershipNo);
    }

    public Member selectUserMembershipInfo(String userId) {
        return memberMapper.selectUserMembershipInfo(userId);
    }

    public int updateMemberInfo(Member member) {
        return memberMapper.updateMemberInfo(member);
    }
}
