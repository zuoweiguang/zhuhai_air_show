package com.navinfo.mapspotter.foundation.util;

import com.mercator.MercatorProjection;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 墨卡托坐标与经纬度坐标转换类
 * Created by cuiliang on 2015/12/30.
 * Modified by songhuixing on 2016/01/15, change pixel from double to int
 */
public class MercatorUtil {

    private static final double R_MAJOR = 6378137.0;
    private static final double R_MINOR = 6356752.3142;
    private static final double originShift = 2 * Math.PI * R_MAJOR / 2.0;

    private int zoom = 14;
    private int tileSize = 256;
    private double initialResolution = 2 * Math.PI * R_MAJOR / tileSize;

    /**
     * 轨迹
     */
    public static final int TRACKPOINT_LEVEL = 12;
    public static final int TRACKPOINT_SIZE = 1024;
    public static final MercatorUtil TRACKPOINT_MERCATOR = new MercatorUtil(TRACKPOINT_SIZE, TRACKPOINT_LEVEL);

    /**
     * default ctor and the zoom is 14
     */
    public MercatorUtil() {
    }

    /**
     * @param level 传入墨卡托等级号
     */
    public MercatorUtil(int tileSize, int level) {
        this.tileSize = tileSize;
        this.zoom = level;

        this.initialResolution = 2 * Math.PI * R_MAJOR / this.tileSize;
    }

    private static final MercatorUtil defaultInstance = new MercatorUtil();
    public static MercatorUtil getDefaultInstance() {
        return defaultInstance;
    }

    /**
     * 格式化瓦片号字符串
     *
     * @param tCoord 瓦片行列
     * @return 瓦片号
     */
    public static String formatMCode(IntCoordinate tCoord) {
        return tCoord.x + "_" + tCoord.y;
    }

    /**
     * 解析瓦片号字符串
     *
     * @param mcode 瓦片号字符串
     * @return 瓦片行列号
     */
    public static IntCoordinate parseMCode(String mcode) {
        String[] splits = mcode.split("_");
        int tx = Integer.parseInt(splits[0]);
        int ty = Integer.parseInt(splits[1]);
        return new IntCoordinate(tx, ty);
    }

    /**
     * 计算分辨率
     *
     * @param zoom 缩放比例
     * @return 分辨率
     */
    public double resolution(int zoom) {
        return initialResolution / Math.pow(2, zoom);
    }

    /**
     * 根据分辨率计算最佳缩放
     *
     * @param resolution 分辨率
     * @return 缩放等级
     */
    public int bestZoom(float resolution)
    {
        double scaleFactor = Math.log(originShift * 2D / tileSize) / Math.log(2);

        return (int) Math.round(scaleFactor - Math.log(resolution) / Math.log(2));
    }

    /**
     * 坐标转瓦片号
     *
     * @param coord 经纬度坐标
     * @return 瓦片号
     */
    public String lonLat2MCode(Coordinate coord) {
        IntCoordinate tile = lonLat2Tile(coord);
        return formatMCode(tile);
    }

    public IntCoordinate lonLat2Tile(Coordinate coord) {
        Coordinate meters = lonLat2Meters(coord);
        IntCoordinate pixels = meters2Pixels(meters);
        return pixels2Tile(pixels);
    }

    public static String lonLat2MCode(Coordinate coord, int level) {
        double factor = Math.pow(2, level) / 2;

        int tx = (int) Math.ceil((coord.x / 180.0 + 1) * factor) - 1;

        double siny = Math.log(Math.tan((90 + coord.y) * Math.PI / 360.0)) / Math.PI;
        int ty = (int) Math.ceil((1 - siny) * factor) - 1;

        return formatMCode(new IntCoordinate(tx, ty));
    }

    public List<String> bound2MCode(Envelope bound) {

        List<String> mcodes = new ArrayList<>();

        IntCoordinate lt = lonLat2Tile(new Coordinate(bound.getMinX(), bound.getMaxY()));
        IntCoordinate rb = lonLat2Tile(new Coordinate(bound.getMaxX(), bound.getMinY()));

        for (int x = lt.x; x <= rb.x; x++) {
            for (int y = lt.y; y <= rb.y; y++) {

                mcodes.add(
                        formatMCode(new IntCoordinate(x, y))
                );
            }
        }

        return mcodes;
    }

