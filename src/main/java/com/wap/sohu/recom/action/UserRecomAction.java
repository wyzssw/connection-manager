/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.sohu.smc.core.annotation.RequestMapping;
import com.sohu.smc.core.server.ActionSupport;
import com.wap.sohu.recom.service.UserRecomService;
import com.wap.sohu.recom.utils.ConvertUtils;

/**
 * 类UserRecomAction.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2013-7-1 下午02:56:18
 */
@Controller
@RequestMapping(value="/recom/user")
public class UserRecomAction extends ActionSupport{

    public static final String counter = RecomAction.class.getSimpleName();
    
    private static final Logger LOGGER = Logger.getLogger(UserRecomAction.class);

    @Autowired
    private UserRecomService   userRecomService;

    @SuppressWarnings("unchecked")
    @RequestMapping(value = { "/recom" })
    public String action(HttpRequest request, HttpResponse response) throws Exception {
        addCounter(counter);
        Map<String, String> map = packRequest(request);
        LOGGER.info(request.getUri().toString());
        map = ConvertUtils.lowerMapKey(map);
        //另外增加参数cid,platform
        long pid = StringUtils.isNotBlank(map.get("pid")) ? Long.valueOf(map.get("pid")) : 0;
        int pgnum =  StringUtils.isNotBlank(map.get("pgnum")) ? Integer.valueOf(map.get("pgnum")):20;
        int pgsize  = StringUtils.isNotBlank(map.get("pgsize")) ? Integer.valueOf(map.get("pgsize")):1;
//        List<Long> list = userRecomService.getRecomUser(pid, pgnum, pgsize);
//        List<Long> list = userRecomService.getFixedRecomUser();
        List<Long> list = new ArrayList<Long>();
        return ConvertUtils.toJsonJk(list);
    }

}
