package com.navinfo.mapspotter.foundation.io;

import com.navinfo.mapspotter.foundation.io.oracle.OracleConnectionTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by SongHuiXing on 2015/12/30.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({DatabaseTest.class, OracleConnectionTest.class})
public class IOTestSuite {

}
