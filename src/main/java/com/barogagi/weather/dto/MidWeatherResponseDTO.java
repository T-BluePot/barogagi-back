package com.barogagi.weather.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MidWeatherResponseDTO {

    /**
     * 지역 정보
     */
    private Area area;

    /**
     * 예보 발표 정보
     */
    private Forecast forecast;

    /**
     * 날짜별 중기예보
     */
    private List<Weather> weather;

    @Getter
    @Builder
    public static class Area {

        /**
         * 지역코드
         */
        private String areaCd;

        /**
         * 시군구코드
         */
        private String sigunguCd;

        /**
         * 지역명
         */
        private String areaName;

        /**
         * 시군구명
         */
        private String sigunguName;
    }

    @Getter
    @Builder
    public static class Forecast {

        /**
         * 예보 발표 시각
         * yyyyMMddHHmm
         */
        private String tmFc;
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
         * 예: SUNNY, PARTLY_CLOUDY, CLOUDY, RAIN...
         */
        private String weatherCode;

        /**
         * 최저기온
         * ℃
         */
        private Integer minTemperature;

        /**
         * 최고기온
         * ℃
         */
        private Integer maxTemperature;

        /**
         * 강수확률
         * %
         */
        private Integer precipitationProbability;
    }
}