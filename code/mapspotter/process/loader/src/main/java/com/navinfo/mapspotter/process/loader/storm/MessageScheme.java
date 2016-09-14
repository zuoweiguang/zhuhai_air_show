package com.navinfo.mapspotter.process.loader.storm;

/**
 * Created by cuiliang on 2016/3/14.
 */

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Didi2Siwei.DidiSiweiProtocol;
import Didi2Siwei.DidiSiweiProtocol.CoordinateType;
import Didi2Siwei.DidiSiweiProtocol.DataSourceVendor;
import Didi2Siwei.DidiSiweiProtocol.Dd2SwTrajPoint;
import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class MessageScheme implements Scheme {

    /**
     *
     */
    private static final long serialVersionUID = 4502976094149991429L;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MessageScheme.class);

    public List<Object> deserialize(byte[] bytes) {
        try {
            Dd2SwTrajPoint data = Dd2SwTrajPoint.parseFrom(bytes);
            String userId = data.getUserid();
            double lng = data.getLng();
            double lat = data.getLat();

            double speed = data.getSpeed();
            double direction = data.getDirection();
            long timestamp = data.getLocationTimestamp();

            DataSourceVendor v = data.getDataSource();

            int isGps = Constants.NOT_GPS;
            String coordType = "";

            if (data.getLocSource() == DidiSiweiProtocol.UserSourceType.LOC_GPS) {
                isGps = Constants.IS_GPS;
            }

            if (data.getCoordType() == CoordinateType.WGS_84) {
                coordType = Constants.WGS_84;
            } else if (data.getCoordType() == CoordinateType.GCJ_02) {
                coordType = Constants.GCJ_02;
            } else if (data.getCoordType() == CoordinateType.BD_09) {
                coordType = Constants.BD_09;
            }

            return new Values(userId, lng, lat, speed, direction, timestamp, isGps, v.getNumber(), coordType);

        } catch (Exception e) {
            LOGGER.error("Cannot parse the provided message!");
        }
        return null;
    }

    public Fields getOutputFields() {
        return new Fields("userId", "lng", "lat", "speed", "direction", "timestamp", "isGps", "sourceType", "coordType");
    }

}