package com.barogagi.plan.query.vo;

import com.barogagi.region.query.vo.RegionDetailVO;
import com.barogagi.tag.query.vo.TagDetailVO;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class PlanDetailVO {
    public int planNum;            // 계획 번호 (PK)
    public String planNm;          // 계획명
    public String startTime;       // 시작시간
    public String endTime;         // 종료시간

    public int itemNum;            // 아이템 번호
    public String itemNm;          // 아이템명

    public int categoryNum;        // 카테고리 번호
    public String categoryNm;      // 카테고리명

    // 지역 리스트
    public List<RegionDetailVO> regionVOList;

    // 태그 리스트
    public List<TagDetailVO> tagDetailVOList;
}
