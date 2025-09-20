package com.barogagi.region.query.service;

import com.barogagi.kakaoplace.client.KakaoGeoCodeClient;
import com.barogagi.kakaoplace.dto.KakaoGeoCodeResDTO;
import com.barogagi.region.dto.RegionGeoCodeResDTO;
import com.barogagi.region.dto.RegionSearchResDTO;
import com.barogagi.region.query.mapper.RegionMapper;
import com.barogagi.region.query.vo.RegionDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RegionQueryService {

    private final RegionMapper regionMapper;

    @Autowired
    public RegionQueryService(RegionMapper regionMapper) {
        this.regionMapper = regionMapper;
    }


    /**
     * 검색어로 지역 정보를 조회하고,
     * 각 지역의 행정구역 단계별 주소를 RegionSearchResDTO 리스트로 반환한다.
     *
     * 예시:
     *   - CASE 1: 서울특별시 / null / 강남구 / 역삼동
     *       → "서울특별시 강남구"
     *       → "서울특별시 강남구 역삼동"
     *
     *   - CASE 2: 경기도 / 안양시 / 만안구 / 석수동
     *       → "경기도 안양시"
     *       → "경기도 안양시 만안구"
     *       → "경기도 안양시 만안구 석수동"
     *
     *   - CASE 3: 울산광역시 / 울주군 / null / 온산읍
     *       → "울산광역시 울주군"
     *       → "울산광역시 울주군 온산읍"
     *
     * @param regionQuery 검색 키워드
     * @return RegionSearchResDTO 리스트 (단계별 주소 포함)
     */
    public List<RegionSearchResDTO> searchList(String regionQuery) {
        List<RegionDetailVO> regionList = regionMapper.selectRegionByRegionNm(regionQuery);

        List<RegionSearchResDTO> result = new ArrayList<>();
        Set<String> seen = new HashSet<>(); // 중복 방지

        for (RegionDetailVO r : regionList) {
            List<String> parts = new ArrayList<>();

            if (r.getRegionLevel1() != null && !r.getRegionLevel1().isBlank()) {
                parts.add(r.getRegionLevel1());
            }
            if (r.getRegionLevel2() != null && !r.getRegionLevel2().isBlank()) {
                parts.add(r.getRegionLevel2());
            }
            if (r.getRegionLevel3() != null && !r.getRegionLevel3().isBlank()) {
                parts.add(r.getRegionLevel3());
            }

            // 상위 주소 (레벨1~레벨3까지) → 중복 방지 후 추가
            String upperAddress = String.join(" ", parts);
            if (!upperAddress.isBlank() && seen.add(upperAddress)) {
                result.add(RegionSearchResDTO.builder()
                        .regionNum(r.getRegionNum())
                        .regionNm(upperAddress)
                        .build());
            }

            // 레벨4 (동/면/리)
            if (r.getRegionLevel4() != null && !r.getRegionLevel4().isBlank()) {
                String fullAddress = upperAddress + " " + r.getRegionLevel4();
                if (seen.add(fullAddress)) {
                    result.add(RegionSearchResDTO.builder()
                            .regionNum(r.getRegionNum())
                            .regionNm(fullAddress)
                            .build());
                }
            }
        }

        return result;
    }


}

