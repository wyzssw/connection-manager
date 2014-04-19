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
import com.wap.sohu.recom.constants.TaoRecomRedisKeyConstants;
import com.wap.sohu.recom.core.redis.lock.DistributedLock;
import com.wap.sohu.recom.log.AccessTraceLogData;
import com.wap.sohu.recom.log.AccessTraceLogData.LogCategoryEnum;
import com.wap.sohu.recom.log.AccessTraceLogData.LogKeyEnum;
import com.wap.sohu.recom.log.StatisticLog;
import com.wap.sohu.recom.model.FilterBuild;
import com.wap.sohu.recom.util.bloom.BloomFilter;
import com.wap.sohu.recom.utils.ConvertUtils;

/**
 * 淘新闻推荐入口类
 * @author hongfengwang 2013-8-7 上午11:28:37
 */
@Service
public class TaoNewsRecomService {

    @Autowired
    private DistributedLock taoNewsLock;

    @Autowired
    private TaoNewsBloomService taoNewsBloomService;

    @Autowired
    private TaoNewsService taoNewsService;

    @Autowired
    private MChannelNewsService mChannelNewsService;

    /**
     * @param cid
     * @param pid
     * @return
     */
    public Map<Integer,String> getTaoRecom(long cid, long pid) {
        Map<Integer,String> resultMap = new LinkedHashMap<Integer,String>();
        String lockKey = String.format(TaoRecomRedisKeyConstants.TAO_USER_LOCK, cid);
        Map<String, BloomFilter> bMap= null;
        long expireTime = 0;boolean result=false;
        if ((expireTime=taoNewsLock.tryLock(lockKey))!=0) {
            try {
                bMap=  taoNewsBloomService.getUserAllBloom(cid);
                Map<Integer,String> tmpMap = getRecomData(cid,pid,bMap);
                result = taoNewsBloomService.multiUpdate(cid,new ArrayList<Integer>(tmpMap.keySet()),bMap,null);
                resultMap = result?tmpMap:resultMap;
            } finally {
                taoNewsLock.unlock(lockKey,expireTime);
                taoNewsBloomService.removeDateListLocal();
                taoNewsBloomService.removeSimpleDateFormatLocal();
            }
        }
        return resultMap;
    }

    /**
     * @param cid
     * @param bMap
     * @return
     */
    private Map<Integer,String> getRecomData(long cid, long pid,Map<String, BloomFilter> bMap) {
        Map<Integer,String> resultMap = new LinkedHashMap<Integer,String>();
        Set<Integer> filterNews = new HashSet<Integer>(4000);
        filterNews.addAll(mChannelNewsService.getMChannelNews());
        //每个新闻一个分类
        Set<Integer> filterCats = new HashSet<Integer>();
        FilterBuild filterBuild = new FilterBuild(filterNews, filterCats, bMap,taoNewsBloomService);
        resultMap.putAll(ConvertUtils.convertList2Map(taoNewsService.getShortLikeNews(cid, pid,filterBuild,1), CommonConstants.RECOM_TYPE_SHORT));
        resultMap.putAll(ConvertUtils.convertList2Map(taoNewsService.getHotNews(cid,pid, filterBuild,1),CommonConstants.HOT_TYPE));
        resultMap.putAll(ConvertUtils.convertList2Map(taoNewsService.getLongLikeNews(cid,pid, filterBuild,2), CommonConstants.RECOM_TYPE_LONG));
        return resultMap;
    }

    /**
     * @param cid
     * @param list
     */
    public void scribeLog(long cid, long pid, Map<Integer,String> restultMap) {
        AccessTraceLogData logData = new AccessTraceLogData(LogCategoryEnum.taoClickStat);
        logData.add(LogKeyEnum.Cid, cid);
        logData.add(LogKeyEnum.PID, pid+"");
        logData.add(LogKeyEnum.RecCount,restultMap.size());
        logData.add(LogKeyEnum.RecNewsIds,restultMap.toString());
        logData.add(LogKeyEnum.TimeStamp,System.currentTimeMillis());
        StatisticLog.info(logData);

    }



}
