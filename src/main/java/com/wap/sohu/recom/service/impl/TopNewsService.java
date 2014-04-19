/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.wap.sohu.recom.cache.core.ICache;
import com.wap.sohu.recom.cache.manager.TopNewsCacheManager;
import com.wap.sohu.recom.constants.CommonConstants;
import com.wap.sohu.recom.constants.TopNewsRedisKeyConstants;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.model.NewsContent;
import com.wap.sohu.recom.service.MChannelNewsService;
import com.wap.sohu.recom.service.NewsCacheService;
import com.wap.sohu.recom.service.NewsFilterService;
import com.wap.sohu.recom.service.NewsTagService;
import com.wap.sohu.recom.service.PropertyProxy;
import com.wap.sohu.recom.utils.ConvertUtils;

/**
 * 类TopNewsService.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2013-6-17 下午02:33:33
 */
@Service
public class TopNewsService {
    
    private  Map<Integer, Double>    incrMap; 
    
    /**
     * 进入热门列表的阀值，如果超过这个点击次数，就进入，并且属于当天新闻
     */
    private static  Integer  threadHold = 400;
    
    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateRecom;
    
    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateUser;
    
    @Autowired
    private NewsCacheService newsCacheService;
    
    @Autowired
    private NewsTagService newsTagService;
    
    
    @Autowired
    private MessageSource messageSource;
    
    @Autowired
    private MChannelNewsService mChannelNewsService;
    
    private Set<Integer> filterSet;
    
    @Autowired
    private NewsFilterService newsFilterService;
    
    @Autowired
    private PropertyProxy propertyProxy;
    
    private static final Logger LOGGER  = Logger.getLogger(TopNewsService.class);
    
    private static Set<Integer> topCatIds  =  new HashSet<Integer>();
    
    private Set<Integer> subIdSet = new HashSet<Integer>();
    
    @PostConstruct
    private void getFilterCatId(){
        String msg = messageSource.getMessage("filter_cat_ids", null, Locale.getDefault());
        List<String> tmpList =  Arrays.asList(msg.split(","));
        filterSet = ConvertUtils.convert2intList(new HashSet<String>(tmpList));
        String threadHoldStr = messageSource.getMessage("hot_news_threadhold",null, Locale.getDefault());
        threadHold = Integer.valueOf(threadHoldStr);
        String[]    topCatIdArray = messageSource.getMessage("toutiao_top_ids", null, Locale.getDefault()).split(",");
        topCatIds.addAll(ConvertUtils.convert2intList(new HashSet<String>(Arrays.asList(topCatIdArray))));
        Map<Integer, Double> map = new LinkedHashMap<Integer, Double>();
        map.put(1, 100D);
        map.put(5, 60D);
        map.put(12,30D);
        map.put(48,10D);
        incrMap = Collections.unmodifiableMap(map);
        subIdSet.addAll(ConvertUtils.convertStringArray2IntegerList(propertyProxy.getProperty("main_channel_recom_subIds_source").split(",")));
    }
    
    @Async
    public  void processTopNews(long cid,int newsId,int channelId,String type){
        if (newsId==0||newsId==12621641) {
            return;
        }
        try {
            updateUserLike(cid,newsId);
            long count = updateTopNews(cid,newsId);
            mChannelNewsService.updateForMainChannel(newsId,count);
//            mChannelNewsService.removeNotLike(cid,newsId,channelId,type);
        } catch (Throwable e) {
            LOGGER.error("error!!which is cid="+cid+" and newsId="+newsId,e);
        }
    }

    
    @Async
    public void processTopGroupNews(long cid,int newsId){
        if (newsId==0) {
            return;
        }
        try {
            long count = updateHotGroupNews(newsId);
            mChannelNewsService.updateForMainChannel(newsId,count);
        } catch (Throwable e) {
            LOGGER.error("error!!which is cid="+cid+" and newsId="+newsId,e);
        }
    }

