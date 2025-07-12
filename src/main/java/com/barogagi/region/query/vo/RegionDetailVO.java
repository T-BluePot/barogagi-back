package com.barogagi.region.query.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class RegionDetailVO {
    private int regionNum;          // 지역 번호
    private String regionLevel1;    // 대분류
    private String regionLevel2;    // 시/군
    private String regionLevel3;    // 구
    private String regionLevel4;    // 동/면/리
}
