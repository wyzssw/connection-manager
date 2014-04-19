/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.constants;

/**
 * 类EdbKeyConstants.java的实现描述：TODO 类实现描述
 *
 * @author hongfengwang 2012-12-11 下午03:27:58
 */
public class EdbKeyConstants {

    /** 短期新闻 cid-tag-score */
    public static final String SHORT_NEWS   = "short_news";
    /** 长期新闻 cid-tag-score */
    public static final String LONG_NEWS    = "long_news";
    /** 短期组图 cid-tag-score */
    public static final String SHORT_GROUP  = "short_group";
    /** 长期组图 cid-tag-score */
    public static final String LONG_GROUP   = "long_group";
    /** 新闻分数 log10(7天内news count)+新闻内tag权重*1.5 */
    public static final String NEWS_SCORE   = "item_news";
    /** 组图分数 log10(group count) */
    public static final String GROUP_SCORE  = "item_group";
    /** 新闻相似度列表 */
    public static final String SIM_NEWS     = "sim_news";
    /** 组图相似度列表 */
    public static final String SIM_GROUP    = "sim_group";
    /** 短期新闻类别 */
    public static final String CAT_SHORT    = "cat_short";
    /** 长期新闻类别 */
    public static final String CAT_LONG     = "cat_long";
    /** subscription类别 */
    public static final String CAT_SUB    = "cat_sub";
    /**  用户喜欢新闻频道  */
    public static final String USER_CHANNEL = "user_channel";
    /**新产品用户短期喜好 */
    public static final String TAO_CAT_LONG = "newproject_cat";
    
}
