package com.barogagi.member.info.service;

import com.barogagi.member.MemberResultCode;
import com.barogagi.member.info.dto.Member;
import com.barogagi.member.info.exception.MemberInfoException;
import com.barogagi.member.info.mapper.MemberMapper;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.EncryptUtil;
import com.barogagi.util.MembershipUtil;
import com.barogagi.util.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MemberService {

    private final MemberMapper memberMapper;
    private final MembershipUtil membershipUtil;
    private final EncryptUtil encryptUtil;

    @Autowired
    public MemberService(MemberMapper memberMapper,
                         MembershipUtil membershipUtil,
                         EncryptUtil encryptUtil)
    {
        this.memberMapper = memberMapper;
        this.membershipUtil = membershipUtil;
        this.encryptUtil = encryptUtil;
    }

    public ApiResponse selectMemberInfo(HttpServletRequest request) {

        String resultCode = "";
        String message = "";
        Member data = null;

        try {

            // 1. 회원번호 구하기
            Map<String, Object> membershipNoInfo = membershipUtil.membershipNoService(request);
            if(!membershipNoInfo.get("resultCode").equals("200")) {
                throw new MemberInfoException(
                        String.valueOf(membershipNoInfo.get("resultCode")),
                        String.valueOf(membershipNoInfo.get("message"))
                );
            }
            String membershipNo = String.valueOf(membershipNoInfo.get("membershipNo"));

            // 2. 회원 정보 조회
            Member memberInfo = this.findByMembershipNo(membershipNo);
            if(null == memberInfo) {
                throw new MemberInfoException(
                        MemberResultCode.NOT_FOUND_USER_INFO.getResultCode(),
                        MemberResultCode.NOT_FOUND_USER_INFO.getMessage()
                );
            }

            // 이메일 복호화
            memberInfo.setEmail(encryptUtil.decrypt(memberInfo.getEmail()));

            // 전화번호 복호화
            memberInfo.setTel(encryptUtil.decrypt(memberInfo.getTel()));

            // 비밀번호는 보내주지 않는다
            memberInfo.setPassword("");

            resultCode = MemberResultCode.FOUND_USER_INFO.getResultCode();
            message = MemberResultCode.FOUND_USER_INFO.getMessage();
            data = memberInfo;

        } catch (MemberInfoException ex) {
            resultCode = ex.getResultCode();
            message = ex.getMessage();
        } catch (Exception e) {
            resultCode = ResultCode.ERROR.getResultCode();
            message = ResultCode.ERROR.getMessage();
        }

        return ApiResponse.resultData(data, resultCode, message);
    }

    public Member findByMembershipNo(String membershipNo) throws Exception {
        return memberMapper.findByMembershipNo(membershipNo);
    }

    public Member selectUserMembershipInfo(String userId) throws Exception {
        return memberMapper.selectUserMembershipInfo(userId);
    }

    public int updateMemberInfo(Member member) throws Exception {
        return memberMapper.updateMemberInfo(member);
    }
}
