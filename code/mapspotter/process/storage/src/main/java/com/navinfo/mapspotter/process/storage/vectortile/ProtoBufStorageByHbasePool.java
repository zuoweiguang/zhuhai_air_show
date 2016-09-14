package com.navinfo.mapspotter.process.storage.vectortile;

import com.navinfo.mapspotter.foundation.io.Hbase;
import com.navinfo.mapspotter.process.convert.WarehouseDataType;
import org.apache.hadoop.hbase.util.Bytes;
import org.hbase.async.GetRequest;
import org.hbase.async.HBaseClient;
import org.hbase.async.KeyValue;
import org.hbase.async.PutRequest;

import java.util.ArrayList;

/**
 * Created by SongHuiXing on 6/6 0006.
 */
public class ProtoBufStorageByHbasePool implements IPbfStorage {

    protected final String pbf_tableName;
    protected final byte[] pbf_family;

    private Hbase hbaseDb = null;

    private static HBaseClient client;

    public ProtoBufStorageByHbasePool(String zkHost,
                                      String table, String family) {
        client = new HBaseClient(zkHost);
        pbf_tableName = table;
        pbf_family = Bytes.toBytes(family);
    }

    private String getRowkey(int z, int x, int y) {
        return z + "_" + x + "_" + y;
    }

    @Override
    public boolean open() {
        return true;
    }

    @Override
    public byte[] getProtobuf(int z, int x, int y, WarehouseDataType.SourceType target) {
        String rowkey = getRowkey(z, x, y);

        GetRequest get = new GetRequest(pbf_tableName, rowkey);

        byte[] pbf_column = Bytes.toBytes(target.toString().toLowerCase());
        get = get.family(pbf_family).qualifier(pbf_column);

        try {
            ArrayList<KeyValue> list = client.get(get).joinUninterruptibly();
            if (list != null && list.size() > 0) {
                for (KeyValue kv : list) {
                    return kv.value();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public byte[] getProtobuf(int z, int x, int y, WarehouseDataType.LayerType target) {
        return new byte[0];
    }

    @Override
    public void close() {

    }

    public boolean insertProtobuf(int z, int x, int y, byte[] buf, String column) {
        byte[] rowkey = Bytes.toBytes(getRowkey(z, x, y));

        byte[] pbf_column = Bytes.toBytes(column);

        PutRequest put = new PutRequest(Bytes.toBytes(pbf_tableName), rowkey, pbf_family, pbf_column, buf);
        try {
            client.put(put).joinUninterruptibly();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    public static void main(String[] args) {
        ProtoBufStorageByHbasePool storage = new ProtoBufStorageByHbasePool("Master.Hadoop:2181", "Road_Vectortile", "Vectortile");
        byte[] r = storage.getProtobuf(10,840,385, WarehouseDataType.SourceType.Road);
        System.out.println(new String(r));

    }
}
