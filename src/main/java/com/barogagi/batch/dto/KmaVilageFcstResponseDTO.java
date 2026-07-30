package com.barogagi.batch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class KmaVilageFcstResponseDTO {

    private Response response;

    @Getter
    @Setter
    public static class Response {

        private Body body;
    }

    @Getter
    @Setter
    public static class Body {

        private Items items;
    }

    @Getter
    @Setter
    public static class Items {

        private List<KmaVilageFcstItemDTO> item;
    }
}
