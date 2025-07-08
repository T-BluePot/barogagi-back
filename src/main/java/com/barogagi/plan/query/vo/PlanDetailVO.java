package com.barogagi.plan.query.vo;

import com.barogagi.region.query.vo.RegionDetailVO;
import com.barogagi.tag.query.vo.TagDetailVO;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class PlanDetailVO {
    private int planNum;            // 계획 번호 (PK)
    private String planNm;          // 계획명
    private String startTime;       // 시작시간
    private String endTime;         // 종료시간

    private int itemNum;            // 아이템 번호
    private String itemNm;          // 아이템명

    private int categoryNum;        // 카테고리 번호
    private String categoryNm;      // 카테고리명

    // 지역 리스트
    private List<RegionDetailVO> regionVOList;

    // 태그 리스트
    private List<TagDetailVO> tagDetailVOList;
}
