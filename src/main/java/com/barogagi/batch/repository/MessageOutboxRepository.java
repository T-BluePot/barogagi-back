package com.barogagi.batch.repository;

import com.barogagi.batch.entity.MessageOutbox;
import com.barogagi.batch.enums.Status;
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
           OR (o.status = 'FAIL' AND o.tryCnt <= 2)
           AND o.updatedAt < :time
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

    @Modifying
    @Query(value = """
            DELETE
            FROM MESSAGE_OUTBOX
            WHERE STATUS = :status
                AND MESSAGE_TYPE = :messageType
            """, nativeQuery = true)
    int deletedMessageOutput(@Param("status") String status, @Param("messageType") String messageType);

    @Modifying
    @Query(value = """
            UPDATE MessageOutbox messageOutBox
            SET messageOutBox.status = :afterStatus
            WHERE messageOutBox.status = :beforeStatus
            """)
    int changeStatus(@Param("afterStatus") Status afterStatus, @Param("beforeStatus") Status beforeStatus);
}
