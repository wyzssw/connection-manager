/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.constants;

/**
 * 类TopNewsRedisKeyConstants.java的实现描述：TODO 类实现描述
 *
 * @author hongfengwang 2013-6-17 下午03:44:47
 */
public class TopNewsRedisKeyConstants {

    public static final String HOT_NEW_ZSET                 = "hot_news_zset";

    public static final String CAT_NEWS_MATRIX_TOUTIAO      = "cat_news_matrix_toutiao_%d";
    
    public static final String CAT_NEWS_MATRIX_CHANNEL      = "cat_news_matrix_channel_%d";

    public static final String CAT_NEWS_MATRIX_TOUTIAO_BACK = "cat_news_matrix_toutiao_back_%d";

    public static final String USER_SHORT_LIKE_CAT          = "user_short_like_cat_%d";

    public static final String USER_LONG_LIKE_CAT           = "user_long_like_cat_%d";

    /** 新闻的热度的对应关系 */
    public static final String NEWS_HOT_COUNT               = "news_hot_%d";

    public static final String NEWS_CATID_MATRIX            = "news_catid_matrix_%d";

    public static final String EDITED_UPDATE_NEWS           = "edited_update_news";

    public static final String NEWS_CAT_INFO                = "news_cat_info_%d";

    public static final String NEWS_ATTRIBUTE_CACHE         = "news_attribute_cache_%d";

    /** 用于分片的key */
    public static final String TOUTIAO_USER                 = "toutiao_user_%d";

    /** 分布式锁key */
    public static final String TOUTIAO_USER_LOCK            = "toutiao_user_%d_lock";

    /** 头条用户特定 key */
    public static final String TOUTIAO_USER_ZSET            = "toutiao_user_%d_zset";
    /** 头条用户bloom hash */
    public static final String TOUTIAO_USER_HASH            = "toutiao_user_%d_hash";

    /** 频道新闻热门列表 */
    public static final String CHANNEL_NEWS_HOT             = "channel_news_hot";

    /** 自媒体新闻subids */
    public static final String NEWS_WEMEDIA_SUBIDS          = "news_wemidia_subids";

    /** 自媒体新闻热门列表 */
    public static final String WEMEDIA_NEWS_HOT             = "wemedia_news_hot";

    /** 刊物新闻热门列表 6 - 11:59 点 */
    public static final String SUB_NEWS_HOT_6_11            = "sub_news_hot_6_11";

    /** 刊物新闻热门列表 12 - 17:59 点 */
    public static final String SUB_NEWS_HOT_12_17           = "sub_news_hot_12_17";

    /** 刊物新闻热门列表 18 - 23:59 点 */
    public static final String SUB_NEWS_HOT_18_23           = "sub_news_hot_18_23";

    /** 用于分片的key */
    public static final String MCHANNEL_USER                = "mchannel_user_%d";

    /** 要闻分布式锁key */
    public static final String MCHANNEL_USER_LOCK           = "mchannel_user_%d_lock";

    /** 要闻用户特定 key */
    public static final String MCHANNEL_USER_ZSET           = "mchannel_user_%d_zset";
    /** 要闻用户bloom hash */
    public static final String MCHANNEL_USER_HASH           = "mchannel_user_%d_hash";


    /** 除去要闻用户特定 key */
    public static final String CHANNEL_OTHER_USER_ZSET       = "cn_u_%d_zset_%d";
    /** 除去要闻用户bloom hash */
    public static final String CHANNEL_OTHER_USER_HASH       = "cn_u_%d_hash_%d";


    /** 要闻频道这两天的新闻 */
    public static final String EDITED_MCHANNEL_NEWS         = "edited_mchannel_news";

    public static final String LOCAL_NEWS_KEY               = "local_news_%s";


    /** 用户对应微博标签所在分类及特征词 redis hash存储 有两个field ，一个field是cat，一个是tag  */
    public static final String  USER_WEIBO_LIKE_HASH        = "u_wb_hash_%d";

    public static final String  USER_WEIBO_LIKE_CAT         = "cat";

    public static final String  USER_WEIBO_LIKE_TAG         = "tag";

    /** tag特征词对应的 新闻列表，2天内过期，2天后重新生成*/
    public static final String  TAG_NEWS_MATRIX_2_DAY       = "tag_news_2day_%d";

    public static final String EDITED_YULECHANNEL_NEWS       = "edited_yulechannel_news";

    public static final String EDITED_FINANCECHANNEL_NEWS       = "edited_financechannel_news";

    public static final String EDITED_ITCHANNEL_NEWS       = "edited_itchannel_news";

    public static final String EDITED_CARCHANNEL_NEWS       = "edited_carchannel_news";

    public static final String EDITED_TVCHANNEL_NEWS             = "edited_tvchannel_news";

    public static final String EDITED_MILITARYCHANNEL_NEWS       = "edited_militarychannel_news";
    
    public static final String  MCHANNEL_USER_NOT_LIKE_HASH      = "mch_u_nlike_%d";
    
    public static final String  EACH_CHANNEL_RECOM_STAT_HASH     = "channel_news_recom_stat_hash";
    
    public static final String  MCHANNEL_NEWS_RECOM_STAT     = "mchannel_news_recom_stat";

}
