/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wap.sohu.recom.constants.RedisKeyConstants;
import com.wap.sohu.recom.constants.TaoRecomRedisKeyConstants;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.dao.NewsChannelDao;
import com.wap.sohu.recom.service.PropertyProxy;
import com.wap.sohu.recom.utils.ConvertUtils;

/**
 * 类UserLikeServiceImpl.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2012-11-26 下午05:39:35
 */
@Service
public class UserLikeService{
    @Autowired
    private UserLikeGroupService userLikeGroupService;
    
    @Autowired
    private UserLikeNewsService userLikeNewsService;
    
    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateUser;
    
    @Autowired
    private PropertyProxy propertyProxy;
    
    @Autowired
    private NewsChannelDao newsChannelDao;
    
    @Autowired
    private StringRedisTemplateExt stringRedisTemplateToutiao;
    

    public Map<Integer, Integer> getRecomItem(long cid, String type, int moreCount) {
        Map<Integer, Integer> map = new LinkedHashMap<Integer, Integer>();
        Set<Integer> filterSet = getUserLikeHistory(type, cid);
        if (type.equals(UserLikeEnum.group.getValue())) {
            map = userLikeGroupService.getRecomGroup(cid,moreCount,filterSet);
        }
        else if (type.equals(UserLikeEnum.news.getValue())) {
            map = userLikeNewsService.getRecomNews(cid,moreCount,filterSet);
        }
        return map;
    }

    
    public List<Integer> getTags(long cid,String type,int morecount) {
        if (type.contains(UserLikeEnum.news.getValue())) {
            return  userLikeNewsService.getItemsByCid(cid,type,morecount,null);
        }
        if (type.contains(UserLikeEnum.group.getValue())) {
            return userLikeGroupService.getTagsByCid(cid, type, morecount, null);
        }
       return new ArrayList<Integer>();
    }
    
//    @Async
    public void setUserLikeHistory(String type,long cid,Collection<Integer> showSet){
        String keyPreffix = "";
        if (type.equals(UserLikeEnum.group.getValue())) {
            keyPreffix = RedisKeyConstants.USER_LIKE_HISTORY_GROUP;
        }else if (type.equals(UserLikeEnum.news.getValue())) {
            keyPreffix = RedisKeyConstants.USER_LIKE_HISTORY_NEWS;
        }
        String key = String.format(keyPreffix, cid);
        Map<Integer, Double> valueToScore = new HashMap<Integer, Double>();
        Double value = (double)new Date().getTime()/1000;
        for (Integer integer : showSet) {
             valueToScore.put(integer, value);
        }
        long sevenDaysTime = System.currentTimeMillis()/1000-86400*7;
        shardedRedisTemplateUser.opsForZSet().removeRangeByScore(key, 0D, sevenDaysTime);
        shardedRedisTemplateUser.opsForBatch().addForInt(key, valueToScore);
    }
    
    public Set<Integer> getUserLikeHistory(String type,long cid){
        String keyPreffix = "";
        if (type.equals(UserLikeEnum.group.getValue())) {
            keyPreffix = RedisKeyConstants.USER_LIKE_HISTORY_GROUP;
        }else if (type.equals(UserLikeEnum.news.getValue())) {
            keyPreffix = RedisKeyConstants.USER_LIKE_HISTORY_NEWS;
        }
        String key = String.format(keyPreffix, cid);
        long sevenDaysTime = System.currentTimeMillis()/1000-86400*7;
        Set<Integer> hisSet = new HashSet<Integer>();
        Set<String> set =  shardedRedisTemplateUser.opsForZSet().rangeByScore(key, sevenDaysTime, Double.MAX_VALUE);
        if (set!=null&&set.size()>0) {
            for (String str : set) {
                hisSet.add(Integer.parseInt(str.toString()));
            }
        }
        return hisSet;
    }
    
    
    
    public static enum UserLikeEnum {
        news("news"), group("grouppic");

        private String value;

        private UserLikeEnum(String value){
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
  
    
    public List<Integer> getUserFirstLike(long cid,long pid){
        Set<String> set = getOriginalLike(cid,pid);
        Set<Integer> catSet = ConvertUtils.convert2intList(set);
        if (catSet==null||catSet.isEmpty()) {
            return null;
        }
        List<Integer> catList = translate2cat(catSet);
        return catList;
    }

    

    /**
     * @param convert2intList
     * @return
     */
    private List<Integer> translate2cat(Set<Integer> convert2intList) {
        List<Integer> list = new ArrayList<Integer>();
        for (Integer channelId : convert2intList) {
             list.add(Integer.valueOf((propertyProxy.getProperty("tao_cat_channel_"+channelId))));
        }
        return list;
    }
    
    /**
     * @return
     */
    private Set<String> getOriginalLike(long cid,long pid) {
        String key = String.format(TaoRecomRedisKeyConstants.TAO_USER_FIRST_LIKE, cid);
        Set<String> set =  stringRedisTemplateToutiao.opsForZSet().reverseRange(key,0,-1);
        if (set==null||set.isEmpty()) {
            String userLike = newsChannelDao.getUserLike(cid, pid);
            if (StringUtils.isBlank(userLike)) {
                return null;
            }
            String [] likeArray =  userLike.split(",");
            List<String> likeSet = Arrays.asList(likeArray);
            set = new LinkedHashSet<String>(likeSet);
            long time = System.currentTimeMillis()/1000L;
            Double timeDouble = Double.valueOf(time);
            for (String string : set) {
                stringRedisTemplateToutiao.opsForZSet().add(key,string,timeDouble);
            }
        }
        return set;
    }


}
