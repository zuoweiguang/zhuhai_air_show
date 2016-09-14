package com.navinfo.mapspotter.foundation.util;

/**
 * Created by gaojian on 2016/4/12.
 */
public class Base64Util {
    private static final char[] pem_array = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
    private static final byte[] pem_convert_array = new byte[256];
    static {
        int var0;
        for(var0 = 0; var0 < 255; ++var0) {
            pem_convert_array[var0] = -1;
        }

        for(var0 = 0; var0 < pem_array.length; ++var0) {
            pem_convert_array[pem_array[var0]] = (byte) var0;
        }

    }

    public static String encode(int n) {
        String result = "";
        while (n != 0) {
            result = pem_array[n & 63] + result;
            n = n >> 6;
        }
        return result.toString();
    }

    public static int decode(String str) {
        int result = 0;
        for (int i = 0; i < str.length(); ++i) {
            result = result << 6;
            result = result | pem_convert_array[str.charAt(i)];
        }
        return result;
    }

}
