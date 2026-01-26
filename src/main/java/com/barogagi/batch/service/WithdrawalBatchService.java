package com.barogagi.batch.service;

import com.barogagi.member.domain.MembershipStatus;
import com.barogagi.member.domain.UserMembershipInfo;
import com.barogagi.member.repository.UserMembershipRepository;
import com.barogagi.sendMessage.alimTalk.service.AlimTalkSendService;
import com.barogagi.sendMessage.sms.service.SmsSendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawalBatchService {

    private final AlimTalkSendService alimTalkSendService;

    private final UserMembershipRepository userMembershipRepository;

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

    @Transactional
    public void processBeforeWithdrawlBatch() {
        LocalDateTime dateTime = LocalDateTime.now().plusHours(24);

        // 1. 24시간 이후 탈퇴 적용 대상 존재 여부 체크
        if(!userMembershipRepository.existsWithdrawalTarget(MembershipStatus.WITHDRAWAL_PENDING, dateTime)) {
            return;
        }

        // 2. 문자/알림톡 발송 대상자 조회
        List<UserMembershipInfo> withdrawlList = userMembershipRepository.findWithdrawalScheduledAfter(MembershipStatus.WITHDRAWAL_PENDING, dateTime);

        int total = 0;
        int successed = 0;
        int failed = 0;
        for(UserMembershipInfo userInfo : withdrawlList) {
            boolean sendResult = alimTalkSendService.sendWithdrawalAlimTalk(userInfo.getTel());
            if(sendResult) {
                successed++;
            } else {
                failed++;
            }
        }
        log.info("[ WithdrawlBatchService.processBeforeWithdrawlBatch() ] total={}, successed={}, failed={}", total, successed, failed);
    }
}
