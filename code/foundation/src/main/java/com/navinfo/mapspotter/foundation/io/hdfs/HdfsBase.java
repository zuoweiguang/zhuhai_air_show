package com.navinfo.mapspotter.foundation.io.hdfs;

import com.navinfo.mapspotter.foundation.io.Util;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by huanghai on 2016/1/4.
 */
public class HdfsBase {
    private static final Logger logger = Logger.getLogger(HdfsBase.class);

    /**
     * 创建文件
     *
     * @param conf 配置文件
     * @param dst  文件hdfs全路径
     * @throws IOException
     */
    public static boolean createFile(Configuration conf, String dst) throws IOException {
        logger.info("dst path : " + dst);
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            Path dstPath = new Path(dst);
            return fs.createNewFile(dstPath);
        } catch (IOException e) {
            logger.error("createFile() IOException -> " + e);
            throw e;
        } finally {
            Util.closeStream(fs);
        }
    }


    /**
     * 文件重命名
     *
     * @param conf    配置文件
     * @param oldfile 原文件hdfs全路径
     * @param newfile 目标文件hdfs全路径
     * @throws IOException
     */
    public static boolean rename(Configuration conf, String oldfile, String newfile) throws IOException {
        logger.info("oldfile : " + oldfile + " newfile : " + newfile);
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            Path oldPath = new Path(oldfile);
            Path newPath = new Path(newfile);
            boolean isok = fs.rename(oldPath, newPath);
            if (isok) {
                logger.info("rename success.");
                return true;
            } else {
                logger.error("rename failure.");
                return false;
            }
        } catch (IOException e) {
            logger.error("rename() IOException -> " + e);
            throw e;
        } finally {
            Util.closeStream(fs);
        }
    }

    /**
     * 删除文件或者文件夹
     *
     * @param conf      配置文件
     * @param filePath  目标文件hdfs全路径
     * @param recursive 是否递归删除
     * @throws IOException
     */
    public static boolean delete(Configuration conf, String filePath, boolean recursive) throws IOException {
        logger.info("filePath : " + filePath + " recursive : " + recursive);
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            Path path = new Path(filePath);
            boolean isok = fs.delete(path, recursive);
            if (isok) {
                logger.info("delete success.");
                return true;
            } else {
                logger.error("delete failure.");
                return false;
            }
        } catch (IOException e) {
            logger.error("delete() IOException -> " + e);
            throw e;
        } finally {
            Util.closeStream(fs);
        }
    }

    /**
     * 创建目录
     *
     * @param conf 配置文件
     * @param path 目录hdfs路径
     */
    public static boolean mkdir(Configuration conf, String path) throws IOException {
        logger.info("path : " + path);
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            Path srcPath = new Path(path);
            boolean isok = fs.mkdirs(srcPath);
            if (isok) {
                logger.info("mkdir success.");
                return true;
            } else {
                logger.error("mkdir failure.");
                return false;
            }
        } catch (IOException e) {
            logger.error("mkdir() IOException -> " + e);
            throw e;
        } finally {
            Util.closeStream(fs);
        }
    }

    /**
     * 获取文件输出流
     *
     * @param conf     配置文件
     * @param filePath 目标文件hdfs全路径
     * @return
     */
    public static OutputStream getHdfsOutputStream(Configuration conf, String filePath) throws IOException {
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            Path path = new Path(filePath);
            return fs.create(path);
        } catch (IOException e) {
            logger.error("getHdfsOutputStream IOException -> " + e);
            throw e;
        } finally {
            Util.closeStream(fs);
        }
    }
}
