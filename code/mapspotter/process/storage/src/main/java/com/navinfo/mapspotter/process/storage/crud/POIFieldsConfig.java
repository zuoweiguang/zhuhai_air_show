package com.navinfo.mapspotter.process.storage.crud;

import com.navinfo.mapspotter.foundation.util.JsonUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by SongHuiXing on 6/23 0023.
 */
public class POIFieldsConfig {

    public static List<String> getFields() {

        InputStream in = POIFieldsConfig.class.getResourceAsStream("/PoiStorageFields");

        InputStreamReader input = new InputStreamReader(in);

        BufferedReader reader = new BufferedReader(input);

        try {
            String content = "";
            String lineTxt;
            while (null !=(lineTxt = reader.readLine())){
                content = content + lineTxt;
            }

            Map<String, Object> poicfg = JsonUtil.getInstance().readMap(content);

            Object fields = poicfg.get("fields");

            return (List<String>)fields;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
                input.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new ArrayList<>();
    }
}
