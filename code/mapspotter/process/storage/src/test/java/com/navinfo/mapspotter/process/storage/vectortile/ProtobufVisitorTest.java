package com.navinfo.mapspotter.process.storage.vectortile;

import com.navinfo.mapspotter.process.convert.WarehouseDataType;
import com.navinfo.mapspotter.process.storage.pool.MongoPool;
import com.navinfo.mapspotter.process.storage.pool.PostgisPool;
import com.vector.tile.VectorTileDecoder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * Created by SongHuiXing on 6/12 0012.
 */
public class ProtobufVisitorTest {
    private PostgisPool pgDb;
    private MongoPool mgDb;

    @Before
    public void setUp() throws Exception {
        pgDb = new PostgisPool("192.168.4.104", 5440,
                        "navinfo",
                        "postgres", "navinfo1!pg");

        mgDb = new MongoPool("192.168.4.128", 27017, "warehouse");
    }

    @After
    public void tearDown() throws Exception {
        pgDb.close();
    }

    @Test
    public void testGetRoad() throws Exception {
        MongoPool mp = new MongoPool("192.168.4.128", 27017, "warehouse");
        mp.setup();
        PostgisPool pp = new PostgisPool("192.168.4.104", 5440, "navinfo", "postgres", "navinfo1!pg");
        pp.setup();
        ProtobufVisitor visitor = new ProtobufVisitor(mp, pp);

        //byte[] pbf = visitor.getProtobuf(14, 2111, 974,WarehouseDataType.SourceType.Admin);
//        byte[] pbf = visitor.getProtobuf(14, 2111, 974,WarehouseDataType.SourceType.Poi);

        //construction_15_26092_13881
//        byte[] pbf = visitor.getStaticDigProtobuf(15, 26092, 13881, "construction");
//        System.out.println(new String(pbf).toString());

//        int minlevel = FilterReader.getMinLevel(WarehouseDataType.SourceType.Dig);
//        System.out.println(minlevel);
//        byte[] pbf = visitor.getInformation(14, 13894, 5968, WarehouseDataType.SourceType.Information, "");
        byte[] pbf = visitor.getProtobuf(10, 844, 386, WarehouseDataType.SourceType.Dig);
        System.out.println(new String(pbf));

        VectorTileDecoder vtd = new VectorTileDecoder();
        Iterable it = vtd.decode(pbf);
        Iterator i = it.iterator();
        while (i.hasNext()){
            Object obj = i.next();
            System.out.println(obj.toString());
        }

        assertNotNull(pbf);
    }
}