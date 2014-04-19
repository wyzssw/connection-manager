/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.utils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * 获取request参数工具类 
 * @author hongfengwang 2012-8-27 上午10:12:18
 */
public class RequestUtil {
    
    private static final Logger LOGGER = Logger.getLogger(RequestUtil.class);
    /**
     * 得到整形参数
     * @param paramValue
     * @param defaultValue
     * @return
     */
    public static int getRequestInt(String paramValue, int defaultValue) {
        if (StringUtils.isBlank(paramValue)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(paramValue.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * 得到long类型参数
     * @param paramValue
     * @param defaultValue
     * @return
     */
    public static long getRequestLong(String paramValue, long defaultValue) {
        if (StringUtils.isBlank(paramValue)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(paramValue.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }
    

    /**
     * 得到string类型参数
     * @param paramValue
     * @param defaultValue
     * @return
     */
    public static String getRequestString(String paramValue, String defaultValue) {
        if (StringUtils.isBlank(paramValue)) {
            return defaultValue;
        }
        return paramValue.trim();       
    }

    public static  List<Long> getRequestList(String paramValue){
        List<Long> longList = new ArrayList<Long>();
        if (StringUtils.isBlank(paramValue)) {
            return longList;
        }
        String[] items  = StringUtils.split(paramValue, ",");
        List<String> strList = Arrays.asList(items);
        for (String string : strList) {
            long value = 0L;
            try {
                value = Long.parseLong(string);
            } catch (Exception e) {
                LOGGER.error(e.getMessage()+" ;"+"requeust parser has error which string is"+string);
                continue;
            }
            longList.add(value);
        }
        return longList;
    }
    
    public static  List<Integer> getRequestIntList(String paramValue){
        List<Integer> longList = new ArrayList<Integer>();
        if (StringUtils.isBlank(paramValue)) {
            return longList;
        }
        String[] items  = StringUtils.split(paramValue, ",");
        List<String> strList = Arrays.asList(items);
        for (String string : strList) {
            Integer value = 0;
            try {
                value = Integer.parseInt(string);
            } catch (Exception e) {
                LOGGER.error(e.getMessage()+" ;"+"requeust parser has error which string is"+string);
                continue;
            }
            longList.add(value);
        }
        return longList;
    }
}
