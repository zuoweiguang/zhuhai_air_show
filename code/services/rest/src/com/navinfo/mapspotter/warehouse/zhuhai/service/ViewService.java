package com.navinfo.mapspotter.warehouse.zhuhai.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mercator.TileUtils;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.navinfo.mapspotter.warehouse.zhuhai.dao.ViewDao;
import com.navinfo.mapspotter.warehouse.zhuhai.util.DataSource;
import com.navinfo.mapspotter.warehouse.zhuhai.util.PropertiesUtil;
import com.vector.tile.VectorTileEncoder;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;

import javax.ws.rs.FormParam;
import javax.ws.rs.PathParam;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zuoweiguang on 2016/9/7.
 */
public class ViewService {

    private ViewDao viewDao = ViewDao.getInstance();
    private static JSONObject prop = PropertiesUtil.getProperties();

    public static GeometryFactory geometryFactory = new GeometryFactory();
    private static ViewService instance;

    private ViewService() {
    }

    public static synchronized ViewService getInstance() {
        if (instance == null) {
            instance = new ViewService();
        }
        return instance;
    }

    public byte[] getTrafficEvent(int z, int x, int y) {
        VectorTileEncoder vtm = new VectorTileEncoder(4096, 16, false);
        try {
            DBCollection col = viewDao.getCollection(prop.getString("eventColName"));
            List<DBObject> eventList = viewDao.getTrafficEvent(col, z, x, y);
            for (DBObject event: eventList) {
                Map<String, Object> attributes = new HashMap<>();
                attributes.put("RoadName", event.get("RoadName"));
                attributes.put("EventDescription", event.get("EventDescription"));
                List<Double> LinkCoordinate = (List<Double>)event.get("LinkCoordinate");

                Coordinate coordinate = new Coordinate(LinkCoordinate.get(0), LinkCoordinate.get(1));
                Point point = geometryFactory.createPoint(coordinate);
                TileUtils.convert2Piexl(x, y, z, point);
                vtm.addFeature(DataSource.LayerType.Events.toString(), attributes, point);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return vtm.encode();
        }
    }

    public byte[] getForecasts(int z, int x, int y, String queryType) {
        VectorTileEncoder vtm = new VectorTileEncoder(4096, 16, false);
        WKTReader reader = new WKTReader();
        try {
            JSONObject prop = PropertiesUtil.getProperties();
            DBCollection col = null;
            if (queryType.equals("halfhour")) {
                col = viewDao.getCollection(prop.getString("halfhour_forecastColName"));
            }
            else if (queryType.equals("onehour")) {
                col = viewDao.getCollection(prop.getString("onehour_forecastColName"));
            }

            List<DBObject> forecastList = viewDao.getForecasts(col, z, x, y);
            for (DBObject forecast: forecastList) {
                try {
                    Map<String, Object> attributes = new HashMap<>();
                    attributes.put("link_id", forecast.get("link_id"));
                    attributes.put("direct", forecast.get("direct"));
                    attributes.put("road_class", forecast.get("road_class"));
                    attributes.put("status", forecast.get("status"));
                    attributes.put("travel_time", forecast.get("travel_time"));
                    String geometryStr = (String)forecast.get("geometry");
                    LineString line = (LineString) reader.read(geometryStr);
                    TileUtils.convert2Piexl(x, y, z, line);

                    if (queryType.equals("halfhour")) {
                        vtm.addFeature(DataSource.LayerType.ForecastHalfhour.toString(), attributes, line);
                    }
                    else if (queryType.equals("onehour")) {
                        vtm.addFeature(DataSource.LayerType.ForecastOnehour.toString(), attributes, line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return vtm.encode();
        }
    }

    public byte[] getTraffic(int z, int x, int y) {
        if (z < 10 || z > 17) {
            return null;
        }
        VectorTileEncoder vtm = new VectorTileEncoder(4096, 16, false);
        WKTReader reader = new WKTReader();
        try {
            DBCollection col = viewDao.getCollection(prop.getString("trafficColName"));
            List<DBObject> trafficList = viewDao.getTraffic(col, z, x, y);
            for (DBObject traffic: trafficList) {
                try {
                    Map<String, Object> attributes = new HashMap<>();
                    attributes.put("link_id", traffic.get("link_id"));
                    attributes.put("direct", traffic.get("direct"));
                    attributes.put("functionclass", traffic.get("functionclass"));
                    attributes.put("status", traffic.get("status"));
                    attributes.put("travelTime", traffic.get("travel_time"));
                    String geometryStr = (String)traffic.get("geometry");
                    LineString line = (LineString) reader.read(geometryStr);
                    TileUtils.convert2Piexl(x, y, z, line);
                    vtm.addFeature(DataSource.LayerType.Traffic.toString(), attributes, line);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return vtm.encode();
        }
    }

    public byte[] getStaff(int z, int x, int y) {

        if (z < 10 || z > 17) {
            return null;
        }

        VectorTileEncoder vtm = new VectorTileEncoder(4096, 16, false);

        try {
            DBCollection col = viewDao.getCollection(prop.getString("userColName"));
            List<DBObject> staffList = viewDao.getStaff(col);
            for (DBObject staff: staffList) {
                try {
                    Map<String, Object> attributes = new HashMap<>();
                    attributes.put("mobile_phone", staff.get("mobile_phone"));
                    attributes.put("user_name", staff.get("user_name"));
                    attributes.put("user_type", staff.get("user_type"));
                    attributes.put("location", staff.get("location"));
                    List<Double> location = (List<Double>)staff.get("location");

                    Coordinate coordinate = new Coordinate(location.get(0), location.get(1));
                    Point point = geometryFactory.createPoint(coordinate);
                    TileUtils.convert2Piexl(x, y, z, point);
                    vtm.addFeature(DataSource.LayerType.Staff.toString(), attributes, point);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return vtm.encode();
        }
    }

    public String addStaff(String mobile_phone, String password, String confirm_password, String user_name,
                           int user_type, String id_card, int sex, int age, String address) {
        String result = null;
        try {
            String check = checkStaffValue(mobile_phone, password, confirm_password, user_name,
                    user_type, id_card, sex, age, address);
            if (null != check) {
                return check;
            }

            DBCollection col = viewDao.getCollection(prop.getString("userColName"));
            int exists = viewDao.checkUserExists(col, mobile_phone);
            if (exists == 1) {
                return "当前用户的手机号码已存在!!";
            } else if (exists == -1) {
                return "查询用户时出错了，请检查服务!!";
            }

            int insertResult = viewDao.addStaff(col, mobile_phone, password, confirm_password, user_name,
                    user_type, id_card, sex, age, address);
            if (insertResult != 0) {
                result = "新增人员失败!!";
            }
            result = "新增人员成功!!";
        } catch (Exception e) {
            e.printStackTrace();
            result = e.getStackTrace().toString();
        } finally {
            return result;
        }
    }

    private String checkStaffValue(String mobile_phone, String password, String confirm_password, String user_name,
                                   int user_type, String id_card, int sex, int age, String address) {
        if (null == mobile_phone || mobile_phone.trim().equals("")) {
            return "联系电话不能为空!!";
        }
        else if (null == password || password.trim().equals("")) {
            return "登陆密码不能为空!!";
        }
        else if (!confirm_password.trim().equals(password.trim())) {
            return "密码确认与首次输入不一致!!";
        }
        else if (user_type != 1 && user_type != 2 && user_type != 3) {
            return "执勤人员选择错误!!";
        }
        else {
            return null;
        }
    }


    public int staffUploadLocation(String mobile_phone, double lon, double lat) {
        int result;
        try {
            DBCollection col = viewDao.getCollection(prop.getString("userColName"));
            result = viewDao.staffUploadLocation(col, mobile_phone, lon, lat);
        } catch (Exception e) {
            e.printStackTrace();
            result = -1;
        }
        return result;
    }


    public byte[] getParking(int z, int x, int y) {

        if (z < 10 || z > 17) {
            return null;
        }

        VectorTileEncoder vtm = new VectorTileEncoder(4096, 16, false);

        try {
            DBCollection col = viewDao.getCollection(prop.getString("parkingColName"));
            List<DBObject> parkList = viewDao.getParking(col);
            for (DBObject park: parkList) {
                try {
                    Map<String, Object> attributes = new HashMap<>();
                    attributes.put("park_name", park.get("park_name"));
                    attributes.put("park_total", park.get("park_total"));
                    attributes.put("in_using", park.get("in_using"));
                    attributes.put("geometry", park.get("geometry"));
                    List<Double> geometry = (List<Double>)park.get("geometry");

                    Coordinate coordinate = new Coordinate(geometry.get(0), geometry.get(1));
                    Point point = geometryFactory.createPoint(coordinate);
                    TileUtils.convert2Piexl(x, y, z, point);
                    vtm.addFeature(DataSource.LayerType.Parking.toString(), attributes, point);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return vtm.encode();
        }

    }


    public byte[] getBus(int z, int x, int y) {

        if (z < 10 || z > 17) {
            return null;
        }

        VectorTileEncoder vtm = new VectorTileEncoder(4096, 16, false);
        WKTReader wktReader = new WKTReader();
        try {
            DBCollection col = viewDao.getCollection(prop.getString("busColName"));
            List<DBObject> busList = viewDao.getBus(col, z, x, y);
            for (DBObject bus: busList) {
                try {
                    Map<String, Object> attributes = new HashMap<>();
                    attributes.put("bus_type", bus.get("bus_type"));
                    attributes.put("card_num", bus.get("card_num"));
                    attributes.put("start_location", bus.get("start_location"));
                    attributes.put("end_location", bus.get("end_location"));
                    attributes.put("current_location", bus.get("current_location"));
                    attributes.put("busload", bus.get("busload"));
                    attributes.put("current_load", bus.get("current_load"));

                    String drive_line = (String)bus.get("drive_line");
                    LineString line = (LineString) wktReader.read(drive_line);
                    TileUtils.convert2Piexl(x, y, z, line);

                    vtm.addFeature(DataSource.LayerType.Bus.toString(), attributes, line);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return vtm.encode();
        }
    }


}
