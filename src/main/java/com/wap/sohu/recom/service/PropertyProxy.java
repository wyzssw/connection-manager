/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * 类PropertyUtils.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2013-4-19 下午10:54:20
 */
@Component
public class PropertyProxy {
    private static final Logger LOGGER = Logger.getLogger(PropertyProxy.class);

    @Autowired
    private MessageSource messageSource;
    
    public String getProperty(String key){
        String result =null;
        try {
            result = messageSource.getMessage(key, null, Locale.getDefault());
        } catch (Exception e) {
            LOGGER.error("get property error ",e);
        }
        return result;
    }
    
}
