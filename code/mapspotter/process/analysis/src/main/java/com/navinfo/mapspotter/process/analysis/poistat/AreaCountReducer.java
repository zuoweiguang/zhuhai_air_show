package com.navinfo.mapspotter.process.analysis.poistat;

import com.navinfo.mapspotter.foundation.model.AdminBlock;
import com.navinfo.mapspotter.foundation.util.PropertiesUtil;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * 按区域几何统计Reducer类
 *
 * Created by gaojian on 2016/1/25.
 */
public class AreaCountReducer extends Reducer<Text, IntWritable, NullWritable, Text> {
    private BlocksAnalysis blocksAnalysis = null;
    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);

        // 初始化区域分析类
        //String strSql= PropertiesUtil.getValue("AreaCount.sql");
        blocksAnalysis = new BlocksAnalysis();
        //blocksAnalysis.Initialize(PropertiesUtil.getValue("AreaCount.host") , PropertiesUtil.getValue("AreaCount.db"),
        //        PropertiesUtil.getValue("AreaCount.user") , PropertiesUtil.getValue("AreaCount.password") ,
        //        PropertiesUtil.getValue("AreaCount.port"));
        //blocksAnalysis.PrepareBlockInfoMap(strSql);
        String blockfile = context.getConfiguration().get("blockfile");
        blocksAnalysis.prepareMap_Json(blockfile , 1);
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        super.cleanup(context);
    }

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context)
            throws IOException, InterruptedException {
        String areaId = key.toString();

        // 同一区域求和
        int count = 0;
        for (IntWritable value : values) {
            count = count + value.get();
        }
        StringBuilder stringBuilder = new StringBuilder();
        BlockInfo blockInfo = blocksAnalysis.GetBlockInfo(areaId);
        if (blockInfo != null){
            stringBuilder.append(blockInfo.getProvince() + "\t");
            stringBuilder.append(blockInfo.getCity() + "\t");
            stringBuilder.append(blockInfo.getCounty() + "\t");
            stringBuilder.append(blockInfo.getArea() + "\t");
            stringBuilder.append(blockInfo.getBlockid() + "\t");
            stringBuilder.append(count);
        }
        context.write(NullWritable.get() , new Text(stringBuilder.toString()));
    }
}
