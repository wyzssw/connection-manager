/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.wap.sohu.mobilepaper.model.pic.GroupPicInfo;
import com.wap.sohu.recom.cache.core.ICache;
import com.wap.sohu.recom.cache.manager.GroupCacheManager;
import com.wap.sohu.recom.constants.CommonConstants;
import com.wap.sohu.recom.constants.RedisKeyConstants;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.service.GroupPicService;
import com.wap.sohu.recom.service.GroupRecomService;
import com.wap.sohu.recom.service.NewsCacheService;
import com.wap.sohu.recom.service.NewsDupService;
import com.wap.sohu.recom.utils.StringExtUtils;

/**
 * 类GroupNewsRecomService.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2013-5-13 上午11:35:27
 */
@Service
public class GroupNewsRecomService {
    
    @Autowired 
    private GroupPicService groupPicService;
    
    @Autowired
    private GroupRecomService groupRecomService;
    
    
    @Autowired
    private NewsUserService newsUserService;
    
    
    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateRecom;
    
    
    @Autowired
    private NewsDupService newsDupService;
    
    
    @Autowired
    private NewsCacheService newsCacheService;
    
    @Autowired
    private MessageSource messageSource;
    
   
    
    /**
     * @param cid
     * @param newsId
     * @param i
     * @return
     */
    public Map<Integer,String> getRecomItem(long cid,int picType, int newsId,int moreCount) {
        List<Integer> pubList =  getPubList();
        Integer pubId = newsCacheService.getNewsPubId(newsId);
        if (pubId!=null&&pubId!=0&&pubList.contains(pubId)) {
            return getSpecialPubIdItem(cid,picType,newsId,pubId,moreCount);
        }
        return getCommonRecomItem(cid, picType, newsId,moreCount);        
    }



    /**
     * 除了tag组图，只推荐特定刊物下的组图
     * @param cid
     * @param picType
     * @param newsId
     * @param pubId
     * @return
     */
    private Map<Integer, String> getSpecialPubIdItem(long cid, int picType, int newsId, Integer pubId,int moreCount) {
        //准备工作
        Map<Integer, String> map = new LinkedHashMap<Integer, String>();
        Set<Integer> groupFilterSet = new HashSet<Integer>();
        groupFilterSet.addAll(groupRecomService.getUserHistorySet(cid));
        //先组图tag关联组图
        List<Integer> relateGroupList = getRelateGroup(picType,newsId,groupFilterSet);
        groupFilterSet.addAll(relateGroupList);
        for (Integer integer : relateGroupList) {
             map.put(integer, "pic");
        }
        if (map.keySet().size()>=moreCount) {
            return map;
        }
        //组图新闻相关
        Set<Integer> set = newsUserService.getNewsHistory(cid);
        set.add(newsId);
        List<Integer> groupNewsList = getRelateNews(newsId,set,true,pubId);
        for (Integer integer : groupNewsList) {
             map.put(integer, "news");
        }
        if (map.keySet().size()>=moreCount) {
            return map;
        }
        set.addAll(groupNewsList);
        //该刊物下随机组图新闻
        List<Integer> ramdomList =  getRamdomGroupNews(pubId,moreCount-map.keySet().size()+2,set);
        
        removeDupOfNews(groupNewsList, ramdomList);
        
        for (Integer integer : ramdomList) {
             map.put(integer, "news");
        }
        return map;
    }



    /**
     * @param groupNewsList
     * @param ramdomList
     */
    private void removeDupOfNews(List<Integer> groupNewsList, List<Integer> ramdomList) {
        for (Integer integer : groupNewsList) {
             String title  = newsCacheService.getNewTitleCache(integer);
             Iterator<Integer> iterator = ramdomList.iterator();
             while (iterator.hasNext()) {
                Integer item = (Integer) iterator.next();
                String  itemTitle = newsCacheService.getNewTitleCache(item);
                int    interSize = StringExtUtils.getInterSize(title, itemTitle, 14);
                if ((double)interSize/title.length()>0.65) {
                     iterator.remove();
                }
            }
        }
        
    }


    /**
     * @param groupNewsList
     * @param hotMoreList
     */
    private void removeDup(List<Integer> groupNewsList, List<GroupPicInfo> hotMoreList) {
        for (Integer item : groupNewsList) {
             Iterator<GroupPicInfo> iterator = hotMoreList.iterator();
             String title = newsCacheService.getNewTitleCache(item);
             while (iterator.hasNext()) {
                GroupPicInfo groupPicInfo = (GroupPicInfo) iterator.next();
                int    interSize = StringExtUtils.getInterSize(title, groupPicInfo.getTitle(), 14);
                if ((double)interSize/title.length()>0.65) {
                     iterator.remove();
                }
            }
        }
    }

    /**
     * @param pubId
     * @param i
     */
    private List<Integer> getRamdomGroupNews(Integer pubId, int leftCount,Set<Integer> set) {
       List<Integer> newsList = new ArrayList<Integer>();
       List<Integer> listCache =  newsCacheService.getCertainPubNews(pubId);
       List<Integer> list= new ArrayList<Integer>(listCache);
       list.removeAll(newsCacheService.getNoPubNews());
       list.removeAll(set);
       int size = list.size();
       int count = leftCount>size?size:leftCount;
       Random random = new Random();
        for (int i = 0; i < count; i++) {
             newsList.add(list.get(random.nextInt(size)));
        }
        removeDuplicate(newsList);
        return newsList;
    }



