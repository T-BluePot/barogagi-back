package com.barogagi.region.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RegionVO {
    private int regionNum;      // 지역 번호 (PK)
    private String regionNm;    // 지역명
}
