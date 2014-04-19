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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wap.sohu.recom.cache.core.ICache;
import com.wap.sohu.recom.cache.manager.GroupCacheManager;
import com.wap.sohu.recom.constants.EdbKeyConstants;
import com.wap.sohu.recom.elephantdb.connector.EdbTemplate;

/**
 * 类groupItemService.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2012-12-13 下午04:28:51
 */
@Service
public class GroupItemService {
    
    @Autowired
    private EdbTemplate edbTemplate;
    

    /**
     * @param value
     * @param set
     * @return
     */
    public List<Integer> getGroupByGroup(Integer gid, Set<Integer> set) {
        ICache<Integer, List<Integer>> iCache = GroupCacheManager.getSimGroup();
//        List<Integer> list = new ArrayList<Integer>();
        List<Integer> list = null;
         if ((list=iCache.get(gid))==null) {
            String jsonString = edbTemplate.getString(EdbKeyConstants.SIM_GROUP,String.valueOf(gid));
            if (StringUtils.isBlank(jsonString)) {
                 return null;
            }
            Map<Integer, Double> map= JSON.parseObject(jsonString, new TypeReference<LinkedHashMap<Integer,Double>>(){});
            list = new ArrayList<Integer>(map.keySet());
            list.removeAll(set);
            iCache.put(gid, list);
        }
        return list;      
    }

}
