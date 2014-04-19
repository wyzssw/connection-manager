package com.wap.sohu.recom.constants;

/**
 * 类ExpireTimeConstants.java的实现描述：TODO 类实现描述
 *
 * @author hongfengwang 2012-7-27 下午02:17:25
 */
public class ExpireTimeConstants {

    /**
     * 用户订阅过期时间
     */
    public static final int USER_SUB_EXP_TIME      = 1 * 60 * 60;

    /** 默认订阅过期时间 */
    public static final int DEFAULT_SUB_EXP_TIME   = 7 * 24 * 60 * 60;

    /** 基站定位数据的缓存时间 */
    public static final int Location_EXP_TIME      = 7 * 24 * 60 * 60;

    /**
     * GPS定位未定位到数据的缓存时间
     */
    public static final int GPS_NO_EXP_TIME   =  10 * 60;
    /**
     * 基站定位未定位到数据的缓存时间
     */
    public static final int LOC_NO_EXP_TIME   =  24 * 60 * 60;
    /** 用户推荐组图队列的缓存时间 */
    // public static final int USER_GROUPPIC_EXP_TIME = 24 * 60 * 60;
    // redis撑不住缩短下缓存时间
    public static final int USER_GROUPPIC_EXP_TIME = 12 * 60 * 60;
    /** 缓存1小时 */
    public static final int MEMCACHED_ONE_HOUR     = 60 * 60;

    /** 缓存半小时 */
    public static final int MEMCACHED_HALF_HOUR    = 30 * 60;
    /** 用户阅读历史过期时间 */
    public static final int USER_HISTORY_EXP_TIME  = 48 * 60 * 60;
    /** 用户不喜欢记录过期时间 */
    public static final int USER_UNLIKE_EXP_TIME   = 24 * 60 * 60;
    /** 缓存1天 */
    public static final int MEMCACHED_ONE_DAY      = 24 * 60 * 60;
    /** 缓存1分钟 */
    public static final int MEMCACHED_ONE_MINUTE   = 1 * 60;
    /** 缓存10分钟 */
    public static final int MEMCACHED_TEN_MINUTE   = 10 * 60;



}