    /**
     * @param newsId
     * @return
     */
    private long updateHotGroupNews(int newsId) {
        Integer dupId =   newsCacheService.getDupId(newsId);
        newsId = (dupId==null||dupId==0)?newsId:dupId;
        String key  = String.format(TopNewsRedisKeyConstants.NEWS_HOT_COUNT,newsId);
        long count = shardedRedisTemplateRecom.opsForValue().increment(key,1);
        shardedRedisTemplateRecom.expire(key,CommonConstants.TOUTIAO_UPDATE_DAYS+1,TimeUnit.DAYS);
        if (newsFilterService.filterCommonNews(newsId,true)||newsFilterService.filterChannelNews(newsId)){
            return count;
        }
        Long time = newsTagService.getNewsTime(newsId);
        if (dupId!=null&&dupId!=0) {
            key  = String.format(TopNewsRedisKeyConstants.NEWS_HOT_COUNT,newsId);
            shardedRedisTemplateRecom.expire(key,CommonConstants.TOUTIAO_UPDATE_DAYS+1,TimeUnit.DAYS);
        }
        if (System.currentTimeMillis()/1000-time<DateUtils.MILLIS_PER_DAY/1000) {
            Long rank  = shardedRedisTemplateRecom.opsForZSet().rank(TopNewsRedisKeyConstants.HOT_NEW_ZSET, String.valueOf(newsId));
            if (rank!=null) {
                return count;
            }
            if (count>threadHold) {
                shardedRedisTemplateRecom.opsForZSet().add(TopNewsRedisKeyConstants.HOT_NEW_ZSET, String.valueOf(newsId),System.currentTimeMillis()/1000);
            }
        }
        return count;
    }

    /**
     * @param cid
     * @param newsId
     */
    private long updateTopNews(long cid, int newsId) {
            long count= updateHotNews(newsId);
            updateCatNews(newsId);
            return count;
    }

    /**
     * @param cid
     * @param newsId
     */
    private void updateUserLike(long cid, int newsId) {
        updateShortLike(cid,newsId);
    }
    
    /**
     * @param newsId
     */
    private void updateCatNews(int newsId) {
        Long time = newsTagService.getNewsTime(newsId);
        //超过两天前的就不更新了
        if (System.currentTimeMillis()/1000-time>2*DateUtils.MILLIS_PER_DAY/1000) {
            return;
        }
        Integer dupId =   newsCacheService.getDupId(newsId);
        newsId = (dupId==null||dupId==0)?newsId:dupId;
        time = newsTagService.getNewsTime(newsId);
        //超过1天前的就不更新了
        if (System.currentTimeMillis()/1000-time>1*DateUtils.MILLIS_PER_DAY/1000) {
            return;
        }
        Set<Integer> set = newsCacheService.getCatIdsCacheForOther(newsId);
        if (set==null||newsFilterService.filterCatNews(newsId,false)) {
            return;
        }
        long hours =  TimeUnit.SECONDS.toHours(System.currentTimeMillis()/1000-time);
        for (Integer catId : set) {
            String key  = String.format(TopNewsRedisKeyConstants.CAT_NEWS_MATRIX_TOUTIAO,catId);
            //不在 cat 矩阵的新闻有可能是小说和过滤新闻，所以删除掉
            Double score = shardedRedisTemplateRecom.opsForZSet().score(key, String.valueOf(newsId));
            if (score==null||score<=0||score-time>DateUtils.MILLIS_PER_DAY/1000) {
                return;
            }
            shardedRedisTemplateRecom.opsForZSet().incrementScore(key, String.valueOf(newsId), getIncr(hours));
        }
        NewsContent newsContent = newsCacheService.getNewsContent(newsId);
        if (newsContent==null) {
            return ;
        }
        List<Integer> subList = newsContent.getSubIds();
        if (subList==null||subList.isEmpty()||Collections.disjoint(subList, subIdSet)) {
            return;
        }
        for (Integer catId : set) {
            String key_channel  = String.format(TopNewsRedisKeyConstants.CAT_NEWS_MATRIX_CHANNEL,catId);
            Double score_channel = shardedRedisTemplateRecom.opsForZSet().score(key_channel, String.valueOf(newsId));
            if (score_channel==null||score_channel<=0||score_channel-time>DateUtils.MILLIS_PER_DAY/1000) {
                return;
            }
            shardedRedisTemplateRecom.opsForZSet().incrementScore(key_channel, String.valueOf(newsId), getIncr(hours));
        }
    }

