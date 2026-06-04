package com.barogagi.batch.scheduler;

import com.barogagi.batch.service.PushBatchService;
import com.barogagi.sendMessage.service.CommonService;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PushSchedular {

    private final PushBatchService pushBatchService;
    private final CommonService commonService;

    // 일정 시작 24시간 전 안내 푸쉬
    @Scheduled(cron = "0 0 * * * *")  // 정각
    @SchedulerLock(
            name = "startSchedulePushBatch",
            lockAtMostFor = "1h",
            lockAtLeastFor = "55m"
    )
    public void scheduleStartPushBatch() {
        if(commonService.isProd()) {
            pushBatchService.scheduleStartPushBatch();
        }
    }
}
