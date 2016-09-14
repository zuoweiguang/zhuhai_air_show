package com.navinfo.mapspotter.foundation.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by SongHuiXing on 2016/1/12.
 */
public class DidiCarTrack extends CarTrack {
    //排序的轨迹点，仅用来构建轨迹时加快速度
    protected SortedSet<CarTrackPoint> m_SortTrack;

    /**
     * default constructor for jackson decode
     */
    public DidiCarTrack(){
        this("");
    }

    public DidiCarTrack(String id) {
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
     *          <th>Longitude</th>
     *          <th>Latitude</th>
     *          <th>Speed</th>
     *          <th>Angle</th>
     *          <th>time</th>
     *         </tr>
     * @return the time stamp of this point
     */
    @Override
    public int pushPoint(String[] fields) {
        int fldCount = fields.length;

        long timeStamp = Long.parseLong(fields[fldCount-1].trim());

        return insertPoint(timeStamp,
                Arrays.copyOfRange(fields,
                        0,
                        fldCount -1));
    }

    /**
     * Just push a point to track
     * @param fields The car point records, should be:
     *        <tr>
     *          <th>Longitude</th>
     *          <th>Latitude</th>
     *          <th>Speed</th>
     *          <th>Angle</th>
     *         </tr>
     * @return 0 成功 -1 无效数据 1 不应作为该轨迹的点
     */
    private int insertPoint(long timeStamp, String[] fields){

        double lon = Double.parseDouble(fields[0]);
        double lat = Double.parseDouble(fields[1]);
        double speed = Double.parseDouble(fields[2]);
        double angle = Double.parseDouble(fields[3]);

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
     * 1. 一共6个字段
     * 2. 速度大于等于0且小于250
     * 3. 角度大于等于0且小于360
     * @param fields
     * @return
     */
    public static boolean isValid(String[] fields){
        if(6 != fields.length)
            return false;

        //speed should faster than 0 and slower than 250
        double speed = Double.parseDouble(fields[3].trim());
        if(0 > speed || speed >= 250)
            return false;

        //angle should between [0,360)
        double angle = Double.parseDouble(fields[4].trim());
        if(0.0 > angle || angle >= 360)
            return false;

        return true;
    }
}
