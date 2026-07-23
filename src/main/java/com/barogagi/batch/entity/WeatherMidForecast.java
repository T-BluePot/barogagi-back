package com.barogagi.batch.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "WEATHER_MID_FORECAST",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_WEATHER_MID_FORECAST",
                        columnNames = {
                                "REG_ID",
                                "TM_FC",
                                "FCST_DATE"
                        }
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeatherMidForecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Schema(description = "중기예보 지역코드")
    @Column(name = "REG_ID", nullable = false)
    private String regId;

    @Schema(description = "예보 발표일시")
    @Column(name = "TM_FC", nullable = false)
    private String tmFc;

    @Schema(description = "예보 대상 날짜")
    @Column(name = "FCST_DATE", nullable = false)
    private String fcstDate;

    @Schema(description = "날씨")
    @Column(name = "WF")
    private String wf;

    @Schema(description = "강수확률")
    @Column(name = "RN_ST")
    private Integer rnSt;

    @Schema(description = "최저기온")
    @Column(name = "TMN")
    private Integer tmn;

    @Schema(description = "최고기온")
    @Column(name = "TMX")
    private Integer tmx;

    @Builder
    public WeatherMidForecast(
            String regId, String tmFc, String fcstDate,
            String wf, Integer rnSt, Integer tmn, Integer tmx
    ) {
        this.regId = regId;
        this.tmFc = tmFc;
        this.fcstDate = fcstDate;
        this.wf = wf;
        this.rnSt = rnSt;
        this.tmn = tmn;
        this.tmx = tmx;
    }
}
