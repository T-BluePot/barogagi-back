package com.barogagi.tavily.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tavily")
@Getter
@Setter
public class TavilyProperties {
    private String apiKey;
    private String url;
    private int maxResults = 5;
    private String searchDepth = "basic";
    private boolean includeAnswer = false;
}