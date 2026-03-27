package com.barogagi.batch.service;

import com.barogagi.batch.entity.MessageOutbox;
import com.barogagi.batch.enums.Status;
import com.barogagi.batch.repository.MessageOutboxRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final EntityManager entityManager;
    private final MessageOutboxRepository messageOutboxRepository;

    // MESSAGE_OUTBOX에 저장
    @Transactional
    public void createOutbox() {
        entityManager.createNativeQuery("""
            INSERT IGNORE INTO MESSAGE_OUTBOX
            (membership_no, user_id, message_type, channel, status, try_cnt, created_at)
            SELECT
                u.membership_no,
                u.user_id,
                'WITHDRAWAL',
                CASE
                    WHEN u.join_type = 'BASIC' THEN 'ALIMTALK'
                    ELSE 'EMAIL'
                END,
                'READY',
                0,
                NOW()
            FROM USER_MEMBERSHIP_INFO u
            WHERE u.status = 'WITHDRAWAL_PENDING'
            AND u.del_date <= :targetDate
        """).setParameter("targetDate", LocalDateTime.now().plusDays(1)).executeUpdate();
    }

    public List<MessageOutbox> findTargets() {
        return messageOutboxRepository.findTargets(LocalDateTime.now().minusMinutes(1));
    }

    @Transactional
    public boolean tryProcessing(Long id) {
        return messageOutboxRepository.updateProcessing(id) > 0;
    }

    public int deletedMessageOutput(String status, String messageType) {
        return messageOutboxRepository.deletedMessageOutput(status, messageType);
    }
}
