package com.barogagi.schedule.repository;

import com.barogagi.schedule.entity.ScheduleShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleShareRepository extends JpaRepository<ScheduleShare, Long>, JpaSpecificationExecutor<ScheduleShare> {

    @Query(value = """
            SELECT COUNT(s) > 0
            FROM ScheduleShare s
            WHERE s.shareToken = :token
            """)
    boolean existsByToken(@Param("token") String token);
}
