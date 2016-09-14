package com.navinfo.mapspotter.foundation.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Created by SongHuiXing on 2016/1/12.
 */
public class JsonSerializeUtil {

    /**
     * Double类序列化，保留两位小数
     */
    public static class CustomDoubleSerialize extends JsonSerializer<Double> {

        private DecimalFormat df = new DecimalFormat("##.00");

        @Override
        public void serialize(Double arg0, JsonGenerator arg1, SerializerProvider arg2)
                throws IOException, JsonProcessingException {

            arg1.writeString(df.format(arg0));
        }

    }
}
