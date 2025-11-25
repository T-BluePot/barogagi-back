package com.barogagi.plan.query.service;

import com.barogagi.kakaoplace.client.KakaoPlaceClient;
import com.barogagi.kakaoplace.dto.KakaoPlaceResDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlaceQueryService {
    private final KakaoPlaceClient kakaoPlaceClient;

    @Autowired
    public PlaceQueryService(KakaoPlaceClient kakaoPlaceClient) {
        this.kakaoPlaceClient = kakaoPlaceClient;
    }

    public List<KakaoPlaceResDTO> searchPlace(String searchKeyword) {

        List<KakaoPlaceResDTO> searchKakaoPlaceList =
                kakaoPlaceClient.searchKakaoPlaceByKeyword(searchKeyword);
        return searchKakaoPlaceList;
    }
}
