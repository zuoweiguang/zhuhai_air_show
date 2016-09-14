package com.navinfo.mapspotter.process.storage.vectortile;

import com.navinfo.mapspotter.foundation.io.DataSource;
import com.navinfo.mapspotter.foundation.io.Hbase;
import com.navinfo.mapspotter.foundation.io.IOUtil;
import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import com.navinfo.mapspotter.process.convert.WarehouseDataType;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * Created by SongHuiXing on 6/6 0006.
 */
public class ProtoBufStorage implements IPbfStorage, AutoCloseable {

    protected final DataSourceParams hbaseParams;

    protected final String pbfTableName;
    protected final byte[] pbfColfamily;

    private Hbase hbaseDb = null;

    public ProtoBufStorage(String zkHost,
                           String table, String colfamily){
        hbaseParams = IOUtil.makeHBaseParam(zkHost);

        pbfTableName = table;
        pbfColfamily = Bytes.toBytes(colfamily);
    }

    @Override
    public boolean open(){
        hbaseDb = (Hbase) DataSource.getDataSource(hbaseParams);

        if(null == hbaseDb)
            return false;

        return true;
    }

    @Override
    public void close(){
        if(null != hbaseDb){
            hbaseDb.close();
        }
    }

    private String getRowkey(int z, int x, int y){
        return z + "_" + x + "_" + y;
    }

    public Put writeProtobuf2Put(int z, int x, int y, byte[] buf, String column){
        Put pt = new Put(Bytes.toBytes(getRowkey(z, x, y)));

        byte[] pbfColumn = Bytes.toBytes(column);

        pt.addColumn(pbfColfamily,
                     pbfColumn,
                     buf);

        return pt;
    }

    @Override
    public byte[] getProtobuf(int z, int x, int y, WarehouseDataType.SourceType target){
        String rowkey = getRowkey(z, x, y);

        byte[] pbfColumn = Bytes.toBytes(target.toString().toLowerCase());

        try(Table table = hbaseDb.getTable(pbfTableName)){
            Get gt = new Get(Bytes.toBytes(rowkey));

            gt.addColumn(pbfColfamily, pbfColumn);

            Result re = table.get(gt);

            if(re.isEmpty())
                return null;

            return re.getValue(pbfColfamily, pbfColumn);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public byte[] getProtobuf(int z, int x, int y, WarehouseDataType.LayerType target){
        return new byte[0];
    }

    public boolean insertProtobuf(int z, int x, int y, byte[] buf, String column){
        try(Table table = hbaseDb.getTable(pbfTableName)) {
            Put pt = writeProtobuf2Put(z, x, y, buf, column);

            table.put(pt);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
