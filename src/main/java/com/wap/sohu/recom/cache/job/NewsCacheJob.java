/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.cache.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.wap.sohu.recom.cache.core.ICache;
import com.wap.sohu.recom.cache.manager.NewsCacheManager;
import com.wap.sohu.recom.constants.CommonConstants;
import com.wap.sohu.recom.constants.RedisKeyConstants;
import com.wap.sohu.recom.constants.TopNewsRedisKeyConstants;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.dao.NewsChannelDao;
import com.wap.sohu.recom.dao.NewsTermDao;
import com.wap.sohu.recom.service.MChannelNewsService;
import com.wap.sohu.recom.utils.ConvertUtils;
import com.wap.sohu.recom.utils.DateUtil;

/**
 * 定时更新任务
 * @author hongfengwang 2012-11-1 上午11:18:04
 */
@Service
public class NewsCacheJob {

    @Autowired
    private NewsTermDao newsTermDao;

    @Autowired
    private NewsChannelDao newsChannelDao;

    @Autowired
    private MChannelNewsService mChannelNewsService;

    @Autowired
    private StringRedisTemplateExt stringRedisTemplateMChannel;

    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateRecom;

    private final Lock lock = new ReentrantLock();
    private boolean canExecute =false;

    @SuppressWarnings("unused")
    @PostConstruct
    private void init(){
        String appName = System.getProperty("appName");
        if (StringUtils.isNotBlank(appName)&&appName.equals("newsRecom")) {
            canExecute = true;
        }
    }


    @SuppressWarnings("unused")
    @Scheduled(fixedDelay=300000)
    private void updateNoPubNews(){
        if (!canExecute) {
            return;
        }
        ICache<String, Set<Integer>> iCache = NewsCacheManager.getNotPubNews();
        Date date = DateUtils.addDays(new Date(), -1);
        List<Integer> list  = newsTermDao.getTermNews(newsTermDao.getNoPubTermIds(date));
        Set<Integer> set = new HashSet<Integer>(list);
        iCache.put(CommonConstants.FILTER_PUB_KEY, set);
    }


    @SuppressWarnings("unused")
    @Scheduled(fixedDelay=300000)
    private void updateDeletedNews(){
        if (!canExecute) {
            return;
        }
        ICache<String, Set<Integer>> iCache = NewsCacheManager.getNewsDeletedNews();
//        Long expireLong = DateUtil.getUnixTime(-CommonConstants.UPDATE_DAYS+3, TimeUnit.DAYS);
        Long expireLong = DateUtil.getUnixTime(-CommonConstants.UPDATE_DAYS+8, TimeUnit.DAYS);
        Set<String> set =shardedRedisTemplateRecom.opsForZSet().rangeByScore(RedisKeyConstants.NEWS_DEL_ZSET, expireLong, System.currentTimeMillis()/1000);
        Set<Integer> intSet = ConvertUtils.convert2intList(set);
        iCache.put(CommonConstants.NEWS_DELETED_CACHE, intSet);
    }

    @SuppressWarnings("unused")
    @Scheduled(fixedDelay=3600000)
    private void updateEditedNews(){
        if (!canExecute) {
            return;
        }
        ICache<String, Set<Integer>> iCache = NewsCacheManager.getEditedNewsCache();
        Set<Integer> noPubSet = NewsCacheManager.getNotPubNews().get(CommonConstants.FILTER_PUB_KEY);
        Set<Integer> filterTypeSet = NewsCacheManager.getFilterTypeNews().get(CommonConstants.NEWS_FILTER_TYPE_KEY);
        Date date = DateUtils.addDays(new Date(), -1);
        List<Integer> list  = newsTermDao.getTermNews(newsTermDao.getPubedTermId(date));
        List<Integer> chList = newsChannelDao.getChannelNewsIds(date);
        Set<Integer> set = new HashSet<Integer>(list);
        set.addAll(chList);
        if (noPubSet!=null) {
            set.removeAll(noPubSet);
        }
        if (filterTypeSet==null) {
            updateFilterTypeNews();
            filterTypeSet= NewsCacheManager.getFilterTypeNews().get(CommonConstants.NEWS_FILTER_TYPE_KEY);
        }
        if (filterTypeSet!=null) {
            set.removeAll(filterTypeSet);
        }
        iCache.put(CommonConstants.EDITED_NEWS_KEY, set);
    }

    @Scheduled(fixedDelay=1800000)
    private void updateFilterTypeNews(){
        if (!canExecute) {
            return;
        }
        //防止锁表
        lock.lock();
        try {
            ICache<String, Set<Integer>> iCache = NewsCacheManager.getFilterTypeNews();
            Date date = DateUtils.addDays(new Date(), -2);
            List<Integer> newsList = new ArrayList<Integer>();
            List<Integer> filterList = newsTermDao.getSkipTermNews(null, date);
            Set<Integer> set = new HashSet<Integer>(newsList);
            set.addAll(filterList);
            iCache.put(CommonConstants.NEWS_FILTER_TYPE_KEY, set);
        }finally{
            lock.unlock();
        }
    }



    @SuppressWarnings("unused")
    @Scheduled(fixedDelay=300000)
    private void getMChannelStat(){
        String statStr = stringRedisTemplateMChannel.opsForValue().get(TopNewsRedisKeyConstants.MCHANNEL_NEWS_RECOM_STAT);
        if (StringUtils.isNotBlank(statStr)&&statStr.equals("on")) {
            mChannelNewsService.setOnStat(true);
        }else {
            mChannelNewsService.setOnStat(false);
        }
        Map<Object, Object> map = stringRedisTemplateMChannel.opsForHash().entries(TopNewsRedisKeyConstants.EACH_CHANNEL_RECOM_STAT_HASH);
        if (map==null) {
            return;
        }
        Map<Integer, Boolean> statMap =new ConcurrentHashMap<Integer, Boolean>();
        for (Map.Entry<Object, Object> item : map.entrySet()) {
            if(StringUtils.isNotBlank(item.getValue().toString())&&item.getValue().toString().equals("on")){
                statMap.put(Integer.valueOf(item.getKey().toString()), Boolean.TRUE);
            }
            if(StringUtils.isNotBlank(item.getValue().toString())&&item.getValue().toString().equals("off")){
                statMap.put(Integer.valueOf(item.getKey().toString()), Boolean.FALSE);
            }
        }
        mChannelNewsService.copyEachStat(statMap);
    }

}
