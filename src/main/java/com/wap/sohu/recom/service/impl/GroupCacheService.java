/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wap.sohu.recom.cache.core.ICache;
import com.wap.sohu.recom.cache.manager.GroupCacheManager;
import com.wap.sohu.recom.constants.EdbKeyConstants;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.elephantdb.connector.EdbTemplate;

/**
 * 类GroupCacheService.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2012-12-13 上午11:01:55
 */
@Service
public class GroupCacheService {
    
    @Autowired
    private EdbTemplate edbTemplate;
    
    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateRecom;
     
    public Map<Integer, Double> getGroupScore(int tid){
        ICache<Integer, Map<Integer, Double>> iCache = GroupCacheManager.getGroupScore();
        Map<Integer, Double> map=null;
        if ((map=iCache.get(tid))==null) {
            String jsonString = edbTemplate.getString(EdbKeyConstants.GROUP_SCORE,String.valueOf(tid));
            if (StringUtils.isBlank(jsonString)) {
                 return null;
            }
            map = JSON.parseObject(jsonString, new TypeReference<LinkedHashMap<Integer, Double>>(){});
            iCache.put(tid, map);
        }
        return map;
    }
    
    
}
