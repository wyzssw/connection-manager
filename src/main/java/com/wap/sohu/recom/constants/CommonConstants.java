/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.constants;

import java.util.regex.Pattern;

/**
 * 一般常量
 * @author hongfengwang 2012-11-1 上午11:06:44
 */
public class CommonConstants {
    //待过滤的未发布新闻
    public static final String FILTER_PUB_KEY  = "filterPubKey";

    /**期刊新闻和即时新闻 */
    public static final String EDITED_NEWS_KEY          = "editedNewsKey";

    public static final String NEWS_FILTER_TYPE_KEY      = "newsFilterTypeKey";

    public static final String NEWS_LAST_CACHE           = "news_last_cache";

    public static final String GROUP_LAST_CACHE          = "group_last_cache";

    public static final String GROUP_SCORE_CACHE         = "group_score_cache";

    public static final String NEWS_SCORE_CACHE          = "news_score_cache";

    public static final String GROUP_DEL_THOSE_DAYS      = "group_del_those_days";

    public static final String NEWS_DELETED_CACHE        =   "news_deleted_cache";

    public static final String NEWS_WEMEDIA_SUBIDS       = "news_wemidia_subids";

    public static final String EDITED_MCHANNEL_NEWS      = "edited_mchannel_news";

    public static final String ENJOY_EDITOR_CHANNEL_NEWS =  "enjoy_editor_channel_news";

    /**更新周期目前是10天 */
//    public static final int    UPDATE_DAYS        = 7;
    public static final int    UPDATE_DAYS        = 7;
//    public static final int    UPDATE_DAYS        = 15;
//    public static final int    UPDATE_DAYS        =   5;

    public static final int    GROUP_UPDATE_DAYS  = 60;

    public static final int    GROUPNEWS_MORECOUNT = 3;

    public static  final Pattern patternNoChinese = Pattern.compile("[^\u4e00-\u9fa5]");

    public static final int    TOUTIAO_UPDATE_DAYS = 2;

    public static final int    TAONEWS_UPDATE_DAYS = 2;

    public static final int    MCHANNEL_UPDATE_DAYS = 2;

    public static final int    FINANCE_CHANNEL_UPDATE_DAYS = 1;
    public static final int    IT_CHANNEL_UPDATE_DAYS = 1;
    public static final int    CAR_CHANNEL_UPDATE_DAYS = 1;
    public static final int    YAOWENCHANNEL_UPDATE_DAYS = 1;
    public static final int    WEMEDIA_UPDATE_DAYS = 2;
    public static final int    SUBID_UPDATE_HOURS = 6;

    public static final int    SHORT_CAT_LIKE_COUNT = 6;
    public static final int    TAO_SHORT_CAT_LIKE_COUNT = 6;

    /**
     * top news : news type
     */
    public static final String     RECOM_TYPE          = "recom";

    public static final String     RECOM_TYPE_SHORT          = "rshort";

    public static final String     RECOM_TYPE_LONG          = "rlong";

    public static final String     RECOM_TYPE_SUB          = "rsub";

    public static final String     RECOM_TYPE_CHANNEL       = "rchannel";

//    public static final String     RECOM_TYPE_CHANNEL_FINANCE   = "channel";


    public static final String     HOT_TYPE            = "hot";

    public static final String     HISTORY_TYPE        = "old";

    public static final String     LOCAL_TYPE          = "local";

    public static final String     WEIBO_TAG_TYPE          = "wtag";


    /**频道新闻热门列表  */
    public static final String  CHANNEL_NEWS_HOT           = "channel_news_hot";

    /**自媒体新闻热门列表 */
    public static final String  WEMEDIA_NEWS_HOT           = "wemedia_news_hot";

    /** 各个新闻频道最新的新闻   */
    public static final String  CHANNEL_RECOM_ZSET          = "channel_recom_zset";
    /** separate channel zset */
    public static final String  CHANNEL_SEPARATE_ZSET = "channel_separate_zset_%d";
    /** channel editor news*/
    public static final String EDITOR_CHANNEL_NEWS = "edited_channel_news_%d";
    /** finance channel id */
    public static final int FINANCE_CHANNEL_ID = 4;

    public static final int ENJOY_CHANNEL_ID   = 3;

    public static final int MAIN_CHANNEL_ID = 1;

    public static final int IT_CHANNEL_ID = 6;

    public static final int CAR_CHANNEL_ID = 11;

    public static final int TV_CHANNEL_ID = 30;

    public static final int MILITARY_CHANNEL_ID = 5;

    public static final int  INTERNATIONAL_CHANNEL_ID = 45;

    public static final int VIDEO_CHANNEL_ID = 36;

    public static final int SPORTS_CHANNEL_ID = 2;

    public static final int HOUSE_CHANNEL_ID = 38;

//    /**刊物新闻热门列表周末早间7-11点*/
//    public static final String  SUB_NEWS_HOT_WEEKENDS         = "sub_news_hot_weekends";
//
//    /**刊物新闻热门列表工作日下午15-22点 */
//    public static final String  SUB_NEWS_HOT_WORKDAY          = "sub_news_hot_workday";

    /** 头条编辑的新闻 */
    public static final String  EDITED_TOUTIAO_NEWS       = "editedToutiaoNews";

    /** 要闻频道的新闻 */
    public static final String  MAIN_CHANNEL_HOT_NEWS     = "main_channel_hot_news";


}
