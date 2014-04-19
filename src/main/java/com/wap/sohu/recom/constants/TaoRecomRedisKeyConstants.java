/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.constants;

/**
 * 淘新闻
 * @author hongfengwang 2013-8-7 下午04:19:46
 */
public class TaoRecomRedisKeyConstants {
    public static final String HOT_NEW_ZSET                 = "tao_hot_zset";

    public static final String CAT_NEWS_MATRIX_TAO           = "tao_cat_news_%d";

    public static final String CAT_NEWS_MATRIX_TOUTIAO_BACK = "cat_news_matrix_toutiao_back_%d";

    public static final String USER_SHORT_LIKE_CAT          = "tao_u_s_%d";

    public static final String USER_LONG_LIKE_CAT           = "tao_u_l_%d";

    /** 新闻的热度的对应关系 */
    public static final String NEWS_HOT_COUNT               = "tao_news_hot_count_%d";
    
     /** 用于分片的key */
    public static final String TAO_USER                     = "tao_u_%d";
    /** 分布式锁key */
    public static final String TAO_USER_LOCK                = "tao_u_%d_lock";
    /**淘新闻用户特定 key */
    public static final String TAO_USER_ZSET                = "tao_u_%d_zset";
    /** 淘新闻用户bloom hash  */
    public static final String TAO_USER_HASH                = "tao_u_%d_hash";
    
    public static final String TAO_USER_NEWS_HISTORY        = "tao_u_news_h_%d";
    
    public static final String TAO_USER_FIRST_LIKE           = "tao_u_like_%d";
    
}
