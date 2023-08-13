package com.thk.im.android.ui.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * 主要终于页面时间的转化
 * 日期工具类
 *
 * @yingmu
 */
public class DateUtil {


    /**
     * 获取聊天消息时间线
     *
     * @param timestamp
     * @return
     */
    public static String getTimeline(long timestamp) {
        Calendar calendar1 = Calendar.getInstance(); // 今天
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(new Date(timestamp));

        Calendar yesterday = Calendar.getInstance(); // 昨天
        yesterday.setTime(calendar1.getTime());
        yesterday.add(Calendar.DATE, -1);
        yesterday.set(Calendar.HOUR_OF_DAY, 0);
        yesterday.set(Calendar.MINUTE, 0);
        yesterday.set(Calendar.SECOND, 0);

        //今天
        if ((calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR))
                && (calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)
                && (calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH)))) {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            Date date = new Date(timestamp);
            if (calendar2.get(Calendar.HOUR_OF_DAY) < 13) {
                return "上午 " + format.format(date);
            } else {
                return "下午 " + format.format(date);
            }
        }
        //昨天
        if ((yesterday.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR))
                && (yesterday.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)
                && (yesterday.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH)))) {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            Date date = new Date(timestamp);
            return "昨天 " + format.format(date);
        }
        //今年
        if (calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)) {
            SimpleDateFormat format = new SimpleDateFormat("MM月dd日");
            Date date = new Date(timestamp);
            return format.format(date);
        }
        //前几年
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
        Date date = new Date(timestamp);
        return format.format(date);
    }


}
