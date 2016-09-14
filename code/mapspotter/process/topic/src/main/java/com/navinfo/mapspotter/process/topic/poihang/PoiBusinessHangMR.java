package com.navinfo.mapspotter.process.topic.poihang;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.navinfo.mapspotter.foundation.model.oldPoiHang.BusinessPoi;
import com.navinfo.mapspotter.foundation.model.oldPoiHang.FastSource;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.navinfo.mapspotter.foundation.util.StringUtil;
import com.navinfo.mapspotter.foundation.util.XmlUtil;
import com.vividsolutions.jts.geom.Coordinate;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * Created by cuiliang on 2016/2/20.
 */
public class PoiBusinessHangMR {

    public static class MyMapper extends Mapper<LongWritable, Text, Text, Text> {
        private static MercatorUtil mkt;
        private static String targetKindCode;

        public void setup(Context context) {
            mkt = new MercatorUtil(256, 14);
            String cpName = context.getConfiguration().get("cpName");
            Constants.initMap();
            targetKindCode = Constants.targetKindCodeMap.get(cpName);
        }

        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            List<String> s = StringUtil.split(value.toString(),
                    Constants.MR_MAIN_SEPARATOR);

            if (s.get(0) != null && s.get(0).equals("0p")) {
                FastSource fastSource = (FastSource) JSONObject.toJavaObject(
                        JSONObject.parseObject(s.get(2)), FastSource.class);

                if (fastSource.getY() != null && fastSource.getX() != null
                        && !fastSource.getY().equals("")
                        && !fastSource.getX().equals("")) {
                    List<String> rowKeys;
                    if (!fastSource.getGcoding().equals(
                            Constants.IS_GEOCODING)) {
                        rowKeys = mkt.lonLat2MCodeList(new Coordinate(
                                fastSource.getX(),
                                fastSource.getY()), 1);
                    } else {
                        rowKeys = mkt.lonLat2MCodeList(new Coordinate(
                                fastSource.getX(),
                                fastSource.getY()), 1);
                    }

                    for (String mktKey : rowKeys) {
                        context.write(new Text(mktKey), value);
                    }
                }
            }

