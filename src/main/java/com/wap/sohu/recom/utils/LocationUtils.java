package com.wap.sohu.recom.utils;

import java.text.DecimalFormat;

import org.apache.commons.lang.StringUtils;

/**
 * 类ValidateUtil.java的实现描述：数据检验工具类
 * 
 * @author yeyanchao 2012-9-28 下午4:00:16
 */
public class LocationUtils {

    /**
     * 格式化城市名称
     * 
     * @param city
     */
    public static String formatCityName(String city) {
        if (StringUtils.isBlank(city)) {
            return null;
        }
        if (city.endsWith("市")) {
            return city.substring(0, city.length() - 1);
        } else if (city.endsWith("自治州")) {
            return city.substring(0, city.length() - 3);
        }
        return city;
    }

    /**
     * 验证经度数据
     * 
     * @param lng
     * @return
     */
    public static boolean validateLng(double lng) {
        return lng >= -180 && lng <= 180;
    }

    /**
     * 验证纬度数据
     * 
     * @param lat
     * @return
     */
    public static boolean validateLat(double lat) {
        return lat >= -90 && lat <= 90;
    }

    /**
     * 格式化经纬度数据：保留三位小数
     * 
     * @param origin
     * @return
     */
    public static String formatLatLng(double origin) {
        DecimalFormat format = new DecimalFormat("###.###");
        String value = format.format(origin);
        return value;
    }
}
