package com.barogagi.region.query.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class RegionVO {
    private int regionNum;      // 지역 번호 (PK)
    private String regionNm;    // 지역명
}
