package com.barogagi.batch.repository;

import com.barogagi.batch.entity.MessageSendHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageSendHistoryRepository extends JpaRepository<MessageSendHistory, Long> {
}
