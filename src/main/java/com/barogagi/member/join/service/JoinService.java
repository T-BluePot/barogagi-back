package com.barogagi.member.join.service;

import com.barogagi.member.join.mapper.JoinMapper;
import com.barogagi.member.join.vo.JoinVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JoinService {

    @Autowired
    private JoinMapper joinMapper;

    // 회원가입 정보 저장
    public int insertMemberInfo(JoinVO vo) throws Exception{
        return joinMapper.insertMemberInfo(vo);
    }

    // 아이디 중복 체크
    public int checkUserId(JoinVO vo) throws Exception{
        return joinMapper.checkUserId(vo);
    }
}
