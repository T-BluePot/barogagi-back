package com.barogagi.region.query.service;

import com.barogagi.kakaoplace.client.KakaoGeoCodeClient;
import com.barogagi.kakaoplace.dto.KakaoGeoCodeResDTO;
import com.barogagi.region.dto.RegionGeoCodeResDTO;
import com.barogagi.region.query.mapper.RegionMapper;
import com.barogagi.region.query.vo.RegionDetailVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RegionGeoCodeService {
    private static final Logger logger = LoggerFactory.getLogger(RegionGeoCodeService.class);

    private final KakaoGeoCodeClient kakaoGeoCodeClient;

    private final RegionMapper regionMapper;

    @Autowired
    public RegionGeoCodeService(KakaoGeoCodeClient kakaoGeoCodeClient,
                                RegionMapper regionMapper) {
        this.kakaoGeoCodeClient = kakaoGeoCodeClient;
        this.regionMapper = regionMapper;
    }

    /**
     * 주소를 받아서 Kakao API로 좌표(x, y) 변환
     */
    public RegionGeoCodeResDTO getGeocode(Integer regionNum) {

        RegionDetailVO region = regionMapper.selectRegionByRegionNum(regionNum);

        if (region == null) { // todo. 에러 처리 필요
            return null;
        }

        // 2. address 문자열 조립 (null/빈 값 무시)
        String address = Stream.of(
                        region.getRegionLevel1(),
                        region.getRegionLevel2(),
                        region.getRegionLevel3(),
                        region.getRegionLevel4()
                )
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "));

        logger.info("address = {}", address);


        // 3. 카카오 API 호출
        List<KakaoGeoCodeResDTO> result = kakaoGeoCodeClient.convertKakaoGeoCode(address);

        if (result.isEmpty()) {
            return null;
        }

        // 4. documents 배열 중 첫 번째 좌표를 DTO에 담아서 리턴
        KakaoGeoCodeResDTO first = result.get(0);

        return RegionGeoCodeResDTO.builder()
                .x(first.getX())
                .y(first.getY())
                .build();
    }
}

