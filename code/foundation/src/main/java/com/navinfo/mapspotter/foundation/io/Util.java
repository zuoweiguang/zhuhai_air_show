package com.navinfo.mapspotter.foundation.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by gaojian on 2016/1/6.
 */
public class Util {

    public static void closeStream(Closeable... args) {
        for (Closeable arg : args) {
            if (arg != null) {
                try {
                    arg.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
