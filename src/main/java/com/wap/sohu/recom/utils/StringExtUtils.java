/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * 类StringExtUtils.java的实现描述：TODO 类实现描述
 * 
 * @author hongfengwang 2012-9-17 下午02:11:09
 */
public class StringExtUtils {

    /**
     * 判断两个字符串交集的个数
     * 
     * @param str1
     * @param str2
     * @return
     */
    public static int getInterSize(String str1, String str2, int cha) {
        if (StringUtils.isBlank(str1) || StringUtils.isBlank(str2)) {
            return 0;
        }
        if (str1.length() - str2.length() > cha || str2.length() - str1.length() < -cha) {
            return 0;
        }
        List<String> list1 = new ArrayList<String>();
        List<String> list2 = new ArrayList<String>();
        char[] chars1 = str1.toCharArray();
        char[] chars2 = str2.toCharArray();
        for (int i = 0; i < chars1.length; i++) {
            list1.add(String.valueOf(chars1[i]));
        }
        for (int j = 0; j < chars2.length; j++) {
            list2.add(String.valueOf(chars2[j]));
        }
        list1.retainAll(list2);
        int result = list1.size();
        return result;
    }
    public static void main(String[] args) {
        
    String abcString="abcd9e";
    String abcString2="abcde67";
    System.out.println(getInterSize(abcString, abcString2, 10));
    }
}
