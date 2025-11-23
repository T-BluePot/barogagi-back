package com.barogagi.region.query.service;

import com.barogagi.region.dto.RegionSearchResDTO;
import com.barogagi.region.query.mapper.RegionMapper;
import com.barogagi.region.query.vo.RegionDetailVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RegionQueryService {
    private static final Logger logger = LoggerFactory.getLogger(RegionQueryService.class);

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

    public RegionDetailVO getRegionByRegionNum(int regionNum) {
        return regionMapper.selectRegionByRegionNum(regionNum);
    }

    public RegionDetailVO getRegionNumByAddress(String address) {

        // todo. !!!!!!!!!! 주소가 이상하게 들어감 !!!!!!!!
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("주소가 입력되지 않았습니다.");
        }

        String[] parts = address.split(" ");

        List<String> tokens = Arrays.stream(parts)
                .filter(t -> !t.matches("\\d+"))
                .collect(Collectors.toList());

        // LEVEL 4 (동/읍/면)
        if (tokens.size() >= 3) {
            String level4 = tokens.get(2);
            RegionDetailVO r4 = regionMapper.selectRegionByLevel4(level4);
            logger.info("#$# r4 = {}", r4);

            if (r4 != null) {
                return r4;
            }
        }
        logger.info("#$# next?");

        // LEVEL 3
        if (tokens.size() >= 3) {
            String level3 = tokens.get(2);
            RegionDetailVO r3 = regionMapper.selectRegionByLevel3(level3);
            logger.info("#$# r3 = {}", r3);

            if (r3 != null) {
                return r3;
            }
        }

        // LEVEL 2
        if (tokens.size() >= 2) {
            String level2 = tokens.get(1);
            RegionDetailVO r2 = regionMapper.selectRegionByLevel2(level2);
            if (r2 != null) {
                return r2;
            }
        }

        // LEVEL 1
        if (tokens.size() >= 1) {
            String level1 = tokens.get(0);
            RegionDetailVO r1 = regionMapper.selectRegionByLevel1(level1);
            if (r1 != null) {
                return r1;
            }
        }

        throw new IllegalStateException("입력된 주소로 지역을 찾을 수 없습니다: " + address);
    }

}

