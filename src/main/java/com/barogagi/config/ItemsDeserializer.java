package com.barogagi.config;

import com.barogagi.batch.dto.TourApiResponse;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.ArrayList;

public class ItemsDeserializer extends JsonDeserializer<TourApiResponse.Items> {

    @Override
    public TourApiResponse.Items deserialize(JsonParser p,
                                             DeserializationContext ctxt) throws IOException {

        if (p.currentToken() == JsonToken.VALUE_STRING) {
            TourApiResponse.Items items = new TourApiResponse.Items();
            items.setItem(new ArrayList<>());
            return items;
        }

        return p.readValueAs(TourApiResponse.Items.class);
    }
}
