package com.navinfo.mapspotter.warehouse.zhuhai.util;

import java.lang.reflect.Method;

/**
 * Created by zuoweiguang on 2016/9/13.
 */
public class Base64 {

    /**
     * 解码
     * @param input
     * @return byte[]
     */
    public static byte[] decodeBase64(String input) throws Exception{
        Class clazz=Class.forName("com.sun.org.apache.xerces.internal.impl.dv.util.Base64");
        Method mainMethod= clazz.getMethod("decode", String.class);
        mainMethod.setAccessible(true);
        Object retObj=mainMethod.invoke(null, input);
        return (byte[])retObj;
    }

}
