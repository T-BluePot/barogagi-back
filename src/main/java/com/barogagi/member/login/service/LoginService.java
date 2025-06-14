package com.barogagi.member.login.service;

import com.barogagi.member.login.mapper.LoginMapper;
import com.barogagi.member.login.vo.LoginVO;
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

    public int selectMemberCnt(LoginVO vo){
        return loginMapper.selectMemberCnt(vo);
    }

    public List<LoginVO> myUserIdList(LoginVO vo){
        return loginMapper.myUserIdList(vo);
    }

    public int updatePassword(LoginVO vo){
        return loginMapper.updatePassword(vo);
    }

    public LoginVO findMembershipNo(LoginVO vo) {
        return loginMapper.findMembershipNo(vo);
    }
}
