package com.navinfo.mapspotter.foundation.io.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by huanghai on 2016/1/4.
 */
public class HdfsBaseTest {
    Configuration conf = null;

    @Before
    public void init() {
        conf = new Configuration();
    }

    @Test
    public void mkdir() throws IOException {
        String path = "/test";
        boolean mkdir = HdfsBase.mkdir(conf, path);
        assertTrue(mkdir);
    }

    @Test
    public void createFile() throws IOException {
        String dst = "/test/hdfs_test.txt";
        boolean flag = HdfsBase.createFile(conf, dst);
        assertTrue(flag);
    }

    @Test
    public void rename() throws IOException {
        String oldfile = "/test/hdfs_test.txt";
        String newfile = "/test/hdfs_test_new.txt";
        boolean rename = HdfsBase.rename(conf, oldfile, newfile);
        assertTrue(rename);
    }

    @Test
    public void getHdfsOutputStream() throws IOException {
        String filepath = "/test/hdfs_test_new.txt";
        Object hdfsOutputStream = HdfsBase.getHdfsOutputStream(conf, filepath);
        assertNotNull(hdfsOutputStream);
    }

    @Test
    public void deleteFile() throws IOException {
        String filePath = "/test/hdfs_test_new.txt";
        boolean delete = HdfsBase.delete(conf, filePath, false);
        assertTrue(delete);
    }

    @Test
    public void deleteDir() throws IOException {
        String filePath = "/test";
        boolean delete = HdfsBase.delete(conf, filePath, true);
        assertTrue(delete);
    }
}
