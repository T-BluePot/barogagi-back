package com.barogagi.batch.service;

import com.barogagi.batch.dto.SendDTO;
import com.barogagi.batch.entity.MessageOutbox;
import com.barogagi.batch.vo.SendResult;
import com.barogagi.member.domain.MembershipStatus;
import com.barogagi.member.domain.UserMembershipInfo;
import com.barogagi.member.repository.UserMembershipRepository;
import com.barogagi.sendMessage.alimTalk.service.AlimTalkSendService;
import com.barogagi.sendMessage.email.dto.SendMailDTO;
import com.barogagi.sendMessage.email.service.EmailSendService;
import com.barogagi.util.EncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawalBatchService {

    private final UserMembershipRepository userMembershipRepository;
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
        int inserted = userMembershipRepository.insertDeletedMembers(now);

        // 3. 원본 테이블 삭제
        int deleted = userMembershipRepository.deleteWithdrawnMembers(now);

        log.info("[ WithdrawalBatchService.processWithdrawlBatch() ] inserted={}, deleted={}", inserted, deleted);
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
}
