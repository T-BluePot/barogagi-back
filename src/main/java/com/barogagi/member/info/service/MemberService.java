package com.barogagi.member.info.service;

import com.barogagi.member.MemberResultCode;
import com.barogagi.member.basic.join.dto.NickNameDTO;
import com.barogagi.member.basic.join.service.JoinService;
import com.barogagi.member.info.dto.Member;
import com.barogagi.member.info.dto.MemberRequestDTO;
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
    private final JoinService joinService;

    @Autowired
    public MemberService(MemberMapper memberMapper,
                         MembershipUtil membershipUtil,
                         EncryptUtil encryptUtil,
                         JoinService joinService)
    {
        this.memberMapper = memberMapper;
        this.membershipUtil = membershipUtil;
        this.encryptUtil = encryptUtil;
        this.joinService = joinService;
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

    public ApiResponse updateMemberProcess(HttpServletRequest request, MemberRequestDTO memberRequestDTO) {

        String resultCode = "";
        String message = "";

        try {

            // 1. 회원번호 구하기
            Map<String, Object> membershipNoInfo = membershipUtil.membershipNoService(request);
            if(!membershipNoInfo.get("resultCode").equals("200")) {
                throw new MemberInfoException(
                        String.valueOf(membershipNoInfo.get("resultCode")),
                        String.valueOf(membershipNoInfo.get("message")));
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

            // 3. 데이터 처리
            // 생년월일
            if(!memberRequestDTO.getBirth().isEmpty()) {
                memberInfo.setBirth(memberRequestDTO.getBirth().replaceAll("[^0-9]", ""));
            }

            // 성별 (M : 남 / W : 여)
            if(!memberRequestDTO.getGender().isEmpty()) {
                memberInfo.setGender(memberRequestDTO.getGender());
            }

            // 닉네임(중복X)
            if(!memberRequestDTO.getNickName().isEmpty()) {
                NickNameDTO nickNameRequest = new NickNameDTO();
                nickNameRequest.setNickName(memberRequestDTO.getNickName());
                int nickNameCnt = joinService.selectNicknameCnt(nickNameRequest);

                if(nickNameCnt > 0) {
                    throw new MemberInfoException(
                            MemberResultCode.UNAVAILABLE_NICKNAME.getResultCode(),
                            MemberResultCode.UNAVAILABLE_NICKNAME.getMessage()
                    );
                }

                memberInfo.setNickName(memberRequestDTO.getNickName());
            }

            int updateMemberInfo = this.updateMemberInfo(memberInfo);
            if(updateMemberInfo <= 0) {
                throw new MemberInfoException(
                        MemberResultCode.FAIL_UPDATE_USER_INFO.getResultCode(),
                        MemberResultCode.FAIL_UPDATE_USER_INFO.getMessage()
                );
            }

            resultCode = MemberResultCode.SUCCESS_UPDATE_USER_INFO.getResultCode();
            message = MemberResultCode.SUCCESS_UPDATE_USER_INFO.getMessage();

        } catch (MemberInfoException ex) {
            resultCode = ex.getResultCode();
            message = ex.getMessage();
        } catch (Exception e) {
            resultCode = ResultCode.ERROR.getResultCode();
            message = ResultCode.ERROR.getMessage();
        }

        return ApiResponse.result(resultCode, message);
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
