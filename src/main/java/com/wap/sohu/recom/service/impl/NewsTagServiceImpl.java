/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wap.sohu.recom.cache.core.ICache;
import com.wap.sohu.recom.cache.manager.NewsCacheManager;
import com.wap.sohu.recom.cache.manager.TopNewsCacheManager;
import com.wap.sohu.recom.constants.CommonConstants;
import com.wap.sohu.recom.constants.RedisKeyConstants;
import com.wap.sohu.recom.constants.TopNewsRedisKeyConstants;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.model.NewsContent;
import com.wap.sohu.recom.service.NewsCacheService;
import com.wap.sohu.recom.service.NewsTagService;
import com.wap.sohu.recom.utils.DateUtil;


/**
 * 通过newsId查tag，传递tag到列内聚模型的服务实现类
 * @author hongfengwang 2012-9-4 下午03:17:29
 */
@Component("newsTagService")
public class NewsTagServiceImpl implements NewsTagService {
    
    @Autowired
    private StringRedisTemplateExt redisTemplateBase;
    
    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateRecom;
    
    
    @Autowired
    private NewsCacheService newsCacheService;
    
    
    
    
    /**
     * 获取新闻生成时间
     * @param newsList
     * @return
     */
    @Override
    public Map<Integer, Long> getNewsTimeMap(List<Integer> newsList){
        Map<Integer, Long> newsTimeMap = new HashMap<Integer, Long>();
        for (Integer newsId : newsList) {
            Long timeStamp = getNewsTime(newsId);
            newsTimeMap.put(newsId, timeStamp);
       }
       return newsTimeMap;
    }
    
    


    /**
     * @param newsId
     * @return 新闻创建时间，单位秒
     */
    @Override
    public Long getNewsTime(Integer newsId) {
        String key = String.format(RedisKeyConstants.NEWS_TIME_MAPPING, newsId);
        ICache<Integer,Long> iCache = NewsCacheManager.getNewsTimeCache();
        Long timeStamp = iCache.get(newsId);
        String temp = "";   
        if (timeStamp==null) {
            temp = shardedRedisTemplateRecom.opsForValue().get(key);
            if (StringUtils.isNotBlank(temp)) {
                timeStamp = Long.parseLong(temp);
                iCache.put(newsId, timeStamp);
            } else {
//                timeStamp = newsTagDao.getNewsTime(newsId);
                NewsContent newsContent = newsCacheService.getNewsContent(newsId);
                if (newsContent==null) {
                    return 0L;
                }
                Date date = newsContent.getNTime();
                if (date==null) {
                    iCache.put(newsId, 0L);
                    return 0L;
                }
                timeStamp = date.getTime()/1000;
                iCache.put(newsId, timeStamp);
                shardedRedisTemplateRecom.opsForValue().set(key, timeStamp.toString(), 7L, TimeUnit.DAYS);
            }
        }
        return timeStamp;
    }

    
    @SuppressWarnings("unchecked")
    @Override
    public List<Integer> getNotInCatIds(int newsId,List<Integer> newsList, int count) {
        if (newsList.size()==0) {
            return new ArrayList<Integer>();
        }
        //存储的是要删除的新闻id
        List<Integer>  rmList = new ArrayList<Integer>();
        Set<String> catSet = newsCacheService.getCatsCache(newsId,true);
        if (catSet==null||catSet.size()==0) {
            return  new ArrayList<Integer>();
        }
        for (Integer hisKey : newsList) {
            Set<String>  hisCat = newsCacheService.getCatsCache(hisKey,true);
            //hisMap为空有可能该新闻已经过期了
            if (hisCat==null||hisCat.size()==0) {
                rmList.add(hisKey);
                continue;
            }
            Collection<String> cc = CollectionUtils.intersection(hisCat, catSet);
            if (cc.size()>0) {
                continue;
            }
            rmList.add(hisKey);            
        }
        return rmList;
    }

    
    @Override
    public void setDelNewsToRedis(List<Integer> list) {
         Long timeStamp  = System.currentTimeMillis()/1000;
         Long expireLong = DateUtil.getUnixTime(-CommonConstants.UPDATE_DAYS+8, TimeUnit.DAYS);
         for (Integer id : list) {
             shardedRedisTemplateRecom.opsForZSet().add(RedisKeyConstants.NEWS_DEL_ZSET, String.valueOf(id), Double.valueOf(timeStamp));
         }
         shardedRedisTemplateRecom.opsForZSet().removeRangeByScore(RedisKeyConstants.NEWS_DEL_ZSET, 0, expireLong);
    }
    
    @Override
    public Set<Integer> getDelNews(){
        Set<Integer> retSet = new HashSet<Integer>();
        Set<String> set = redisTemplateBase.opsForZSet().range(RedisKeyConstants.NEWS_DEL_ZSET, 0, -1);
        for (String string : set) {
             retSet.add(Integer.valueOf(string));
        }
        return retSet;
    }


    @Override
    public boolean hasCommonCat(int newsId, Integer dupId) {
        Set<String> mySet = newsCacheService.getCatsCache(newsId,true);
        Set<String> hisSet = newsCacheService.getCatsCache(dupId,true);
        if (mySet==null||hisSet==null) {
            return false;
        }
        Collection<String> cc = CollectionUtils.intersection(mySet, hisSet);
        if (cc.size()>0) {
            return true;
        }
        return false;
    }
    
    /**
     * query category news
     * 要闻频道和大头条共用这个key cat_news_matrix_toutiao
     * @param catId
     * @return
     */
    @Override
    public List<Integer> queryCatNews(Integer catId) {
        List<Integer> catNewsCache = TopNewsCacheManager.getCatTopNewsCache().get(catId);
        if (catNewsCache == null || catNewsCache.isEmpty()) {
            String queryKey = String.format(TopNewsRedisKeyConstants.CAT_NEWS_MATRIX_TOUTIAO, catId);
            Set<String> catNewsSet = shardedRedisTemplateRecom.opsForZSet().reverseRange(queryKey,0, 160);
            catNewsCache = new ArrayList<Integer>();
            if (catNewsSet != null && !catNewsSet.isEmpty()) {
                for (String tuple : catNewsSet) {
                    catNewsCache.add(Integer.parseInt(tuple));
                }
            }
            TopNewsCacheManager.getCatTopNewsCache().put(catId, catNewsCache);
        }
        return catNewsCache;
    }




    @Override
    public List<Integer> querySelectedCatNews(Integer catId) {
        List<Integer> catNewsCache = TopNewsCacheManager.getCatSelectedNewsCache().get(catId);
        if (catNewsCache == null || catNewsCache.isEmpty()) {
            String queryKey = String.format(TopNewsRedisKeyConstants.CAT_NEWS_MATRIX_CHANNEL, catId);
            Set<String> catNewsSet = shardedRedisTemplateRecom.opsForZSet().reverseRange(queryKey,0, 200);
            catNewsCache = new ArrayList<Integer>();
            if (catNewsSet != null && !catNewsSet.isEmpty()) {
                for (String tuple : catNewsSet) {
                    catNewsCache.add(Integer.parseInt(tuple));
                }
            }
            TopNewsCacheManager.getCatSelectedNewsCache().put(catId, catNewsCache);
        }
        return catNewsCache;
    }
       

    
}
