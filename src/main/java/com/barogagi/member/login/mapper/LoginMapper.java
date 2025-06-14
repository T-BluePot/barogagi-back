package com.barogagi.member.login.mapper;

import com.barogagi.member.login.vo.LoginVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LoginMapper {

    int selectMemberCnt(LoginVO vo);

    List<LoginVO> myUserIdList(LoginVO vo);

    int updatePassword(LoginVO vo);

    LoginVO findMembershipNo(LoginVO vo);
}
