package com.navinfo.mapspotter.foundation.io.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.navinfo.mapspotter.foundation.util.DateTimeUtil;

import java.io.IOException;
import java.util.Date;

/**
 * Created by SongHuiXing on 2016/1/5.
 */
public class MongoDateFormatter {
    public static class MongoDateSerializer extends JsonSerializer<Date> {

        @Override
        public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException, JsonProcessingException {

            jsonGenerator.writeStartObject();   //{

            jsonGenerator.writeStringField("$date", DateTimeUtil.fromDate(date));

            jsonGenerator.writeEndObject();     //}

            jsonGenerator.flush();
        }
    }

    public static class MongoDateDeserializer extends JsonDeserializer<Date> {

        @Override
        public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException, JsonProcessingException {
            JsonNode root = jsonParser.getCodec().readTree(jsonParser);

            JsonNode dateNode = root.findValue("$date");

            long miliSecond = dateNode.asLong();

            return new Date(miliSecond);
        }
    }
}
