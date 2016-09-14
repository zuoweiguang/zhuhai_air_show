package com.navinfo.mapspotter.process.analysis.poistat;

import com.navinfo.mapspotter.foundation.model.AdminBlock;
import com.navinfo.mapspotter.foundation.model.PoiKind;
import com.navinfo.mapspotter.foundation.model.PoiStatResult;
import com.navinfo.mapspotter.foundation.util.StringUtil;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;

import java.io.IOException;

/**
 * Poi下挂后分析
 *
 * Created by gaojian on 2016/2/2.
 */
public class PoiAnalysisReducer extends TableReducer<Text, Text, ImmutableBytesWritable> {
    private PoiKind poiKind = null;
    private AreaAnalysis areaAnalysis = null;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);

        // 初始化POI kind
        poiKind = new PoiKind();
        poiKind.init();

        // 初始化区域分析类
        areaAnalysis = new AreaAnalysis();
        areaAnalysis.initialize();
        areaAnalysis.prepareAreaMap();
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        super.cleanup(context);

        areaAnalysis.destroy();
    }

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {
        PoiStatResult result = null;

        // 同一POI求和
        int count = 0;
        for (Text value : values) {
            result = PoiStatResult.parse(value.toString());
            count = count + result.getCount();
        }
        result.setCount(count);

        if (!StringUtil.isEmpty(result.getKind())) {
            // kind不为空，则给大分类中分类赋值
            result.setKindName(poiKind.getKindName(result.getKind()));
            result.setKindMed(poiKind.getMediumId(result.getKind()));
            result.setKindTop(poiKind.getTopId(result.getKindMed()));
        }

        if (!StringUtil.isEmpty(result.getAdmin())) {
            // 区域代码不为空，则给省市赋值
            AdminBlock adminBlock = areaAnalysis.getAreaInfo(result.getAdmin());
            if (adminBlock != null) {
                result.setAdminName(adminBlock.getTown());
                result.setCity(adminBlock.getCity());
                result.setProvince(adminBlock.getProvince());
            }
        }

        // 输出
        String rowkey = "";
        Put put = new Put(Bytes.toBytes(rowkey));
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("pid"), Bytes.toBytes(result.getPid()));
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("lon"), Bytes.toBytes(result.getPid()));
        context.write(new ImmutableBytesWritable(Bytes.toBytes(rowkey)), put);
    }
}
