package com.barogagi.batch.scheduler;

import com.barogagi.batch.service.WithdrawalBatchService;
import com.barogagi.sendMessage.service.CommonService;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WithdrawlScheduler {

    private final WithdrawalBatchService withdrawalBatchService;
    private final CommonService commonService;

    // 탈퇴 처리
    @Scheduled(cron = "0 0 9 * * *") // 09:00
    @SchedulerLock(
            name = "runWithdrawalBatch",
            lockAtMostFor = "10m",
            lockAtLeastFor = "1m"
    )
    public void runWithdrawalBatch() {
        if(commonService.isProd()) {
            withdrawalBatchService.processWithdrawlBatch();
        }
    }

    // 탈퇴 처리 전 사전 고지
    @Scheduled(cron = "0 0 10-18 * * *")  // 정각
    @SchedulerLock(
            name = "beforeWithdrawalBatch",
            lockAtMostFor = "1h",
            lockAtLeastFor = "55m"
    )
    public void beforeWithdrawalBatch() {
        if(commonService.isProd()) {
            withdrawalBatchService.processBeforeWithdrawlBatch();
        }
    }
  
    @Scheduled(cron = "0 */30 10-18 * * *")
    @SchedulerLock(
            name = "changeStatusBatch",
            lockAtMostFor = "25m",
            lockAtLeastFor = "25m"
    )
    public void changeStatusBatch() {
        if(commonService.isProd()) {
            withdrawalBatchService.changeStatusBatch();
        }
    }
}
