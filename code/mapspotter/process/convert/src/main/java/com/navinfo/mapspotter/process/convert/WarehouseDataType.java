package com.navinfo.mapspotter.process.convert;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SongHuiXing on 6/14 0014.
 */
public class WarehouseDataType {

    public enum DataSourceType {
        Mongo,
        PostGIS,
    }

    public enum SourceType {
        Road,
        Admin,
        Background,
        Poi,
        PoiHeatMap,
        EditHeatMap,
        Dig,
        Information,
        SogouTrack,
        Block,
        Traffic,
        BaoTouTraffic,
        ZhuhaiAirShow,
    }

    public enum LayerType {
        Road,
        RailWay,
        Admin,
        AdminBoundary,
        AdminFlag,
        LU,
        LC,
        CityModel,
        Poi,
        PoiDayEditHeatmap,
        PoiMonthEditHeatmap,
        PoiCollectHeatmap,
        Construction,
        Missingroad,
        RestricDetail,
        InfoPoi,
        InfoRoad,
        SogouSearch,
        MapbarSearch,
        BlockHistory,
        TrafficStatus,
        BaotouTrafficStatus,
        TrafficEvents,
        Forecast,
    }

    public static List<LayerType> getLayers(SourceType srcType) {
        ArrayList<LayerType> targetTypes = new ArrayList<>();

        switch (srcType) {
            case Road: {
                targetTypes.add(LayerType.Road);
                targetTypes.add(LayerType.RailWay);
            }
            break;
            case Admin: {
                targetTypes.add(LayerType.Admin);
                targetTypes.add(LayerType.AdminBoundary);
                targetTypes.add(LayerType.AdminFlag);
            }
            break;
            case Background: {
                targetTypes.add(LayerType.LC);
                targetTypes.add(LayerType.LU);
                targetTypes.add(LayerType.CityModel);
            }
            break;
            case Poi: {
                targetTypes.add(LayerType.Poi);
            }
            break;
            case PoiHeatMap:{
                targetTypes.add(LayerType.SogouSearch);
                targetTypes.add(LayerType.MapbarSearch);
            }
            case EditHeatMap:{
                targetTypes.add(LayerType.PoiDayEditHeatmap);
                targetTypes.add(LayerType.PoiMonthEditHeatmap);
                targetTypes.add(LayerType.PoiCollectHeatmap);
            }
            break;
            case Dig: {
                targetTypes.add(LayerType.Construction);
                targetTypes.add(LayerType.Missingroad);
                targetTypes.add(LayerType.RestricDetail);
            }
            break;
            case Information: {
                targetTypes.add(LayerType.InfoPoi);
                targetTypes.add(LayerType.InfoRoad);
            }
            break;
            case Block:{
                targetTypes.add(LayerType.BlockHistory);
            }
            break;
            case Traffic:{
                targetTypes.add(LayerType.TrafficStatus);
            }
            break;
            case BaoTouTraffic:{
                targetTypes.add(LayerType.BaotouTrafficStatus);
            }
            break;
            case ZhuhaiAirShow:{
                targetTypes.add(LayerType.TrafficEvents);
                targetTypes.add(LayerType.Forecast);
            }
            break;
        }

        return targetTypes;
    }

    public static SourceType getSourceType(LayerType lyrType) {
        switch (lyrType) {
            case Road:
                return SourceType.Road;
            case RailWay:
                return SourceType.Road;
            case Admin:
                return SourceType.Admin;
            case AdminBoundary:
                return SourceType.Admin;
            case AdminFlag:
                return SourceType.Admin;
            case LU:
                return SourceType.Background;
            case LC:
                return SourceType.Background;
            case CityModel:
                return SourceType.Background;
            case Poi:
                return SourceType.Poi;
            case SogouSearch:
                return SourceType.PoiHeatMap;
            case MapbarSearch:
                return SourceType.PoiHeatMap;
            case PoiDayEditHeatmap:
                return SourceType.EditHeatMap;
            case PoiMonthEditHeatmap:
                return SourceType.EditHeatMap;
            case PoiCollectHeatmap:
                return SourceType.EditHeatMap;
            case Construction:
                return SourceType.Dig;
            case Missingroad:
                return SourceType.Dig;
            case RestricDetail:
                return SourceType.Dig;
            case InfoPoi:
                return SourceType.Information;
            case InfoRoad:
                return SourceType.Information;
            case BlockHistory:
                return SourceType.Block;
            case TrafficStatus:
                return SourceType.Traffic;
            case TrafficEvents:
                return SourceType.ZhuhaiAirShow;
            case Forecast:
                return SourceType.ZhuhaiAirShow;
        }

        return SourceType.Road;
    }
}
