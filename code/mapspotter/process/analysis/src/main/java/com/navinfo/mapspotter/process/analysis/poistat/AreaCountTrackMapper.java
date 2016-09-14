package com.navinfo.mapspotter.process.analysis.poistat;

import com.navinfo.mapspotter.foundation.util.*;
import com.vividsolutions.jts.geom.*;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

import java.io.IOException;
import java.util.*;

/**
 * 轨迹按Block统计
 * Created by gaojian on 2016/3/8.
 */
public class AreaCountTrackMapper extends TableMapper<Text, IntWritable> {
    private static final Logger logger = Logger.getLogger(AreaCountTrackMapper.class);
    private BlocksAnalysis blocksAnalysis = null;
    Set<String> tiles = null;

    private byte[] family;
    private byte[] qualifier;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        //String strSql = PropertiesUtil.getValue("AreaCount.sql");
        blocksAnalysis = new BlocksAnalysis();
        //blocksAnalysis.Initialize(PropertiesUtil.getValue("AreaCount.host") , PropertiesUtil.getValue("AreaCount.db"),
        //        PropertiesUtil.getValue("AreaCount.user") , PropertiesUtil.getValue("AreaCount.password") ,
        //        PropertiesUtil.getValue("AreaCount.port"));
        //blocksAnalysis.PrepareRtree(strSql);
        String blockfile = context.getConfiguration().get("blockfile");
        blocksAnalysis.prepareRTree_Json(blockfile , 1);

        ProvinceUtil provinceUtil = new ProvinceUtil();
        String meshlist = context.getConfiguration().get("meshlist");
        provinceUtil.initProvinceMeshes(meshlist , 1);
        tiles = new HashSet<>();
        tiles.addAll(provinceUtil.provinceTiles("北京市", MercatorUtil.TRACKPOINT_LEVEL));
        tiles.addAll(provinceUtil.provinceTiles("江苏省", MercatorUtil.TRACKPOINT_LEVEL));
        tiles.addAll(provinceUtil.provinceTiles("青海省", MercatorUtil.TRACKPOINT_LEVEL));
        tiles.addAll(provinceUtil.provinceTiles("宁夏回族自治区", MercatorUtil.TRACKPOINT_LEVEL));

        family = Bytes.toBytes(context.getConfiguration().get("family"));
        qualifier = Bytes.toBytes(context.getConfiguration().get("qualifier"));
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {

    }

    @Override
    protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {
        String rowkey = Bytes.toString(value.getRow());
        String tile = StringUtil.reverse(rowkey);
        if (!tiles.contains(tile)) return;

        String area = null;
        Envelope bound = MercatorUtil.TRACKPOINT_MERCATOR.mercatorBound(tile);
        Geometry geometry = GeoUtil.createPolygon(bound);
        List<BlockInfo> rlistb = blocksAnalysis.Contains(bound , geometry);
        if (!rlistb.isEmpty()){
            area = rlistb.get(0).blockid;
        }

        IntCoordinate tileCoord = MercatorUtil.parseMCode(tile);

        int sum = 0;
        Integer[][] matrix = MatrixUtil.deserializeMatrix(value.getValue(family, qualifier), true);
        for (int x = 0; x < MercatorUtil.TRACKPOINT_SIZE; ++x) {
            for (int y = 0; y < MercatorUtil.TRACKPOINT_SIZE; ++y) {
                if (matrix[y][x] != null && matrix[y][x] > 0) {
                    int count = matrix[y][x];
                    sum += count;

                    if (area == null) {
                        Coordinate lonlat = MercatorUtil.TRACKPOINT_MERCATOR.inTile2LonLat(new IntCoordinate(x, y), tileCoord);
                        Geometry point = GeoUtil.createPoint(lonlat.x , lonlat.y);
                        Coordinate coor = point.getCoordinate();
                        List<BlockInfo> rlist = blocksAnalysis.Contains(coor , point);
                        if (!rlist.isEmpty()){
                            context.write(new Text(rlist.get(0).getBlockid()), new IntWritable(count));
                        }
                    }
                }
            }
        }

        if (area != null) {
            context.write(new Text(area), new IntWritable(sum));
        }

    }
}
