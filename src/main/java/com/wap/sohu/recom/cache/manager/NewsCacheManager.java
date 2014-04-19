/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.cache.manager;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wap.sohu.recom.cache.core.AbstractCacheManager;
import com.wap.sohu.recom.cache.core.ICache;
import com.wap.sohu.recom.model.NewsContent;

/**
 * 类NewsCacheManager.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2012-9-4 下午06:12:32
 */
public class NewsCacheManager {

     
    /** 本地缓存新闻-时间的映射表 */
    private static final ICache<Integer,Long>                     NEWS_TIME_CACHE         = AbstractCacheManager.getInstance().getCache("news_time_cache", 20000, 60*60*8);
    
    /** 本地缓存news-cat列表  */
    private static final ICache<Integer, Set<String>>             NEWS_CAT_CACHE         =  AbstractCacheManager.getInstance().getCache("news_cat_cache", 5000, 60*60*3);
    
    /**本地缓存news-catid列表 */
    private static final ICache<Integer, Set<Integer>>          NEWS_CATID_CACHE          =  AbstractCacheManager.getInstance().getCache("news_catid_cache", 20000, 60*60*6);
    
    private static final ICache<Integer, Map<Integer, Double>>   NEWS_SIM_CACHE           =  AbstractCacheManager.getInstance().getCache("news_sim_cache",30000,60*5);
    
    private static final ICache<Integer, Map<Integer, Double>>       TAG_TOP_CACHE        =  AbstractCacheManager.getInstance().getCache("tag_top_cache",30000,60*60*3);
    
    /** newsType缓存 */
    private static final ICache<Integer, Integer>                    NEWS_DUP_CACHE       =  AbstractCacheManager.getInstance().getCache("news_dup_cache",30000,60*60*3);
    
    private static final ICache<String, Set<Integer>>                NOT_PUB_NEWS         =  AbstractCacheManager.getInstance().getCache("not_pub_news",2000,60*60*1);
    
    /**期刊新闻和即时新闻优先 */
    private static final ICache<String, Set<Integer>>                EDITED_NEWS          =  AbstractCacheManager.getInstance().getCache("edited_news",10000,60*60*1);
    
    private static final ICache<Integer, String>                  NEWS_TITLE_CAHCE        =  AbstractCacheManager.getInstance().getCache("news_title_cache",20000,60*60*3);
    
    private static final ICache<Integer, String>                  NEWS_CONTENT_CAHCE      =  AbstractCacheManager.getInstance().getCache("news_content_cache",20000,60*60*3);
    
    private static final ICache<String, Set<Integer>>             NEWS_FILTER_TYPE_CACHE  =  AbstractCacheManager.getInstance().getCache("news_type_cache",50,60*60*3);
    
    private static final ICache<String, Set<Integer>>             NEWS_LAST_HOUR_CACHE    =  AbstractCacheManager.getInstance().getCache("news_last_cache",10000,60*60*5);
    
    private static final ICache<Integer, Map<Integer, Double>>    NEWS_SCORE_CACHE        =  AbstractCacheManager.getInstance().getCache("news_score_cache",40000,60*60);
    
    /**itembase产生的新闻相似列表缓存 */
    private static final ICache<Integer, List<Integer>>            NEWS_ITEM_SIM_CACHE    = AbstractCacheManager.getInstance().getCache("news_item_sim_cache", 20000, 60*60*3);
    
    /** 新闻--->pubId的关系 */
    private static final ICache<Integer, Integer>                  NEWS_PUBID_CACHE       = AbstractCacheManager.getInstance().getCache("news_pubid_cache",6000,60*20);
    
    /** pubid-新闻对应关系  */
    private static final ICache<Integer, List<Integer>>            PUBID_NEWS_LIST        = AbstractCacheManager.getInstance().getCache("pubid_news_list",100,60*60);
    
    private static final ICache<Integer, NewsContent>              NEWS_ATTRIBUTE_CACHE  =  AbstractCacheManager.getInstance().getCache("news_attribute_cache",40000,60*60*10);
    
    /**删除的新闻 */
    private static final ICache<String,Set<Integer>>               NEWS_DELETED_NEWS     =  AbstractCacheManager.getInstance().getCache("news_deleted_news",1000,60*60*5);
    
    private static final ICache<String,Set<Integer>>               NEWS_WEMIDIA_SUBIDS   =  AbstractCacheManager.getInstance().getCache("news_wemidia_subids",1000,60*60*48);
    
    public static  ICache<String,Set<Integer>>  getNewsWemidiaSubids(){
        return NEWS_WEMIDIA_SUBIDS;
    }
    
    public static ICache<String, Set<Integer>> getNewsDeletedNews(){
        return NEWS_DELETED_NEWS;
    }
    
    public static ICache<Integer, List<Integer>>   getPubIdNewsList(){
        return PUBID_NEWS_LIST;
    }
    
    public static ICache<Integer, Integer>        getNewsPubIdCache(){
        return NEWS_PUBID_CACHE;
    }
    
    
    /**  */
    public static ICache<Integer, Set<Integer>>                getNewsCatidCache(){
           return NEWS_CATID_CACHE;
    }
    
    /**
     * @return the newsItemSimCache
     */
    public static ICache<Integer, List<Integer>>               getNewsItemSimCache() {
        return NEWS_ITEM_SIM_CACHE;
    }

    public static final ICache<String, Set<Integer>>            getLastHourNews(){
        return NEWS_LAST_HOUR_CACHE;
    }
    
    public static  final  ICache<String,Set<Integer>>           getFilterTypeNews(){
        return NEWS_FILTER_TYPE_CACHE;
    }
    
    
    public static ICache<String, Set<Integer>> getEditedNewsCache(){
        return EDITED_NEWS;
    }
    
    /**
     * @return the newsTitleCahce
     */
    public static ICache<Integer, String> getNewsTitleCahce() {
        return NEWS_TITLE_CAHCE;
    }
    
    /**
     * @return the newsContentCahce
     */
    public static ICache<Integer, String> getNewsContentCahce() {
        return NEWS_CONTENT_CAHCE;
    }
    
    
    public static final ICache<String, Set<Integer>> getNotPubNews() {
        return NOT_PUB_NEWS;
    }
    
    /**
     * @return the newsDupCache
     */
    public static ICache<Integer, Integer> getNewsDupCache() {
        return NEWS_DUP_CACHE;
    }

    /**
     * @return the tagTopCache
     */
    public static ICache<Integer, Map<Integer, Double>> getTagTopCache() {
        return TAG_TOP_CACHE;
    }

    /**
     * @return the newsSimCache
     */
    public static ICache<Integer,Map<Integer,Double>> getNewsSimCache() {
        return NEWS_SIM_CACHE;
    }
    
    public static ICache<Integer,Long> getNewsTimeCache(){
         return NEWS_TIME_CACHE;
    }

    
    public static ICache<Integer, Set<String>> getNewsCatCache() {
        return NEWS_CAT_CACHE;
    }

    /**
     * @return
     */
    public static ICache<Integer, Map<Integer, Double>> getNewsScore() {
        return NEWS_SCORE_CACHE;
    }

    /**
     * @return
     */
    public static ICache<Integer, NewsContent> getNewsContentMore() {
        return NEWS_ATTRIBUTE_CACHE;
    }
    

}
