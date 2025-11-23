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

    // 키워드로 지역 목록 검색 - level 1부터 가장 정확도 높은 순으로 10개 리턴
    List<RegionDetailVO> selectRegionByRegionNm(String regionQuery);

    // 지역 번호로 지역명 조회 (단순 String 반환)
//    String selectRegionNameByRegionNum(int regionNum);

    RegionDetailVO selectRegionByLevel4(String level4);

    RegionDetailVO selectRegionByLevel3(String level3);

    RegionDetailVO selectRegionByLevel2(String level2);

    RegionDetailVO selectRegionByLevel1(String level1);
}
