package com.navinfo.mapspotter.foundation.util;

import org.junit.Test;

/**
 * Created by SongHuiXing on 2016/3/8.
 */
public class StringZipUtilTest {

    @Test
    public void testCompress() throws Exception {
        //测试字符串
        String str="468743168-871515#[321231,234234,1231,1232,43534,64564,8465435,4654654,6544654,4654654,45646]";

        System.out.println("原长度："+str.length());

        System.out.println("压缩后："+ StringZipUtil.compress(str).length());

        System.out.println("解压缩："+ StringZipUtil.uncompress(StringZipUtil.compress(str)));
    }

    @Test
    public void test(){
        assert(query(1,"sf"));
    }
    public boolean query(int a, String... others){
        String b1 = others[0];
        return true;
    }
}