package com.navinfo.mapspotter.process.storage.vectortile;

import com.navinfo.mapspotter.process.convert.WarehouseDataType;
import com.navinfo.mapspotter.process.convert.vectortile.*;
import com.navinfo.mapspotter.process.storage.pool.MongoPool;
import com.navinfo.mapspotter.process.storage.pool.PostgisPool;

/**
 * Vector tile的Protobuf的查询器
 * Created by SongHuiXing on 6/7 0007.
 */
public class ProtobufVisitor {
    private static final boolean INSERT_NEWPBF = false;

    private final InformationConverter mongoConvert;

    private final InformationConverter pgConvert;

    public ProtobufVisitor(MongoPool mongoPool, PostgisPool pgPool){
        mongoConvert = new MgConverter(mongoPool.getMongo());

        pgConvert = new PgConverter(pgPool.getPgDatabase());
    }

//    private IPbfStorage hbaseSorage =
//                            new ProtoBufStorageByHbasePool("Master.Hadoop:2181,Slave3.Hadoop:2181",
//                                                            "Road_Vectortile",
//                                                            "Vectortile");

    private IPbfStorage redisStorage = new RedisInfoStorage("192.168.4.104", 6379, "navinfo1!rd");

    public byte[] getProtobuf(int z, int x, int y, WarehouseDataType.SourceType type){
        if(type == WarehouseDataType.SourceType.Poi
                || type == WarehouseDataType.SourceType.Dig){
            return runtimeCreatePbf(z, x, y, type, mongoConvert);
        } else if(WarehouseDataType.SourceType.Traffic == type){
            return runtimeCreatePbf(z, x, y, type, pgConvert);
        } else {
            return getStaticProtobuf(z, x, y, type);
        }
    }

    public byte[] getPoiHeatMapProtobuf(int z, int x, int y, WarehouseDataType.SourceType type){
        if(z < 3 || z > 17)
            return null;
        byte[] pbf = redisStorage.getProtobuf(z, x, y, type);

        return pbf;
    }

    private byte[] getStaticProtobuf(int z, int x, int y, WarehouseDataType.SourceType type){
        int minlevel = FilterReader.getMinLevel(type);

        //不提供特别小的等级与大于16级的Protobuf
        if(z < minlevel || z > 17)
            return null;

        byte[] pbf = null;

        try {
            if(z < 17) {
                pbf = redisStorage.getProtobuf(z, x, y, type);
            } else {
                if(type != WarehouseDataType.SourceType.Dig) {
                    pbf = runtimeCreatePbf(z, x, y, type, pgConvert);
                } else {
                    pbf = runtimeCreatePbf(z, x, y, type, mongoConvert);
                }
            }
        } finally {
//            hbaseSorage.close();
        }

        return pbf;
    }

    private static byte[] runtimeCreatePbf(int z, int x, int y,
                                    WarehouseDataType.SourceType type,
                                    InformationConverter convertor){
        byte[] pbf = null;

        try {
            pbf = convertor.getProtobuf(z, x, y, type);
        } finally {
//            convertor.close();
        }

        return pbf;
    }

    public byte[] getInformation(int z, int x, int y, WarehouseDataType.SourceType srcType, String condition) {
        int minlevel = FilterReader.getMinLevel(srcType);
        //只提供14级~17级的Protobuf
        if(z < minlevel || z > 17)
            return null;

        byte[] pbf = null;

        try {
            pbf = mongoConvert.getProtobuf(z, x, y, srcType, condition);
        } finally {
//            convertor.close();
        }

        return pbf;

    }


    @Override
    protected void finalize() throws Throwable {
        redisStorage.close();
    }

    public static void main(String[] args){

    }
}
