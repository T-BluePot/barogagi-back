package com.barogagi.batch.service;

import com.barogagi.batch.entity.MessageOutbox;
import com.barogagi.batch.enums.Status;
import com.barogagi.batch.repository.MessageOutboxArchiveRepository;
import com.barogagi.batch.vo.SendResult;
import com.barogagi.member.domain.MembershipStatus;
import com.barogagi.member.domain.UserMembershipInfo;
import com.barogagi.member.repository.UserMembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawalBatchService {

    private final UserMembershipRepository userMembershipRepository;
    private final MessageOutboxArchiveRepository messageOutboxArchiveRepository;
    private final OutboxService outboxService;
    private final MessageSendService messageSendService;
    private final MessageResultService messageResultService;

    @Transactional
    public void processWithdrawlBatch() {
        LocalDateTime now = LocalDateTime.now();

        // 1. 탈퇴 적용 대상 존재 여부 체크
        if(!userMembershipRepository.existsWithdrawalTarget(MembershipStatus.WITHDRAWAL_PENDING, now)) {
            return;
        }

        // 2. DELETED_MEMBERSHIP_INFO에 저장
        int insertedDeletedMembers = userMembershipRepository.insertDeletedMembers(now);

        // 3. 원본 테이블 삭제
        int deletedUserMembershipInfo = userMembershipRepository.deleteWithdrawnMembers(now);

        // 4. MESSAGE_OUTBOX의 SUCCESS 데이터를 ARCHIVE에 옮기기
        int insertedMessageOutboxArchive = messageOutboxArchiveRepository.insertMessageOutputArchive(Status.SUCCESS.name(), "WITHDRAWAL");

        // 5. MESSAGE_OUTBOX의 SUCCESS 데이터 삭제
        int deletedMessageOutbox = outboxService.deletedMessageOutput(Status.SUCCESS.name(), "WITHDRAWAL");

        log.info("[ WithdrawalBatchService.processWithdrawlBatch() ] insertedDeletedMembers={}, deletedUserMembershipInfo={}, insertedMessageOutboxArchive={}, deletedMessageOutbox={}",
                insertedDeletedMembers, deletedUserMembershipInfo, insertedMessageOutboxArchive, deletedMessageOutbox);
    }

    public void processBeforeWithdrawlBatch() {

        // 1. 발송 대상자를 발송 대상자 테이블에 저장
        outboxService.createOutbox();

        // 2. 발송 대상자 조회
        List<MessageOutbox> list = outboxService.findTargets();

        for(MessageOutbox messageOutbox : list) {
            if(!outboxService.tryProcessing(messageOutbox.getId())) {
                continue;
            }

            UserMembershipInfo userInfo = userMembershipRepository.findById(messageOutbox.getMembershipNo()).orElse(null);
            SendResult result = messageSendService.send(messageOutbox, userInfo);

            messageResultService.handleResult(messageOutbox, result.isSuccess(), result.getErrorMessage());
        }
    }

    public void changeStatusBatch() {
        outboxService.changeStatus(Status.READY, Status.PROCESSING);
    }
}
