package com.barogagi.plan.query.service;

import com.barogagi.kakaoplace.client.KakaoPlaceClient;
import com.barogagi.kakaoplace.dto.KakaoPlaceResDTO;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.Validator;
import com.barogagi.util.exception.BasicException;
import com.barogagi.util.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlaceQueryService {
    private final KakaoPlaceClient kakaoPlaceClient;
    private final Validator validator;

    @Autowired
    public PlaceQueryService(KakaoPlaceClient kakaoPlaceClient, Validator validator) {
        this.kakaoPlaceClient = kakaoPlaceClient;
        this.validator = validator;
    }


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
}
