package com.wap.sohu.recom.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * 类DateThresholdUtils.java的实现描述：time threshold controll
 * @author yeyanchao Sep 6, 2013 4:30:31 PM
 */
public class DateThresholdUtils {

    private static  Calendar c  = Calendar.getInstance();


    private static Date advertisementNewEnd;

    static {
        c.set(2013, Calendar.SEPTEMBER, 9, 23, 59, 59);
        advertisementNewEnd = c.getTime();
    }

    public static boolean isAdvertise(){
        long now = System.currentTimeMillis();
        if(now<=advertisementNewEnd.getTime()){
            return true;
        }
        return false;
    }

}
