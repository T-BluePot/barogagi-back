package com.barogagi.region.query.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class RegionDetailVO {
    public int regionNum;          // 지역 번호
    public String regionLevel1;    // 대분류
    public String regionLevel2;    // 시/군
    public String regionLevel3;    // 구
    public String regionLevel4;    // 동/면/리
}
