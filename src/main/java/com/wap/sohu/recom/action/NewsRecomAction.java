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
import com.wap.sohu.recom.utils.ConvertUtils;


/**
 * 推荐新闻的action类
 * @author hongfengwang 2012-9-4 下午02:24:15
 */
@Controller
@RequestMapping(value="/recom/news")
public class NewsRecomAction extends ActionSupport {

    public static final String counter = NewsRecomAction.class.getSimpleName();

    private static final Logger LOGGER= Logger.getLogger(NewsRecomAction.class);

    @Autowired
    private NewsRecomService newsRecomService;
    
    @Autowired
    private TopNewsService topNewsService;


    /**
     *  1、查询news-tag列表，得到tag列表，tag排序(排序规则待定，根据权重)，
     *    在列内聚缓存中从第一个tag取5个，第二个tag取3个，第三个取....其余以近邻进行补足。
     *    然后以热度和及时性进行排序得到最终新闻推荐列表；
     *    如果不在news-tag列表中，则实时调微博接口得到tag列表，然后同上。
     *  2、异步更新列内聚，热度，用户历史记录，刷新本地缓存
     *  3、返回新闻推荐列表
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = { "/correlation.go" })
    public String action(HttpRequest request, HttpResponse response) throws Exception {
        addCounter(counter);
        Map<String, String> map = packRequest(request);
        LOGGER.info(request.getUri().toString());
        map = ConvertUtils.lowerMapKey(map);
        long cid = StringUtils.isNotBlank(map.get("cid")) ? Long.valueOf(map.get("cid")) : 0L;
        int newsId = StringUtils.isNotBlank(map.get("newsid")) ? Integer.valueOf(map.get("newsid")) : 0;
        int moreCount  = StringUtils.isNotBlank(map.get("morecount"))?Integer.valueOf(map.get("morecount")):0;
        int channelId = StringUtils.isNotBlank(map.get("channelid"))?Integer.valueOf(map.get("channelid")):0;
        String type = StringUtils.isNotBlank(map.get("type"))?map.get("type"):"";
        List<Integer> list = newsRecomService.getRecomNews(cid, newsId, moreCount,channelId);
        newsRecomService.asyncUpdateCache(cid,newsId);
        newsRecomService.scribeLog(cid,newsId,list);
        topNewsService.processTopNews(cid,newsId,channelId,type);
        if (list == null || list.size() < 3 || list.size() <moreCount) {
            LOGGER.info("news recomlist less than 3 " + "size= " + (list == null ? 0 : list.size()) + " url is "
                        + request.getUri().toString());
            LOGGER.info("news recomlist less than count" + "size= " + (list == null ? 0 : list.size()) + " url is "
                        + request.getUri().toString());
        }
        return ConvertUtils.toJsonJk(list);
    }


}
