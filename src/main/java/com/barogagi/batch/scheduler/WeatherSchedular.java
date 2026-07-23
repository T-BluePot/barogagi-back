package com.barogagi.batch.scheduler;

import com.barogagi.batch.service.WeatherService;
import com.barogagi.sendMessage.service.CommonService;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeatherSchedular {

    private final CommonService commonService;
    private final WeatherService weatherService;

    // 오늘 기준3일까지의 예보 조회(단기예보조회)
    @Scheduled(cron = "0 10 2,5,8,11,14,17,20,23 * * *")
    @SchedulerLock(
            name = "shortWeatherBatch",
            lockAtMostFor = "1h",
            lockAtLeastFor = "55m"
    )
    public void shortWeatherBatch() {
        if(commonService.isProd()) {
            weatherService.shortWeatherBatch();
        }
    }
}