    public List<String> bound2MCode(Envelope bound, int level) {

        List<String> mcodes = new ArrayList<>();

        long ltX = MercatorProjection.longitudeToTileX(bound.getMinX(), (byte)level);
        long ltY = MercatorProjection.latitudeToTileY(bound.getMaxY(), (byte)level);
        IntCoordinate lt = new IntCoordinate((int)ltX, (int)ltY);

        long rbX = MercatorProjection.longitudeToTileX(bound.getMaxX(), (byte)level);
        long rbY = MercatorProjection.latitudeToTileY(bound.getMinY(), (byte)level);
        IntCoordinate rb = new IntCoordinate((int)rbX, (int)rbY);

        for (int x = lt.x; x <= rb.x; x++) {
            for (int y = lt.y; y <= rb.y; y++) {

                mcodes.add(
                        formatMCode(new IntCoordinate(x, y))
                );
            }
        }

        return mcodes;
    }

    /**
     * 计算该坐标所在的祖先Tile号码
     *
     * @param coord      经纬度
     * @param generation 上溯的层级
     * @return
     * @author songhuixing
     */
    public String lonLat2AncestorMCode(Coordinate coord, int generation) {
        IntCoordinate thislevel = lonLat2Pixels(coord, this.zoom);

        double pixelsPerAncestor = Math.pow(2, generation);

        int px = (int) Math.ceil(thislevel.x / pixelsPerAncestor);
        int py = (int) Math.ceil(thislevel.y / pixelsPerAncestor);

        IntCoordinate ancestorTile = pixels2Tile(new IntCoordinate(px, py));

        return formatMCode(ancestorTile);
    }

    /**
     * 根据坐标计算扩圈墨卡托list
     *
     * @param coord   经纬度
     * @param ringNum 扩大圈数
     * @return
     */
    public List<String> lonLat2MCodeList(Coordinate coord, int ringNum) {
        List<String> mcodes = new ArrayList<>();

        IntCoordinate tile = lonLat2Tile(coord);
        int min = -ringNum;
        int max = ringNum + 1;
        String mcode;
        for (int i = min; i < max; i++) {
            for (int j = min; j < max; j++) {
                mcode = formatMCode(new IntCoordinate(
                        tile.x + i, tile.y + j
                ));
                mcodes.add(mcode);
            }
        }
        return mcodes;
    }

    /**
     * 瓦片号计算经纬度范围
     *
     * @param mcode 瓦片号
     * @param level 等级
     * @return Envelope
     */
    public static Envelope mercatorBound(String mcode, int level) {
        IntCoordinate tile = parseMCode(mcode);

        return mercatorBound(level, tile.x, tile.y);
    }

    public static Envelope mercatorBound(int z, int x, int y){
        double res = Math.pow(2, z) / 2.0;

        double minlon = (x / res - 1) * 180;
        double maxlon = ((x + 1) / res - 1) * 180;

        double my = 1 - y / res;
        double maxlat = Math.atan(Math.exp(my * Math.PI)) * 360 / Math.PI - 90;
        my = 1 - (y + 1) / res;
        double minlat = Math.atan(Math.exp(my * Math.PI)) * 360 / Math.PI - 90;

        return new Envelope(minlon, maxlon, minlat, maxlat);
    }
    public Envelope mercatorBound(String mcode) {
        return mercatorBound(mcode, zoom);
    }

    /**
     * 经纬度转墨卡托坐标
     *
     * @param coord 经纬度
     * @return 墨卡托坐标
     */
    public static Coordinate lonLat2Meters(Coordinate coord) {
        double x = coord.x * originShift / 180.0;
        double siny = Math.log(Math.tan((90 + coord.y) * Math.PI / 360.0)) / (Math.PI / 180.0);
        double y = siny * originShift / 180.0;
        return new Coordinate(x, y);
    }

    /**
     * 经纬度转墨卡托坐标
     *
     * @param worldCoords 经纬度坐标序列[lon, lat, lon, lat, ...]
     * @return 墨卡托坐标序列
     */
    public static double[] lonLat2Meters(double[] worldCoords) {
        double[] mercators = new double[worldCoords.length];
        Coordinate coord = new Coordinate();

        for (int i = 0; i < worldCoords.length / 2; i++) {
            coord.x = worldCoords[2 * i];
            coord.y = worldCoords[2 * i + 1];
            Coordinate mercator = lonLat2Meters(coord);
            mercators[2 * i] = mercator.x;
            mercators[2 * i + 1] = mercator.y;
        }

        return mercators;
    }

    /**
     * 墨卡托坐标转经纬度
     *
     * @param meters 墨卡托坐标
     * @return 经纬度
     */
    public static Coordinate meters2LonLat(Coordinate meters) {
        double lon = (meters.x / originShift) * 180.0;
        double sinLat = (meters.y / originShift) * 180.0;
        double lat = 180 / Math.PI * (2 * Math.atan(Math.exp(sinLat * Math.PI / 180.0)) - Math.PI / 2.0);
        return new Coordinate(lon, lat);
    }

