package com.barogagi.approval.repository;

import com.barogagi.approval.domain.ApprovalNumInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalNumInfoRepository extends JpaRepository<ApprovalNumInfo, Integer> {

    Optional<ApprovalNumInfo> findTopByTelAndTypeAndCompleteYnOrderByRegDateDesc(
            String tel,
            String type,
            String completeYn
    );

    Optional<ApprovalNumInfo> findTopByTelAndTypeAndCompleteYnAndAuthCodeOrderByRegDateDesc(
            String tel,
            String type,
            String completeYn,
            String authCode
    );

    @Query(value = """
            SELECT a
            FROM ApprovalNumInfo a
            WHERE a.tel = :tel
            AND a.completeYn = :completeYn
            AND a.type = :type
            AND a.regDate >= :time
            """)
    List<ApprovalNumInfo> findApprovalNumInfo(@Param("tel") String tel,
                                              @Param("completeYn") String completeYn,
                                              @Param("type") String type,
                                              @Param("time")LocalDateTime time);
}
