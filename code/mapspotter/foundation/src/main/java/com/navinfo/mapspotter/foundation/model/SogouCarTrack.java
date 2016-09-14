package com.navinfo.mapspotter.foundation.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 搜狗轨迹
 * Created by SongHuiXing on 2016/1/24.
 */
public class SogouCarTrack extends CarTrack {
    private static DateFormat DateFormater = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss");

    //排序的轨迹点，仅用来构建轨迹时加快速度
    protected SortedSet<CarTrackPoint> m_SortTrack;

    /**
     * default constructor for jackson decode
     */
    public SogouCarTrack(){
        this("");
    }

    public SogouCarTrack(String id) {
        super(id);
        m_SortTrack = new ConcurrentSkipListSet<>();
    }

    @Override
    public List<CarTrackPoint> getCarTrack(){
        if(null == m_CarTrack){
            m_CarTrack = new ArrayList<>();
            m_CarTrack.addAll(m_SortTrack);
            m_SortTrack.clear();
        }
        return m_CarTrack;
    }

    /**
     * Just push a point to track
     * @param fields The car point records, should be:
     *        <tr>
     *          <th>time</th>
     *          <th>Longitude</th>
     *          <th>Latitude</th>
     *          <th>Angle</th>
     *          <th>Speed</th>
     *          <th>others...</th>
     *         </tr>
     * @return the time stamp of this point
     */
    @Override
    public int pushPoint(String[] fields) {
        long timeStamp;
        try{
            Date dateTime = DateFormater.parse(fields[0].trim());

            timeStamp = dateTime.getTime() / 1000;
        }catch(ParseException e){
            return -1;
        }

        return insertPoint(timeStamp, Arrays.copyOfRange(fields, 1, fields.length));
    }

    /**
     * Just push a point to track
     * @param fields The car point records, should be:
     *        <tr>
     *          <th>Longitude</th>
     *          <th>Latitude</th>
     *          <th>Angle</th>
     *          <th>Speed</th>
     *         </tr>
     * @return 0 成功 -1 无效数据 1 不应作为该轨迹的点
     */
    private int insertPoint(long timeStamp, String[] fields){

        double lon = Double.parseDouble(fields[0]);
        double lat = Double.parseDouble(fields[1]);
        double speed = Double.parseDouble(fields[3]);
        double angle = Double.parseDouble(fields[2]);

        String[] others = new String[]{};

        CarTrackPoint pt = new CarTrackPoint(timeStamp, lon, lat,speed,angle,others);

//        CarTrackPoint lastPt = null;
//        if(!m_SortTrack.isEmpty()){
//            lastPt = m_SortTrack.last();
//        }

        if(!m_SortTrack.add(pt))
            return -1;

//        if(null != lastPt){ //两点间隔过长
//            if(s_maxDistanceBetweenPoints < SpatialUtil.getDistance(lastPt.getLongitude(),
//                                                                    lastPt.getLatitude(),
//                                                                    pt.getLongitude(),
//                                                                    pt.getLatitude())) {
//                m_SortTrack.remove(pt);
//                return 1;
//            }
//        }

        if(m_SortTrack.size() > 1){
            if(m_Envelope[0] > lon)
                m_Envelope[0] = lon;
            else if(m_Envelope[2] < lon)
                m_Envelope[2] = lon;

            if(m_Envelope[1] > lat)
                m_Envelope[1] = lat;
            else if(m_Envelope[3] < lat)
                m_Envelope[3] = lat;
        }else{
            m_Envelope[0] = m_Envelope[2] = lon;
            m_Envelope[1] = m_Envelope[3] = lat;
        }

        return 0;
    }

    /**
     * 检查输入数据是否符合规则
     * 1. 一共7个字段
     * 2. 速度大于等于0且小于250
     * 3. 角度大于等于0且小于360
     * @param fields
     * @return -1 if not valid, the seconds timestamp
     */
    public static long isValid(String[] fields){
        long timestamp = 0;
        //ignore the point which's year is newer than now
        try{
            if(7 != fields.length)
                return -1;

            //speed should faster than 0 and slower than 250
            double speed = Double.parseDouble(fields[4].trim());
            if(0 > speed || speed >= 250)
                return -1;

            //angle should between [0,360)
            double angle = Double.parseDouble(fields[3].trim());
            if(0.0 > angle || angle >= 360)
                return -1;

            //car id should not like "-1" or "-1-1"
            String[] carids = fields[6].split("\\-");
            if(0 == carids.length || carids[0].isEmpty())
                return -1;

            Date dateTime = DateFormater.parse(fields[0].trim());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateTime);

            int ptYear = calendar.get(Calendar.YEAR);
            if(ptYear == 1970)
                return -1;

            calendar.setTime(new Date());

            if(ptYear > calendar.get(Calendar.YEAR))
                return -1;

            timestamp = dateTime.getTime() / 1000;
        }catch(Exception e){
            return -1;
        }

        return timestamp;
    }

}
