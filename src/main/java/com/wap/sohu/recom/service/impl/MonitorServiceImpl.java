/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.service.MonitorService;

/**
 * 类MonitorServiceImpl.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2012-12-5 上午10:20:29
 */
@Service("monitorService")
public class MonitorServiceImpl implements MonitorService{

    @Autowired
    private StringRedisTemplateExt redisTemplateBase;
    
    @Override
    public String check() {
        String result = redisTemplateBase.opsForValue().get("recom_health_check");
        return result;
    }

}
