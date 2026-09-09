package com.barogagi.calendar.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HolidaysResponseDTO {

    private Response response;

    @Getter
    @Setter
    public static class Response {

        private HolidaysResponseHeader header;
        private HolidaysResponseBody body;
    }
}
