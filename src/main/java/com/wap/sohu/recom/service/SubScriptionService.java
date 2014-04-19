/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wap.sohu.recom.cache.core.ICache;
import com.wap.sohu.recom.cache.manager.NewsCacheManager;
import com.wap.sohu.recom.constants.CommonConstants;
import com.wap.sohu.recom.constants.TopNewsRedisKeyConstants;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.dao.SubscriptionDao;
import com.wap.sohu.recom.utils.ConvertUtils;

/**
 * 类SubScriptionService.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2013-8-14 上午10:49:14
 */
@Service
public class SubScriptionService {
    
    @Autowired
    private SubscriptionDao subscriptionDao;
    
    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateRecom;
    
    public Set<Integer> getWeMediaSubIds(){
        Set<Integer> set = null;
        ICache<String, Set<Integer>>  iCache = NewsCacheManager.getNewsWemidiaSubids();
        if ((set=iCache.get(CommonConstants.NEWS_WEMEDIA_SUBIDS))==null) {
            Set<String> tmpSet =  shardedRedisTemplateRecom.opsForSet().members(TopNewsRedisKeyConstants.NEWS_WEMEDIA_SUBIDS);
            set = ConvertUtils.convert2intList(tmpSet);
            if (set==null||set.isEmpty()) {
                set=subscriptionDao.getWemediaSubIds();
            }
            iCache.put(CommonConstants.NEWS_WEMEDIA_SUBIDS, set);
        }
        return set;
    }

}
