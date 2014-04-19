/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wap.sohu.recom.cache.manager.GroupCacheManager;
import com.wap.sohu.recom.constants.CommonConstants;
import com.wap.sohu.recom.constants.EdbKeyConstants;
import com.wap.sohu.recom.elephantdb.connector.EdbTemplate;
import com.wap.sohu.recom.service.GroupRecomService;
import com.wap.sohu.recom.utils.MapSortUtil;

/**
 * 不用了
 * @author hongfengwang 2012-11-26 下午05:55:06
 */
@Service
public class UserLikeGroupService{
    
    @Autowired
    private EdbTemplate edbTemplate;
    
    @Autowired
    private GroupRecomService groupRecomService;
    
    @Autowired
    private GroupCacheService groupCacheService;
    
    @Autowired
    private GroupItemService groupItemService;

    

    public Map<Integer,Integer> getRecomGroup(long cid, int moreCount,Set<Integer> filterSet) {
        Set<Integer> set = groupRecomService.getUserHistorySet(cid);
        set.addAll(filterSet);
        List<Integer> shortTagList = getTagsByCid(cid,EdbKeyConstants.SHORT_GROUP,10,null);
        List<Integer> longTagList  = getTagsByCid(cid,EdbKeyConstants.LONG_GROUP,5,shortTagList);
        Map<Integer,Integer> longMap  = getGroupByTags(cid, longTagList,set,1);
        set.addAll(longMap.keySet());
        int lack = longMap.isEmpty()?moreCount:moreCount-1;
        Map<Integer,Integer> shortMap = getGroupByTags(cid, shortTagList,set,lack);
        shortMap.putAll(longMap);
        if (!shortMap.isEmpty()&&  shortMap.size()<moreCount) {
            fillItemGroup(shortMap,moreCount,set);
        }
        return shortMap;
    }
    
    /**
     * @param map
     * @param moreCount
     * @param set
     */
    private void fillItemGroup(Map<Integer, Integer> map, int morecount, Set<Integer> set) {
        int count =0;
        List<Integer> list = new ArrayList<Integer>();
        for (Map.Entry<Integer, Integer> item : map.entrySet()) {
             List<Integer> listTmp = groupItemService.getGroupByGroup(item.getValue(),set);
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

    /**
     * 不用了
     * @param cid
     * @param tags
     * @param filterSet
     * @param count
     * @return
     */
    public Map<Integer,Integer> getGroupByTags(long cid,List<Integer> tags,Set<Integer> filterSet,int count){
        Set<Integer> group24Set =  GroupCacheManager.getLastHourGroup().get(CommonConstants.GROUP_LAST_CACHE);
        Map<Integer,Integer> retMap = new LinkedHashMap<Integer,Integer>();
        Set<Integer> set = GroupCacheManager.getGroupDelThoseDays().get(CommonConstants.GROUP_DEL_THOSE_DAYS);
        if (set!=null) {
            filterSet.addAll(set);
        }
        for (Integer tag : tags) {
            Map<Integer, Double> map = groupCacheService.getGroupScore(tag);
            if (map==null) {
                continue;
            }
            for (Map.Entry<Integer, Double> integer : map.entrySet()) {
                 if(group24Set!=null&&group24Set.contains(integer)){
                     integer.setValue(integer.getValue()+0.7);
                 }
            }
            List<Integer> list= MapSortUtil.sortMapByValue2(map);
            for (Integer  item: list) {
                 if (!filterSet.contains(item)) {
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
    
    
    public List<Integer> getTagsByCid(long cid,String key,int count,List<Integer> filterList){
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