            if (s.get(0) != null && s.get(0).equals("p")) {
                BusinessPoi businessPoi;
                businessPoi = JSONObject.toJavaObject(
                        JSONObject.parseObject(s.get(1)), BusinessPoi.class);
                if ((targetKindCode != null)
                        && (!targetKindCode.equals(""))
                        && (!targetKindCode.contains(businessPoi.getKindCode())))
                    return;

                if (businessPoi != null) {
                    String mktKey = mkt.lonLat2MCode(new Coordinate(
                            businessPoi.getX(),
                            businessPoi.getY()
                    ));

                    context.write(new Text(mktKey), value);
                }

            }
        }
    }

    public static class MyReducer extends Reducer<Text, Text, Text, Text> {
        private static Map<String, Double> map;
        private String cpName;

        protected void setup(Context context) throws IOException,
                InterruptedException {
            // 加载每批数据源自身的相似度系数
            String similarityJson = context.getConfiguration().get(
                    "similarityConfJson");
            SimilarityConstant.initSimilarityMap(similarityJson);
            cpName = context.getConfiguration().get("cpName");
            map = SimilarityConstant.similarityMap.get(cpName);
        }

        ;

        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

            List<BusinessPoi> fusePois = new ArrayList<BusinessPoi>();
            List<FastSource> poiSources = new ArrayList<FastSource>();

            try {
                for (Text val : values) {

                    List<String> s = StringUtil.split(val.toString(),
                            Constants.MR_MAIN_SEPARATOR);

                    // poi数据
                    if (s.get(0) != null && s.get(0).equals("p")) {
                        BusinessPoi fusePoi = JSONObject.toJavaObject(
                                JSONObject.parseObject(s.get(1)),
                                BusinessPoi.class);
                        fusePois.add(fusePoi);
                    }

                    // pid 对应不上的数据（source中的数据）
                    if (s.get(0) != null && s.get(0).equals("0p")) {
                        FastSource source = JSONObject.toJavaObject(
                                JSONObject.parseObject(s.get(2)),
                                FastSource.class);
                        poiSources.add(source);
                    }
                }

                if (poiSources.size() <= 0)
                    return;

                for (FastSource s : poiSources) {

                    //String cpName = s.makeCpName();
                    if (cpName == null || "".equals(cpName))
                        cpName = Constants.BAIDU_CP;
                    Map<String, Double> map = SimilarityConstant.similarityMap
                            .get(cpName);
                    if (map == null) {
                        map = SimilarityConstant.similarityMap
                                .get(Constants.BAIDU_CP);
                    }
                    BusinessPoi bestSimilarityPoi = null;
                    double r = 0.0, t;
                    for (BusinessPoi p : fusePois) {
                        t = LevenshteinExtend.getSimilarityRatioModify(s, p,
                                map);
                        if (t > r) {
                            bestSimilarityPoi = p;
                            r = t;
                        }

                    }
                    BusinessPoi fusePoi = new BusinessPoi();
                    if (bestSimilarityPoi != null) {
                        fusePoi = bestSimilarityPoi;
                    }
                    String tab = "\t";
                    double threshold = map.get("hangBorderLine").doubleValue();
                    if (r >= threshold) {

                        String show = bestSimilarityPoi.getPid() + tab +
                                bestSimilarityPoi.getAddr().replace("\r\n", "").replace("\n", "") + tab +
                                bestSimilarityPoi.getName().replace("\r\n", "").replace("\n", "") + tab +
                                bestSimilarityPoi.getKindCode() + tab +
                                bestSimilarityPoi.getAdminCode() + tab +
                                bestSimilarityPoi.getTel() + tab +
                                bestSimilarityPoi.getPostCode() + tab +
                                bestSimilarityPoi.getX() + tab +
                                bestSimilarityPoi.getY() + tab +
                                s.getAddr() + tab +
                                s.getName() + tab +
                                s.getX() + tab +
                                s.getY() + tab +
                                s.getRemark();

                        String keyTemp = "1b"
                                + Constants.MR_MAIN_SEPARATOR
                                + "businessHang"
                                + Constants.MR_MAIN_SEPARATOR
                                + s.getUuid()
                                + Constants.MR_MAIN_SEPARATOR
                                + show
                                + Constants.MR_MAIN_SEPARATOR + r;
                        context.write(new Text(keyTemp), new Text(""));

                    } else {

                        String show = "" + tab +
                                "" + tab +
                                "" + tab +
                                "" + tab +
                                "" + tab +
                                "" + tab +
                                "" + tab +
                                "" + tab +
                                "" + tab +
                                s.getAddr() + tab +
                                s.getName() + tab +
                                s.getX() + tab +
                                s.getY() + tab +
                                s.getRemark();


                        String keyTemp = "0b"
                                + Constants.MR_MAIN_SEPARATOR
                                + "businessHang"
                                + Constants.MR_MAIN_SEPARATOR
                                + s.getUuid()
                                + Constants.MR_MAIN_SEPARATOR
                                + show
                                + Constants.MR_MAIN_SEPARATOR + r;
                        context.write(new Text(keyTemp), new Text(""));
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            fusePois.clear();
            poiSources.clear();
        }
    }

    public static void main(String[] args) throws Exception {
        String cpname = args[0];
        String cpdate = args[1];
        Configuration conf = new Configuration();
        String similarityConfPath = "/fusion/similarityXML/";

        String similarityConfJson = XmlUtil.parseXMLtoJson(similarityConfPath);
        conf.set("similarityConfJson", similarityConfJson);
        conf.set("cpName", "dealership");
        Job job = new Job(conf, "PoiBusinessHangMR");

        job.setJarByClass(PoiBusinessHangMR.class);
        job.setMapperClass(PoiBusinessHangMR.MyMapper.class);
        job.setReducerClass(PoiBusinessHangMR.MyReducer.class);
        job.setNumReduceTasks(8);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(
                "hdfs://Master.Hadoop:9000/mapreduce/workspace/" + cpname + "/" + cpdate + "/pidHang/output"));// hdfs://namenode01:9000/mapreduce/lsTest/readpoi/output
        FileInputFormat.addInputPath(job, new Path(
                "hdfs://Master.Hadoop:9000/mapreduce/workspace/" + cpname + "/" + cpdate + "/poi/output"));// hdfs://namenode01:9000/mapreduce/lsTest/output
        FileOutputFormat.setOutputPath(job, new Path(
                "hdfs://Master.Hadoop:9000/mapreduce/workspace/" + cpname + "/" + cpdate + "/businessHang/output"));
        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }

    public static Job poiBusinessHangModifyMRJob(String[] args)
            throws Exception {
        String similarityConfJson = args[0];
        String cpDate = args[1];
        String cpName = args[2];
        String poiPidOutput = args[3];
        String poiBoxOutput = args[4];
        String busenessOutput = args[5];
        Configuration confBusinessHang = new Configuration();
        confBusinessHang.set("similarityConfJson", similarityConfJson);
        confBusinessHang.set("cpName", cpName);
        Job jobBusinessHang = new Job(confBusinessHang,
                "5-PoiBusinessHangMR_datax_" + cpDate);
        jobBusinessHang.setJarByClass(PoiBusinessHangMR.class);
        jobBusinessHang.setMapperClass(PoiBusinessHangMR.MyMapper.class);
        jobBusinessHang.setReducerClass(PoiBusinessHangMR.MyReducer.class);
        jobBusinessHang.setMapOutputKeyClass(Text.class);
        jobBusinessHang.setMapOutputValueClass(Text.class);
        jobBusinessHang.setNumReduceTasks(8);
        FileInputFormat.addInputPath(jobBusinessHang, new Path(poiPidOutput));
        FileInputFormat.addInputPath(jobBusinessHang, new Path(poiBoxOutput));
        FileOutputFormat.setOutputPath(jobBusinessHang,
                new Path(busenessOutput));
        return jobBusinessHang;
    }

}
