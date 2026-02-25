package com.barogagi.member.repository;

import com.barogagi.member.domain.UserMembershipInfo;
import com.barogagi.member.info.dto.UserInfoResponseDTO;
import com.barogagi.member.login.dto.UserIdDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface UserMembershipRepository extends JpaRepository<UserMembershipInfo, String>, JpaSpecificationExecutor<UserMembershipInfo> {

    // 아이디 중복 여부
    boolean existsByUserId(String userId);

    // 닉네임 중복 여부(true : 존재, false : 민존재)
    boolean existsByNickName(String nickName);

    // 전화번호 중복 여부
    boolean existsByTel(String tel);

    UserMembershipInfo findByUserId(String userId);

    UserInfoResponseDTO findByMembershipNo(String membershipNo);

    UserIdDTO findByTel(String tel);

    int deleteByMembershipNo(String membershipNo);
}
