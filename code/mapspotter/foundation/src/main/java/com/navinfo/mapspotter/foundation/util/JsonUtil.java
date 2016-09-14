package com.navinfo.mapspotter.foundation.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Json对象的读写
 * Created by SongHuiXing on 2016/1/7.
 */
public class JsonUtil {
    private static JsonUtil s_instance = null;

    public static JsonUtil getInstance(){
        if(null == s_instance)
            s_instance = new JsonUtil();

        return s_instance;
    }

    private ObjectMapper m_objMapper = new ObjectMapper();

    private JsonFactory m_factory = new JsonFactory();

    private ArrayType m_doubleArrayType = null;
    private ArrayType m_shortArrayType = null;
    private ArrayType m_intArrayType = null;

    private JsonUtil(){
        TypeFactory factory = m_objMapper.getTypeFactory();

        m_doubleArrayType = factory.constructArrayType(double.class);
        m_shortArrayType = factory.constructArrayType(short.class);
        m_intArrayType = factory.constructArrayType(int.class);

        m_objMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public String write2String(Object obj)
            throws JsonProcessingException {
        return m_objMapper.writeValueAsString(obj);
    }

    public <T> T readValue(String jacksonStr, Class<T> javaClass)
            throws IOException {
        return m_objMapper.readValue(jacksonStr, javaClass);
    }

    public <K,V> Map<K, V> readMap(String jsonstr) throws IOException {
        return m_objMapper.readValue(jsonstr, new TypeReference<Map<K, V>>(){});
    }

    public double[] readDoubleArray(byte[] bytes)
            throws IOException{

        return m_objMapper.readValue(bytes, m_doubleArrayType);
    }

    public double[] readDoubleArray(String jsStr)
            throws IOException{

        return m_objMapper.readValue(jsStr, m_doubleArrayType);
    }

    public short[] readShortArray(String jsonstr)
            throws IOException{
        return m_objMapper.readValue(jsonstr, m_shortArrayType);
    }

    public int[] readIntArray(String jsonstr) throws IOException {
        return m_objMapper.readValue(jsonstr, m_intArrayType);
    }

    public List<String> readStringArray(String jsonstr)
            throws IOException{

        ArrayList<String> strs = new ArrayList<>();

        JsonNode root = m_objMapper.readTree(jsonstr);

        Iterator<JsonNode> nodes = root.elements();
        while (nodes.hasNext()){
            strs.add(nodes.next().toString());
        }

        return strs;
    }

    public List<int[]> readIntMatrix(byte[] matrixJson){
        return readIntMatrix(Bytes.toString(matrixJson));
    }

    public List<int[]> readIntMatrix(String matrixJson){
        List<int[]> matrixValues = new ArrayList<>();

        JsonNode root;
        try {
            root = m_objMapper.readTree(matrixJson);

            Iterator<JsonNode> coordNodes = root.elements();
            while (coordNodes.hasNext()) {
                JsonNode rowNode = coordNodes.next();

                int[] rowvalues = readIntArray(rowNode.toString());

                matrixValues.add(rowvalues);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return matrixValues;
    }

    public String writeDoubleArray(double[] array){
        return writeDoubleArray(array, 1);
    }

    public String writeDoubleArray(double[] array, int decimal){
        String formatStr = String.format("%%.%df", decimal);

        JsonGenerator generator;
        String res = "";
        try(StringWriter writer = new StringWriter()) {
            generator = m_factory.createGenerator(writer);

            generator.writeStartArray();
            for(int i=0;i<array.length;i++){
                if(i > 0)
                    generator.writeRaw(',');

                generator.writeRaw(String.format(formatStr, array[i]));
            }
            generator.writeEndArray();

            generator.close();

            res = writer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    public <T> ArrayList<T> readCollection(String json, Class<T> type) throws IOException {
        TypeFactory factory = m_objMapper.getTypeFactory();

        CollectionType collType = factory.constructCollectionType(ArrayList.class, type);

        return m_objMapper.readValue(json,collType);
    }

    public JsonObject readJson(String json) {
        try {
            return new JsonObject(m_objMapper.readTree(json));
        } catch (Exception e) {
            Logger.getLogger(JsonUtil.class).error(e);
            return null;
        }
    }
}
