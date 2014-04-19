/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.action;

import org.apache.log4j.Logger;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.sohu.smc.core.annotation.RequestMapping;
import com.sohu.smc.core.server.ActionSupport;
import com.wap.sohu.recom.service.MonitorService;

/**
 * 类HealthCheckAction.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2012-12-5 上午10:15:45
 */
@Controller
@RequestMapping(value="/recom/monitor")
public class HealthCheckAction extends ActionSupport{
    
    
    private static final Logger LOGGER = Logger.getLogger(HealthCheckAction.class);

    public static final String counter = HealthCheckAction.class.getSimpleName();
    
    @Autowired
    private MonitorService monitorService;

  

    @RequestMapping(value = { "/check.go" })
    public String action(HttpRequest req, HttpResponse resp) throws Exception {
        addCounter(counter);
        return monitorService.check();
    }

}
