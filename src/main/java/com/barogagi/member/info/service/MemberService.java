package com.barogagi.member.info.service;

import com.barogagi.member.info.dto.Member;
import com.barogagi.member.info.mapper.MemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    @Autowired
    private MemberMapper memberMapper;

    public Member findByMembershipNo(String membershipNo) throws Exception {
        return memberMapper.findByMembershipNo(membershipNo);
    }

    public Member selectUserMembershipInfo(String userId) throws Exception {
        return memberMapper.selectUserMembershipInfo(userId);
    }
}
