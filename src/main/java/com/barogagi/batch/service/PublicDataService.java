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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public void insertLocalPopularArea() {

        long start = System.currentTimeMillis();

        // 지역코드 조회
        List<KorTourOrgLocalCode> codeList = korTourOrgLocalCodeRepository.findLocalCode("areaBasedList1");

        if (codeList.isEmpty()) {
            return;
        }

        // 지역코드 Map
        Map<String, KorTourOrgLocalCode> localCodeMap = codeList.stream().collect(Collectors.toMap(code -> code.getAreaCd() + "_" + code.getSigunguCd(), Function.identity()));

        // 첫 번째 지역으로 저장할 기준년월 확인
        TourApiResponse firstResponse = findLatestResponse(codeList.get(0).getAreaCd(), codeList.get(0).getSigunguCd());

        log.info("첫 번째 Tour API: {}ms", System.currentTimeMillis() - start);

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

        // 첫 번째 지역 + 나머지 지역 응답을 담을 리스트
        List<TourApiResponse> responses = new ArrayList<>();
        responses.add(firstResponse);

        // 나머지 지역은 순차적으로 Tour API 호출
        long tourStart = System.currentTimeMillis();
        for (int i = 1; i < codeList.size(); i++) {
            KorTourOrgLocalCode localCode = codeList.get(i);

            TourApiResponse response = findLatestResponse(localCode.getAreaCd(), localCode.getSigunguCd());

            if (response != null) {
                responses.add(response);
            }
        }

        log.info("전체 Tour API: {}ms", System.currentTimeMillis() - tourStart);

        // Kakao API 조회 + DB 저장
        long kakaoStart = System.currentTimeMillis();
        replaceLocalPopularData(responses, localCodeMap);

        log.info("Kakao + DB 저장: {}ms", System.currentTimeMillis() - kakaoStart);
        log.info("전체 배치: {}ms", System.currentTimeMillis() - start);

        long end = System.currentTimeMillis();
        log.info("인기 지역 조회 완료 - ({} ms)", end - start);
    }

    public void replaceLocalPopularData(List<TourApiResponse> responses, Map<String, KorTourOrgLocalCode> localCodeMap) {
        // Kakao API 조회 + Entity 생성
        List<LocalPopularReplace> entities = responses.stream().filter(Objects::nonNull).flatMap(response -> createEntities(response, localCodeMap).stream()).toList();

        // DB 저장
        saveLocalPopularData(entities);
    }

    @Transactional
    public void saveLocalPopularData(List<LocalPopularReplace> entities) {
        log.info("기존 LOCAL_POPULAR_REPLACE 데이터 삭제");
        localPopularReplaceRepository.deleteAllInBatch();
        localPopularReplaceRepository.saveAll(entities);
    }

    private TourApiResponse findLatestResponse(String areaCd, String sigunguCd) {YearMonth yearMonth = YearMonth.now().minusMonths(1);

        for (int i = 0; i < 3; i++) {
            String baseYm = yearMonth.format(DateTimeFormatter.ofPattern("yyyyMM"));

            TourApiResponse response = apiClient.getCenterPlaces(baseYm, areaCd, sigunguCd);

            if (response != null && response.getResponse().getBody().getTotalCount() > 0) {
                return response;
            }

            yearMonth = yearMonth.minusMonths(1);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        return null;
    }

    private List<LocalPopularReplace> createEntities(TourApiResponse response, Map<String, KorTourOrgLocalCode> localCodeMap) {
        List<CompletableFuture<LocalPopularReplace>> futures =
                response.getResponse()
                        .getBody()
                        .getItems()
                        .getItem()
                        .stream()
                        .map(item -> CompletableFuture.supplyAsync(() -> {
                                    KorTourOrgLocalCode localCode = localCodeMap.get(item.getAreaCd() + "_" + item.getSignguCd());
                                    String regionName = localCode.getAreaNm() + " " + localCode.getSigunguNm();
                                    KakaoPlaceResDTO matched = searchKakaoWithRetry(item.getHubTatsNm(), regionName, item.getMapX(), item.getMapY());

                                    if (matched != null && matched.getPlaceUrl() != null && !matched.getPlaceUrl().isEmpty()) {
                                        item.setImageUrl(matched.getPlaceUrl());
                                    } else {
                                        item.setImageUrl("");
                                    }
                                    return new LocalPopularReplace(item);
                                }, executorService)
                        )
                        .toList();

        return futures.stream().map(CompletableFuture::join).toList();
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
        if (results != null && !results.isEmpty()) return results.get(0);

        // 2차: 장소명만 (좌표 기반)
        List<KakaoPlaceResDTO> retry = kakaoPlaceClient.searchKakaoPlace(placeName, x, y, radius, 1);
        if (retry != null && !retry.isEmpty()) return retry.get(0);

        // 3차: 장소명 + 지역명 (좌표/반경 없이)
        List<KakaoPlaceResDTO> fallback = kakaoPlaceClient.searchKakaoPlace(query1, null, null, 0, 1);
        if (fallback != null && !fallback.isEmpty()) return fallback.get(0);

        return null;
    }
}
