package com.navinfo.mapspotter.warehouse.bean;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by cuiliang on 2016/6/17.
 */
@XmlRootElement
public class LayerBean implements DataBean {
    public String id;
    public String name;
    public String type;
    public String source;
    public String style;
    public String filter;
    public String desc;
    public String source_layer;
    public String minzoom;
    public String maxzoom;
    public String interactive;
    public String soluID;

    public LayerBean(String id, String name, String type, String source,
                     String style, String filter, String desc, String source_layer,
                     String minzoom, String maxzoom, String interactive, String soluID) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.source = source;
        this.style = style;
        this.filter = filter;
        this.desc = desc;
        this.source_layer = source_layer;
        this.minzoom = minzoom;
        this.maxzoom = maxzoom;
        this.interactive = interactive;
        this.soluID = soluID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getSource_layer() {
        return source_layer;
    }

    public void setSource_layer(String source_layer) {
        this.source_layer = source_layer;
    }

    public String getMinzoom() {
        return minzoom;
    }

    public void setMinzoom(String minzoom) {
        this.minzoom = minzoom;
    }

    public String getMaxzoom() {
        return maxzoom;
    }

    public void setMaxzoom(String maxzoom) {
        this.maxzoom = maxzoom;
    }

    public String getInteractive() {
        return interactive;
    }

    public void setInteractive(String interactive) {
        this.interactive = interactive;
    }

    public String getSoluID() {
        return soluID;
    }

    public void setSoluID(String soluID) {
        this.soluID = soluID;
    }
}
