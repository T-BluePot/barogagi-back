package com.barogagi.batch.entity;

import com.barogagi.batch.dto.TourApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "LOCAL_POPULAR_REPLACE"
)
@Getter
@Setter
@NoArgsConstructor
public class LocalPopularReplace {

    public LocalPopularReplace(TourApiResponse.HubItem item) {
        this.baseYm = item.getBaseYm();
        this.mapX = item.getMapX();
        this.mapY = item.getMapY();
        this.areaCd = item.getAreaCd();
        this.areaNm = item.getAreaNm();
        this.signguCd = item.getSignguCd();
        this.signguNm = item.getSignguNm();
        this.hubTatsCd = item.getHubTatsCd();
        this.hubTatsNm = item.getHubTatsNm();
        this.hubCtgryLclsNm = item.getHubCtgryLclsNm();
        this.hubCtgryMclsNm = item.getHubCtgryMclsNm();
        this.hubRank = item.getHubRank();
    }

    @Schema(description = "번호")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POPULAR_REPLACE_NO")
    private Long popularReplaceNo;

    @Schema(description = "기준년월")
    @Column(name = "BASE_YM")
    private String baseYm;

    @Schema(description = "x 좌표값")
    @Column(name = "MAP_X")
    private String mapX;

    @Schema(description = "Y 좌표값")
    @Column(name = "MAP_Y")
    private String mapY;

    @Schema(description = "지역코드")
    @Column(name = "AREA_CODE")
    private String areaCd;

    @Schema(description = "지역명")
    @Column(name = "AREA_NM")
    private String areaNm;

    @Schema(description = "시군구코드")
    @Column(name = "SI_GN_GU_CODE")
    private String signguCd;

    @Schema(description = "시군구명")
    @Column(name = "SI_GN_GU_NM")
    private String signguNm;

    @Schema(description = "중심지 관광지코드")
    @Column(name = "HUB_TATS_CODE")
    private String hubTatsCd;

    @Schema(description = "중심지 관광지명")
    @Column(name = "HUB_TATS_NM")
    private String hubTatsNm;

    @Schema(description = "중심지 카테고리 대분류명")
    @Column(name = "HUB_CTGRY_LCLS_NM")
    private String hubCtgryLclsNm;

    @Schema(description = "중심지 카테고리 중분류명")
    @Column(name = "HUB_CTGRY_MCLS_NM")
    private String hubCtgryMclsNm;

    @Schema(description = "중심지 순위")
    @Column(name = "HUB_RANK")
    private String hubRank;
}
