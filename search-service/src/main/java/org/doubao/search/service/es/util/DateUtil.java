package org.doubao.search.service.es.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_FORMAT);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    /**
     * 将LocalDateTime格式化为字符串
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return DEFAULT_FORMATTER.format(dateTime);
    }

    /**
     * 将LocalDateTime格式化为日期字符串
     */
    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return DATE_FORMATTER.format(dateTime);
    }

    /**
     * 将字符串解析为LocalDateTime
     */
    public static LocalDateTime parse(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateStr, DEFAULT_FORMATTER);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(dateStr + " 00:00:00", DEFAULT_FORMATTER);
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
