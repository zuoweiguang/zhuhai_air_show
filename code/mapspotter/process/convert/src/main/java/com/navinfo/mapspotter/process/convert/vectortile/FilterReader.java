package com.navinfo.mapspotter.process.convert.vectortile;

import com.navinfo.mapspotter.process.convert.WarehouseDataType;
import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.List;

/**
 * Created by SongHuiXing on 6/7 0007.
 */
public class FilterReader {

    public static String getFilter(WarehouseDataType.LayerType lyrType, int level){
        SAXReader reader = new SAXReader();

        InputStream in = FilterReader.class.getResourceAsStream("/PbfFilter.xml");

        String filter = " ";
        String source = WarehouseDataType.getSourceType(lyrType).toString();
        String layer = lyrType.toString();
        try {
            Document doc = reader.read(in);

            Element root = doc.getRootElement();

            String path = source + "/" + layer + "/Level-" + level + "/condition";
            List<Node> conditions = root.selectNodes(path);

            for(Node n : conditions){
                Element conditionEle = (Element)n;
                if(null == conditionEle)
                    continue;

                Attribute attr = conditionEle.attribute("filter");
                if(null == attr)
                    continue;

                filter = filter + attr.getValue() + " and ";
            }
        } catch (DocumentException e) {
            e.printStackTrace();
            return "";
        }

        return filter;
    }

    public static int getMinLevel(WarehouseDataType.SourceType srcType){
        SAXReader reader = new SAXReader();

        InputStream in = FilterReader.class.getResourceAsStream("/PbfFilter.xml");

        String filter = " ";
        String source = srcType.toString();
        try {
            Document doc = reader.read(in);

            Element root = doc.getRootElement();

            Node target = root.selectSingleNode("./" + source);

            Element targetEle = (Element)target;
            if(null == targetEle)
                return 3;

            Attribute attr = targetEle.attribute("minlevel");
            if(null == attr)
                return 3;

            return Integer.parseInt(attr.getValue());

        } catch (DocumentException e) {
            e.printStackTrace();
            return 3;
        }
    }
}
