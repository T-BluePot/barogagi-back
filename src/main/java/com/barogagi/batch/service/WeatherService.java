package com.barogagi.batch.service;

import com.barogagi.batch.dto.KmaVilageFcstItemDTO;
import com.barogagi.batch.dto.WeatherGridDTO;
import com.barogagi.batch.entity.WeatherShortForecast;
import com.barogagi.batch.repository.KorTourOrgLocalCodeRepository;
import com.barogagi.batch.repository.WeatherShortForecastRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final KorTourOrgLocalCodeRepository korTourOrgLocalCodeRepository;
    private final WeatherShortForecastRepository weatherShortForecastRepository;
    private final PublicDataService publicDataService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm");
    private static final DateTimeFormatter FORECAST_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH00");
    private static final List<Integer> SHORT_WEATHER_BASE_HOURS = List.of(2, 5, 8, 11, 14, 17, 20, 23);

    @Transactional
    public void shortWeatherBatch() {

        // 1. 기존 예보 전체 삭제
        weatherShortForecastRepository.deleteAllInBatch();

        // 2. 현재 시각
        LocalDateTime now = LocalDateTime.now();

        // 3. 현재 시각 기준 가장 최근 발표 시각
        LocalDateTime baseDateTime = getLatestBaseDateTime(now);

        String baseDate = baseDateTime.format(DATE_FORMATTER);
        String baseTime = baseDateTime.format(TIME_FORMATTER);

        // 4. 현재 시간대의 예보 시각
        String targetFcstDate = now.format(DATE_FORMATTER);
        String targetFcstTime = now.format(FORECAST_TIME_FORMATTER);

        // 5. 날씨 격자 조회
        List<WeatherGridDTO> weatherGridDTOS = korTourOrgLocalCodeRepository.findDistinctWeatherGrid("areaBasedList1");

        for (WeatherGridDTO weatherGridDTO : weatherGridDTOS) {

            String nx = weatherGridDTO.getNx();
            String ny = weatherGridDTO.getNy();

            // 6. 최신 발표 기준으로 단기예보 조회
            List<KmaVilageFcstItemDTO> weatherList = publicDataService.getVilageFcst(baseDate, baseTime, nx, ny);

            // 7. 현재 시간대의 예보만 필터링
            List<KmaVilageFcstItemDTO> targetWeatherList = weatherList.stream()
                            .filter(item ->
                                    item.getFcstDate().equals(targetFcstDate)
                                            && item.getFcstTime().equals(targetFcstTime)
                            )
                            .toList();

            if (targetWeatherList.isEmpty()) {
                log.warn("현재 시간대 예보 없음. nx={}, ny={}, baseDate={}, baseTime={}, fcstDate={}, fcstTime={}",
                        nx, ny,
                        baseDate, baseTime,
                        targetFcstDate, targetFcstTime
                );
                continue;
            }

            // 8. 카테고리별 날씨 데이터 추출
            Map<String, String> categoryMap = targetWeatherList.stream()
                            .collect(Collectors.toMap(
                                    KmaVilageFcstItemDTO::getCategory,
                                    KmaVilageFcstItemDTO::getFcstValue
                            ));

            // 9. 현재 시간대 예보 1개 저장
            WeatherShortForecast forecast = WeatherShortForecast.builder()
                            .nx(nx)
                            .ny(ny)
                            .baseDate(baseDate)
                            .baseTime(baseTime)
                            .fcstDate(targetFcstDate)
                            .fcstTime(targetFcstTime)
                            .tmp(categoryMap.get("TMP"))
                            .sky(categoryMap.get("SKY"))
                            .pty(categoryMap.get("PTY"))
                            .pop(categoryMap.get("POP"))
                            .pcp(categoryMap.get("PCP"))
                            .reh(categoryMap.get("REH"))
                            .wsd(categoryMap.get("WSD"))
                            .vec(categoryMap.get("VEC"))
                            .build();

            weatherShortForecastRepository.save(forecast);
        }
    }

    /**
     * 현재 시각 기준 가장 최근 발표된 기상청 단기예보 기준 시각
     *
     * 예:
     * 현재 15:00 ~ 17:59 → 14:00 발표본
     * 현재 18:00 ~ 20:59 → 17:00 발표본
     */
    private LocalDateTime getLatestBaseDateTime(LocalDateTime now) {

        int currentHour = now.getHour();

        int latestBaseHour = SHORT_WEATHER_BASE_HOURS.stream()
                .filter(baseHour -> baseHour <= currentHour)
                .max(Integer::compareTo)
                .orElse(23);

        if (latestBaseHour == 23 && currentHour < 2) {
            return now.minusDays(1)
                    .withHour(23)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);
        }

        return now.withHour(latestBaseHour)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }
}