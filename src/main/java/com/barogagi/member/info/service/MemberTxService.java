package com.barogagi.member.info.service;

import com.barogagi.member.domain.UserMembershipInfo;
import com.barogagi.member.join.basic.dto.Gender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberTxService {

    @Transactional
    public void updateBirth(UserMembershipInfo userMembershipInfo, String birth) {
        userMembershipInfo.setBirth(birth);
    }

    @Transactional
    public void updateGender(UserMembershipInfo userMembershipInfo, Gender gender) {
        userMembershipInfo.setGender(gender);
    }

    @Transactional
    public void updateNickName(UserMembershipInfo userMembershipInfo, String nickname) {
        userMembershipInfo.setNickName(nickname);
    }
}
