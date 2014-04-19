/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wap.sohu.recom.constants.RedisKeyConstants;
import com.wap.sohu.recom.constants.TopNewsRedisKeyConstants;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;

/**
 * 新闻用户相关服务实现类
 * @author hongfengwang 2012-9-4 下午04:27:31
 */
@Component("newsUserService")
public class NewsUserService {

    
    @Autowired
    private RedisThreadLocalService redisThreadLocalService;
    
    @Autowired
    private StringRedisTemplateExt  shardedRedisTemplateUser;
    

    public Set<Integer> getNewsHistory(long cid){
          String key = String.format(RedisKeyConstants.NEWS_USER_HISTORY, cid);
          long fourDaysTime = System.currentTimeMillis()/1000-86400*7;
          Set<Integer> hisSet = new HashSet<Integer>();
          Set<String> set =  redisThreadLocalService.getShardedJedisUser().zRangeByScore(key, fourDaysTime, Double.MAX_VALUE);
          if (set!=null&&set.size()>0) {
              for (String str : set) {
                  hisSet.add(Integer.parseInt(str.toString()));
              }
          }
          return hisSet;
      }
      
    
      public void setHistory(long cid, int newsId) {
          String key = String.format(RedisKeyConstants.NEWS_USER_HISTORY, cid);
          long fourDaysTime = System.currentTimeMillis()/1000-86400*7;
          redisThreadLocalService.getShardedJedisUser().zRemRangeByScore(key, 0D, fourDaysTime);
          redisThreadLocalService.getShardedJedisUser().zAdd(key,new Date().getTime()/1000, String.valueOf(newsId));
          redisThreadLocalService.getShardedJedisUser().expire(key, 86400*7);
      }
    
      public List<String> queryShortCat(long cid,long pid) {
          String key = String.format(TopNewsRedisKeyConstants.USER_SHORT_LIKE_CAT, cid);
          List<String> shortLikeCat = shardedRedisTemplateUser.opsForList().range(key, 0, -1);
          return shortLikeCat;
      }

      public List<Integer> queryShortCatId(long cid,long pid){
          String key = String.format(TopNewsRedisKeyConstants.USER_SHORT_LIKE_CAT, cid);
          List<String> shortLikeCat = shardedRedisTemplateUser.opsForList().range(key, 0, -1);
          if (shortLikeCat==null) {
            return null;
          }
          List<Integer> retList = new ArrayList<Integer>();
          for (String string : shortLikeCat) {
              retList.add(Integer.valueOf(string));
          }
          return retList;
      }
}
