package com.wap.sohu.recom.utils;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.twitter.common.net.InetSocketAddressHelper;


/**
 * 类CommonUtils.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2012-7-24 下午03:42:14
 */
public class CommonUtils {
    
    private static final Logger LOGGER = Logger.getLogger(CommonUtils.class);
    /**
     * 从一个list中随机取出size个元素
     * 
     * @param <T>
     * @param list
     * @param size
     * @return
     */
    public static <T> List<T> getRandomList(final List<T> list, int size) {
        int listSize = list.size();
        if (size < 0) {
            return new ArrayList<T>();
        }
        if (listSize <= size) {
            return list;
        }

        List<T> x_ret = new ArrayList<T>();
        Random random = new Random();

        List<Integer> tmp = new LinkedList<Integer>();
        for (int i = 0; i < list.size(); i++) {
            tmp.add(i);
        }
        for (int i = 0; i < size; i++) {
            int rdm = random.nextInt(tmp.size());
            x_ret.add(list.get(tmp.remove(rdm)));
        }

        return x_ret;
    }

    
    public static int getRequestInt(Map<String, String> map, final String name, int defaultValue) {
        String value = map.get(name);
        if (value == null)
            return defaultValue;

        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    public static boolean getRequestBool(Map<String, String> map, final String name, boolean defaultValue) {
        String value = map.get(name);
        if (value == null)
            return defaultValue;

        try {
            return Boolean.valueOf(value.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    public static String getLocalHost(int port){
        String ipString="";
        try {
             ipString = InetSocketAddressHelper.getLocalAddress(0).toString();
        } catch (UnknownHostException e) {
            LOGGER.error("error when get ip", e);
        }
        ipString = StringUtils.substringAfter(ipString, "/");
        ipString = StringUtils.substringBefore(ipString, ":");
        return ipString;
    }

}
