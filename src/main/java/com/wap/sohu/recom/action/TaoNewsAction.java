/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.action;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.alibaba.fastjson.JSON;
import com.sohu.smc.core.annotation.RequestMapping;
import com.sohu.smc.core.server.ActionSupport;
import com.wap.sohu.recom.service.TaoNewsRecomService;
import com.wap.sohu.recom.utils.ConvertUtils;


/**
 * 类ChannelNewsRecomAction.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2013-7-16 下午04:48:03
 */
@Controller
@RequestMapping(value = "/recom/taonews")
public class TaoNewsAction extends ActionSupport {
    public static final String counter = TaoNewsAction.class.getSimpleName();


    @Autowired
    private TaoNewsRecomService taoNewsRecomService;
    

    @SuppressWarnings("unchecked")
    @RequestMapping(value = { "/pull" })
    public String action(HttpRequest request, HttpResponse response) throws Exception {
        addCounter(counter);
        Map<String, String> map = packRequest(request);
        map = ConvertUtils.lowerMapKey(map);
        long cid = StringUtils.isNotBlank(map.get("cid")) ? Long.valueOf(map.get("cid")) : 0L;
        long pid = StringUtils.isNotBlank(map.get("pid")) ? Long.valueOf(map.get("pid")) : 0L;
        Map<Integer,String> resultMap = taoNewsRecomService.getTaoRecom(cid,pid);
        taoNewsRecomService.scribeLog(cid,pid,resultMap);
        String json = JSON.toJSONString(resultMap.entrySet());
        json = json.replaceAll("key", "newsId").replaceAll("value", "type");
        return json;
//        return JSON.toJSONString(restultMap);
    }
}
