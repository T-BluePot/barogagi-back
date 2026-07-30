package com.barogagi.weather.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ShortWeatherResponseDTO {

    private Area area;

    /**
     * 예보 정보
     */
    private Forecast forecast;

    private List<Weather> weather;

    @Getter
    @Builder
    public static class Area {

        private String areaCd;

        private String sigunguCd;

        private String areaName;

        private String sigunguName;
    }

    @Getter
    @Builder
    public static class Forecast {

        /**
         * 예보 발표 날짜
         * yyyyMMdd
         *
         * 예: 20260727
         */
        private String baseDate;

        /**
         * 예보 발표 시각
         * HHmm
         *
         * 예: 1100
         */
        private String baseTime;
    }

    @Getter
    @Builder
    public static class Weather {

        /**
         * 예보 대상 날짜
         * yyyyMMdd
         */
        private String date;

        /**
         * 사람이 읽을 수 있는 날씨
         * 예: 맑음, 구름많음, 흐림
         */
        private String weather;

        /**
         * 프론트엔드에서 사용할 날씨 코드
         * 예: SUNNY, PARTLY_CLOUDY, CLOUDY
         */
        private String weatherCode;

        /**
         * 기온
         * ℃
         */
        private Integer temperature;

        /**
         * 강수확률
         * %
         */
        private Integer precipitationProbability;

        /**
         * 강수량
         * 예: 강수없음, 1mm 미만, 5mm
         */
        private String precipitation;

        /**
         * 습도
         * %
         */
        private Integer humidity;

        /**
         * 풍속
         * m/s
         */
        private Double windSpeed;

        /**
         * 풍향
         * 0 ~ 360도
         */
        private Integer windDirection;
    }
}