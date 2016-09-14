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
    }

    public enum LayerType {
        Events,
        ForecastHalfhour,
        ForecastOnehour,
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
        }
        return null;
    }
}
