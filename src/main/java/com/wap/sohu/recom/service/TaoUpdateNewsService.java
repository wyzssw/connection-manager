/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service;

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
import org.springframework.stereotype.Service;

import com.wap.sohu.recom.constants.CommonConstants;
import com.wap.sohu.recom.constants.TaoRecomRedisKeyConstants;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.log.AccessTraceLogData;
import com.wap.sohu.recom.log.AccessTraceLogData.LogCategoryEnum;
import com.wap.sohu.recom.log.AccessTraceLogData.LogKeyEnum;
import com.wap.sohu.recom.log.StatisticLog;
import com.wap.sohu.recom.utils.ConvertUtils;

/**
 * 新闻点击处理类
 * @author hongfengwang 2013-8-7 下午12:10:50
 */
@Service
public class TaoUpdateNewsService {
    
    private static final Logger LOGGER = Logger.getLogger(TaoUpdateNewsService.class);
    
    @Autowired
    private NewsFilterService newsFilterService;
    
    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateRecom;
    
    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateUser;
    
    @Autowired
    private NewsCacheService newsCacheService;
    
    @Autowired
    private NewsTagService newsTagService;
    
    private static final Integer threadHold = 400;
    
    @Autowired
    private PropertyProxy propertyProxy;
    
    private Set<Integer> filterSet;
    
    private  Map<Integer, Double>    incrMap; 
    
    private static Set<Integer> topCatIds  =  new HashSet<Integer>();
    
    @PostConstruct
    public void init(){
        Map<Integer, Double> map = new LinkedHashMap<Integer, Double>();
        map.put(1, 100D);
        map.put(5, 60D);
        map.put(12,30D);
        map.put(48,10D);
        incrMap = Collections.unmodifiableMap(map);
        String msg = propertyProxy.getProperty("filter_cat_ids");
        List<String> tmpList =  Arrays.asList(msg.split(","));
        filterSet = ConvertUtils.convert2intList(new HashSet<String>(tmpList));
        String[]    topCatIdArray = propertyProxy.getProperty("toutiao_top_ids").split(",");
        topCatIds.addAll(ConvertUtils.convert2intList(new HashSet<String>(Arrays.asList(topCatIdArray))));
    }

    /**
     * @param cid
     * @param pid
     * @param newsId
     * @param type
     */
    public void processClick(long cid, long pid, Integer newsId, String type) {
        if (newsId==0) {
            return;
        }
        try {
            updateUserLike(cid,newsId);
            updateTaoNews(cid,newsId);
        } catch (Throwable e) {
            LOGGER.error("error!!which is cid="+cid+" and newsId="+newsId,e);
        }
        scribeLog(cid,pid,newsId,type);
    }

    /**
     * @param cid
     * @param newsId
     * @return
     */
    private long updateTaoNews(long cid, Integer newsId) {
        long count = updateHotNews(newsId);
        updateCatNews(newsId);
        return count;
    }

    /**
     * @param newsId
     */
    private void updateCatNews(Integer newsId) {
        if (newsFilterService.filterCatNewsByTime(newsId, 1, TimeUnit.DAYS)) {
            return;
        }
        Integer dupId =   newsCacheService.getDupId(newsId);
        newsId = (dupId==null||dupId==0)?newsId:dupId;
        if (newsFilterService.filterCatNewsByTime(newsId, 1, TimeUnit.DAYS)) {
            return ;
        }
        Set<Integer> set = newsCacheService.getCatIdsCacheForOther(newsId);
        if (set==null||newsFilterService.filterCatNews(newsId,false)) {
            return;
        }
        Long time = newsTagService.getNewsTime(newsId);
        long hours =  TimeUnit.SECONDS.toHours(System.currentTimeMillis()/1000-time);
        for (Integer catId : set) {
            String key  = String.format(TaoRecomRedisKeyConstants.CAT_NEWS_MATRIX_TAO,catId);
            //不在 cat 矩阵的新闻有可能是小说和过滤新闻，所以删除掉
            Double score = shardedRedisTemplateRecom.opsForZSet().score(key, String.valueOf(newsId));
            if (score==null||score<=0||score-time>DateUtils.MILLIS_PER_DAY/1000) {
                return;
            }
            shardedRedisTemplateRecom.opsForZSet().incrementScore(key, String.valueOf(newsId), getIncr(hours));
        }
        
    }

    /**
     * @param newsId
     * @return
     */
    private long updateHotNews(Integer newsId) {
        String key  = String.format(TaoRecomRedisKeyConstants.NEWS_HOT_COUNT,newsId);
        shardedRedisTemplateRecom.expire(key,CommonConstants.TAONEWS_UPDATE_DAYS+1,TimeUnit.DAYS);
        long count = shardedRedisTemplateRecom.opsForValue().increment(key,1);
        Integer dupId =   newsCacheService.getDupId(newsId);
        newsId = (dupId==null||dupId==0)?newsId:dupId;
        if (newsFilterService.filterCommonNews(newsId,false)||newsFilterService.filterChannelNews(newsId)){
            return 0L;
        }
        if (dupId!=null&&dupId!=0) {
            key  = String.format(TaoRecomRedisKeyConstants.NEWS_HOT_COUNT,newsId);
            shardedRedisTemplateRecom.expire(key,CommonConstants.TAONEWS_UPDATE_DAYS+1,TimeUnit.DAYS);
            count = shardedRedisTemplateRecom.opsForValue().increment(key,1);
        }
        Long time = newsTagService.getNewsTime(newsId);
        if (System.currentTimeMillis()/1000-time<DateUtils.MILLIS_PER_DAY/1000) {
            Long rank  = shardedRedisTemplateRecom.opsForZSet().rank(TaoRecomRedisKeyConstants.HOT_NEW_ZSET, String.valueOf(newsId));
            if (rank!=null) {
                return count;
            }
            if (count>threadHold) {
                shardedRedisTemplateRecom.opsForZSet().add(TaoRecomRedisKeyConstants.HOT_NEW_ZSET, String.valueOf(newsId),System.currentTimeMillis()/1000);
            }
        }
        return count;
    }

    /**
     * @param cid
     * @param newsId
     */
    private void updateUserLike(long cid, Integer newsId) {
        updateShortLike(cid,newsId);
    }

    /**
     * @param cid
     * @param pid
     * @param newsId
     * @param type
     */
    private void scribeLog(long cid, long pid, Integer newsId, String type) {
        AccessTraceLogData logData = new AccessTraceLogData(LogCategoryEnum.taoClickStat);
        logData.add(LogKeyEnum.Cid, cid);
        logData.add(LogKeyEnum.PID,pid+"");
        logData.add(LogKeyEnum.RecType,type);
        logData.add(LogKeyEnum.TimeStamp,System.currentTimeMillis());
        StatisticLog.info(logData);
    }

    /**
     * 
     */
    private void updateShortLike(long cid, int newsId) {
        String key = String.format(TaoRecomRedisKeyConstants.USER_SHORT_LIKE_CAT, cid);
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
        shardedRedisTemplateUser.opsForList().trim(key, 0, CommonConstants.TAO_SHORT_CAT_LIKE_COUNT);
        shardedRedisTemplateUser.expire(key, CommonConstants.TAONEWS_UPDATE_DAYS+1, TimeUnit.DAYS);
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
