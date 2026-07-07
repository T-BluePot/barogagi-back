package com.barogagi.batch.scheduler;

import com.barogagi.batch.service.PublicDataService;
import com.barogagi.sendMessage.service.CommonService;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AreaSchedular {

    // 공공 데이터 접근 service
    private final PublicDataService publicDataService;

    private final CommonService commonService;

    /*
    티맵 모빌리티(주)의 내비게이션 데이터를 기반으로 산출된 지역별 중심 관광지 정보입니다.
    해당 지역의 관광지 중 타 관광지와 연계 방문하는 빈도가 높은 관광지가 중심 관광지가 됩니다.
    Tmap 어플리케이션 사용자가 목적지를 조회하고 거리 기준으로 100m, 시간 기준으로 1분 이상 이동한 조건을 모두 충족한 경우에 한해 집계되며,
    차량이동을 기준으로 하므로 실제 연계 방문 정도나 연계 방문하는 사람의 수와는 차이가 있을 수 있습니다.
    지자체별 타 관광지와 가장 많이 연결되는 중심 관광지 100위 정보를 제공합니다.
     */
    // 전국·지역별 추천, 인기 지역
    @Scheduled(cron = "0 0 0 1 * *")
    @SchedulerLock(
            name = "localPopularAreaBatch",
            lockAtMostFor = "30m",
            lockAtLeastFor = "5m"
    )
    public void localPopularAreaBatch() {
        if(commonService.isProd()) {
            publicDataService.insertLocalPopularArea();
        }
        publicDataService.insertLocalPopularArea();
    }
}
