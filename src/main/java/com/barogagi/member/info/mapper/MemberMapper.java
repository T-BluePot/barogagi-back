package com.barogagi.member.info.mapper;

import com.barogagi.member.info.dto.Member;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper {

    public Member findByMembershipNo(String membershipNo);

    public Member selectUserMembershipInfo(String userId);

    public int updateMemberInfo(Member member);
}