    /**
     * 
     */
    public List<Integer> getPubList() {
        String pubIds = messageSource.getMessage("groupnews_pubid_list", null, Locale.getDefault());
        List<String> list = Arrays.asList(StringUtils.split(pubIds));
        List<Integer> pubList = new ArrayList<Integer>();
        for (String string : list) {
             pubList.add(Integer.valueOf(string));
        }
        return pubList;
    }



    /**
     * @param cid
     * @param picType
     * @param newsId
     * @return
     */
    public Map<Integer, String> getCommonRecomItem(long cid, int picType, int newsId,int moreCount) {
       //准备工作
        Map<Integer, String> map = new LinkedHashMap<Integer, String>();
        Set<Integer> groupFilterSet = new HashSet<Integer>();
        groupFilterSet.addAll(groupRecomService.getUserHistorySet(cid));
        //先组图tag关联组图
        List<Integer> relateGroupList = getRelateGroup(picType,newsId,groupFilterSet);
        groupFilterSet.addAll(relateGroupList);
        for (Integer integer : relateGroupList) {
             map.put(integer, "pic");
        }
        if (map.keySet().size()>=moreCount) {
            return map;
        }
        //组图新闻相关
        Set<Integer> set = newsUserService.getNewsHistory(cid);
        set.add(newsId);
        List<Integer> groupNewsList = getRelateNews(newsId,set,false,0);
        for (Integer integer : groupNewsList) {
             map.put(integer, "news");
        }
        if (map.keySet().size()>=moreCount) {
            return map;
        }
        //组图切词相关
        List<Integer> splitList =groupRecomService.getSplitGid(newsId,groupFilterSet);
        groupFilterSet.addAll(splitList);
        for (Integer integer : splitList) {
             map.put(integer, "pic");
        }
        if (map.keySet().size()>=moreCount) {
            return map;
        }
        //热门组图
        List<GroupPicInfo> hotMoreList =groupRecomService.getGroupFromHotList(moreCount-map.keySet().size()+2, picType,groupFilterSet);
        
        //避免与新闻重复
        removeDup(groupNewsList, hotMoreList);
        List<Integer> hotGidList = groupRecomService.convert2IntList(hotMoreList);
        splitList.addAll(hotGidList);
        for (Integer integer : hotGidList) {
             map.put(integer, "pic");
        }
        return map;
    }



   

    
    
    /**
     * 获取组图tag相关组图
     * @param cid
     * @param picType
     * @param newsId
     * @param groupFilterSet
     * @return
     */
     
    public List<Integer> getRelateGroup(int picType, int newsId,Set<Integer> groupFilterSet){
        Integer gid =  groupPicService.getNewsEmbedGid(newsId);
        List<GroupPicInfo> list = new ArrayList<GroupPicInfo>();
        // 组图新闻推荐--tag推荐
        if (gid!=0) {
            list = groupRecomService.getRecommondPic(gid, picType, groupFilterSet,CommonConstants.GROUPNEWS_MORECOUNT);
        }
        return groupRecomService.convert2IntList(list);
    }
    
    
    /**
     * 获取分词相关组图新闻
     * @param cid
     * @param newsId
     * @param filterPub
     * @return
     */
    public List<Integer> getRelateNews(int newsId,Set<Integer> set,boolean filterPub,int pubId){
        ICache<Integer, Map<Integer, Double>> iCache = GroupCacheManager.getGroupNewsSimNews();
        Map<Integer,Double> map = null;
        if ((map=iCache.get(newsId))==null) {
            map = new LinkedHashMap<Integer,Double>();
            String key = String.format(RedisKeyConstants.GROUPNEWS_SIM_NEWS, newsId);
            Set<TypedTuple<String>> recomSet =  shardedRedisTemplateRecom.opsForZSet().reverseRangeWithScores(key, 0, 20);
            for (TypedTuple<String> tuple : recomSet) {
                if (tuple.getScore()>0.1) {
                    Integer recNewsId = Integer.parseInt(tuple.getValue());
                    map.put(recNewsId, tuple.getScore());
                }
            }
            iCache.put(newsId, map);
        }
        if (map==null||map.isEmpty()) {
            return new ArrayList<Integer>();
        }
        set.addAll(newsCacheService.getNoPubNews());
        set.addAll(newsCacheService.getDeletedNews());
        if (filterPub) {
            Iterator<Integer> iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                Integer id = (Integer) iterator.next();
                int hisPubId = newsCacheService.getNewsPubId(id);
                if (pubId!=hisPubId||set.contains(id)) {
                    iterator.remove();
                }
            }
            
        }
        List<Integer> newsList = new ArrayList<Integer>(map.keySet());
        newsList.removeAll(set);
        removeDuplicate(newsList);
        return newsList;
    }
    
    
    
    public void removeDuplicate(List<Integer> newsList) {
        int n = newsList.size();
        for (int i = 0; i < n; i++) {
            if (newsList.size() < i+2) {
                break;
            }
            newsDupService.filterDupGroupNews(newsList.get(i), newsList,i+1);
        }
    }
    
    
    
    /**
     * 加入组图新闻历史记录
     * @param cid
     * @param newsId
     */
    public void setGroupNewsHistory(long cid,int newsId){
        newsUserService.setHistory(cid, newsId);   
    }
    
    

}
