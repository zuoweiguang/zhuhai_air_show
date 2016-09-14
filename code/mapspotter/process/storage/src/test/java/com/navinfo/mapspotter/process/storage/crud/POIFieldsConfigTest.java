package com.navinfo.mapspotter.process.storage.crud;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by SongHuiXing on 6/23 0023.
 */
public class POIFieldsConfigTest {

    @Test
    public void testGetFields() throws Exception {
        List<String> fields = POIFieldsConfig.getFields();

        assertTrue(fields.size() > 0);
    }
}