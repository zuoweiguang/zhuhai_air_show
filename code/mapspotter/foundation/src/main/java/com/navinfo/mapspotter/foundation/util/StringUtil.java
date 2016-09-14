package com.navinfo.mapspotter.foundation.util;

import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author cuiliang
 */
public class StringUtil {
    /**
     * 字符串翻转 abc <-> cba
     *
     * @param input 输入字符串
     * @return 输出字符串
     */
    public static String reverse(String input) {
        StringBuilder builder = new StringBuilder(input).reverse();
        return builder.toString();
    }

    /**
     * 字符串预处理: 将null,"null"等转成"",其他trim后输出
     *
     * @param input 输入字符串
     * @return 输出字符串
     */
    public static String pretreatment(String input) {
        if (null == input) {
            return "";
        } else if ("".equals(input.trim())) {
            return "";
        } else if ("NULL".equals(input.trim().toUpperCase())) {
            return "";
        } else {
            return input.trim();
        }
    }

    /**
     * 将字符串按照指定字符分割并trim后转成List
     *
     * @param input 输入字符串
     * @param regex 分隔符
     * @return 输出String List
     */
    public static List<String> split(String input, String regex) {
        List<String> list = new ArrayList<String>();
        if (null == input)
            return list;
        if (input.trim().length() == 0) {
            list.add("");
            return list;
        }
        String[] array = input.trim().split(regex);
        for (String str : array) {
            list.add(str);
        }
        return list;
    }

    /**
     * 左补齐
     *
     * @param content 字符串
     * @param c       补齐字符
     * @param length  补齐后长度
     * @return 补齐后字符串
     */
    public static String fillLeft(String content, String c, long length) {
        while (content.length() < length) {
            content = c + content;
        }
        return content;
    }

    /**
     * 右补齐
     *
     * @param content 字符串
     * @param c       补齐字符
     * @param length  补齐后长度
     * @return 补齐后字符串
     */
    public static String fillRight(String content, String c, long length) {
        while (content.length() < length) {
            content = content + c;
        }
        return content;
    }

    /**
     * 左边trim掉指定字符
     *
     * @param input 输入字符串
     * @param c     指定字符
     * @return trim后的字符串
     */
    public static String leftTrimByStr(String input, String c) {
        if (input.startsWith(c)) {
            return leftTrimByStr(input.substring(1, input.length()), c);
        } else {
            return input;
        }
    }

    /**
     * 右边trim掉指定字符
     *
     * @param input 输入字符串
     * @param c     指定字符
     * @return trim后的字符串
     */
    public static String rightTrimByStr(String input, String c) {
        if (input.endsWith(c)) {
            return rightTrimByStr(input.substring(0, input.length() - 1), c);
        } else {
            return input;
        }
    }

    /**
     * 两边trim掉指定字符
     *
     * @param input 输入字符串
     * @param c     指定字符
     * @return trim后的字符串
     */
    public static String bothTrimByStr(String input, String c) {
        String trimString = leftTrimByStr(input, c);
        trimString = rightTrimByStr(trimString, c);
        return trimString;
    }

    /**
     * 生成随机字符串，由数字和字母组成
     *
     * @param length
     * @return
     */
    public static String getRandomString(int length) { // length表示生成字符串的长度
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 判断字符串是否为空，包括null和空字符串
     *
     * @param str 字符串
     * @return true：为空；false：不为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判断两个字符串是否相等，包括null的情况
     * @param str1 字符串1
     * @param str2 字符串2
     * @return true：相等；false：不想等
     */
    public static boolean equals(String str1, String str2) {
        return (str1 == null) ? (str2 == null) : (str1.equals(str2));
    }

    /**
     * 使用指定的分隔符连接多个对象
     * @param elements  待连接对象
     * @param delim     分隔符
     * @param <T>
     * @return
     */
    public static <T> String join(Iterable<T> elements, String delim) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (T elem : elements) {
            if (first) {
                first = false;
            } else {
                result.append(delim);
            }
            result.append(elem);
        }
        return result.toString();
    }
    public static String uuid() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static String lessUUID(){
        UUID uuid = UUID.randomUUID();

        long most = uuid.getMostSignificantBits();
        long least = uuid.getLeastSignificantBits();

        return (digits(most >> 32, 8) +
                digits(most >> 16, 4) +
                digits(most, 4) +
                digits(least >> 48, 4) +
                digits(least, 12));
    }

    private static String digits(long val, int digits) {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }

    public static String encoderByMd5(String str){
        //确定计算方法
        MessageDigest md5= null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        BASE64Encoder base64en = new BASE64Encoder();
        //加密后的字符串
        String newstr= null;
        try {
            newstr = base64en.encode(md5.digest(str.getBytes("utf-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return newstr;
    }
}
