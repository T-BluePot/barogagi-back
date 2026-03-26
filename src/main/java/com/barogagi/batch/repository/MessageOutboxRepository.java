package com.barogagi.batch.repository;

import com.barogagi.batch.entity.MessageOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageOutboxRepository extends JpaRepository<MessageOutbox, Long> {

    @Query("""
        SELECT o FROM MessageOutbox o
        WHERE o.status = 'READY'
           OR (o.status = 'FAIL' AND o.tryCnt <= 3)
    """)
    List<MessageOutbox> findTargets(@Param("time") LocalDateTime time);

    @Modifying
    @Query("""
        UPDATE MessageOutbox o
        SET o.status = 'PROCESSING'
        WHERE o.id = :id
          AND o.status IN ('READY', 'FAIL')
    """)
    int updateProcessing(@Param("id") Long id);
}
