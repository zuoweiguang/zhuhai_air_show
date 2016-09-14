package com.navinfo.mapspotter.process.convert.vectortile;

import com.navinfo.mapspotter.process.convert.WarehouseDataType;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by SongHuiXing on 6/14 0014.
 */
public class FilterReaderTest {

    @Test
    public void testGetFilter() throws Exception {
        String filter = FilterReader.getFilter(WarehouseDataType.LayerType.Road, 11);

        assertEquals(filter, "kind in (1,2,3,4,5,6)");
    }

    @Test
    public void testGetMinLevel() throws Exception {
        int minlevel = FilterReader.getMinLevel(WarehouseDataType.SourceType.Background);

        assertEquals(8, minlevel);
    }
}