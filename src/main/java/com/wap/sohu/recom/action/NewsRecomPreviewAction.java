/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.action;

import java.util.HashMap;
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
import com.wap.sohu.recom.service.NewsRecomService;
import com.wap.sohu.recom.utils.ConvertUtils;

/**
 * 新闻推荐预览Action
 * @author hongfengwang 2012-9-24 下午04:34:40
 */
@Controller
@RequestMapping(value="/recom/news")
public class NewsRecomPreviewAction extends ActionSupport{
    private static final Logger LOGGER= Logger.getLogger(NewsRecomAction.class);

    public static final String counter = NewsRecomPreviewAction.class.getSimpleName();


    @Autowired
    private NewsRecomService newsRecomService;


    @RequestMapping(value = { "/preview.go" })
    public String action(HttpRequest request, HttpResponse response) throws Exception {
        addCounter(counter);
        @SuppressWarnings("unchecked")
        Map<String, String> map = packRequest(request);
        LOGGER.info(request.getUri().toString());
        map = ConvertUtils.lowerMapKey(map);
        long cid = StringUtils.isNotBlank(map.get("cid")) ? Long.valueOf(map.get("cid")) : 0L;
        int newsId = StringUtils.isNotBlank(map.get("newsid")) ? Integer.valueOf(map.get("newsid")) : 0;
        int moreCount  = StringUtils.isNotBlank(map.get("morecount"))?Integer.valueOf(map.get("morecount")):0;
        int channelId = StringUtils.isNotBlank(map.get("channelid"))?Integer.valueOf(map.get("channelid")):0;
//        List<Integer> list = newsRecomService.getRecomNews(cid, newsId, moreCount,channelId);
        Map<Integer, String> premap = new HashMap<Integer, String>();
        List<Integer> list = newsRecomService.getRecomNewsPreview(cid, newsId, moreCount,channelId,premap);
//        newsRecomService.asyncUpdateCache(cid,newsId);
        if (list == null || list.size() < 3 || list.size() <moreCount) {
            LOGGER.warn("news recomlist less than 3 " + "size= " + (list == null ? 0 : list.size()) + " url is "
                        + request.getUri().toString());
            LOGGER.warn("news recomlist less than count" + "size= " + (list == null ? 0 : list.size()) + " url is "
                        + request.getUri().toString());
        }
//        return ConvertUtils.toJsonJk(list);
        String str1 =  ConvertUtils.toJsonJk(list);
        String str2 =  ConvertUtils.toJsonJk(premap);
        response.setHeader("Content-Type","text/plain; charset=utf-8" );
        return str1+"==="+str2;
    }

}
