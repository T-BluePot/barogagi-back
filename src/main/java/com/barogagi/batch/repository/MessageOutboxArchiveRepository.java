package com.barogagi.batch.repository;

import com.barogagi.batch.entity.MessageOutboxArchive;
import com.barogagi.batch.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageOutboxArchiveRepository extends JpaRepository<MessageOutboxArchive, Long>  {

    @Modifying
    @Query(value = """
            INSERT INTO MESSAGE_OUTBOX_ARCHIVE
                (OUTBOX_NO, MEMBERSHIP_NO, USER_ID, MESSAGE_TYPE, CHANNEL, STATUS, TRY_CNT, CREATED_AT, UPDATED_AT)
            SELECT
                OUTBOX_NO, MEMBERSHIP_NO, USER_ID, MESSAGE_TYPE, CHANNEL, STATUS, TRY_CNT, CREATED_AT, UPDATED_AT
            FROM MESSAGE_OUTBOX
            WHERE STATUS = :status
                AND MESSAGE_TYPE = :messageType
            """, nativeQuery = true)
    int insertMessageOutputArchive(@Param("status") String status, @Param("messageType") String messageType);
}
