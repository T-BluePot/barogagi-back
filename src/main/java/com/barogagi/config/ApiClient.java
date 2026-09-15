package com.barogagi.config;

import com.barogagi.batch.dto.TourApiResponse;
import com.barogagi.calendar.dto.HolidaysResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiClient {

    private final RestClient restClient;

    @Value("${data.api.base-url}")
    private String baseUrl;

    @Value("${data.api.service-key}")
    private String serviceKey;

    @Value("${areaBasedList1.path}")
    private String areaBasedList1Path;

    @Value("${SpcdeInfoService.path}")
    private String spcdeInfoServicePath;

    public TourApiResponse getCenterPlaces(String baseYm, String areaCd, String signguCd) {
        URI uri = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(baseUrl)
                .path(areaBasedList1Path)
                .queryParam("serviceKey", serviceKey)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 10)
                .queryParam("MobileOS", "WEB")
                .queryParam("MobileApp", "fitpl")
                .queryParam("baseYm", baseYm)
                .queryParam("areaCd", areaCd)
                .queryParam("signguCd", signguCd)
                .queryParam("_type", "json")
                .build(true)
                .toUri();

        TourApiResponse response = restClient.get().uri(uri).retrieve().body(TourApiResponse.class);
        return response;
    }

    public HolidaysResponseDTO getRestDeInfo(String year, String month) {
        URI uri = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(baseUrl)
                .path(spcdeInfoServicePath)
                .queryParam("serviceKey", serviceKey)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 100)
                .queryParam("_type", "json")
                .queryParam("solYear", year)
                .queryParam("solMonth", month)
                .build(true)
                .toUri();

        HolidaysResponseDTO response = restClient.get().uri(uri).retrieve().body(HolidaysResponseDTO.class);
        return response;
    }
}
