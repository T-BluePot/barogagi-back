package com.barogagi.tavily.client;


import com.barogagi.tavily.dto.TavilyResultDTO;
import com.barogagi.tavily.dto.TavilySearchReqDTO;
import com.barogagi.tavily.dto.TavilySearchResDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class TavilyClient {

    private final RestTemplate restTemplate;
    private final TavilyProperties props;

    public TavilyClient(TavilyProperties props) {
        this.restTemplate = new RestTemplate();
        this.props = props;
    }

    /**
     * Tavily Search API 호출
     *
     * @param query      검색어 (예: "체험 여주시")
     * @param maxResults 최대 결과 수 (null이면 properties 기본값)
     * @return 검색 결과 리스트
     */
    public List<TavilyResultDTO> search(String query, Integer maxResults) {
        TavilySearchReqDTO req = TavilySearchReqDTO.builder()
                .apiKey(props.getApiKey())
                .query(query)
                .searchDepth(props.getSearchDepth())
                .maxResults(maxResults != null ? maxResults : props.getMaxResults())
                .includeAnswer(props.isIncludeAnswer())
                .includeRawContent(false)
                .build();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<TavilySearchReqDTO> entity = new HttpEntity<>(req, headers);

            ResponseEntity<TavilySearchResDTO> response = restTemplate.exchange(
                    props.getUrl(),
                    HttpMethod.POST,
                    entity,
                    TavilySearchResDTO.class
            );

            if (response.getBody() != null && response.getBody().getResults() != null) {
                log.info("tavily search success: query={}, resultSize={}",
                        query, response.getBody().getResults().size());
                return response.getBody().getResults();
            }

        } catch (Exception e) {
            log.error("tavily search failed: query={}, error={}", query, e.getMessage());
        }

        return Collections.emptyList();
    }
}