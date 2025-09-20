package com.barogagi.region.query.mapper;

import com.barogagi.region.query.vo.RegionDetailVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RegionMapper {

    // 계획 상세 조회 - 지역 상세 조회
    List<RegionDetailVO> selectRegionByPlanNum(int planNum);

    // 지역 번호로 지역명 조회
    RegionDetailVO selectRegionByRegionNum(int regionNum);
}
