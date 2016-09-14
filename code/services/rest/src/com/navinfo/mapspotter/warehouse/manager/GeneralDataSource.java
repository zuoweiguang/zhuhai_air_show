package com.navinfo.mapspotter.warehouse.manager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Created by cuiliang on 2016/6/21.
 */
public class GeneralDataSource {

    DataSourceService service = new DataSourceService();

    private static final String root_service_url = "http://192.168.4.128:8080/mapspotter";

    public void addRoad() {
        service.delDataSource("road");
        JSONArray source_layers = new JSONArray();

        JSONObject layer = new JSONObject();

        JSONArray rdAttributes = new JSONArray();

        JSONObject kind = new JSONObject();
        JSONObject kindValue = new JSONObject();
        kindValue.put("作业中", 0);
        kindValue.put("高速道路", 1);
        kindValue.put("城市高速", 2);
        kindValue.put("国道", 3);
        kindValue.put("省道", 4);
        kindValue.put("预留", 5);
        kindValue.put("县道", 6);
        kindValue.put("乡镇村道路", 7);
        kindValue.put("其它道路", 8);
        kindValue.put("非引导道路", 9);
        kindValue.put("步行道路", 10);
        kindValue.put("人渡", 11);
        kindValue.put("轮渡", 13);
        kindValue.put("10级路(障碍物)", 15);
        kind.put("id", "kind");
        kind.put("name", "道路种别");
        kind.put("attr", kindValue);


        JSONObject functionclass = new JSONObject();
        JSONObject fcValue = new JSONObject();
        fcValue.put("等级1", 1);
        fcValue.put("等级2", 2);
        fcValue.put("等级3", 3);
        fcValue.put("等级4", 4);
        fcValue.put("等级5", 5);
        functionclass.put("id", "functionclass");
        functionclass.put("name", "功能等级");
        functionclass.put("attr", fcValue);

        rdAttributes.add(kind);
        rdAttributes.add(functionclass);

        layer.put("id", "Road");
        layer.put("type", "line");
        layer.put("name", "道路层");
        layer.put("attrs", rdAttributes);
        source_layers.add(layer);


        JSONObject rwLayer = new JSONObject();
        JSONArray rwAttributes = new JSONArray();

        JSONObject rwKind = new JSONObject();
        JSONObject rwKindValue = new JSONObject();
        rwKindValue.put("铁路", 1);
        rwKindValue.put("磁悬浮", 2);
        rwKindValue.put("地铁/轻轨", 3);
        rwKind.put("id", "kind");
        rwKind.put("name", "铁路种别");
        rwKind.put("attr", rwKindValue);

        rwAttributes.add(rwKind);

        rwLayer.put("id", "RailWay");
        rwLayer.put("type", "line");
        rwLayer.put("name", "铁路层");
        rwLayer.put("attrs", rwAttributes);
        source_layers.add(rwLayer);
    }

