package com.dianping.cache.alarm.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by lvshiyun on 15/12/20.
 */
public class DateUtil {
    protected static Logger logger = LoggerFactory.getLogger(DateUtil.class);

    private static final SimpleDateFormat TIME_SDF = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.US);

    private static final SimpleDateFormat MS_TIME_SDF = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss.SSS", Locale.US);

    private static final SimpleDateFormat YM_SDF = new SimpleDateFormat(
            "yyyy-MM", Locale.US);

    private static final SimpleDateFormat MT_SDF = new SimpleDateFormat(
            "yyyy MMM d HH:mm:ss", Locale.US);

    private static final SimpleDateFormat TOMCAT_SDF = new SimpleDateFormat(
            "yyyy-MM-dd_HH-mm-ss", Locale.US);

    private static final SimpleDateFormat DATE_SDF = new SimpleDateFormat(
            "yyyy-MM-dd", Locale.US);

    private static final SimpleDateFormat MINUTE_IN_DAY_SDF = new SimpleDateFormat(
            "HH:mm", Locale.US);

    private static final SimpleDateFormat MINUTE_SDF = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm", Locale.US);

    private static final SimpleDateFormat NUMBER_MS_SDF = new SimpleDateFormat(
            "yyyyMMddHHmmssSSS", Locale.US);

    private static final SimpleDateFormat CAT_DAY_SDF = new SimpleDateFormat(
            "yyyyMMdd", Locale.US);

    private static final SimpleDateFormat CAT_HOUR_SDF = new SimpleDateFormat(
            "yyyyMMddHH", Locale.US);

    private static final SimpleDateFormat CAT_MINUTE_SDF = new SimpleDateFormat(
            "mm", Locale.US);

    public static String getCatDayString(Date date) {
        if (date == null) {
            return "";
        }
        synchronized (CAT_DAY_SDF) {
            return CAT_DAY_SDF.format(date);
        }
    }

    public static String getCatHourString(Date date) {
        if (date == null) {
            return "";
        }
        synchronized (CAT_HOUR_SDF) {
            return CAT_HOUR_SDF.format(date);
        }
    }

    public static String getCatMinuteString(Date date) {
        if (date == null) {
            return "";
        }
        synchronized (CAT_MINUTE_SDF) {
            return CAT_MINUTE_SDF.format(date);
        }
    }

    /**
     * 得到yyyy-MM-dd HH:mm:ss格式的日期
     *
     * @param date
     * @return
     */
    public static String getString(Date date) {
        if (date == null) {
            return "";
        }
        synchronized (TIME_SDF) {
            return TIME_SDF.format(date);
        }
    }

    public static String getDateString(Date date) {
        if (date == null) {
            return "";
        }
        synchronized (DATE_SDF) {
            return DATE_SDF.format(date);
        }
    }

    public static String getMinuteString(Date date) {
        if (date == null) {
            return "";
        }
        synchronized (MINUTE_SDF) {
            return MINUTE_SDF.format(date);
        }
    }

    public static String getMinuteInDayString(Date date) {
        if (date == null) {
            return "";
        }
        synchronized (MINUTE_IN_DAY_SDF) {
            return MINUTE_IN_DAY_SDF.format(date);
        }
    }

    /**
     * 得到yyyy-MM-dd HH:mm:ss.SSS格式的时间
     *
     * @return
     */
    public static String getMsString(Date date) {
        if (date == null) {
            return "";
        }
        synchronized (MS_TIME_SDF) {
            return MS_TIME_SDF.format(date);
        }
    }

    /**
     * 得到yyyyMMdd格式的日期，生成归档文件夹用
     *
     * @param date
     * @return
     */
    public static String getDayDateString(Date date) {
        if (date == null) {
            return "";
        }
        synchronized (YM_SDF) {
            return YM_SDF.format(date);
        }
    }

    /**
     * @desc:时间类型转换－String to Date
     * @param inputTime
     * @return: 如果解析失败，返回当前时间
     */
    public static Date parseDate(String inputTime) {
        if (inputTime == null) {
            return new Date();
        }
        if ("".equals(inputTime)) {
            return new Date();
        }
        try {
            String standardTime;
            if (inputTime.split(" ").length <= 1) {
                standardTime = inputTime + " 00:00:00";
            } else {
                standardTime = inputTime;
            }
            synchronized (TIME_SDF) {
                return TIME_SDF.parse(standardTime);
            }
        } catch (Exception e) {
            logger.error("parse fail", e);
        }
        return new Date();
    }

    public static Date parseNumberMsDate(String inputTime) {
        if (inputTime == null) {
            return null;
        }
        if ("".equals(inputTime)) {
            return null;
        }
        try {
            synchronized (NUMBER_MS_SDF) {
                return NUMBER_MS_SDF.parse(inputTime);
            }
        } catch (Exception e) {
            logger.error("parse fail", e);
        }
        return null;
    }

    public static String getNumberMsString(Date date) {
        if (date == null) {
            return "";
        }
        synchronized (NUMBER_MS_SDF) {
            return NUMBER_MS_SDF.format(date);
        }
    }

    public static Date parseTomcat(String tomcatStartTime) {
        try {
            synchronized (TOMCAT_SDF) {
                return TOMCAT_SDF.parse(tomcatStartTime);
            }
        } catch (Exception e) {
            logger.error("parse fail", e);
        }
        return new Date();
    }

    public static String formalizeMonthTime(String inputTime) {
        try {
            Date inputDate;
            synchronized (MT_SDF) {
                inputDate = MT_SDF.parse(inputTime);
            }
            String result;
            synchronized (TIME_SDF) {
                result = TIME_SDF.format(inputDate);
            }
            return result;
        } catch (Exception e) {
            logger.error("parse fail", e);
        }
        return inputTime;
    }
}