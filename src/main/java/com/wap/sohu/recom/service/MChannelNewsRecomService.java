/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wap.sohu.recom.constants.CommonConstants;
import com.wap.sohu.recom.constants.TopNewsRedisKeyConstants;
import com.wap.sohu.recom.core.redis.lock.DistributedLock;
import com.wap.sohu.recom.log.AccessTraceLogData;
import com.wap.sohu.recom.log.AccessTraceLogData.LogCategoryEnum;
import com.wap.sohu.recom.log.AccessTraceLogData.LogKeyEnum;
import com.wap.sohu.recom.log.StatisticLog;
import com.wap.sohu.recom.model.BloomKeyBuild;
import com.wap.sohu.recom.model.FilterBuild;
import com.wap.sohu.recom.service.data.ChannelDataServiceRegister;
import com.wap.sohu.recom.service.data.ChannelNewsDataService;
import com.wap.sohu.recom.service.impl.MChannelNewsBloomService;
import com.wap.sohu.recom.service.impl.NewsUserService;
import com.wap.sohu.recom.util.bloom.BloomFilter;
import com.wap.sohu.recom.utils.ConvertUtils;

/**
 * 类ChannelNewsRecomService.java的实现描述：TODO 类实现描述
 * @author hongfengwang 2013-7-16 下午04:53:37
 */
@Service
public class MChannelNewsRecomService {

    @Autowired
    private DistributedLock mchannelLock;

    @Autowired
    private NewsCacheService newsCacheService;

    @Autowired
    private NewsUserService newsUserService;

    @Autowired
    private MChannelNewsBloomService mChannelNewsBloomService;

    @Autowired
    private MChannelNewsService mChannelNewsService;

    @Autowired
    private Map<String,ChannelNewsDataService> channelServiceMap ;

    private static final String CHANNEL_PATTERN = "channel_%d";

    private static final String DEFAULT_CHANNEL = "channel_1";

    /**
     * @param cid
     * @param pid
     * @param channelId TODO
     * @return
     */
    public Map<Integer,String> getChannelRecom(long cid, long pid, int channelId) {
        Map<Integer,String> resultMap = new LinkedHashMap<Integer,String>();
        if (!mChannelNewsService.getOnStat()||!mChannelNewsService.getEachStat(channelId)) {
            return resultMap;
        }
        String lockKey = String.format(TopNewsRedisKeyConstants.MCHANNEL_USER_LOCK, cid);
        Map<String, BloomFilter> bMap= null;
        long expireTime = 0;boolean result=false;
        if ((expireTime=mchannelLock.tryLock(lockKey))!=0) {
            try {
                bMap=  mChannelNewsBloomService.getUserAllBloom(cid,channelId);
                ChannelNewsDataService dataService = ChannelDataServiceRegister.getChannelNewsDataService(channelId);
                if (dataService==null) {
                    return resultMap;
                }
                Set<Integer> filterNews = new HashSet<Integer>();
                Set<Integer> clickHistory = newsUserService.getNewsHistory(cid);
                Set<Integer> deletedNews  = newsCacheService.getDeletedNews();
                filterNews.addAll(clickHistory);
                filterNews.addAll(deletedNews);
                FilterBuild filterbuild = new FilterBuild(filterNews, new HashSet<Integer>(), bMap,mChannelNewsBloomService);
                Map<Integer,String> map = dataService.queryRecomData(cid, pid, 2, filterbuild);
//              Map<Integer,String> map = getRecomData(cid,bMap);
                BloomKeyBuild bloomKeyBuild = mChannelNewsBloomService.getBloomKeyBuild(cid, channelId);
                result = mChannelNewsBloomService.multiUpdate(cid,new ArrayList<Integer>(map.keySet()),bMap,bloomKeyBuild);
                resultMap = result?map:resultMap;
            } finally {
                mchannelLock.unlock(lockKey,expireTime);
                mChannelNewsBloomService.removeDateListLocal();
                mChannelNewsBloomService.removeSimpleDateFormatLocal();
            }
        }
        return resultMap;
    }

    /**
     * @param cid
     * @param bMap
     * @return
     */
    private Map<Integer,String> getRecomData(long cid, Map<String, BloomFilter> bMap) {
        Map<Integer, String> map = new LinkedHashMap<Integer, String>();
        Set<Integer> filterNews = new HashSet<Integer>();
        Set<Integer> clickHistory = newsUserService.getNewsHistory(cid);
        filterNews.addAll(clickHistory);
        filterNews.addAll(newsCacheService.getDeletedNews());
        filterNews.addAll(mChannelNewsService.getMChannelNews());

        //每个新闻一个分类
        Set<Integer> filterCats = new HashSet<Integer>();
//        ConvertUtils.putList2Map(map, mChannelNewsService.getChannelNews(cid, filterNews, filterCats, bMap,1), CommonConstants.HOT_TYPE);
//        ConvertUtils.putList2Map(map, mChannelNewsService.getShortLikeNews(cid, filterNews, filterCats, bMap,1), CommonConstants.RECOM_TYPE_SHORT);
//        ConvertUtils.putList2Map(map, mChannelNewsService.getWemediaNews(cid, filterNews, filterCats,bMap,1), CommonConstants.HOT_TYPE);
//        ConvertUtils.putList2Map(map, mChannelNewsService.getSubNews(cid, filterNews, filterCats, bMap,1), CommonConstants.HOT_TYPE);
        ConvertUtils.putList2Map(map, mChannelNewsService.getCommonChannelNews(cid, filterNews, filterCats, bMap,2), CommonConstants.RECOM_TYPE_CHANNEL);
        return map;
    }

    /**
     * @param cid
     * @param list
     */
    public void scribeLog(long cid, Map<Integer,String> map,int channelId) {
        AccessTraceLogData logData = new AccessTraceLogData(LogCategoryEnum.mChannelNewRec);
        logData.add(LogKeyEnum.Cid, cid);
        logData.add(LogKeyEnum.RecCount,map.size());
        logData.add(LogKeyEnum.RecNewsIds,map.toString());
        logData.add(LogKeyEnum.CHANNELID,channelId);
        logData.add(LogKeyEnum.TimeStamp,System.currentTimeMillis());
        StatisticLog.info(logData);
    }

}
