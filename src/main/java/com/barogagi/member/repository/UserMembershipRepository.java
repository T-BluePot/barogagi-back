package com.barogagi.member.repository;

import com.barogagi.member.domain.MembershipStatus;
import com.barogagi.member.domain.UserMembershipInfo;
import com.barogagi.member.info.dto.UserInfoResponseDTO;
import com.barogagi.member.login.dto.UserIdDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    // 회원 탈퇴
    int deleteByMembershipNo(String membershipNo);

    // 회원 탈퇴 원상복구
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE UserMembershipInfo u
           SET u.status = :updateStatus,
               u.delDate = NULL
         WHERE u.membershipNo = :membershipNo
           AND u.status = :beforeStatus
    """)
    void restoreWithdrawal(@Param("membershipNo") String membershipNo,
                          @Param("updateStatus") MembershipStatus updateStatus,
                          @Param("beforeStatus") MembershipStatus beforeStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE UserMembershipInfo u
           SET u.status = :status,
               u.delDate = :delDate
         WHERE u.membershipNo = :membershipNo
    """)
    int updateWithdrawalPending(
            @Param("membershipNo") String membershipNo,
            @Param("status") MembershipStatus status,
            @Param("delDate") LocalDateTime delDate
    );
}
