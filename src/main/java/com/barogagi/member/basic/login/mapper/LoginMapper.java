package com.barogagi.member.basic.login.mapper;

import com.barogagi.member.basic.login.dto.LoginVO;
import com.barogagi.member.basic.login.dto.LoginDTO;
import com.barogagi.member.basic.login.dto.SearchUserIdDTO;
import com.barogagi.member.basic.login.dto.UserIdDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LoginMapper {

    int selectMemberCnt(LoginDTO loginDTO);

    List<UserIdDTO> myUserIdList(SearchUserIdDTO searchUserIdDTO);

    int updatePassword(LoginDTO loginDTO);

    LoginVO findMembershipNo(LoginVO vo);
}
