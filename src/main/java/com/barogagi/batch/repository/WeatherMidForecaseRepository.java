package com.barogagi.batch.repository;

import com.barogagi.batch.entity.WeatherMidForecast;
import com.barogagi.batch.entity.WeatherShortForecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeatherMidForecaseRepository extends JpaRepository<WeatherMidForecast, Long> {

    /**
     * 특정 위치 + 특정 날짜 범위 조회
     *
     * 예:
     * 20260727 ~ 20260728
     */
    List<WeatherMidForecast> findByRegIdAndFcstDateInOrderByFcstDateAsc(String regId, List<String> fcstDates);

    /**
     * 특정 위치의 저장된 전체 중기예보 조회
     *
     * startDate=ALL
     * endDate=ALL
     */
    List<WeatherMidForecast> findByRegIdOrderByFcstDateAsc(String regId);
}