    public void addBackground() {
        service.delDataSource("background");
        JSONArray source_layers = new JSONArray();

        JSONObject layer = new JSONObject();

        JSONArray lcAttributes = new JSONArray();

        JSONObject kind = new JSONObject();
        JSONObject kindValue = new JSONObject();
        kindValue.put("海域", 1);
        kindValue.put("河川域", 2);
        kindValue.put("湖沼池", 3);
        kindValue.put("水库", 4);
        kindValue.put("港湾", 5);
        kindValue.put("运河", 6);
        kindValue.put("公园", 11);
        kindValue.put("高尔夫球场", 12);
        kindValue.put("滑雪场", 13);
        kindValue.put("树林林地", 14);
        kindValue.put("草地", 15);
        kindValue.put("绿化带", 16);
        kindValue.put("岛", 17);
        kind.put("id", "kind");
        kind.put("name", "土地覆盖种别");
        kind.put("attr", kindValue);

        lcAttributes.add(kind);

        layer.put("id", "LC");
        layer.put("type", "fill");
        layer.put("name", "土地覆盖层");
        layer.put("attrs", lcAttributes);
        source_layers.add(layer);


        JSONObject luLayer = new JSONObject();
        JSONArray luAttributes = new JSONArray();

        JSONObject luKind = new JSONObject();
        JSONObject luKindValue = new JSONObject();
        luKindValue.put("大学", 1);
        luKindValue.put("购物中心", 2);
        luKindValue.put("医院", 3);
        luKindValue.put("体育场", 4);
        luKindValue.put("公墓", 5);
        luKindValue.put("地上停车场", 6);
        luKindValue.put("工业区", 7);
        luKindValue.put("机场", 11);
        luKindValue.put("机场跑道", 12);
        luKindValue.put("BUA面", 21);
        luKindValue.put("邮编面", 22);
        luKindValue.put("FM面", 23);
        luKindValue.put("车厂面", 24);
        luKindValue.put("休闲娱乐", 30);
        luKindValue.put("景区", 31);
        luKindValue.put("会展中心", 32);
        luKindValue.put("火车站", 33);
        luKindValue.put("文化场馆", 34);
        luKindValue.put("商务区", 35);
        luKindValue.put("商业区", 36);
        luKindValue.put("小区", 37);
        luKindValue.put("广场", 38);
        luKindValue.put("特色区域", 39);
        luKindValue.put("地下停车场", 40);
        luKindValue.put("地铁出入口面", 41);
        luKind.put("id", "kind");
        luKind.put("name", "土地利用种别");
        luKind.put("attr", luKindValue);

        luAttributes.add(luKind);

        luLayer.put("id", "LU");
        luLayer.put("type", "fill");
        luLayer.put("name", "土地利用层");
        luLayer.put("attrs", luAttributes);
        source_layers.add(luLayer);

        JSONObject cmLayer = new JSONObject();
        JSONArray cmAttributes = new JSONArray();

        JSONObject cmKind = new JSONObject();
        JSONObject cmKindValue = new JSONObject();
        cmKindValue.put("厂矿企业", 1001);
        cmKindValue.put("商业建筑", 1002);
        cmKindValue.put("会议,展览中心", 2001);
        cmKindValue.put("宗教", 3001);
        cmKindValue.put("公众活动性建筑", 3002);
        cmKindValue.put("高等教育", 4001);
        cmKindValue.put("一般学校", 4002);
        cmKindValue.put("其它教育设施", 4003);
        cmKindValue.put("急救中心", 5001);
        cmKindValue.put("公安局", 5002);
        cmKindValue.put("消防", 5003);
        cmKindValue.put("交警队", 5004);
        cmKindValue.put("其它紧急服务设施", 5005);
        cmKindValue.put("邮政", 6001);
        cmKindValue.put("政府机关", 6002);
        cmKindValue.put("其它政府性建筑", 6003);
        cmKindValue.put("名胜古迹", 7001);
        cmKindValue.put("医院", 8001);
        cmKindValue.put("医疗服务", 8002);
        cmKindValue.put("其它医疗设施", 8003);
        cmKindValue.put("高尔夫球场", 9001);
        cmKindValue.put("游乐园,公园", 9002);
        cmKindValue.put("滑雪场", 9003);
        cmKindValue.put("其它休闲设施", 9004);
        cmKindValue.put("公寓,别墅", 1101);
        cmKindValue.put("小区", 1102);
        cmKindValue.put("其它居民建筑", 1103);
        cmKindValue.put("批发市场,建材", 1201);
        cmKindValue.put("餐饮", 1202);
        cmKindValue.put("百货商场商城", 1203);
        cmKindValue.put("其它商业", 1204);
        cmKindValue.put("体育场馆", 1301);
        cmKindValue.put("其它运动场所", 1302);
        cmKindValue.put("动,植物园", 1401);
        cmKindValue.put("陵园,公墓", 1402);
        cmKindValue.put("其它景点建筑", 1403);
        cmKindValue.put("火车站", 1501);
        cmKindValue.put("机场", 1502);
        cmKindValue.put("长途客运站", 1503);
        cmKindValue.put("港口", 1504);
        cmKindValue.put("其它运输相关建筑", 1505);
        cmKindValue.put("其它", 1601);
        cmKind.put("id", "kind");
        cmKind.put("name", "市街图种别");
        cmKind.put("attr", cmKindValue);

        cmAttributes.add(cmKind);

        cmLayer.put("id", "CityModel");
        cmLayer.put("type", "fill");
        cmLayer.put("name", "市街图层");
        cmLayer.put("attrs", cmAttributes);
        source_layers.add(cmLayer);

        service.addDataSource("background", "vector",
                                source_layers.toJSONString(),
                                "[\"" + root_service_url + "/view/background/{z}/{x}/{y}\"]",
                                "", 0, false, "", "背景", 3, 17);
    }


    public void addPoi() {
        service.delDataSource("poi");
        JSONArray source_layers = new JSONArray();

        JSONObject layer = new JSONObject();

        JSONArray rdAttributes = new JSONArray();

        layer.put("id", "Poi");
        layer.put("type", "circle");
        layer.put("name", "POI层");
        layer.put("attrs", rdAttributes);
        source_layers.add(layer);

        service.addDataSource("poi", "vector",
                                source_layers.toJSONString(),
                                "[\"" + root_service_url + "/view/poi/{z}/{x}/{y}\"]",
                                "", 0, false, "", "兴趣点", 3, 17);
    }

    public void addAdmin() {
        service.delDataSource("admin");
        JSONArray source_layers = new JSONArray();

        JSONObject layer = new JSONObject();

        JSONArray rdAttributes = new JSONArray();

        layer.put("id", "Admin");
        layer.put("type", "fill");
        layer.put("name", "行政区划面层");
        layer.put("attrs", rdAttributes);
        source_layers.add(layer);

        JSONObject boundlayer = new JSONObject();

        JSONArray boundAttributes = new JSONArray();

        boundlayer.put("id", "AdminBoundary");
        boundlayer.put("type", "line");
        boundlayer.put("name", "行政区划边界线层");
        boundlayer.put("attrs", boundAttributes);
        source_layers.add(boundlayer);


        service.addDataSource("admin", "vector",
                            source_layers.toJSONString(),
                            "[\"" + root_service_url + "/view/admin/{z}/{x}/{y}\"]",
                            "", 0, false, "", "行政区划", 3, 17);
    }

    public void update(){

    }

    public static void main(String[] args) {
        GeneralDataSource gen = new GeneralDataSource();
        gen.addRoad();
        gen.addBackground();
        gen.addAdmin();
        gen.addPoi();
        //gen.update();
    }
}
