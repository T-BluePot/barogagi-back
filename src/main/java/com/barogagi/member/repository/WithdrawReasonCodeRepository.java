package com.barogagi.member.repository;

import com.barogagi.member.withdraw.domain.WithdrawReasonCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WithdrawReasonCodeRepository extends JpaRepository<WithdrawReasonCode, Integer>, JpaSpecificationExecutor<WithdrawReasonCode> {

    // 탈퇴 사유 조회
    @Query(value = """
            SELECT withdrawReasonCode
            FROM WithdrawReasonCode withdrawReasonCode
            WHERE withdrawReasonCode.useAt = :useAt
            ORDER BY withdrawReasonCode.sort ASC
            """)
    List<WithdrawReasonCode> findWithdrawReasonCode(@Param("useAt") String useAt);

    @Query(value = """
            SELECT w
            FROM WithdrawReasonCode w
            WHERE w.reasonNo = :reasonNo
            AND w.useAt = 'Y'
            """)
    WithdrawReasonCode findWithdrawReasonCodeInfo(@Param("reasonNo") int reasonNo);
}
