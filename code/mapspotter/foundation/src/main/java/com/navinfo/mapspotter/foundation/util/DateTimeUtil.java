package com.navinfo.mapspotter.foundation.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间日期相关工具
 * Created by cuiliang on 2015/12/30.
 */
public class DateTimeUtil {

    private static long THOUSAND = 1000;

    private final static String DEFAULT_PARTTEN = "YYYY-MM-dd HH:mm:ss";

    private static final Logger logger = Logger.getLogger(DateTimeUtil.class);

    /**
     * 将当天日期按照格式转成字符串
     *
     * @param partten 格式
     * @return 日期字符串
     */
    public static String formatDate(String partten) {
        SimpleDateFormat format = new SimpleDateFormat(partten);
        return format.format(new Date());
    }


    /**
     * 将当天日期按照格式转成字符串
     *
     * @return 日期字符串
     */
    public static String formatDate() {
        return formatDate(DEFAULT_PARTTEN);
    }


    /**
     * 将指定日期按照格式转成字符串
     *
     * @param partten 格式
     * @param date    传入日期
     * @return 日期字符串
     */
    public static String formatDate(String partten, Date date) {
        SimpleDateFormat format = new SimpleDateFormat(partten);
        return format.format(date);
    }

    /**
     * 将指定日期按照格式转成字符串
     *
     * @param date 传入日期
     * @return 日期字符串
     */
    public static String formatDate(Date date) {
        return formatDate(DEFAULT_PARTTEN, date);
    }

    /**
     * 将指定日期字符串转成日期格式
     *
     * @param partten 格式
     * @param dateStr 日期字符串
     * @return
     */
    public static Date parseDate(String partten, String dateStr){
        SimpleDateFormat format = new SimpleDateFormat(partten);
        try {
            return format.parse(dateStr);
        } catch (ParseException e) {
            logger.error("date parse error");
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 计算两个日期之间相差秒数
     *
     * @param date1
     * @param date2
     * @return 相差秒数
     */
    public static long diffSecond(Date date1, Date date2) {
        long diff = Math.abs(date1.getTime() - date2.getTime());
        return diff / THOUSAND;
    }

    /**
     * 计算两个日期(long型)之间相差秒数
     *
     * @param date1
     * @param date2
     * @return 相差秒数
     */
    public static long diffSecond(long date1, long date2) {
        long diff = Math.abs(date1 - date2);
        return diff / THOUSAND;
    }

    /**
     * Transform Calendar to ISO 8601 string.
     */
    public static String fromDate(final Date date) {
        String formatted = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .format(date);
        return formatted.substring(0, 22) + ":" + formatted.substring(22);
    }

    /**
     * Transform ISO 8601 string to Calendar.
     */
    public static Date toDate(final String iso8601string)
            throws ParseException {

        String s = iso8601string.replace("Z", "+00:00");
        try {
            s = s.substring(0, 22) + s.substring(23);  // to get rid of the ":"
        } catch (IndexOutOfBoundsException e) {
            throw new ParseException("Invalid length", 0);
        }

        Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(s);

        return date;
    }

    public static String getNow(){
        return formatDate("YYYY-MM-dd HH:mm:ss");
    }
    public static String getNowMonthDate(){
        return formatDate("MMdd");
    }
    public static String getNowYearMonthDate(){
        return formatDate("YYYYMMdd");
    }
    public static String getNowYearMonthDateHour(){
        return formatDate("HH");
    }
    public static String getPreHour(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - 1);
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHH");
        return df.format(calendar.getTime());
    }

    public static String getPreDate(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 1);
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHH");
        return df.format(calendar.getTime());
    }
}
