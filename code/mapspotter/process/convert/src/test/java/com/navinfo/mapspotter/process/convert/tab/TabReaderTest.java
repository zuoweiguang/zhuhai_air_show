package com.navinfo.mapspotter.process.convert.tab;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by SongHuiXing on 7/21 0021.
 */
public class TabReaderTest {

    @Test
    public void testConvertTab2Pg() throws Exception {
        TabReader reader = new TabReader();

        reader.convertTab2Pg("E:\\WorkSpace\\data\\历史Block\\14冬15春\\BLOCK.TAB",
                            "192.168.4.104", 5440, "navinfo",
                            "postgres", "navinfo1!pg");
    }
}