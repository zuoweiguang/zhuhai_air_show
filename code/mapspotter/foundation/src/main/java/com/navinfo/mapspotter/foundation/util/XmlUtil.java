package com.navinfo.mapspotter.foundation.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by cuiliang on 2016/1/11.
 * xml解析
 */
public class XmlUtil {
    public static List<Element> parseXml2List(String filename){
        List<Element> list = new ArrayList();
        Document doc = null;
        try {
            doc = parse(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Element root = doc.getRootElement();
        for (Iterator iter = root.elementIterator(); iter.hasNext();){
            Element element = (Element) iter.next();
            list.add(element);
        }
        return list;
    }

    public static Document parse(String url) throws DocumentException {
        SAXReader reader;
        reader = new SAXReader();
        Document document = reader.read(url);
        return document;
    }

    public static List<Document> parse2(String url,List<Document> tempList) throws DocumentException {
        List<Document> docList = tempList;
        File file = new File(url);
        SAXReader reader = new SAXReader();
        if(!file.isDirectory()){
            Document document = reader.read(file.getPath());
            docList.add(document);
        }else{
            File[] fileList = file.listFiles();
            for(File f : fileList){
                if(!f.isDirectory()){
                    Document document = reader.read(f.getPath());
                    docList.add(document);
                }else{
                    parse2(f.getPath(),docList);
                }
            }
        }
        return docList;
    }

    public static String parseXMLtoJson(String filename)throws DocumentException {
        List<Document> documentList = new ArrayList<Document>();
        List<Document> docList = parse2(filename,documentList);
        StringBuffer str = new StringBuffer();
        str.append("[");
        for(Document document: docList){
            Element root = document.getRootElement();
            int j = 0;

            str.append("{");
            for ( Iterator i = root.elementIterator( "key" ); i.hasNext(); j++) {
                Element foo = (Element) i.next();
                String name = foo.attributeValue("name");
                String value = foo.getStringValue();
                str.append("\"").append(name).append("\":").append("\"").append(value).append("\",");

            }
            String temp = str.toString().substring(0, str.length()-1);
            str.setLength(0);
            str.append(temp).append("},");
        }
        String temp = str.toString();
        if(str.length() > 1){
            temp = str.toString().substring(0, str.length()-1);
        }
        str.setLength(0);
        str.append(temp).append("]");
        return str.toString();
    }
}
