package com.navinfo.mapspotter.foundation.io;

import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import com.navinfo.mapspotter.foundation.util.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * HDFS文件操作类
 *
 * Created by huanghai on 2016/1/4.
 * modified by gaojian 20160203 增加非静态方法，将Configuration和FileSystem作为类成员
 */
public class Hdfs extends DataSource {
    private static final Logger logger = Logger.getLogger(Hdfs.class);

    private Configuration conf = null;
    private FileSystem fs = null;

    protected Hdfs() {
        super();
    }

    @Override
    protected int open(DataSourceParams params) {
        try {
            conf = new Configuration();
            fs = FileSystem.get(conf);
            return 0;
        } catch (Exception e) {
            logger.error(e);
            return -1;
        }
    }

    @Override
    public void close() {
        IOUtil.closeStream(fs);
    }

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
            IOUtil.closeStream(fs);
        }
    }
    public boolean createFile(String dst) {
        try {
            return fs.createNewFile(new Path(dst));
        } catch (IOException e) {
            logger.error("createFile() IOException -> " + e);
            return false;
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
            IOUtil.closeStream(fs);
        }
    }
    public boolean rename(String oldfile, String newfile) {
        try {
            return fs.rename(new Path(oldfile), new Path(newfile));
        } catch (IOException e) {
            logger.error("rename() IOException -> " + e);
            return false;
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
            IOUtil.closeStream(fs);
        }
    }
    public boolean delete(String file, boolean recursive) {
        try {
            return fs.delete(new Path(file), recursive);
        } catch (IOException e) {
            logger.error("delete() IOException -> " + e);
            return false;
        }
    }
    public boolean delete(String file) {
        return delete(file, false);
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
            IOUtil.closeStream(fs);
        }
    }
    public boolean mkdir(String dir) {
        try {
            return fs.mkdirs(new Path(dir));
        } catch (IOException e) {
            logger.error("mkdir() IOException -> " + e);
            return false;
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
            IOUtil.closeStream(fs);
        }
    }
    public OutputStream getHdfsOutputStream(String filePath) {
        try {
            return fs.create(new Path(filePath));
        } catch (IOException e) {
            logger.error("getHdfsOutputStream IOException -> " + e);
            return null;
        }
    }

    /**
     *
     * @param inputPath 输入hdfs路径
     * @param fs        文件系统
     * @param pathList  输入/输出 存入inputPath路径下文件全路径
     * @param prefix    前缀过滤
     * @throws IOException
     */
    public static void addInputPath(Path inputPath, FileSystem fs, List<Path> pathList, String prefix) throws IOException {
        if (fs.isDirectory(inputPath)) {
            FileStatus[] listStatus = fs.listStatus(inputPath);
            for (FileStatus file : listStatus) {
                addInputPath(file.getPath(), fs, pathList, prefix);
            }
        } else {
            if (inputPath.getName().startsWith(prefix)) {
                logger.info("Add input path : " + inputPath);
                pathList.add(inputPath);
            } else {
                logger.info("Error input path : " + inputPath);
            }
        }
    }

    /**
     *
     * @param input     输入hdfs路径
     * @param fs        文件系统
     * @param prefix    前缀过滤
     * @param mapNum    map个数
     * @return
     * @throws IOException
     */
    public static long CalMapReduceSplitSize(String[] input, FileSystem fs, String prefix, long mapNum) throws IOException {
        List<Path> pathList = new ArrayList<Path>();
        long totalLen = 0;
        int fileNumber = 0;
        for (int i = 0; i < input.length; i++) {
            Path inputPath = new Path(input[i]);
            addInputPath(inputPath, fs, pathList, prefix);
        }
        for (Path path : pathList) {
            if (path.getName().startsWith(prefix)) {
                totalLen += fs.getFileStatus(path).getLen();
                fileNumber++;
            }
        }
        logger.info("input file number : " + fileNumber + " total length is : " + totalLen);
        return totalLen / mapNum;
    }

    public static long getFileSize(Configuration conf, String path) {
        FileSystem fs = null;
        try {
            long totalSize = 0L;
            fs = FileSystem.get(conf);
            FileStatus[] listStatus = fs.listStatus(new Path(path));
            for (FileStatus fstatus : listStatus) {
                totalSize += fstatus.getLen();
            }
            return totalSize;
        } catch (IOException e) {
            logger.error(e);
            return -1L;
        } finally {
            IOUtil.closeStream(fs);
        }
    }
    public long getFileSize(String path) {
        try {
            long totalSize = 0L;
            FileStatus[] listStatus = fs.listStatus(new Path(path));
            for (FileStatus fstatus : listStatus) {
                totalSize += fstatus.getLen();
            }
            return totalSize;
        } catch (IOException e) {
            logger.error(e);
            return -1L;
        }
    }

    public static boolean exists(Configuration conf, String path) {
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            return fs.exists(new Path(path));
        } catch (IOException e) {
            logger.error(e);
            return false;
        } finally {
            IOUtil.closeStream(fs);
        }
    }

    public static boolean deleteIfExists(Configuration conf, String path) {
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            Path p = new Path(path);
            if (fs.exists(p)) {
                fs.delete(p, true);
                return true;
            }
        } catch (IOException e) {
            logger.error(e);
        } finally {
            IOUtil.closeStream(fs);
        }
        return false;
    }

    public static InputStream readFile(Configuration conf , String fileName){
        FileSystem fs = null;
        try{
            fs = FileSystem.get(conf);
            Path path = new Path(fileName);
            if (fs.exists(path)){
                return  fs.open(path);
            }
        }catch (IOException e){
            logger.error(e);
        }
        return null;
    }
}
