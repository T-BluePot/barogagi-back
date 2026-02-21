package com.barogagi.tavily.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TavilyResultDTO {
    private String title;
    private String url;
    private String content;
    private double score;
}