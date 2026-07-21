package com.barogagi.batch.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "KOR_TOUR_ORG_LOCAL_CODE",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_kto_localcode", columnNames = {
                        "AREA_CODE", "TYPE", "SIGUNGU_CODE"
                })
        }
)
@Getter
@NoArgsConstructor
public class KorTourOrgLocalCode {

    @Schema(description = "번호")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LOCAL_CODE_NO")
    private Long localCodeNo;

    @Schema(description = "종류")
    @Column(name = "TYPE")
    private String type;

    @Schema(description = "지역코드")
    @Column(name = "AREA_CODE")
    private String areaCd;

    @Schema(description = "지역명")
    @Column(name = "AREA_NM")
    private String areaNm;

    @Schema(description = "시,군,구 코드")
    @Column(name = "SIGUNGU_CODE")
    private String sigunguCd;

    @Schema(description = "시, 군, 구 명")
    @Column(name = "SIGUNGU_NM")
    private String sigunguNm;

    @Schema(description = "x 격자")
    @Column(name = "WEATHER_NX")
    private String weatherNx;

    @Schema(description = "y 격자")
    @Column(name = "WEATHER_NY")
    private String weatherNy;

    @Schema(description = "중기예보구역 아이디")
    @Column(name = "WEATHER_MID_REG_ID")
    private String weatherMidRegId;
}
