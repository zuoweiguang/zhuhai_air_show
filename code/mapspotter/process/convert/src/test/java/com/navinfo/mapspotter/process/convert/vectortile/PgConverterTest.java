package com.navinfo.mapspotter.process.convert.vectortile;

import com.mercator.MercatorProjection;
import com.mercator.TileUtils;
import com.navinfo.mapspotter.foundation.io.DataSource;
import com.navinfo.mapspotter.foundation.io.IOUtil;
import com.navinfo.mapspotter.foundation.io.PostGISDatabase;
import com.navinfo.mapspotter.foundation.util.IntCoordinate;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.navinfo.mapspotter.process.convert.WarehouseDataType;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by SongHuiXing on 6/7 0007.
 */
public class PgConverterTest {
    private PgConverter pgCreator;

    private GeometryFactory factory = new GeometryFactory();

    private MercatorUtil mercatorUtil = new MercatorUtil(4096, 13);

    private PostGISDatabase database;

    @Before
    public void setUp() throws Exception {
        database = (PostGISDatabase) DataSource.getDataSource(
                IOUtil.makePostGISParam("192.168.4.104", 5440,
                                        "navinfo",
                                        "postgres", "navinfo1!pg"));

        pgCreator = new PgConverter(database);
    }

    @After
    public void tearDown() throws Exception {
        database.close();
    }

    @Test
    public void testGetProtobuf() throws Exception {
        byte[] pbf = pgCreator.getProtobuf(16, 53960, 24829, WarehouseDataType.SourceType.Background);

        System.out.println(pbf.length);
    }

    @Test
    public void testConvertLine2VtGeo() throws Exception {
        WKTWriter wktWriter = new WKTWriter();

        Coordinate[] coords = new Coordinate[5];
        coords[0] = new Coordinate(116.2328, 39.23982);
        coords[1] = new Coordinate(116.2338, 39.23982);
        coords[2] = new Coordinate(116.2338, 39.23972);
        coords[3] = new Coordinate(116.2328, 39.23972);
        coords[4] = coords[0];

        Polygon box = factory.createPolygon(coords);

        System.out.print(wktWriter.write(box));
    }

    @Test
    public void testOther(){
        Coordinate coordinate0 = new Coordinate(113.28344,39.28261);
        Point pt = factory.createPoint(coordinate0);

        double px = MercatorProjection.longitudeToPixelX(113.28344, (byte)13);
        double py = MercatorProjection.latitudeToPixelY(39.28261, (byte)13);
        System.out.println("Pixel coord is "+ px + " : " + py);

        TileUtils.convert2Piexl(6673, 3122, 13, pt);

        System.out.println("Tile coord is "+ pt.getX() + " : " + pt.getY());

        Coordinate coordinate1 = new Coordinate(113.28344,39.28261);

        System.out.println(mercatorUtil.lonLat2MCode(coordinate1));

        IntCoordinate inTileCoord = mercatorUtil.lonLat2InTile(coordinate1);
        System.out.println("Another tile coord is "+ inTileCoord.x + " : " + inTileCoord.y);
    }

    @Test
    public void testString(){
        long t1 = System.currentTimeMillis();
        byte[] pbf = pgCreator.getProtobuf(16, 53960, 24829, WarehouseDataType.SourceType.Traffic);

        long t2 = System.currentTimeMillis();

        System.out.println(t2 - t1);
    }
}