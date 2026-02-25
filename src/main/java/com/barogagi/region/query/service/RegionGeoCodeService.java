package com.barogagi.region.query.service;

import com.barogagi.kakaoplace.client.KakaoGeoCodeClient;
import com.barogagi.kakaoplace.dto.KakaoGeoCodeResDTO;
import com.barogagi.region.dto.RegionGeoCodeResDTO;
import com.barogagi.region.query.mapper.RegionMapper;
import com.barogagi.region.query.vo.RegionDetailVO;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.Validator;
import com.barogagi.util.exception.BasicException;
import com.barogagi.util.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
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
    private final Validator validator;

    @Autowired
    public RegionGeoCodeService(KakaoGeoCodeClient kakaoGeoCodeClient,
                                RegionMapper regionMapper,
                                Validator validator) {
        this.kakaoGeoCodeClient = kakaoGeoCodeClient;
        this.regionMapper = regionMapper;
        this.validator = validator;
    }

    public ApiResponse getGeocode(Integer regionNum, HttpServletRequest request) {
        try {

            // 1. API SECRET KEY 검증
            if (!validator.apiSecretKeyCheck(request.getHeader("API-KEY"))) {
                return ApiResponse.error(ErrorCode.NOT_EQUAL_API_SECRET_KEY.getCode(), ErrorCode.NOT_EQUAL_API_SECRET_KEY.getMessage());
            }

            // 2. 입력값 검증
            if (validator.isInvalidInteger(regionNum)) {
                return ApiResponse.error(ErrorCode.INVALID_REQUEST.getCode(),
                        ErrorCode.INVALID_REQUEST.getMessage());
            }

            // 3. 지역 조회
            RegionDetailVO region = regionMapper.selectRegionByRegionNum(regionNum);
            if (region == null) {
                return ApiResponse.error(ErrorCode.NOT_FOUND_REGION.getCode(),
                        ErrorCode.NOT_FOUND_REGION.getMessage());
            }

            // 4. address 문자열 조립 (null/빈 값 무시)
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

            // 5. 카카오 API 호출
            List<KakaoGeoCodeResDTO> result = kakaoGeoCodeClient.convertKakaoGeoCode(address);

            if (result.isEmpty()) {
                return ApiResponse.error(ErrorCode.NOT_FOUND_REGION.getCode(),
                        "좌표 변환에 실패했습니다.");
            }

            // 6. documents 배열 중 첫 번째 좌표를 DTO에 담아서 리턴
            KakaoGeoCodeResDTO first = result.get(0);

            RegionGeoCodeResDTO geoCodeResult = RegionGeoCodeResDTO.builder()
                    .x(first.getX())
                    .y(first.getY())
                    .regionNum(region.getRegionNum())
                    .regionLevel1(region.getRegionLevel1())
                    .regionLevel2(region.getRegionLevel2())
                    .regionLevel3(region.getRegionLevel3())
                    .regionLevel4(region.getRegionLevel4())
                    .build();

            return ApiResponse.success(geoCodeResult, "주소 좌표 변환 성공");

        } catch (BasicException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(),
                    ErrorCode.INTERNAL_ERROR.getMessage());
        }
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
                .regionNum(region.getRegionNum())
                .regionLevel1(region.getRegionLevel1())
                .regionLevel2(region.getRegionLevel2())
                .regionLevel3(region.getRegionLevel3())
                .regionLevel4(region.getRegionLevel4())
                .build();
    }
}
