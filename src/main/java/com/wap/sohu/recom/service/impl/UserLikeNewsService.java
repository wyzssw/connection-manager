/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wap.sohu.recom.cache.manager.NewsCacheManager;
import com.wap.sohu.recom.constants.CommonConstants;
import com.wap.sohu.recom.constants.EdbKeyConstants;
import com.wap.sohu.recom.elephantdb.connector.EdbTemplate;
import com.wap.sohu.recom.service.NewsCacheService;
import com.wap.sohu.recom.service.NewsDupService;
import com.wap.sohu.recom.utils.MapSortUtil;

/**
 * 类UserLikeNewsServiceImpl.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2012-11-26 下午05:55:26
 */
@Service
public class UserLikeNewsService{
    
    @Autowired
    private EdbTemplate edbTemplate;
    
    @Autowired
    private NewsUserService newsUserService;
    
    @Autowired
    private NewsCacheService newsCacheService;
    
    @Autowired
    private NewsItemService newsItemService;
    
    @Autowired
    private NewsDupService newsDupService;

    public Map<Integer,Integer> getRecomNews(long cid, int moreCount,Set<Integer> filterSet){
        Set<Integer> set      = newsUserService.getNewsHistory(cid);
        set.addAll(filterSet);
        List<Integer> shortTagList = getItemsByCid(cid,EdbKeyConstants.SHORT_NEWS,10,null);
        List<Integer> longTagList = getItemsByCid(cid,EdbKeyConstants.LONG_NEWS,5,shortTagList);
        List<Integer> shortCatList = getItemsByCid(cid, EdbKeyConstants.CAT_SHORT, 3, null);
        List<Integer> longCatList = getItemsByCid(cid, EdbKeyConstants.CAT_LONG, 3, null);
        Map<Integer,Integer> longMap  = getNewsByTags(cid, longTagList,set,1,longCatList);
        set.addAll(longMap.keySet());
        int lack = longMap.isEmpty()?moreCount:moreCount-1;
        Map<Integer,Integer> shortMap = getNewsByTags(cid, shortTagList,set,lack,shortCatList);
        shortMap.putAll(longMap);
        if (!shortMap.isEmpty()&&  shortMap.size()<moreCount) {
             fillItemNews(shortMap,moreCount,set);
             if (shortMap.size()<moreCount) {
                 return new  LinkedHashMap<Integer, Integer>();
            }
        }
        List<Integer> list = new ArrayList<Integer>(shortMap.values());
        int n = list.size();
        while (n>0) {
            newsDupService.filterDupNews(list.get(--n), list);
            if (list.size()<moreCount) {
                return new  LinkedHashMap<Integer, Integer>();
            }
        }
        return shortMap;
    }
    
    /**
     * 将map以itembase算法出的结果进行填充
     * @param map
     */
    private void fillItemNews(Map<Integer, Integer> map,int morecount,Set<Integer> set) {
        int count =0;
        List<Integer> list = new ArrayList<Integer>();
        for (Map.Entry<Integer, Integer> item : map.entrySet()) {
             List<Integer> listTmp = newsItemService.getNewsByNews(item.getValue(),set);
             if (listTmp==null) {
                continue;
            }
             set.addAll(listTmp);
             list.addAll(listTmp);
             if (list.size()> morecount-map.size()) {
                 break;
            }
        }
        int cut = morecount - map.size();
        list = list.subList(0, list.size()>cut?cut:list.size());
        for (Integer integer : list) {
             map.put(--count, integer);
        }
    }

    public Map<Integer,Integer> getNewsByTags(long cid,List<Integer> tags,Set<Integer> filterSet,int count,List<Integer> catList){
        Map<Integer, Integer> retMap = new LinkedHashMap<Integer, Integer>();
        Set<Integer> editedSet = NewsCacheManager.getEditedNewsCache().get(CommonConstants.EDITED_NEWS_KEY);
        Set<Integer> last24News = NewsCacheManager.getLastHourNews().get(CommonConstants.NEWS_LAST_CACHE);
        for (Integer tag : tags) {
            Map<Integer, Double> map = newsCacheService.getNewsScore(tag,2.5);
            if (map==null) {
                continue;
            }            
            for (Map.Entry<Integer, Double> entry : map.entrySet()) {
                 if (last24News!=null && last24News.contains(entry.getKey())) {
                     entry.setValue(entry.getValue()+0.7);
                }
            }
            List<Integer> list = MapSortUtil.sortMapByValue(map);
            for (Integer item : list) {
                 if (editedSet.contains(item)&&!filterSet.contains(item)&&!newsDupService.isLowQualityNews(item)) {
                     if (catList!=null) {
                         Set<Integer> set  = newsCacheService.getCatIdsCache(item);
                         if (Collections.disjoint(set, catList)) {
                            continue;   
                         } 
                     }
                     retMap.put(tag, item);
                     filterSet.add(item);
                     break;
                }
            }
            if (retMap.size()>=count) {
                break;
            }
        }
        return retMap;
    }
    
    
    
    public List<Integer> getItemsByCid(long cid,String key,int count,List<Integer> filterList){
        String jsonString = edbTemplate.getString(key,String.valueOf(cid));
        if (StringUtils.isBlank(jsonString)) {
            return new ArrayList<Integer>();
        }
        Map<Integer, Double> map = JSON.parseObject(jsonString, new TypeReference<LinkedHashMap<Integer, Double>>(){}); 
        List<Integer> keys = new ArrayList<Integer>();
        for (Map.Entry<Integer, Double> item : map.entrySet()) {
             if (filterList!=null && filterList.contains(item.getKey())) {
                 continue;
             }
             keys.add(item.getKey());
        }  
        keys = keys.subList(0, count>keys.size()?keys.size():count);
        return keys;
    }
    

}
