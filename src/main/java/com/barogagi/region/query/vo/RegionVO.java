package com.barogagi.region.query.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RegionVO {
    public int regionNum;      // 지역 번호 (PK)
    public String regionNm;    // 지역명
}
