package com.barogagi.batch.scheduler;

import com.barogagi.batch.service.WithdrawalBatchService;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WithdrawlScheduler {

    private final WithdrawalBatchService withdrawalBatchService;

    // 탈퇴 처리
    @Scheduled(fixedDelay = 60_000) // 1분
    @SchedulerLock(
            name = "withdrawalBatch",
            lockAtMostFor = "10m",
            lockAtLeastFor = "1m"
    )
    public void runWithdrawalBatch() {
        withdrawalBatchService.processWithdrawlBatch();
    }

    // 탈퇴 처리 전 사전 고지
    @Scheduled(fixedDelay = 60_000) // 1분
    @SchedulerLock(
            name = "beforeWithdrawlBatch",
            lockAtMostFor = "10m",
            lockAtLeastFor = "1m"
    )
    public void beforeWithdrawalBatch() {
        withdrawalBatchService.processBeforeWithdrawlBatch();
    }
}
