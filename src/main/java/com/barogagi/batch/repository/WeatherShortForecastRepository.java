package com.barogagi.batch.repository;

import com.barogagi.batch.entity.WeatherShortForecast;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeatherShortForecastRepository extends JpaRepository<WeatherShortForecast, Long> {

    /**
     * 특정 위치 + 특정 날짜 범위 조회
     *
     * 예:
     * 20260727 ~ 20260728
     */
    List<WeatherShortForecast> findByNxAndNyAndFcstDateInOrderByFcstDateAscFcstTimeAsc(String nx, String ny, List<String> fcstDates);

    /**
     * 특정 위치의 저장된 전체 단기예보 조회
     *
     * startDate=ALL
     * endDate=ALL
     */
    List<WeatherShortForecast> findByNxAndNyOrderByFcstDateAscFcstTimeAsc(String nx, String ny);
}