    /**
     * 墨卡托坐标转像素坐标
     *
     * @param meters   墨卡托坐标
     * @param level 等级
     * @return 像素坐标
     */
    public IntCoordinate meters2Pixels(Coordinate meters, int level) {
        double res = resolution(level);

        int px = (int) Math.ceil((meters.x + originShift) / res) - 1;
        int py = (int) Math.ceil((originShift - meters.y) / res) - 1;

        return new IntCoordinate(px, py);
    }

    public IntCoordinate meters2Pixels(Coordinate meters) {
        return meters2Pixels(meters, zoom);
    }

    /**
     * 像素坐标转墨卡托坐标（左上角）
     *
     * @param pixels 像素坐标
     * @param level 等级
     * @return 墨卡托坐标
     */
    public Coordinate pixels2Meters(IntCoordinate pixels, int level) {
        double res = resolution(level);

        double mx = ((double)pixels.x + 0.5) * res - originShift;
        double my = originShift - ((double)pixels.y + 0.5) * res;

        return new Coordinate(mx, my);
    }

    public Coordinate pixels2Meters(IntCoordinate pixels) {
        return pixels2Meters(pixels, zoom);
    }

    /**
     * 经纬度转像素坐标
     *
     * @param coord 经纬度
     * @return 像素坐标
     */
    public IntCoordinate lonLat2Pixels(Coordinate coord, int level) {
        Coordinate meters = lonLat2Meters(coord);

        return meters2Pixels(meters, level);
    }
    public IntCoordinate lonLat2Pixels(Coordinate coord) {
        return lonLat2Pixels(coord, zoom);
    }

    public int[] lonLat2Pixels(double[] worldCoords, int level) {
        int[] pixels = new int[worldCoords.length];

        for (int i = 0; i < worldCoords.length / 2; i++) {
            IntCoordinate mercator = lonLat2Pixels(
                    new Coordinate(worldCoords[2 * i], worldCoords[2 * i + 1]),
                    level);
            pixels[2 * i] = mercator.x;
            pixels[2 * i + 1] = mercator.y;
        }

        return pixels;
    }

    public int[] lonLat2Pixels(double[] worldCoords) {
        int[] pixels = new int[worldCoords.length];

        for (int i = 0; i < worldCoords.length / 2; i++) {
            IntCoordinate mercator = lonLat2Pixels(
                    new Coordinate(worldCoords[2 * i], worldCoords[2 * i + 1]),
                    this.zoom);
            pixels[2 * i] = mercator.x;
            pixels[2 * i + 1] = mercator.y;
        }

        return pixels;
    }

    /**
     * 像素坐标转经纬度（左上角）
     * @param pixels 像素坐标
     * @param level 等级
     * @return 经纬度
     */
    public Coordinate pixels2LonLat(IntCoordinate pixels, int level) {
        Coordinate meters = pixels2Meters(pixels, level);
        return meters2LonLat(meters);
    }

    public Coordinate pixels2LonLat(IntCoordinate pixels) {
        return pixels2LonLat(pixels, zoom);
    }

    /**
     * 墨卡托坐标转瓦块号
     *
     * @param meters 墨卡托坐标
     * @param level 等级
     * @return 瓦块号码[列, 行]
     */
    public static IntCoordinate meters2Tile(Coordinate meters, int level) {
        int tx = (int) Math.ceil((meters.x / originShift + 1) * Math.pow(2, level) / 2D) - 1;
        int ty = (int) Math.ceil((1 - meters.y / originShift) * Math.pow(2, level) / 2D) - 1;

        return new IntCoordinate(tx, ty);
    }

    public IntCoordinate meters2Tile(Coordinate meters) {
        return meters2Tile(meters, zoom);
    }

    public String meters2MCode(Coordinate meters) {
        return formatMCode(meters2Tile(meters));
    }

    /**
     * 绝对像素坐标转瓦块号码
     *
     * @param pixels 像素坐标
     * @return 瓦块号码[列, 行]
     */
    public IntCoordinate pixels2Tile(IntCoordinate pixels) {
        int tx = (int) Math.ceil((pixels.x + 1) / (double) tileSize) - 1;
        int ty = (int) Math.ceil((pixels.y + 1) / (double) tileSize) - 1;

        return new IntCoordinate(tx, ty);
    }

    public String pixels2MCode(IntCoordinate pixels) {
        return formatMCode(pixels2Tile(pixels));
    }

