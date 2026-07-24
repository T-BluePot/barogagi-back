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

    private static final List<Integer> SHORT_WEATHER_BASE_HOURS = List.of(2, 5, 8, 11, 14, 17, 20, 23);

    @Transactional
    public void shortWeatherBatch() {

        // 1. 기존 단기예보 전체 삭제
        weatherShortForecastRepository.deleteAllInBatch();

        // 2. 현재 시각
        LocalDateTime now = LocalDateTime.now();

        // 3. 가장 최근 발표 시각
        LocalDateTime baseDateTime = getLatestBaseDateTime(now);
        String baseDate = baseDateTime.format(DATE_FORMATTER);
        String baseTime = baseDateTime.format(TIME_FORMATTER);

        // 4. 현재 시각 기준 가장 최근 예보 시간
        String targetFcstTime = getLatestForecastTime(now);

        // 5. 오늘부터 3일 날짜
        String today = now.format(DATE_FORMATTER);
        String tomorrow = now.plusDays(1).format(DATE_FORMATTER);
        String dayAfterTomorrow = now.plusDays(2).format(DATE_FORMATTER);
        List<String> targetFcstDates = List.of(today, tomorrow, dayAfterTomorrow);

        // 6. 날씨 격자 조회
        List<WeatherGridDTO> weatherGridDTOS = korTourOrgLocalCodeRepository.findDistinctWeatherGrid("areaBasedList1");

        // 7. 격자별 날씨 조회
        for (WeatherGridDTO weatherGridDTO : weatherGridDTOS) {

            String nx = weatherGridDTO.getNx();
            String ny = weatherGridDTO.getNy();

            // 8. 최신 발표 기준 단기예보 조회
            List<KmaVilageFcstItemDTO> weatherList = publicDataService.getVilageFcst(baseDate, baseTime, nx, ny);

            // 9. 오늘/내일/모레 + 동일한 예보 시간만 필터링
            List<KmaVilageFcstItemDTO> targetWeatherList = weatherList.stream()
                                                            .filter(item -> targetFcstDates.contains(item.getFcstDate()))
                                                            .filter(item -> item.getFcstTime().equals(targetFcstTime))
                                                            .toList();

            // 10. 날짜별 저장
            for (String fcstDate : targetFcstDates) {
                List<KmaVilageFcstItemDTO> dateWeatherList = targetWeatherList.stream().filter(item -> item.getFcstDate().equals(fcstDate)).toList();

                if (dateWeatherList.isEmpty()) {
                    log.warn("예보 데이터 없음. " + "nx={}, ny={}, fcstDate={}, fcstTime={}", nx, ny, fcstDate, targetFcstTime);
                    continue;
                }

                // 11. 카테고리별 값 추출
                Map<String, String> categoryMap = dateWeatherList.stream().collect(
                                                    Collectors.toMap(
                                                        KmaVilageFcstItemDTO::getCategory,
                                                        KmaVilageFcstItemDTO::getFcstValue
                                                ));

                // 12. Entity 생성
                WeatherShortForecast forecast = WeatherShortForecast.builder()
                                                .nx(nx).ny(ny)
                                                .baseDate(baseDate).baseTime(baseTime)
                                                .fcstDate(fcstDate).fcstTime(targetFcstTime)
                                                .tmp(categoryMap.get("TMP"))
                                                .sky(categoryMap.get("SKY"))
                                                .pty(categoryMap.get("PTY"))
                                                .pop(categoryMap.get("POP"))
                                                .pcp(categoryMap.get("PCP"))
                                                .reh(categoryMap.get("REH"))
                                                .wsd(categoryMap.get("WSD"))
                                                .vec(categoryMap.get("VEC"))
                                                .build();

                // 13. 저장
                weatherShortForecastRepository.save(forecast);
            }
        }
    }

    /**
     * 현재 시각 기준 가장 최근 발표된 기상청 단기예보 기준 시각
     */
    private LocalDateTime getLatestBaseDateTime(LocalDateTime now) {

        int currentHour = now.getHour();
        int latestBaseHour = SHORT_WEATHER_BASE_HOURS.stream().filter(baseHour -> baseHour <= currentHour)
                        .max(Integer::compareTo)
                        .orElse(23);

        // 현재 시간이 00:00 ~ 01:59라면
        // 전날 23시 발표본 사용
        if (latestBaseHour == 23 && currentHour < 2) {
            return now.minusDays(1).withHour(23).withMinute(0).withSecond(0).withNano(0);
        }

        return now.withHour(latestBaseHour).withMinute(0).withSecond(0).withNano(0);
    }

    private String getLatestForecastTime(LocalDateTime now) {
        int currentHour = now.getHour();
        int latestForecastHour = (currentHour / 3) * 3;
        return String.format("%02d00", latestForecastHour);
    }
}