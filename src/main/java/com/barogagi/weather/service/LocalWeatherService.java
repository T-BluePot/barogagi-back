package com.barogagi.weather.service;

import com.barogagi.batch.entity.KorTourOrgLocalCode;
import com.barogagi.batch.entity.WeatherMidForecast;
import com.barogagi.batch.entity.WeatherShortForecast;
import com.barogagi.batch.repository.KorTourOrgLocalCodeRepository;
import com.barogagi.batch.repository.WeatherMidForecaseRepository;
import com.barogagi.batch.repository.WeatherShortForecastRepository;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.InputValidate;
import com.barogagi.util.Validator;
import com.barogagi.util.exception.ErrorCode;
import com.barogagi.weather.dto.MidWeatherResponseDTO;
import com.barogagi.weather.dto.ShortWeatherResponseDTO;
import com.barogagi.weather.exception.LocalWeatherException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalWeatherService {

    private final Validator validator;
    private final InputValidate inputValidate;

    private final WeatherShortForecastRepository weatherShortForecastRepository;
    private final WeatherMidForecaseRepository weatherMidForecaseRepository;
    private final KorTourOrgLocalCodeRepository korTourOrgLocalCodeRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);
    private static final long MAX_SEARCH_DAYS = 10;

    public ApiResponse getShortWeather(String apiSecretKey, String areaCd, String sigunguCd, String startDate, String endDate) {

        // 1. API KEY 검증
        if (!validator.apiSecretKeyCheck(apiSecretKey)) {
            throw new LocalWeatherException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
        }

        // 2. 지역 코드 기본값 처리
        if (inputValidate.isEmpty(areaCd) || inputValidate.isEmpty(sigunguCd)) {
            areaCd = "11";
            sigunguCd = "11110";
        }

        // 3. 조회 날짜 생성
        List<String> targetDates = getTargetDates(startDate, endDate);

        // 4. 지역 코드 조회
        KorTourOrgLocalCode localCode = korTourOrgLocalCodeRepository.findLocalCodeInfo(areaCd, sigunguCd);

        if (localCode == null) {
            throw new LocalWeatherException(ErrorCode.NOT_FOUND_LOCAL_CODE);
        }

        // 5. 날씨 격자 좌표 조회
        String nx = localCode.getWeatherNx();
        String ny = localCode.getWeatherNy();

        // 6. 날씨 조회
        List<WeatherShortForecast> forecasts;

        // startDate=ALL, endDate=ALL
        // → 해당 지역의 저장된 전체 단기예보 조회
        if (targetDates == null) {
            forecasts = weatherShortForecastRepository.findByNxAndNyOrderByFcstDateAscFcstTimeAsc(nx, ny);
        } else {
            // 지정한 날짜 범위만 조회
            forecasts = weatherShortForecastRepository.findByNxAndNyAndFcstDateInOrderByFcstDateAscFcstTimeAsc(nx, ny, targetDates);
        }

        // 7. 조회 결과가 없는 경우
        if (forecasts.isEmpty()) {
            throw new LocalWeatherException(ErrorCode.NOT_FOUND_WEATHER);
        }

        // 8. 응답 DTO 생성
        ShortWeatherResponseDTO response =
                ShortWeatherResponseDTO.builder()

                        // 지역 정보
                        .area(
                                ShortWeatherResponseDTO.Area.builder()
                                        .areaCd(localCode.getAreaCd())
                                        .sigunguCd(localCode.getSigunguCd())
                                        .areaName(localCode.getAreaNm())
                                        .sigunguName(localCode.getSigunguNm())
                                        .build()
                        )

                        // 예보 발표 정보
                        .forecast(
                                ShortWeatherResponseDTO.Forecast.builder()
                                        .baseDate(forecasts.get(0).getBaseDate())
                                        .baseTime(forecasts.get(0).getBaseTime())
                                        .build()
                        )

                        // 날씨 정보
                        .weather(
                                forecasts.stream()
                                        .map(this::convertToWeather)
                                        .toList()
                        )
                        .build();

        return ApiResponse.resultData(response, "LW200", "날씨 조회 성공");
    }

    public ApiResponse getMidWeather(String apiSecretKey, String areaCd, String sigunguCd, String startDate, String endDate) {

        // 1. API KEY 검증
        if (!validator.apiSecretKeyCheck(apiSecretKey)) {
            throw new LocalWeatherException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
        }

        // 2. 지역 코드 기본값 처리
        if (inputValidate.isEmpty(areaCd) || inputValidate.isEmpty(sigunguCd)) {
            areaCd = "11";
            sigunguCd = "11110";
        }

        // 3. 조회 날짜 생성
        List<String> targetDates = getTargetDates(startDate, endDate);

        // 4. 지역 코드 조회
        KorTourOrgLocalCode localCode = korTourOrgLocalCodeRepository.findLocalCodeInfo(areaCd, sigunguCd);

        if (localCode == null) {
            throw new LocalWeatherException(ErrorCode.NOT_FOUND_LOCAL_CODE);
        }

        // 5. 기상청 중기예보 지역코드 조회
        String weatherMidRegId = localCode.getWeatherMidRegId();

        // 6. 날씨 조회
        List<WeatherMidForecast> forecasts;

        // startDate=ALL, endDate=ALL
        // → 해당 지역의 저장된 전체 단기예보 조회
        if (targetDates == null) {
            forecasts = weatherMidForecaseRepository.findByRegIdOrderByFcstDateAsc(weatherMidRegId);
        } else {
            // 지정한 날짜 범위만 조회
            forecasts = weatherMidForecaseRepository.findByRegIdAndFcstDateInOrderByFcstDateAsc(weatherMidRegId, targetDates);
        }

        // 7. 조회 결과가 없는 경우
        if (forecasts.isEmpty()) {
            throw new LocalWeatherException(ErrorCode.NOT_FOUND_WEATHER);
        }

        // 8. 응답 DTO 생성
        MidWeatherResponseDTO response =
                MidWeatherResponseDTO.builder()

                        // 지역 정보
                        .area(
                                MidWeatherResponseDTO.Area.builder()
                                        .areaCd(localCode.getAreaCd())
                                        .sigunguCd(localCode.getSigunguCd())
                                        .areaName(localCode.getAreaNm())
                                        .sigunguName(localCode.getSigunguNm())
                                        .build()
                        )

                        // 예보 발표 정보
                        .forecast(
                                MidWeatherResponseDTO.Forecast.builder()
                                        .tmFc(forecasts.get(0).getTmFc())
                                        .build()
                        )

                        // 날씨 정보
                        .weather(
                                forecasts.stream()
                                        .map(this::convertToWeather)
                                        .toList()
                        )
                        .build();

        return ApiResponse.resultData(response, "LW200", "날씨 조회 성공");
    }

    /**
     * 조회 날짜 생성
     *
     * startDate = ALL
     * endDate   = ALL
     * → null 반환
     * → 저장된 전체 날짜 조회
     *
     * startDate = 20260727
     * endDate   = 20260728
     * → 20260727 ~ 20260728 조회
     *
     * startDate = 20260727
     * endDate   = 20260727
     * → 20260727 하루 조회
     */
    private List<String> getTargetDates(String startDate, String endDate) {

        boolean startDateIsAll = "ALL".equalsIgnoreCase(startDate);
        boolean endDateIsAll = "ALL".equalsIgnoreCase(endDate);

        // 1. 둘 다 ALL이면 전체 조회
        if (startDateIsAll && endDateIsAll) {
            return null;
        }

        // 2. 한쪽만 ALL이면 잘못된 요청
        if (startDateIsAll || endDateIsAll) {
            throw new LocalWeatherException(ErrorCode.FAIL_INVALID_FORMAT);
        }

        // 3. 날짜가 비어 있으면 잘못된 요청
        if (inputValidate.isEmpty(startDate) || inputValidate.isEmpty(endDate)) {
            throw new LocalWeatherException(ErrorCode.FAIL_INVALID_FORMAT);
        }

        // 4. 날짜 검증
        LocalDate start = parseDate(startDate);
        LocalDate end = parseDate(endDate);

        // 5. 종료일이 시작일보다 빠른 경우
        if (end.isBefore(start)) {
            throw new LocalWeatherException(ErrorCode.FAIL_INVALID_FORMAT);
        }

        // 6. 시작일 ~ 종료일까지 날짜 생성
        long days = ChronoUnit.DAYS.between(start, end);

        if (days > MAX_SEARCH_DAYS) {
            throw new LocalWeatherException(ErrorCode.DATE_RANGE_EXCEEDED);
        }

        List<String> targetDates = new ArrayList<>();

        LocalDate currentDate = start;

        while (!currentDate.isAfter(end)) {
            targetDates.add(currentDate.format(DATE_FORMATTER));
            currentDate = currentDate.plusDays(1);
        }

        return targetDates;
    }

    /**
     * yyyyMMdd 날짜 검증 및 LocalDate 변환
     */
    private LocalDate parseDate(String date) {
        date = date.replaceAll("[^0-9]", "");
        // 8자리 숫자인지 확인
        if (!date.matches("^\\d{8}$")) {
            throw new LocalWeatherException(ErrorCode.FAIL_INVALID_FORMAT);
        }
        try {
            return LocalDate.parse(date, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new LocalWeatherException(ErrorCode.FAIL_INVALID_FORMAT);
        }
    }

    /**
     * Entity → Response DTO 변환
     */
    private ShortWeatherResponseDTO.Weather convertToWeather(WeatherShortForecast forecast) {
        String weatherCode = convertWeatherCode(forecast.getSky(), forecast.getPty());
        String weather = convertWeatherName(weatherCode);

        return ShortWeatherResponseDTO.Weather.builder()
                .date(forecast.getFcstDate())
                .weather(weather)
                .weatherCode(weatherCode)
                .temperature(parseInteger(forecast.getTmp()))
                .precipitationProbability(parseInteger(forecast.getPop()))
                .precipitation(convertPrecipitation(forecast.getPty(), forecast.getPcp()))
                .humidity(parseInteger(forecast.getReh()))
                .windSpeed(parseDouble(forecast.getWsd()))
                .windDirection(parseInteger(forecast.getVec()))
                .build();
    }

    private MidWeatherResponseDTO.Weather convertToWeather(WeatherMidForecast forecast) {
        String weatherCode = convertWeatherCode(forecast.getWf());
        String weather = convertWeatherName(weatherCode);

        return MidWeatherResponseDTO.Weather.builder()
                .date(forecast.getFcstDate())
                .weather(weather)
                .weatherCode(weatherCode)
                .minTemperature(forecast.getTmn())
                .maxTemperature(forecast.getTmx())
                .precipitationProbability(forecast.getRnSt())
                .build();
    }

    /**
     * 기상청 SKY + PTY
     * → 프론트엔드용 날씨 코드
     */
    private String convertWeatherCode(String sky, String pty) {

        // 강수 형태 우선
        if (pty != null && !"0".equals(pty)) {
            return switch (pty) {
                case "1" -> "RAIN";
                case "2" -> "RAIN_SNOW";
                case "3" -> "SNOW";
                case "4" -> "SHOWER";
                default -> "UNKNOWN";
            };
        }

        // 강수 없음
        if(sky == null) {
            return "UNKNOWN";
        }

        return switch (sky) {
            case "1" -> "SUNNY";
            case "3" -> "PARTLY_CLOUDY";
            case "4" -> "CLOUDY";
            default -> "UNKNOWN";
        };
    }

    private String convertWeatherCode(String wf) {

        if (wf == null || wf.isBlank()) {
            return "UNKNOWN";
        }

        if (wf.contains("소나기")) {
            return "SHOWER";
        }

        if (wf.contains("비/눈")) {
            return "RAIN_SNOW";
        }

        if (wf.contains("눈")) {
            return "SNOW";
        }

        if (wf.contains("비")) {
            return "RAIN";
        }

        if (wf.equals("맑음")) {
            return "SUNNY";
        }

        if (wf.equals("구름많음")) {
            return "PARTLY_CLOUDY";
        }

        if (wf.equals("흐림")) {
            return "CLOUDY";
        }

        return "UNKNOWN";
    }

    /**
     * 프론트엔드 표시용 날씨명
     */
    private String convertWeatherName(String weatherCode) {

        return switch (weatherCode) {
            case "SUNNY" -> "맑음";
            case "PARTLY_CLOUDY" -> "구름많음";
            case "CLOUDY" -> "흐림";
            case "RAIN" -> "비";
            case "RAIN_SNOW" -> "비/눈";
            case "SNOW" -> "눈";
            case "SHOWER" -> "소나기";
            default -> "알 수 없음";
        };
    }

    /**
     * 강수량 표시용 변환
     */
    private String convertPrecipitation(String pty, String pcp) {

        // 강수 없음
        if (pty == null || "0".equals(pty)) {
            return "강수없음";
        }

        // 강수는 있으나 강수량 정보가 없는 경우
        if (pcp == null || pcp.isBlank()) {
            return "강수 있음";
        }

        return pcp;
    }

    /**
     * String → Integer
     */
    private Integer parseInteger(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDouble(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Double.valueOf(value);

        } catch (NumberFormatException e) {
            return null;
        }
    }
}