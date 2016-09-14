package com.navinfo.mapspotter.process.storage.vectortile;

import com.navinfo.mapspotter.process.convert.WarehouseDataType;

/**
 * Created by SongHuiXing on 6/16 0016.
 */
public interface IPbfStorage {
    boolean open();

    byte[] getProtobuf(int z, int x, int y, WarehouseDataType.SourceType target);

    byte[] getProtobuf(int z, int x, int y, WarehouseDataType.LayerType target);

    void close();
}
