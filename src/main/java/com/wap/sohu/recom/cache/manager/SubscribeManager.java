package com.wap.sohu.recom.cache.manager;

import java.util.List;

import com.wap.sohu.recom.cache.core.AbstractCacheManager;
import com.wap.sohu.recom.cache.core.ICache;
import com.wap.sohu.recom.model.SubscriptionDo;
import com.wap.sohu.recom.model.TermInfo;

/**
 * 类SubscribeManager.java的实现描述：刊物推荐 本地缓存
 *
 * @author yeyanchao 2012-12-7 上午10:38:03
 */
public class SubscribeManager {

    /**
     * 默认订阅刊物缓存
     */
    private static final ICache<String, List<Integer>>         DEFAULT_SUB_CACHE   = AbstractCacheManager.getInstance().getCache("defaultSub",
                                                                                                                                 100,
                                                                                                                                 3 * 60 * 60);

    /**
     * 热门刊物
     */
    private static final ICache<String, List<SubscriptionDo>>  HOT_SUB_CACHE       = AbstractCacheManager.getInstance().getCache("hotSub",
                                                                                                                                 100,
                                                                                                                                 3 * 60 * 60);
    /**
     * 区域订阅刊物
     */
    private static final ICache<String, List<SubscriptionDo>>  LOCATION_SUB_CACHE  = AbstractCacheManager.getInstance().getCache("locationSub",
                                                                                                                                 2000,
                                                                                                                                 3 * 60 * 60);

    /**
     * 推荐订阅刊物
     */
    private static final ICache<Integer, List<SubscriptionDo>> RECOM_SUB_CACHE     = AbstractCacheManager.getInstance().getCache("recomSub",
                                                                                                                                 2000,
                                                                                                                                 3 * 60 * 60);

    /**
     * 用户订阅刊物
     */
    private static final ICache<Long, List<Integer>>           USER_SUB_CACHE      = AbstractCacheManager.getInstance().getCache("userSub",
                                                                                                                                 3000,
                                                                                                                                 20 * 60);
    /** 刊物的生成时间 */
    private static final ICache<Integer,TermInfo>                     TERM_TIME_CACHE  = AbstractCacheManager.getInstance().getCache("term_time_cache",100,60*60*5); 
    
    public static ICache<Integer,TermInfo>  getTermInfoCache(){
        return TERM_TIME_CACHE;
    }

    /**
     * 运营推荐刊物列表：
     */
    private static final ICache<String, List<Integer>>         OPERATION_SUB_CACHE = AbstractCacheManager.getInstance().getCache("opSub",
                                                                                                                                 3000,
                                                                                                                                 20 * 60 * 60);

    /**
     * 刊物分类
     */
    private static final ICache<Integer, List<Integer>>        SUB_TYPE_CACHE      = AbstractCacheManager.getInstance().getCache("subType",
                                                                                                                                 3000,
                                                                                                                                 20 * 60 * 60);

    public static ICache<String, List<Integer>> getDefaultSub() {
        return DEFAULT_SUB_CACHE;
    }

    public static ICache<String, List<SubscriptionDo>> getHotSub() {
        return HOT_SUB_CACHE;
    }

    public static ICache<String, List<SubscriptionDo>> getLocationSub() {
        return LOCATION_SUB_CACHE;
    }

    public static ICache<Integer, List<SubscriptionDo>> getRecomSub() {
        return RECOM_SUB_CACHE;
    }

    public static ICache<Long, List<Integer>> getUserSub() {
        return USER_SUB_CACHE;
    }

    public static ICache<String, List<Integer>> getOperationSubs() {
        return OPERATION_SUB_CACHE;
    }

    public static ICache<Integer, List<Integer>> getSubTypes() {
        return SUB_TYPE_CACHE;
    }

}
