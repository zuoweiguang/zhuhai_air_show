package com.navinfo.mapspotter.foundation.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
}
