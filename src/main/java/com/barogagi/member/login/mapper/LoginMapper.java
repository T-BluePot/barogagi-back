package com.barogagi.member.login.mapper;

import com.barogagi.member.login.vo.LoginVO;
import com.barogagi.member.login.vo.LoginDTO;
import com.barogagi.member.login.vo.SearchUserIdDTO;
import com.barogagi.member.login.vo.UserIdDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LoginMapper {

    int selectMemberCnt(LoginDTO loginDTO);

    List<UserIdDTO> myUserIdList(SearchUserIdDTO searchUserIdDTO);

    int updatePassword(LoginDTO loginDTO);

    LoginVO findMembershipNo(LoginVO vo);
}
