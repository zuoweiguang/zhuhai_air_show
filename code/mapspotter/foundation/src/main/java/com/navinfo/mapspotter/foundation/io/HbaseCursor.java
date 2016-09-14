package com.navinfo.mapspotter.foundation.io;

import com.navinfo.mapspotter.foundation.util.Logger;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HBase表遍历指针
 *
 * Created by gaojian on 2016/2/26.
 */
public class HbaseCursor implements Cursor {
    private static final Logger logger = Logger.getLogger(HbaseCursor.class);

    private Table table = null;
    private ResultScanner rs = null;
    private Result result = null;

    protected HbaseCursor(Table table, ResultScanner rs) {
        this.table = table;
        this.rs = rs;
        result = null;
    }

    protected HbaseCursor(Result result) {
        this.result = result;
    }

    @Override
    public boolean next() {
        if (rs == null) {
            throw new NullPointerException();
        }

        try {
            result = rs.next();
        } catch (Exception e) {
            logger.error(e);
            result = null;
        }

        return (result != null);
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    public String getRowkey() {
        byte[] rowkey = result.getRow();
        if (rowkey == null) return null;
        return Bytes.toString(rowkey);
    }

    public Map< String, List<String> > getColumns() {
        HashMap< String, List<String> > returnMap = new HashMap<>();

        for (Cell cell : result.rawCells()) {
            String family = Bytes.toString(CellUtil.cloneFamily(cell));
            String qualifier = Bytes.toString(CellUtil.cloneQualifier(cell));
            if (returnMap.containsKey(family)) {
                returnMap.get(family).add(qualifier);
            } else {
                List<String> list = new ArrayList<>();
                list.add(qualifier);
                returnMap.put(family, list);
            }
        }

        return returnMap;
    }

    public boolean hasColumn(String family, String qualifier) {
        return result.containsColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier));
    }

    /**
     * 获取指定列的值
     *
     * @param field 列族和列名之间以冒号分隔
     * @return
     * @throws Exception
     */
    @Override
    public Object get(String field) throws Exception {
        if (field.equalsIgnoreCase(":rowkey")) {
            return Bytes.toString(result.getRow());
        }

        String[] names = field.split(":");
        return get(names[0], names[1]);
    }

    public byte[] get(String family, String qualifier) {
        return result.getValue(Bytes.toBytes(family), Bytes.toBytes(qualifier));
    }

    public String getString(String family, String qualifier) {
        byte[] value = get(family, qualifier);
        if (value == null) return null;
        return Bytes.toString(value);
    }

    @Override
    public void close() throws Exception {
        IOUtil.closeStream(rs);
        rs = null;
        IOUtil.closeStream(table);
        table = null;
    }
}