    /**
     * @param newsId
     */
    private long updateHotNews(int newsId) {
        String key  = String.format(TopNewsRedisKeyConstants.NEWS_HOT_COUNT,newsId);
        long count = shardedRedisTemplateRecom.opsForValue().increment(key,1);
        shardedRedisTemplateRecom.expire(key,CommonConstants.TOUTIAO_UPDATE_DAYS+1,TimeUnit.DAYS);
        Integer dupId =   newsCacheService.getDupId(newsId);
        newsId = (dupId==null||dupId==0)?newsId:dupId;
        if (newsFilterService.filterCommonNews(newsId,false)||newsFilterService.filterChannelNews(newsId)){
            return 0L;
        }
        if (dupId!=null&&dupId!=0) {
            key  = String.format(TopNewsRedisKeyConstants.NEWS_HOT_COUNT,newsId);
            shardedRedisTemplateRecom.expire(key,CommonConstants.TOUTIAO_UPDATE_DAYS+1,TimeUnit.DAYS);
            count = shardedRedisTemplateRecom.opsForValue().increment(key,1);
        }
        Long time = newsTagService.getNewsTime(newsId);
        if (System.currentTimeMillis()/1000-time<DateUtils.MILLIS_PER_DAY/1000) {
            Long rank  = shardedRedisTemplateRecom.opsForZSet().rank(TopNewsRedisKeyConstants.HOT_NEW_ZSET, String.valueOf(newsId));
            if (rank!=null) {
                return count;
            }
            if (count>threadHold) {
                shardedRedisTemplateRecom.opsForZSet().add(TopNewsRedisKeyConstants.HOT_NEW_ZSET, String.valueOf(newsId),System.currentTimeMillis()/1000);
            }
        }
        return count;
    }


    /**
     * @param cid
     * @param newsId
     */
    private void updateShortLike(long cid, int newsId) {
        String key = String.format(TopNewsRedisKeyConstants.USER_SHORT_LIKE_CAT, cid);
        Set<Integer> set = newsCacheService.getCatIdsCacheForOther(newsId);
        if (set==null||set.size()==0) {
            return ;
        }
        for (Integer catId : set) {
            if (filterSet.contains(catId)||topCatIds.contains(catId)) {
                continue;
            }
            shardedRedisTemplateUser.opsForList().leftPush(key, String.valueOf(catId));
        }
        shardedRedisTemplateUser.opsForList().trim(key, 0, CommonConstants.SHORT_CAT_LIKE_COUNT);
        shardedRedisTemplateUser.expire(key, CommonConstants.TOUTIAO_UPDATE_DAYS+1, TimeUnit.DAYS);
    }
    
    
    public Set<Integer> getEditedToutiaoNews(){
        ICache<String, Set<Integer>> iCache = TopNewsCacheManager.getEditedToutiaoNews();
        Set<Integer> set = null;
        if ((set=iCache.get(CommonConstants.EDITED_NEWS_KEY))==null) {
             Set<String> setTmp = shardedRedisTemplateRecom.opsForZSet().range(TopNewsRedisKeyConstants.EDITED_UPDATE_NEWS, 0, -1);
             set = ConvertUtils.convert2intList(setTmp);
             iCache.put(CommonConstants.EDITED_NEWS_KEY,set);
        }
        return set;
    }

    private Double getIncr(Long hours){
        Double incr = 0D ;
        for (Map.Entry<Integer, Double> entry : incrMap.entrySet()) {
            if (entry.getKey()>hours) {
                incr = entry.getValue();
                break;
            }
        }
      return incr;
    }
   
}
