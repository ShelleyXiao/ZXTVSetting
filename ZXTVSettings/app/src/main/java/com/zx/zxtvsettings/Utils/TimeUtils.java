package com.zx.zxtvsettings.Utils;


import android.text.format.Time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by ShaudXiao on 2016/7/12.
 */
public class TimeUtils {

    public static String getTime() {
        String date = getDateTimeNow();
        String sTime = date.substring(11, date.length() - 3);
        return sTime;
    }

    public static String getDate() {
        String date = getDateTimeNow();
        String sDate = date.substring(0, 11);
        return sDate;
    }


    public static String getDateTimeNow() {
        Time timer = new Time();
        timer.setToNow();
        DateFormat.getDateInstance();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = df.format(calendar.getTime());

        return timeStr;
    }

}
