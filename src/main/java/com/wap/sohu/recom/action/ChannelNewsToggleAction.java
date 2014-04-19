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
import com.wap.sohu.recom.service.MChannelNewsService;
import com.wap.sohu.recom.utils.ConvertUtils;


/**
 * 类ChannelNewsRecomAction.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2013-7-16 下午04:48:03
 */
@Controller
@RequestMapping(value = "/recom/channel")
public class ChannelNewsToggleAction extends ActionSupport {
    public static final String counter = ChannelNewsToggleAction.class.getSimpleName();


    @Autowired
    private MChannelNewsService mChannelNewsService;
    

    @SuppressWarnings("unchecked")
    @RequestMapping(value = { "/toggle" })
    public String action(HttpRequest request, HttpResponse response) throws Exception {
        addCounter(counter);
        Map<String, String> map = packRequest(request);
        map = ConvertUtils.lowerMapKey(map);
        String key   = StringUtils.isNotBlank(map.get("key")) ? map.get("key"): "";
        String open  = StringUtils.isNotBlank(map.get("open"))?map.get("open"):"";
        String channelId = StringUtils.isNotBlank(map.get("channelid"))?map.get("channelid"):"0";
        if (mChannelNewsService.check(key)) {
            if (channelId.equals("0")) {
                mChannelNewsService.setOnOff(open);
            }else {
                mChannelNewsService.setEachStat(Integer.valueOf(channelId), open);
            }
            return "success";
        }else {
            return "fail";
        }
    }
}
