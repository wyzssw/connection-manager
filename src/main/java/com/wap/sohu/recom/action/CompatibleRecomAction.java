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
import com.wap.sohu.recom.service.MChannelNewsRecomService;
import com.wap.sohu.recom.utils.CompatibleUtil;
import com.wap.sohu.recom.utils.ConvertUtils;

/**
 * 类ChannelNewsRecomAction.java的实现描述：修改请求接口 与 响应报文 兼容新的频道推荐列表
 *
 * @author yeyanchao 2013-7-16 下午04:48:03
 */
@Controller
@RequestMapping(value = "/recom")
public class CompatibleRecomAction extends ActionSupport {

    public static final String       counter = CompatibleRecomAction.class.getSimpleName();

    public static final String       TYPE    = "recom";

    @Autowired
    private MChannelNewsRecomService mChannelNewsRecomService;

    @SuppressWarnings("unchecked")
    @RequestMapping(value = { "/recom" })
    public String action(HttpRequest request, HttpResponse response) throws Exception {
        addCounter(counter);
        Map<String, String> map = packRequest(request);
        map = ConvertUtils.lowerMapKey(map);
        long cid = StringUtils.isNotBlank(map.get("cid")) ? Long.valueOf(map.get("cid")) : 0L;
        long pid = StringUtils.isNotBlank(map.get("pid")) ? Long.valueOf(map.get("pid")) : 0L;
        int channelId = StringUtils.isNotBlank(map.get("ch")) ? Integer.valueOf(map.get("ch")) : 0;
        Map<Integer, String> resultMap = mChannelNewsRecomService.getChannelRecom(cid, pid, channelId);
        return CompatibleUtil.toCompatibleJson(resultMap, TYPE);
    }

}
