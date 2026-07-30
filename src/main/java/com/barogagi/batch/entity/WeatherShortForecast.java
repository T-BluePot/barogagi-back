package com.barogagi.batch.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "WEATHER_SHORT_FORECAST",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_WEATHER_SHORT_FORECAST",
                        columnNames = {
                                "NX",
                                "NY",
                                "FCST_DATE",
                                "FCST_TIME"
                        }
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WeatherShortForecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Schema(description = "기상청 격자 x좌표")
    @Column(name = "NX")
    private String nx;

    @Schema(description = "기상청 격자 y좌표")
    @Column(name = "NY")
    private String ny;

    @Schema(description = "예보 발표일자")
    @Column(name = "BASE_DATE")
    private String baseDate;

    @Schema(description = "예보 발표시간")
    @Column(name = "BASE_TIME")
    private String baseTime;

    @Schema(description = "예보 대상 날짜")
    @Column(name = "FCST_DATE")
    private String fcstDate;

    @Schema(description = "예보 대상 시간")
    @Column(name = "FCST_TIME")
    private String fcstTime;

    @Schema(description = "기온")
    @Column(name = "TMP")
    private String tmp;

    @Schema(description = "하늘상태")
    @Column(name = "SKY")
    private String sky;

    @Schema(description = "강수형태")
    @Column(name = "PTY")
    private String pty;

    @Schema(description = "강수확률")
    @Column(name = "POP")
    private String pop;

    @Schema(description = "강수량")
    @Column(name = "PCP")
    private String pcp;

    @Schema(description = "습도")
    @Column(name = "REH")
    private String reh;

    @Schema(description = "풍속")
    @Column(name = "WSD")
    private String wsd;

    @Schema(description = "풍향")
    @Column(name = "VEC")
    private String vec;
}
