package com.barogagi.batch.service;

import com.barogagi.batch.dto.KmaMidLandFcstItemDTO;
import com.barogagi.batch.dto.KmaMidTaItemDTO;
import com.barogagi.batch.dto.KmaVilageFcstItemDTO;
import com.barogagi.batch.dto.WeatherGridDTO;
import com.barogagi.batch.entity.WeatherMidForecast;
import com.barogagi.batch.entity.WeatherShortForecast;
import com.barogagi.batch.repository.KorTourOrgLocalCodeRepository;
import com.barogagi.batch.repository.WeatherMidForecaseRepository;
import com.barogagi.batch.repository.WeatherShortForecastRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final KorTourOrgLocalCodeRepository korTourOrgLocalCodeRepository;
    private final WeatherShortForecastRepository weatherShortForecastRepository;
    private final WeatherMidForecaseRepository weatherMidForecaseRepository;
    private final PublicDataService publicDataService;

    private static final DateTimeFormatter SHORT_WEATHER_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter SHORT_WEATHER_TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm");
    private static final List<Integer> SHORT_WEATHER_BASE_HOURS = List.of(2, 5, 8, 11, 14, 17, 20, 23);

    private final Executor weatherExecutor;

    /**단기예보 배치*/
    public void shortWeatherBatch() {

        long start = System.currentTimeMillis();

        // 현재 시각
        LocalDateTime now = LocalDateTime.now();

        // 가장 최근 단기예보 발표 시각
        LocalDateTime baseDateTime = getLatestBaseDateTime(now);
        String baseDate = baseDateTime.format(SHORT_WEATHER_DATE_FORMATTER);
        String baseTime = baseDateTime.format(SHORT_WEATHER_TIME_FORMATTER);

        // 현재 시각 기준 가장 최근 예보 시간
        String targetFcstTime = getLatestForecastTime(now);

        // 오늘 / 내일 / 모레 (List -> Set으로 변경한 이유 : contains 실행 시 List는 하나씩 비교, Set은 바로 발견)
        Set<String> targetFcstDates = Set.of(
                                                now.format(SHORT_WEATHER_DATE_FORMATTER),
                                                now.plusDays(1).format(SHORT_WEATHER_DATE_FORMATTER),
                                                now.plusDays(2).format(SHORT_WEATHER_DATE_FORMATTER)
        );

        // 날씨 격자 조회
        List<WeatherGridDTO> weatherGridDTOS = korTourOrgLocalCodeRepository.findDistinctWeatherGrid("areaBasedList1");

        // 저장할 Entity를 메모리에 모음
        List<WeatherShortForecast> forecasts = new ArrayList<>(weatherGridDTOS.size() * 3);
        List<CompletableFuture<List<WeatherShortForecast>>> futures = new ArrayList<>(weatherGridDTOS.size());
        AtomicBoolean hasError = new AtomicBoolean(false);

        // 격자별 날씨 조회
        for (WeatherGridDTO weatherGridDTO : weatherGridDTOS) {CompletableFuture<List<WeatherShortForecast>> future = CompletableFuture.supplyAsync(
                    () -> createForecast(weatherGridDTO, baseDate, baseTime, targetFcstDates, targetFcstTime), weatherExecutor)
                            .exceptionally(ex -> {
                                hasError.set(true);
                                log.error("단기예보 조회 실패 nx={}, ny={}", weatherGridDTO.getNx(), weatherGridDTO.getNy(), ex);
                                return Collections.emptyList();
                            });
            futures.add(future);
        }

        for (CompletableFuture<List<WeatherShortForecast>> future : futures) {
            forecasts.addAll(future.join());
        }

        if (hasError.get()) {
            log.error("단기예보 배치 실패 - 기존 데이터를 유지합니다.");
            return;
        }

        // 마지막에 한 번에 저장
        saveShortForecasts(forecasts);

        long end = System.currentTimeMillis();

        log.info("단기예보 배치 완료 - {}건 저장 ({} ms)", forecasts.size(), end - start);
    }

    private List<WeatherShortForecast> createForecast(WeatherGridDTO weatherGridDTO, String baseDate, String baseTime, Set<String> targetFcstDates, String targetFcstTime) {

        String nx = weatherGridDTO.getNx();
        String ny = weatherGridDTO.getNy();

        List<KmaVilageFcstItemDTO> weatherList = publicDataService.getVilageFcst(baseDate, baseTime, nx, ny);

        List<WeatherShortForecast> forecasts = new ArrayList<>(3);

        // 오늘/내일/모레 + 특정 예보 시간 필터링
        List<KmaVilageFcstItemDTO> targetWeatherList = weatherList.stream()
                .filter(item -> targetFcstDates.contains(item.getFcstDate()) && targetFcstTime.equals(item.getFcstTime()))
                .toList();

        Map<String, List<KmaVilageFcstItemDTO>> weatherByDate = targetWeatherList.stream().collect(Collectors.groupingBy(KmaVilageFcstItemDTO::getFcstDate));

        // 날짜별 Entity 생성
        for (String fcstDate : targetFcstDates) {

            List<KmaVilageFcstItemDTO> dateWeatherList = weatherByDate.get(fcstDate);

            if (dateWeatherList == null || dateWeatherList.isEmpty()) {
                continue;
            }

            Map<String, String> categoryMap = dateWeatherList.stream().collect(Collectors.toMap(KmaVilageFcstItemDTO::getCategory, KmaVilageFcstItemDTO::getFcstValue));

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

            forecasts.add(forecast);
        }

        return forecasts;
    }

    /**
     * 중기예보 배치
     *
     * 중기예보 API
     * - 06시 발표
     * - 18시 발표
     *
     * 중기기온:
     * 4일 ~ 10일
     *
     * 중기육상:
     * 4일 ~ 10일
     */
    public void midWeatherBatch() {

        long start = System.currentTimeMillis();

        // 가장 최근 발표 시각
        LocalDateTime baseDateTime = getLatestMidBaseDateTime();

        String tmFc = baseDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

        // 중복 제거된 regId 조회
        List<String> regIds = korTourOrgLocalCodeRepository.findDistinctWeatherMidRegId("areaBasedList1");

        // 저장할 Entity를 메모리에 모음
        List<WeatherMidForecast> forecasts = new ArrayList<>(regIds.size() * 7);
        List<CompletableFuture<List<WeatherMidForecast>>> futures = new ArrayList<>(regIds.size());

        // 지역별 처리
        for (String regId : regIds) {
            CompletableFuture<List<WeatherMidForecast>> future = CompletableFuture.supplyAsync(() -> {
                        KmaMidTaItemDTO ta = publicDataService.getMidTa(regId, tmFc);
                        KmaMidLandFcstItemDTO land = publicDataService.getMidLandFcst(regId, tmFc);
                        return createForecasts(regId, tmFc, ta, land);

                    }, weatherExecutor).exceptionally(ex -> {
                        log.error("중기예보 조회 실패 regId={}", regId, ex);
                        return Collections.emptyList();
                    });

            futures.add(future);
        }

        for (CompletableFuture<List<WeatherMidForecast>> future : futures) {
            forecasts.addAll(future.join());
        }

        // 마지막에 한 번에 저장
        saveMidForecasts(forecasts);

        long end = System.currentTimeMillis();

        log.info("중기예보 배치 완료 - {}건 저장 ({} ms)", forecasts.size(), end - start);
    }

    @Transactional
    public void saveShortForecasts(List<WeatherShortForecast> forecasts) {

        // 기존 단기예보 전체 삭제
        weatherShortForecastRepository.deleteAllInBatch();

        weatherShortForecastRepository.saveAll(forecasts);
    }

    @Transactional
    private void saveMidForecasts(List<WeatherMidForecast> forecasts) {

        weatherMidForecaseRepository.deleteAllInBatch();

        weatherMidForecaseRepository.saveAll(forecasts);
    }

    private List<WeatherMidForecast> createForecasts(String regId, String tmFc, KmaMidTaItemDTO ta, KmaMidLandFcstItemDTO land) {

        List<WeatherMidForecast> forecasts = new ArrayList<>(7);
        LocalDate baseDate = LocalDate.parse(tmFc.substring(0, 8), DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 4일
        addForecast(forecasts, regId, tmFc, baseDate.plusDays(4),
                land.getWf4Am(), land.getWf4Pm(),
                land.getRnSt4Am(), land.getRnSt4Pm(),
                ta.getTaMin4(), ta.getTaMax4()
        );

        // 5일
        addForecast(forecasts, regId, tmFc, baseDate.plusDays(5),
                land.getWf5Am(), land.getWf5Pm(),
                land.getRnSt5Am(), land.getRnSt5Pm(),
                ta.getTaMin5(), ta.getTaMax5()
        );

        // 6일
        addForecast(forecasts, regId, tmFc, baseDate.plusDays(6),
                land.getWf6Am(), land.getWf6Pm(),
                land.getRnSt6Am(), land.getRnSt6Pm(),
                ta.getTaMin6(), ta.getTaMax6()
        );

        // 7일
        addForecast(forecasts, regId, tmFc, baseDate.plusDays(7),
                land.getWf7Am(), land.getWf7Pm(),
                land.getRnSt7Am(), land.getRnSt7Pm(),
                ta.getTaMin7(), ta.getTaMax7()
        );

        // 8일
        addForecast(forecasts, regId, tmFc, baseDate.plusDays(8),
                land.getWf8(), null,
                land.getRnSt8(), null,
                ta.getTaMin8(), ta.getTaMax8()
        );

        // 9일
        addForecast(forecasts, regId, tmFc, baseDate.plusDays(9),
                land.getWf9(), null,
                land.getRnSt9(), null,
                ta.getTaMin9(), ta.getTaMax9()
        );

        // 10일
        addForecast(forecasts, regId, tmFc, baseDate.plusDays(10),
                land.getWf10(), null,
                land.getRnSt10(), null,
                ta.getTaMin10(), ta.getTaMax10()
        );

        return forecasts;
    }

    private void addForecast(List<WeatherMidForecast> forecasts,
                             String regId, String tmFc, LocalDate fcstDate,
                             String wfAm, String wfPm,
                             Integer rnStAm, Integer rnStPm,
                             Integer tmn, Integer tmx) {
        String wf = combineWeather(wfAm, wfPm);
        Integer rnSt = combineRainProbability(rnStAm, rnStPm);

        WeatherMidForecast forecast = WeatherMidForecast.builder()
                        .regId(regId)
                        .tmFc(tmFc)
                        .fcstDate(fcstDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                        .wf(wf)
                        .rnSt(rnSt)
                        .tmn(tmn)
                        .tmx(tmx)
                        .build();

        forecasts.add(forecast);
    }

    /**
     * 오전 / 오후 날씨 조합
     *
     * 예:
     * 오전: 맑음
     * 오후: 흐림
     *
     * 결과:
     * 맑음 / 흐림
     */
    private String combineWeather(String wfAm, String wfPm) {

        boolean hasAm = wfAm != null && !wfAm.isBlank();
        boolean hasPm = wfPm != null && !wfPm.isBlank();

        if (hasAm && hasPm) {
            return wfAm + " / " + wfPm;
        }

        if (hasAm) {
            return wfAm;
        }

        if (hasPm) {
            return wfPm;
        }

        return null;
    }


    /**
     * 오전 / 오후 강수확률 조합
     *
     * 현재 DB에는 하나의 RN_ST만 있으므로
     * 오전/오후 중 높은 확률을 저장
     */
    private Integer combineRainProbability(
            Integer rnStAm,
            Integer rnStPm
    ) {
        if (rnStAm != null && rnStPm != null) {
            return Math.max(rnStAm, rnStPm);
        }

        if (rnStAm != null) {
            return rnStAm;
        }

        return rnStPm;
    }


    /**
     * 현재 시각 기준 가장 최근 단기예보 발표 시각
     */
    private LocalDateTime getLatestBaseDateTime(LocalDateTime now) {

        int currentHour = now.getHour();
        int latestBaseHour = SHORT_WEATHER_BASE_HOURS.stream()
                        .filter(baseHour -> baseHour <= currentHour)
                        .max(Integer::compareTo)
                        .orElse(23);

        // 00:00 ~ 01:59
        // 전날 23시 발표본 사용
        if (latestBaseHour == 23 && currentHour < 2) {
            return now.minusDays(1).withHour(23).withMinute(0).withSecond(0).withNano(0);
        }

        return now.withHour(latestBaseHour).withMinute(0).withSecond(0).withNano(0);
    }


    /**
     * 현재 시각 기준 가장 최근 단기예보 시간
     */
    private String getLatestForecastTime(LocalDateTime now) {

        int hour = now.getHour();

        int forecastHour = ((hour / 3) + 1) * 3;

        if (forecastHour == 24) {
            forecastHour = 0;
        }

        return String.format("%02d00", forecastHour);
    }


    /**
     * 현재 시각 기준 가장 최근 중기예보 발표 시각
     *
     * 06:00 ~ 17:59
     * → 당일 06:00 발표본
     *
     * 18:00 이후
     * → 당일 18:00 발표본
     *
     * 00:00 ~ 05:59
     * → 전날 18:00 발표본
     */
    private LocalDateTime getLatestMidBaseDateTime() {

        LocalDateTime now = LocalDateTime.now();

        if (now.getHour() >= 18) {
            return now.withHour(18).withMinute(0).withSecond(0).withNano(0);
        }

        if (now.getHour() >= 6) {
            return now.withHour(6).withMinute(0).withSecond(0).withNano(0);
        }

        return now.minusDays(1).withHour(18).withMinute(0).withSecond(0).withNano(0);
    }
}