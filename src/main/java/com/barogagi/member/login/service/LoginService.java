package com.barogagi.member.login.service;

import com.barogagi.member.login.dto.LoginDTO;
import com.barogagi.member.login.dto.LoginVO;
import com.barogagi.member.login.dto.SearchUserIdDTO;
import com.barogagi.member.login.dto.UserIdDTO;
import com.barogagi.member.login.mapper.LoginMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoginService {

    private final LoginMapper loginMapper;

    @Autowired
    public LoginService(LoginMapper loginMapper){
        this.loginMapper = loginMapper;
    }

    public int selectMemberCnt(LoginDTO loginDTO){
        return loginMapper.selectMemberCnt(loginDTO);
    }

    public LoginVO findByUserId(LoginDTO loginDTO) {
        return loginMapper.findByUserId(loginDTO);
    }

    public List<UserIdDTO> myUserIdList(SearchUserIdDTO searchUserIdDTO){
        return loginMapper.myUserIdList(searchUserIdDTO);
    }

    public int updatePassword(LoginDTO loginDTO){
        return loginMapper.updatePassword(loginDTO);
    }

    public LoginVO findMembershipNo(LoginVO vo) {
        return loginMapper.findMembershipNo(vo);
    }

    public LoginVO findMembershipByToken(String token) {
        return loginMapper.findMembershipByToken(token);
    }
}