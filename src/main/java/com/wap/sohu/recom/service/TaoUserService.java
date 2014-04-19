/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wap.sohu.recom.constants.CommonConstants;
import com.wap.sohu.recom.constants.TaoRecomRedisKeyConstants;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;

/**
 * 类TaoUserService.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2013-8-8 下午03:48:46
 */
@Service
public class TaoUserService {
    
    @Autowired
    private StringRedisTemplateExt stringRedisTemplateToutiao;
    
    public Set<Integer> getTaoNewsHistory(long cid) {
        String key = String.format(TaoRecomRedisKeyConstants.TAO_USER_NEWS_HISTORY, cid);
        long fourDaysTime = System.currentTimeMillis()/1000-86400*7;
        Set<Integer> hisSet = new HashSet<Integer>();
        Set<String> set =  stringRedisTemplateToutiao.opsForZSet().rangeByScore(key, fourDaysTime, Double.MAX_VALUE);
        if (set!=null&&set.size()>0) {
            for (String str : set) {
                hisSet.add(Integer.parseInt(str.toString()));
            }
        }
        return hisSet;
    }
    
    public void setHistory(long cid, int newsId) {
        String key = String.format(TaoRecomRedisKeyConstants.TAO_USER_NEWS_HISTORY, cid);
        long fourDaysTime = System.currentTimeMillis()/1000-86400*7;
        stringRedisTemplateToutiao.opsForZSet().rangeByScore(key, 0D, fourDaysTime);
        stringRedisTemplateToutiao.opsForZSet().add(key,String.valueOf(newsId),System.currentTimeMillis()/1000);
        stringRedisTemplateToutiao.expire(key, CommonConstants.TAONEWS_UPDATE_DAYS,TimeUnit.DAYS);
    }

}
