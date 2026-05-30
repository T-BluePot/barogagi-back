package com.barogagi.plan.query.service;

import com.barogagi.kakaoplace.client.KakaoPlaceClient;
import com.barogagi.kakaoplace.dto.KakaoPlaceResDTO;
import com.barogagi.plan.dto.PopularPlaceResDTO;
import com.barogagi.plan.query.mapper.PlanMapper;
import com.barogagi.region.controller.RegionController;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.Validator;
import com.barogagi.util.exception.BasicException;
import com.barogagi.util.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlaceQueryService {
    private final KakaoPlaceClient kakaoPlaceClient;
    private final Validator validator;
    private final PlanQueryService planQueryService;
    private final PlanMapper planMapper;

    @Autowired
    public PlaceQueryService(KakaoPlaceClient kakaoPlaceClient, Validator validator, PlanQueryService planQueryService, PlanMapper planMapper) {
        this.kakaoPlaceClient = kakaoPlaceClient;
        this.validator = validator;
        this.planQueryService = planQueryService;
        this.planMapper = planMapper;
    }
    private static final Logger logger = LoggerFactory.getLogger(PlaceQueryService.class);


    public ApiResponse searchPlace(String searchKeyword, HttpServletRequest request) {
        try {
            // 1. API SECRET KEY 검증
            if (!validator.apiSecretKeyCheck(request.getHeader("API-KEY"))) {
                return ApiResponse.error(ErrorCode.NOT_EQUAL_API_SECRET_KEY.getCode(),
                        ErrorCode.NOT_EQUAL_API_SECRET_KEY.getMessage());
            }

            // 2. Kakao API로 장소 검색
            List<KakaoPlaceResDTO> searchKakaoPlaceList =
                    kakaoPlaceClient.searchKakaoPlaceByKeyword(searchKeyword);

            return ApiResponse.success(searchKakaoPlaceList, "장소 검색 성공");

        } catch (BasicException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(),
                    ErrorCode.INTERNAL_ERROR.getMessage());
        }
    }

    public ApiResponse searchPopularPlaces(Integer limit, HttpServletRequest request) {
        logger.info("#$# searchPopularPlaces");

        try {
            if (!validator.apiSecretKeyCheck(request.getHeader("API-KEY"))) {
                return ApiResponse.error(ErrorCode.NOT_EQUAL_API_SECRET_KEY.getCode(),
                        ErrorCode.NOT_EQUAL_API_SECRET_KEY.getMessage());
            }

            if (limit == null || limit <= 0) {
                return ApiResponse.error(ErrorCode.INVALID_REQUEST.getCode(),
                        ErrorCode.INVALID_REQUEST.getMessage());
            }

            List<PopularPlaceResDTO> popularPlaces = planMapper.selectPopularPlaces(limit);
            logger.info("조회된 인기 장소 수: {}", popularPlaces.size());

            // planLink로 OG 이미지 파싱
            List<PopularPlaceResDTO> result = popularPlaces.stream()
                    .map(place -> {
                        String imageUrl = fetchOgImage(place.getPlanLink());
                        return place.toBuilder()
                                .imageUrl(imageUrl)
                                .build();
                    })
                    .collect(Collectors.toList());

            return ApiResponse.success(result, "인기 장소 조회 성공");

        } catch (BasicException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("searchPopularPlaces 오류: {}", e.getMessage(), e);

            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(),
                    ErrorCode.INTERNAL_ERROR.getMessage());
        }
    }

    private String fetchOgImage(String url) {
        try {
            logger.info("OG 이미지 파싱 시작 - url: {}", url);  // 여기 로그 찍히는지 확인

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Referer", "https://place.map.kakao.com/")
                    .timeout(5000)
                    .get();

            Element ogImage = doc.selectFirst("meta[property=og:image]");
            logger.info("OG 이미지 파싱 결과 - imageUrl: {}", ogImage);  // 결과 확인

            if (ogImage != null) {
                String imageUrl = ogImage.attr("content");
                // 프로토콜 상대 URL 처리
                if (imageUrl.startsWith("//")) {
                    imageUrl = "https:" + imageUrl;
                }
                return imageUrl;
            }
        } catch (IOException e) {
            logger.warn("OG 이미지 파싱 실패 - url: {}, message: {}", url, e.getMessage());
        }
        return null;
    }
}
