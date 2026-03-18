package com.barogagi.batch.scheduler;

import com.barogagi.batch.service.WithdrawalBatchService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WithdrawlScheduler {

    private final WithdrawalBatchService withdrawalBatchService;

    private final String IS_WITHDRAW_MEMBER_FLAG;  // 탈퇴 처리 배치 flag
    private final String IS_PRE_WITHDRAWAL_NOTICE_FLAG;  // 탈퇴 처리 전 사전 고지 flag

    public WithdrawlScheduler(Environment environment, WithdrawalBatchService withdrawalBatchService) {
        this.withdrawalBatchService = withdrawalBatchService;
        this.IS_WITHDRAW_MEMBER_FLAG = environment.getRequiredProperty("withdraw.member.flag");
        this.IS_PRE_WITHDRAWAL_NOTICE_FLAG = environment.getRequiredProperty("pre.withdrawal.notice");
    }

    // 탈퇴 처리
    @Scheduled(fixedDelay = 60_000) // 1분
    @SchedulerLock(
            name = "withdrawalBatch",
            lockAtMostFor = "10m",
            lockAtLeastFor = "1m"
    )
    public void runWithdrawalBatch() {
        if(Boolean.parseBoolean(IS_WITHDRAW_MEMBER_FLAG)) {
            withdrawalBatchService.processWithdrawlBatch();
        }
    }

    // 탈퇴 처리 전 사전 고지
    @Scheduled(fixedDelay = 60_000) // 1분
    @SchedulerLock(
            name = "beforeWithdrawlBatch",
            lockAtMostFor = "10m",
            lockAtLeastFor = "1m"
    )
    public void beforeWithdrawalBatch() {
//        if(Boolean.parseBoolean(IS_PRE_WITHDRAWAL_NOTICE_FLAG)) {
//            withdrawalBatchService.processBeforeWithdrawlBatch();
//        }
        withdrawalBatchService.processBeforeWithdrawlBatch();
    }
}
