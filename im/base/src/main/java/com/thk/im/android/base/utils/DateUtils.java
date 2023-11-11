package com.thk.im.android.base.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class DateUtils {


    /**
     * 获取聊天消息时间线
     */
    public static String timeToMsgTime(long ms, long now) {
        Calendar showDate = Calendar.getInstance();
        showDate.setTime(new Date(ms));
        Calendar nowData = Calendar.getInstance();
        nowData.setTime(new Date(now));

        // 不是今年
        if (showDate.get(Calendar.YEAR) != nowData.get(Calendar.YEAR)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date(ms);
            return format.format(date);
        }
        // 今天
        if ((showDate.get(Calendar.YEAR) == nowData.get(Calendar.YEAR))
                && (showDate.get(Calendar.MONTH) == nowData.get(Calendar.MONTH)
                && (showDate.get(Calendar.DAY_OF_MONTH) == nowData.get(Calendar.DAY_OF_MONTH)))) {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            Date date = new Date(ms);
            return format.format(date);
        }

        SimpleDateFormat format = new SimpleDateFormat("MM-dd");
        Date date = new Date(ms);
        return format.format(date);
    }


    public static String secondToDuration(int seconds) {
        int h = seconds / 3600;
        int m = seconds / 60;
        int s = seconds % 60;
        return h == 0 ? String.format("%02d:%02d", m, s) : String.format("%02d:%02d:%02d", h, m, s);
    }


}
