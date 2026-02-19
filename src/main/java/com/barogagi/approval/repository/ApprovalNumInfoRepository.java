package com.barogagi.approval.repository;

import com.barogagi.approval.domain.ApprovalNumInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

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
}
