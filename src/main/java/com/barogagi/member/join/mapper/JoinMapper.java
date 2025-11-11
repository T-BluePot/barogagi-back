package com.barogagi.member.join.mapper;

import com.barogagi.member.join.dto.JoinDTO;
import com.barogagi.member.join.dto.NickNameDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JoinMapper {

    // 회원가입 정보 저장
    int insertMemberInfo(JoinDTO vo);

    // 아이디 중복 체크
    int checkUserId(JoinDTO vo);

    // 닉네임 중복 체크
    int checkNickName(NickNameDTO dto);

    // 회원번호 중복 체크
    int checkDuplicateMembershipNo(String membershipNo);
}
