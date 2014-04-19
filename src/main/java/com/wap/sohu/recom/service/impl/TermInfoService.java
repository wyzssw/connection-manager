/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wap.sohu.recom.cache.core.ICache;
import com.wap.sohu.recom.cache.manager.SubscribeManager;
import com.wap.sohu.recom.dao.TermInfoDao;
import com.wap.sohu.recom.model.TermInfo;

/**
 * 类TermInfoService.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2013-1-9 上午10:21:08
 */
@Service
public class TermInfoService {
    
    private static final Logger LOGGER = Logger.getLogger(TermInfoService.class);
    
    @Autowired
    private TermInfoDao termInfoDao;
    
    public TermInfo getTermInfo(int termId){
        ICache<Integer,TermInfo> iCache = SubscribeManager.getTermInfoCache();
        TermInfo termInfo  = null;
        if ((termInfo=iCache.get(termId))==null) {
            termInfo = termInfoDao.getTermInfo(termId);
            if (termInfo==null) {
                LOGGER.error("error!! termId "+termId+" isn't exit in db");
                return null;
            }
            iCache.put(termId, termInfo);
        }
        return termInfo;
    }
    
}
