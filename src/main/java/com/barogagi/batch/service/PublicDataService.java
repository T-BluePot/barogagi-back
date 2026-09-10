package com.barogagi.batch.service;

import com.barogagi.batch.dto.*;
import com.barogagi.batch.entity.KorTourOrgLocalCode;
import com.barogagi.batch.entity.LocalPopularReplace;
import com.barogagi.batch.repository.KorTourOrgLocalCodeRepository;
import com.barogagi.batch.repository.LocalPopularReplaceRepository;
import com.barogagi.config.ApiClient;
import com.barogagi.kakaoplace.client.KakaoPlaceClient;
import com.barogagi.kakaoplace.dto.KakaoPlaceResDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicDataService {

    private final ApiClient apiClient;
    private final RestClient restClient;
    private final KakaoPlaceClient kakaoPlaceClient;

    private final KorTourOrgLocalCodeRepository korTourOrgLocalCodeRepository;
    private final LocalPopularReplaceRepository localPopularReplaceRepository;

    @Value("${apihub.kma.api-key}")
    private String kmaApiKey;

    @Value("${kakao.radius}")
    private int radius;

    @Transactional
    public void insertLocalPopularArea() {

        // 지역코드 조회
        List<KorTourOrgLocalCode> codeList = korTourOrgLocalCodeRepository.findLocalCode("areaBasedList1");

        if (codeList.isEmpty()) {
            return;
        }

        // 첫 번째 지역으로 저장할 기준년월 확인
        TourApiResponse firstResponse = findLatestResponse(codeList.get(0).getAreaCd(), codeList.get(0).getSigunguCd());

        if (firstResponse == null) {
            log.info("저장할 데이터가 없습니다.");
            return;
        }

        String baseYm = firstResponse.getResponse().getBody().getItems().getItem().get(0).getBaseYm();

        // 이미 저장된 월이면 배치 종료
        if (localPopularReplaceRepository.existsByBaseYm(baseYm)) {
            log.info("{} 데이터는 이미 저장되어 있습니다.", baseYm);
            return;
        }

        // 새로운 월 데이터이므로 기존 데이터 삭제
        log.info("기존 LOCAL_POPULAR_REPLACE 데이터 삭제");
        localPopularReplaceRepository.deleteAllInBatch();

        // 첫 번째 지역 저장
        saveItems(firstResponse);

        // 나머지 지역 저장
        for (int i = 1; i < codeList.size(); i++) {
            KorTourOrgLocalCode localCode = codeList.get(i);
            TourApiResponse response = findLatestResponse(localCode.getAreaCd(), localCode.getSigunguCd());
            if (response == null) {
                continue;
            }

            saveItems(response);
        }
    }

    private TourApiResponse findLatestResponse(String areaCd, String sigunguCd) {

        YearMonth yearMonth = YearMonth.now().minusMonths(1);
        for (int i = 0; i < 3; i++) {
            String baseYm = yearMonth.format(DateTimeFormatter.ofPattern("yyyyMM"));
            TourApiResponse response = apiClient.getCenterPlaces(baseYm, areaCd, sigunguCd);
            if (response != null && response.getResponse().getBody().getTotalCount() > 0) {
                return response;
            }
            yearMonth = yearMonth.minusMonths(1);
        }
        return null;
    }

    private void saveItems(TourApiResponse response) {

        List<LocalPopularReplace> entities =
                response.getResponse()
                        .getBody()
                        .getItems()
                        .getItem()
                        .stream()
                        .peek(item -> {
                            KorTourOrgLocalCode korTourOrgLocalCode = korTourOrgLocalCodeRepository.findLocalCodeInfo(item.getAreaCd(), item.getSignguCd());
                            String regionName = korTourOrgLocalCode.getAreaNm() + " " + korTourOrgLocalCode.getSigunguNm();

                            KakaoPlaceResDTO matched = searchKakaoWithRetry(item.getHubTatsNm(), regionName, item.getMapX(), item.getMapY());
                            if (matched != null && matched.getPlaceUrl() != null && !matched.getPlaceUrl().isEmpty()) {
                                item.setImageUrl(Objects.requireNonNull(matched).getPlaceUrl());
                            } else {
                                item.setImageUrl("");
                            }
                        })
                        .map(LocalPopularReplace::new)
                        .toList();

        localPopularReplaceRepository.saveAll(entities);
    }

    public List<KmaVilageFcstItemDTO> getVilageFcst(String baseDate, String baseTime, String nx, String ny) {

        String url = UriComponentsBuilder
                .fromHttpUrl("https://apihub.kma.go.kr/api/typ02/openApi/" + "VilageFcstInfoService_2.0/getVilageFcst")
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 1000)
                .queryParam("dataType", "JSON")
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTime)
                .queryParam("nx", nx)
                .queryParam("ny", ny)
                .queryParam("authKey", kmaApiKey)
                .build(false)
                .toUriString();

        KmaVilageFcstResponseDTO response = restClient.get().uri(url).retrieve().body(KmaVilageFcstResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException("기상청 응답이 없습니다.");
        }

        return Objects.requireNonNull(response).getResponse().getBody().getItems().getItem();
    }

    public KmaMidTaItemDTO getMidTa(String regId, String tmFc) {
        String url = String.valueOf(UriComponentsBuilder.fromHttpUrl("https://apihub.kma.go.kr/api/typ02/openApi/MidFcstInfoService/getMidTa")
                        .queryParam("pageNo", 1)
                        .queryParam("numOfRows", 10)
                        .queryParam("dataType", "JSON")
                        .queryParam("regId", regId)
                        .queryParam("tmFc", tmFc)
                        .queryParam("authKey", kmaApiKey)
                        .build(false)
        );

        KmaMidTaResponseDTO response = restClient.get().uri(url).retrieve().body(KmaMidTaResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException("기상청 중기기온 응답이 없습니다.");
        }

        return response.getResponse().getBody().getItems().getItem().get(0);
    }

    public KmaMidLandFcstItemDTO getMidLandFcst(String regId, String tmFc) {

        String url = String.valueOf(
                UriComponentsBuilder
                        .fromHttpUrl(
                                "https://apihub.kma.go.kr/api/typ02/openApi/MidFcstInfoService/getMidLandFcst"
                        )
                        .queryParam("pageNo", 1)
                        .queryParam("numOfRows", 10)
                        .queryParam("dataType", "JSON")
                        .queryParam("regId", regId)
                        .queryParam("tmFc", tmFc)
                        .queryParam("authKey", kmaApiKey)
                        .build(false)
        );

        KmaMidLandFcstResponseDTO response = restClient.get().uri(url).retrieve().body(KmaMidLandFcstResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException("기상청 중기육상예보 응답이 없습니다.");
        }

        return response.getResponse().getBody().getItems().getItem().get(0);
    }

    /**
     * 카카오 장소 검색 (재시도 포함)
     * 1차: "장소명 + 지역명" 으로 검색
     * 2차: "장소명"만으로 재검색
     * 3차: "장소명 + 지역명"으로 검색하되 좌표/반경 없이 (즉, 전국 검색)
     * 둘 다 실패 시 null 반환
     */
    private KakaoPlaceResDTO searchKakaoWithRetry(String placeName, String regionName, String x, String y) {
        // 1차: 장소명 + 지역명 (좌표 기반)
        String query1 = placeName + " " + regionName;
        List<KakaoPlaceResDTO> results = kakaoPlaceClient.searchKakaoPlace(query1, x, y, radius, 1);
        log.info("kakao 1차: query={}, resultSize={}", query1, results != null ? results.size() : "null");
        if (results != null && !results.isEmpty()) return results.get(0);

        // 2차: 장소명만 (좌표 기반)
        List<KakaoPlaceResDTO> retry = kakaoPlaceClient.searchKakaoPlace(placeName, x, y, radius, 1);
        log.info("kakao 2차: query={}, resultSize={}", placeName, retry != null ? retry.size() : "null");
        if (retry != null && !retry.isEmpty()) return retry.get(0);

        // 3차: 장소명 + 지역명 (좌표/반경 없이)
        List<KakaoPlaceResDTO> fallback = kakaoPlaceClient.searchKakaoPlace(query1, null, null, 0, 1);
        log.info("kakao 3차(좌표 없음): query={}, resultSize={}", query1, fallback != null ? fallback.size() : "null");
        if (fallback != null && !fallback.isEmpty()) return fallback.get(0);

        return null;
    }
}
