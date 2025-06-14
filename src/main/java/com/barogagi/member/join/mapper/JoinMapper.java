package com.barogagi.member.join.mapper;

import com.barogagi.member.join.vo.JoinVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JoinMapper {

    // 회원가입 정보 저장
    int insertMemberInfo(JoinVO vo);

    // 아이디 중복 체크
    int checkUserId(JoinVO vo);
}
