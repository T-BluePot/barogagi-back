package com.barogagi.batch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class KmaMidLandFcstResponseDTO {

    private Response response;

    @Getter
    @NoArgsConstructor
    public static class Response {
        private Body body;
    }

    @Getter
    @NoArgsConstructor
    public static class Body {
        private Items items;
    }

    @Getter
    @NoArgsConstructor
    public static class Items {
        private List<KmaMidLandFcstItemDTO> item;
    }
}