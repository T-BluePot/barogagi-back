package com.barogagi.batch.dto;

import com.barogagi.config.ItemsDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TourApiResponse {

    private Response response;

    @Getter
    @Setter
    public static class Response {
        private Header header;
        private Body body;
    }

    @Getter
    @Setter
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    @Getter
    @Setter
    public static class Body {

        @JsonDeserialize(using = ItemsDeserializer.class)
        private Items items;

        private int numOfRows;
        private int pageNo;
        private int totalCount;
    }

    @Getter
    @Setter
    public static class Items {
        private List<HubItem> item;
    }

    @Getter
    @Setter
    public static class HubItem {
        private String baseYm;
        private String mapX;
        private String mapY;
        private String areaCd;
        private String areaNm;
        private String signguCd;
        private String signguNm;
        private String hubTatsCd;
        private String hubTatsNm;
        private String hubCtgryLclsNm;
        private String hubCtgryMclsNm;
        private String hubRank;
    }
}
