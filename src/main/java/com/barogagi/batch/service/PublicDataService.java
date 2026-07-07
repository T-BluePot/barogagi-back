package com.barogagi.batch.service;

import com.barogagi.batch.dto.TourApiResponse;
import com.barogagi.batch.entity.KorTourOrgLocalCode;
import com.barogagi.batch.entity.LocalPopularReplace;
import com.barogagi.batch.repository.KorTourOrgLocalCodeRepository;
import com.barogagi.batch.repository.LocalPopularReplaceRepository;
import com.barogagi.config.TourApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicDataService {

    private final TourApiClient tourApiClient;
    private final KorTourOrgLocalCodeRepository korTourOrgLocalCodeRepository;
    private final LocalPopularReplaceRepository localPopularReplaceRepository;

    @Transactional
    public void insertLocalPopularArea() {

        // 지역코드 조회
        List<KorTourOrgLocalCode> codeList =
                korTourOrgLocalCodeRepository.findLocalCode("areaBasedList1");

        if (codeList.isEmpty()) {
            return;
        }

        // 첫 번째 지역으로 저장할 기준년월 확인
        TourApiResponse firstResponse = findLatestResponse(
                codeList.get(0).getAreaCd(),
                codeList.get(0).getSigunguCd());

        if (firstResponse == null) {
            log.info("저장할 데이터가 없습니다.");
            return;
        }

        String baseYm = firstResponse.getResponse()
                .getBody()
                .getItems()
                .getItem()
                .get(0)
                .getBaseYm();

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

            TourApiResponse response = findLatestResponse(
                    localCode.getAreaCd(),
                    localCode.getSigunguCd());

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

            TourApiResponse response =
                    tourApiClient.getCenterPlaces(baseYm, areaCd, sigunguCd);

            if (response != null &&
                    response.getResponse().getBody().getTotalCount() > 0) {
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
                        .map(LocalPopularReplace::new)
                        .toList();

        localPopularReplaceRepository.saveAll(entities);
    }
}
