package com.example.ductran.soundrecorder.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by duc.tran on 1/22/2018.
 */

public class Utils {
    public static final String DATE_FORMAT_DEFAULT_STR = "yyyyMMdd_HHmmss";

    public static SimpleDateFormat getSimpleDateFormat(String formatStr) {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        Locale locale = new Locale("vi", "VI");
        SimpleDateFormat format = new SimpleDateFormat(formatStr, locale);
        format.setTimeZone(timeZone);
        return format;
    }



    public static String getDateTime(long mills, String formatStr) {
        Date date = new Date(mills);
        SimpleDateFormat format = getSimpleDateFormat(formatStr);
        String fDate = format.format(date);
        return fDate;
    }
}
