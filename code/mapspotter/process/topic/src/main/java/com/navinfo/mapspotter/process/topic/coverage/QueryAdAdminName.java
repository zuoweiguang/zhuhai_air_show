package com.navinfo.mapspotter.process.topic.coverage;

import com.navinfo.mapspotter.foundation.io.MysqlDatabase;
import com.navinfo.mapspotter.foundation.io.OracleDatabase;
import com.navinfo.mapspotter.foundation.io.SqlCursor;
import com.navinfo.mapspotter.foundation.io.SqlDatabase;
import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huanghai on 2016/3/12.
 */
public class QueryAdAdminName {
    public static final Logger logger = Logger.getLogger(QueryAdAdminName.class);

    /**
     * 查询区域名称
     *
     * @return
     * @throws SQLException
     */
    public Map<String, String> queryAdadminName(String host, int port, String database, String username, String passWord) throws SQLException {
        // 查询城市
        String sql = "select regionid, max(provnm), max(citynm) from ci_para_ad_admin where citynm in ('北京','上海','广州','深圳','沈阳','长春','成都','天津','重庆','武汉','苏州','南京','杭州','宁波','青岛','石家庄','郑州','昆明','西安','大连','福州','厦门','济南','长沙','哈尔滨','东莞','佛山','合肥','南昌','海口','乌鲁木齐','太原','呼和浩特','兰州','贵阳','西宁','南宁','唐山','常州','惠州','秦皇岛','三亚','绍兴','温州','无锡','芜湖','烟台','盐城','中山','珠海','保定','扬州','柳州','潍坊','南通','泉州','徐州','金华','台州','银川','香港','澳门') group by regionid";
        DataSourceParams params = new DataSourceParams();
        params.setHost(host);
        params.setPort(port);
        params.setDb(database);
        params.setUser(username);
        params.setPassword(passWord);
        params.setType(DataSourceParams.SourceType.MySql);
        MysqlDatabase db = (MysqlDatabase) SqlDatabase.getDataSource(params);
        SqlCursor sqlCursor = db.query(sql);
        Map<String, String> adMap = new HashMap<>();
        try {
            while (sqlCursor.next()) {
                String regionId = sqlCursor.getString(1);
                String provnm = sqlCursor.getString(2);
                String citynm = sqlCursor.getString(3);
                String value = provnm + "-" + citynm;
                adMap.put(regionId, value);
            }
        } catch (SQLException e) {
            logger.error("queryAdadminName : " + e);
            throw e;
        } finally {
            db.close();
        }
        return adMap;
    }
}

