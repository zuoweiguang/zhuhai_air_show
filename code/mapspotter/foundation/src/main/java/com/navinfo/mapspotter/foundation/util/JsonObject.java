package com.navinfo.mapspotter.foundation.util;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by gaojian on 2016/2/2.
 */
public class JsonObject {
    private JsonNode root = null;

    public JsonObject(JsonNode node) {
        root = node;
    }

    public String get() {
        return root.asText();
    }

    public JsonObject getValue(String field) {
        JsonNode node = root.get(field);
        return node == null ? null : new JsonObject(node);
    }

    public String getStringValue(String field) {
        JsonNode node = root.get(field);
        return node == null ? null : node.asText();
    }

    public int getIntValue(String field) {
        JsonNode node = root.get(field);
        return node == null ? 0 : node.asInt();
    }

    public double getDoubleValue(String field) {
        JsonNode node = root.get(field);
        return node == null ? Double.NaN : node.asDouble();
    }

    public Collection<JsonObject> getArrayValue(String field) {
        Collection<JsonObject> result = new ArrayList<>();
        JsonNode array = root.get(field);
        Iterator<JsonNode> node = array.iterator();
        while (node.hasNext()) {
            result.add(new JsonObject(node.next()));
        }
        return result;
    }
}
