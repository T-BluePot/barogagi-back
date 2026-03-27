package com.barogagi.member.repository;

import com.barogagi.member.domain.DeletedMembershipInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface DeletedMembershipRepository extends JpaRepository<DeletedMembershipInfo, String>,
        JpaSpecificationExecutor<DeletedMembershipInfo> {

    @Query("""
        SELECT COUNT(d) > 0
        FROM DeletedMembershipInfo d
        WHERE d.userId = :userId
          AND d.withdrawnAt >= :limitDate
    """)
    boolean existsRecentlyWithdrawnUser(
            @Param("userId") String userId,
            @Param("limitDate") LocalDateTime limitDate
    );
}
