package com.navinfo.mapspotter.warehouse.zhuhai.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SongHuiXing on 6/14 0014.
 */
public class DataSource {

    public enum SourceType {
        ZhuhaiEvents,
        ZhuhaiForecastHalfhour,
        ZhuhaiForecastOnehour,
        ZhuhaiStaff,
        ZhuhaiParking,
    }

    public enum LayerType {
        Events,
        ForecastHalfhour,
        ForecastOnehour,
        Staff,
        Parking,
    }

    public static List<LayerType> getLayers(SourceType srcType) {
        ArrayList<LayerType> targetTypes = new ArrayList<>();

        switch (srcType) {
            case ZhuhaiEvents:{
                targetTypes.add(LayerType.Events);
            }
            break;
            case ZhuhaiForecastHalfhour:{
                targetTypes.add(LayerType.ForecastHalfhour);
            }
            break;
            case ZhuhaiForecastOnehour:{
                targetTypes.add(LayerType.ForecastOnehour);
            }
            break;
            case ZhuhaiStaff:{
                targetTypes.add(LayerType.Staff);
            }
            break;
            case ZhuhaiParking:{
                targetTypes.add(LayerType.Parking);
            }
            break;
        }

        return targetTypes;
    }

    public static SourceType getSourceType(LayerType lyrType) {
        switch (lyrType) {
            case Events:
                return SourceType.ZhuhaiEvents;
            case ForecastHalfhour:
                return SourceType.ZhuhaiForecastHalfhour;
            case ForecastOnehour:
                return SourceType.ZhuhaiForecastOnehour;
            case Staff:
                return SourceType.ZhuhaiStaff;
            case Parking:
                return SourceType.ZhuhaiParking;
        }
        return null;
    }
}
