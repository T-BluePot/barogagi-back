package com.barogagi.batch.service;

import com.barogagi.batch.entity.MessageOutbox;
import com.barogagi.batch.entity.MessageSendHistory;
import com.barogagi.batch.enums.Channel;
import com.barogagi.batch.enums.Status;
import com.barogagi.batch.repository.MessageOutboxRepository;
import com.barogagi.batch.repository.MessageSendHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageResultService {

    private final MessageOutboxRepository messageOutboxRepository;
    private final MessageSendHistoryRepository messageSendHistoryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleResult(MessageOutbox messageOutbox, boolean success, String errorMessage) {
        MessageOutbox outbox = messageOutboxRepository.findById(messageOutbox.getId()).orElseThrow();

        if (success) {
            outbox.markSuccess();

        } else {
            // 최대 3회 이상이면 FAIL 처리
            if (outbox.getTryCnt() >= 3) {
                outbox.markFail();
            } else {
                // 재시도 가능한 상태로 READY로 변경
                outbox.markRetry();
            }

            // 4. ALIMTALK 실패 시 SMS 발송
            if (Channel.ALIMTALK.equals(messageOutbox.getChannel())) {
                createSmsFallback(messageOutbox);
            }
        }

        // 5. 발송 이력 저장
        messageSendHistoryRepository.save(
                new MessageSendHistory(
                        messageOutbox.getMembershipNo(),
                        messageOutbox.getUserId(),
                        messageOutbox.getMessageType(),
                        messageOutbox.getChannel(),
                        success ? Status.SUCCESS : Status.FAIL,
                        errorMessage
                )
        );

        messageOutboxRepository.save(outbox);
    }

    private void createSmsFallback(MessageOutbox messageOutbox) {
        try {
            MessageOutbox sms = MessageOutbox.create(
                    messageOutbox.getMembershipNo(),
                    messageOutbox.getUserId(),
                    messageOutbox.getMessageType(),
                    Channel.SMS
            );
            messageOutboxRepository.save(sms);

        } catch (Exception ignored) {
            // UNIQUE KEY로 중복 방지
        }
    }
}
