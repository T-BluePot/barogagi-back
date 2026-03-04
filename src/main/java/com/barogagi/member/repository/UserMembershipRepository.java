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
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
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
               u.delDate = NULL,
               u.reasonNo = 0,
               u.withdrawReason = NULL
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
               u.delDate = :delDate,
               u.reasonNo = :reasonNo,
               u.withdrawReason = :withdrawReason
         WHERE u.membershipNo = :membershipNo
    """)
    int updateWithdrawalPending(
            @Param("membershipNo") String membershipNo,
            @Param("status") MembershipStatus status,
            @Param("delDate") LocalDateTime delDate,
            @Param("reasonNo") int reasonNo,
            @Param("withdrawReason") String withdrawReason
    );

    @Query(value = """
                SELECT COUNT(u) > 0
                FROM UserMembershipInfo u
                WHERE u.status = :status
                  AND u.delDate <= :now
            """)
    boolean existsWithdrawalTarget(@Param("status") MembershipStatus status,
                                   @Param("now") LocalDateTime now);

    @Query( value = """
                SELECT u
                FROM UserMembershipInfo u
                WHERE u.status = :status
                AND u.delDate <= :date
            """)
    List<UserMembershipInfo> findWithdrawalScheduledAfter(@Param("status") MembershipStatus status,
                                                    @Param("date") LocalDateTime dateTime);

    @Modifying
    @Query(value = """
                INSERT INTO DELETED_MEMBERSHIP_INFO
                    (MEMBERSHIP_NO, USER_ID, JOINED_AT, WITHDRAWN_AT, REASON_NO, WITHDRAW_REASON)
                SELECT
                    MEMBERSHIP_NO, USER_ID, REG_DATE, DEL_DATE, REASON_NO, WITHDRAW_REASON
                FROM USER_MEMBERSHIP_INFO
                WHERE STATUS = 'WITHDRAWAL_PENDING'
                  AND DEL_DATE <= :now
            """, nativeQuery = true)
    int insertDeletedMembers(@Param("now") LocalDateTime now);

    @Modifying
    @Query(value = """
                DELETE
                FROM USER_MEMBERSHIP_INFO
                WHERE STATUS = 'WITHDRAWAL_PENDING'
                  AND DEL_DATE <= :now
            """, nativeQuery = true)
    int deleteWithdrawnMembers(@Param("now") LocalDateTime now);
}
