/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.action;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.sohu.smc.core.annotation.RequestMapping;
import com.sohu.smc.core.server.ActionSupport;
import com.wap.sohu.recom.service.NewsTagService;
import com.wap.sohu.recom.utils.RequestUtil;

/**
 * 删除新闻的Action
 * @author hongfengwang 2012-9-24 下午12:08:29
 */
@Controller
@RequestMapping(value="recom/news")
public class NewsDelAction extends ActionSupport{
    private static final Logger LOGGER = Logger.getLogger(NewsDelAction.class);

    public static final String counter = NewsDelAction.class.getSimpleName();


    @Autowired
    private NewsTagService newsTagService;

    @RequestMapping(value = { "/delnews" })
    public String action(HttpRequest request, HttpResponse resp) throws Exception {
        addCounter(counter);
        Map<String, String> map = packRequest(request);
        List<Integer> list = RequestUtil.getRequestIntList(map.get("newsIds"));
        newsTagService.setDelNewsToRedis(list);
        return "Refresh sucess";
    }

}
