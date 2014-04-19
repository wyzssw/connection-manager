/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.action;

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
import com.wap.sohu.recom.service.impl.TopNewsService;
import com.wap.sohu.recom.utils.CompatibleUtil;
import com.wap.sohu.recom.utils.ConvertUtils;


/**
 * 类ChannelNewsRecomAction.java的实现描述：修改请求接口 与 响应报文 兼容新的相关新闻推荐列表
 * @author hongfengwang 2013-7-16 下午04:48:03
 */
@Controller
@RequestMapping(value = "/recom")
public class CompatibleRelevanceAction extends ActionSupport {

    public static final String counter = CompatibleRelevanceAction.class.getSimpleName();

    private static final Logger LOGGER  = Logger.getLogger(CompatibleRelevanceAction.class);

    private static final String TYPE = "relevance";

    @Autowired
    private NewsRecomService newsRecomService;

    @Autowired
    private TopNewsService topNewsService;



    @SuppressWarnings("unchecked")
    @RequestMapping(value = { "/relevance" })
    public String action(HttpRequest request, HttpResponse response) throws Exception {
        addCounter(counter);
        Map<String, String> map = packRequest(request);
        LOGGER.info(request.getUri().toString());
        map = ConvertUtils.lowerMapKey(map);
        long cid = StringUtils.isNotBlank(map.get("cid")) ? Long.valueOf(map.get("cid")) : 0L;
        int newsId = StringUtils.isNotBlank(map.get("nid")) ? Integer.valueOf(map.get("nid")) : 0;
        int moreCount  = StringUtils.isNotBlank(map.get("n"))?Integer.valueOf(map.get("n")):0;
        if(moreCount<=0) moreCount=3;
        int channelId = StringUtils.isNotBlank(map.get("ch"))?Integer.valueOf(map.get("ch")):0;
        String type = StringUtils.isNotBlank(map.get("type"))?map.get("type"):"";
        List<Integer> list = newsRecomService.getRecomNews(cid, newsId, moreCount,channelId);
        newsRecomService.asyncUpdateCache(cid,newsId);
//        newsRecomService.scribeLog(cid,newsId,list);
        topNewsService.processTopNews(cid,newsId,channelId,type);
        if (list == null || list.size() < 3 || list.size() <moreCount) {
            LOGGER.info("news recomlist less than 3 " + "size= " + (list == null ? 0 : list.size()) + " url is "
                        + request.getUri().toString());
            LOGGER.info("news recomlist less than count" + "size= " + (list == null ? 0 : list.size()) + " url is "
                        + request.getUri().toString());
        }
        return CompatibleUtil.toCompatibleJson(list, TYPE);
    }
}