    /**
     * 像素坐标转瓦片内坐标
     *
     * @param pixels 像素坐标
     * @return 瓦片内坐标
     */
    public IntCoordinate pixelsInTile(IntCoordinate pixels) {
        IntCoordinate tile = pixels2Tile(pixels);

        int ix = pixels.x - tile.x * tileSize;
        int iy = pixels.y - tile.y * tileSize;

        return new IntCoordinate(ix, iy);
    }


    /**
     * 瓦片内坐标转像素坐标
     *
     * @param coord 瓦片内坐标
     * @return 像素坐标
     */
    public IntCoordinate inTile2Pixels(IntCoordinate coord, IntCoordinate tile) {
        int px = coord.x + tile.x * tileSize;
        int py = coord.y + tile.y * tileSize;
        return new IntCoordinate(px, py);
    }

    public IntCoordinate inTile2Pixels(IntCoordinate pixels, String mcode) {
        IntCoordinate tile = parseMCode(mcode);
        return inTile2Pixels(pixels, tile);
    }

    public IntCoordinate inTile2Pixels(int x, int y, String mcode) {
        IntCoordinate pixels = new IntCoordinate(x, y);
        return inTile2Pixels(pixels, mcode);
    }

    /**
     * 经纬度计算瓦片内像素行列序号
     *
     * @param coord 经纬度坐标
     * @return 瓦片内行列序号
     */
    public IntCoordinate lonLat2InTile(Coordinate coord) {
        IntCoordinate pixels = lonLat2Pixels(coord);
        return pixelsInTile(pixels);
    }

    /**
     * 瓦片内像素坐标计算经纬度
     *
     * @param coord 瓦片内坐标
     * @param tile 瓦片号
     * @return 经纬度
     */
    public Coordinate inTile2LonLat(IntCoordinate coord, IntCoordinate tile, int level) {
        IntCoordinate pixels = inTile2Pixels(coord, tile);
        return pixels2LonLat(pixels, level);
    }
    public Coordinate inTile2LonLat(IntCoordinate coord, IntCoordinate tile) {
        return inTile2LonLat(coord, tile, zoom);
    }

    public static List<String> bounds2Tiles(Geometry geo, int level){
        Envelope envelope = geo.getEnvelopeInternal();

        Coordinate lt = new Coordinate(envelope.getMinX(), envelope.getMaxY());
        Coordinate rb = new Coordinate(envelope.getMaxX(), envelope.getMinY());

        return bounds2Tiles(lt, rb, level);
    }

    public static List<String> bounds2Tiles(Coordinate lt,
                                     Coordinate rb,
                                     int level){
        String minTile = MercatorUtil.lonLat2MCode(lt, level);
        int minx = Integer.parseInt(minTile.split("_")[0]);
        int miny = Integer.parseInt(minTile.split("_")[1]);

        String maxTile = MercatorUtil.lonLat2MCode(rb, level);
        int maxx = Integer.parseInt(maxTile.split("_")[0]);
        int maxy = Integer.parseInt(maxTile.split("_")[1]);

        List<String> tiles = new ArrayList<>();
        for (int j = minx; j <= maxx; j++) {
            for (int k = miny; k <= maxy; k++) {
                tiles.add(j+"_"+k);
            }
        }

        return tiles;
    }

    public static void main(String args[]){
        int minlevel = Integer.parseInt(args[0]);
        int maxlevel = Integer.parseInt(args[1]);

        double minx = Double.parseDouble(args[2]);
        double miny = Double.parseDouble(args[3]);
        double maxx = Double.parseDouble(args[4]);
        double maxy = Double.parseDouble(args[5]);

        Coordinate lt = new Coordinate(minx, maxy);
        Coordinate rb = new Coordinate(maxx, miny);

        String path = args[6];

        for (int i = minlevel; i <= maxlevel; i++) {
            MercatorUtil util = new MercatorUtil(4096, i);

            IntCoordinate min = util.lonLat2Tile(lt);
            IntCoordinate max = util.lonLat2Tile(rb);

            OutputStreamWriter out1 = null;
            try {
                out1 = new OutputStreamWriter(new FileOutputStream(path + "/tile_" + i));
                BufferedWriter writer = new BufferedWriter(out1);

                for (int j = min.x; j <= max.x; j++) {
                    for (int k = min.y; k <= max.y; k++) {
                        writer.write(i + "_" + j + "_" + k);
                        writer.newLine();
                    }
                }

                writer.flush();
                writer.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(null != out1) {
                    try {
                        out1.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
