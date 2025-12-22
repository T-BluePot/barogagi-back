package com.barogagi.member.basic.join.mapper;

import com.barogagi.member.basic.join.dto.JoinDTO;
import com.barogagi.member.basic.join.dto.NickNameDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JoinMapper {

    // 회원가입 정보 저장
    int insertMemberInfo(JoinDTO vo);

    // 아이디 중복 체크
    int selectUserIdCnt(JoinDTO vo);

    // 닉네임 중복 체크
    int selectNicknameCnt(NickNameDTO dto);

    // 회원번호 중복 체크
    int checkDuplicateMembershipNo(String membershipNo);
}
