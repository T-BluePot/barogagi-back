package com.barogagi.taviliy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TavilySearchReqDTO {
    @JsonProperty("api_key")
    private String apiKey;

    private String query;

    @JsonProperty("search_depth")
    private String searchDepth;

    @JsonProperty("max_results")
    private int maxResults;

    @JsonProperty("include_answer")
    private boolean includeAnswer;

    @JsonProperty("include_raw_content")
    private boolean includeRawContent;
}