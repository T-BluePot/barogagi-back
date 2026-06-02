package com.barogagi.sendMessage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class CommonService {

    private final Environment environment;

    public boolean isProd() {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }

    public boolean isDev() {
        return Arrays.asList(environment.getActiveProfiles()).contains("dev");
    }
}
