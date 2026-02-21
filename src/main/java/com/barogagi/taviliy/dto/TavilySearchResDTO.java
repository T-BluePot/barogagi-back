package com.barogagi.taviliy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TavilySearchResDTO {
    private String query;
    private String answer;
    private List<TavilyResultDTO> results;

    @JsonProperty("response_time")
    private String responseTime;
}