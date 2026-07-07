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
                        "areaCd", "type", "sigunguCd"
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
}
