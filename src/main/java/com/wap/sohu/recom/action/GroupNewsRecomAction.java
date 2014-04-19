/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.action;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.sohu.smc.core.annotation.RequestMapping;
import com.sohu.smc.core.server.ActionSupport;
import com.wap.sohu.recom.service.impl.GroupNewsRecomService;
import com.wap.sohu.recom.utils.ConvertUtils;


/**
 * 推荐新闻的action类
 * @author hongfengwang 2012-9-4 下午02:24:15
 */
@Controller
@RequestMapping(value="/recom/groupnews")
public class GroupNewsRecomAction extends ActionSupport {

    public static final String counter = GroupNewsRecomAction.class.getSimpleName();

    private static final Logger LOGGER= Logger.getLogger(GroupNewsRecomAction.class);

    @Autowired
    private GroupNewsRecomService groupNewsRecomService;

    private static final Integer DEFAULT_MORECOUNT = 4;
    
    @SuppressWarnings("unchecked")
    @RequestMapping(value = { "/correlation.go" })
    public String action(HttpRequest request, HttpResponse response) throws Exception {
        addCounter(counter);
        Map<String, String> map = packRequest(request);
        LOGGER.info(request.getUri().toString());
        map = ConvertUtils.lowerMapKey(map);
        long cid = StringUtils.isNotBlank(map.get("cid")) ? Long.valueOf(map.get("cid")) : 0L;
        int newsId = StringUtils.isNotBlank(map.get("newsid")) ? Integer.valueOf(map.get("newsid")) : 0;
        int picType = StringUtils.isNotBlank(map.get("pictype")) ? Integer.valueOf(map.get("pictype")) : 0;
        int moreCount = StringUtils.isNotBlank(map.get("morecount")) ? Integer.valueOf(map.get("morecount")) : 0;
        if (moreCount==0) {
            moreCount = DEFAULT_MORECOUNT;
        }
        Map<Integer, String> resultMap = groupNewsRecomService.getRecomItem(cid,picType,newsId,moreCount);
        groupNewsRecomService.setGroupNewsHistory(cid,newsId);
        int count = 0;
        String inner = "{\"id\":%d,\"type\":\"%s\"}";
        String outer = "[%s]";
        StringBuffer sb = new StringBuffer();
        if (resultMap==null||resultMap.size()<moreCount) {
            LOGGER.error("error!!!error!!! "+request.getUri().toString()+" get empty result");
        }
        for (Map.Entry<Integer, String> item : resultMap.entrySet()) {
            sb.append(String.format(inner, item.getKey(),item.getValue()));
            count++;
            if (count<moreCount) {
                sb.append(",");
            }else {
                break;
            }
        }
        return String.format(outer, sb.toString());
    }


}
