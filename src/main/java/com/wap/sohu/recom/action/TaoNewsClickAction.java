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

import com.sohu.smc.core.annotation.RequestMapping;
import com.sohu.smc.core.server.ActionSupport;
import com.wap.sohu.recom.service.TaoUpdateNewsService;
import com.wap.sohu.recom.utils.ConvertUtils;


/**
 * 类ChannelNewsRecomAction.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2013-7-16 下午04:48:03
 */
@Controller
@RequestMapping(value = "/recom/taonews")
public class TaoNewsClickAction extends ActionSupport {
    public static final String counter = TaoNewsClickAction.class.getSimpleName();


    @Autowired
    private TaoUpdateNewsService taoUpdateNewsService;
    

    @SuppressWarnings("unchecked")
    @RequestMapping(value = { "/click" })
    public String action(HttpRequest request, HttpResponse response) throws Exception {
        addCounter(counter);
        Map<String, String> map = packRequest(request);
        map = ConvertUtils.lowerMapKey(map);
        long cid = StringUtils.isNotBlank(map.get("cid")) ? Long.valueOf(map.get("cid")) : 0L;
        long pid = StringUtils.isNotBlank(map.get("pid")) ? Long.valueOf(map.get("pid")) : 0L;
        Integer newsId = StringUtils.isNotBlank(map.get("newsid")) ? Integer.valueOf(map.get("newsid")) : 0;
        String  type   = StringUtils.isNotBlank(map.get("type"))?map.get("type"):"";
        taoUpdateNewsService.processClick(cid,pid,newsId,type);
        return "success";
    }
}
