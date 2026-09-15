package com.barogagi.calendar.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class HolidaysResponseBody {

    @JsonDeserialize(using = ItemsDeserializer.class)
    private Items items;

    private int numOfRows;
    private int pageNo;
    private int totalCount;

    @Getter
    @Setter
    public static class Items {

        private List<HolidaysItem> item;
    }

    public static class ItemsDeserializer extends JsonDeserializer<Items> {

        @Override
        public Items deserialize(JsonParser parser, DeserializationContext context)
                throws IOException {

            // items가 ""인 경우
            if (parser.currentToken() == JsonToken.VALUE_STRING) {
                return null;
            }

            // items가 객체인 경우
            JsonNode node = parser.getCodec().readTree(parser);

            Items items = new Items();

            JsonNode itemNode = node.get("item");

            if (itemNode != null && itemNode.isArray()) {

                List<HolidaysItem> holidayItems = new ArrayList<>();

                for (JsonNode item : itemNode) {
                    HolidaysItem holidayItem =
                            parser.getCodec().treeToValue(item, HolidaysItem.class);

                    holidayItems.add(holidayItem);
                }

                items.setItem(holidayItems);
            }

            return items;
        }
    }
}