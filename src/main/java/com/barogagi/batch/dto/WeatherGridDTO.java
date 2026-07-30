package com.barogagi.batch.dto;

import lombok.Getter;

@Getter
public class WeatherGridDTO {

    private final String nx;
    private final String ny;

    public WeatherGridDTO(String nx, String ny) {
        this.nx = nx;
        this.ny = ny;
    }
}
