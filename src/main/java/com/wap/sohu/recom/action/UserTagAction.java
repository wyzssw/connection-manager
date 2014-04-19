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
import com.wap.sohu.recom.service.impl.UserLikeService;
import com.wap.sohu.recom.utils.ConvertUtils;

/**
 * 类UserLikeAction.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2012-11-26 下午05:30:17
 */
@Controller
@RequestMapping(value="/recom/user")
public class UserTagAction extends ActionSupport{
    
    private static final Logger LOGGER= Logger.getLogger(UserTagAction.class);

    @Autowired
    private UserLikeService userLikeService;
    
    @SuppressWarnings("unchecked")
    @RequestMapping(value = { "/tag.go" })
    public String action(HttpRequest req, HttpResponse resp) throws Exception {
        Map<String, String> map = packRequest(req);
        LOGGER.info(req.getUri().toString());
        map = ConvertUtils.lowerMapKey(map);
        long cid = StringUtils.isNotBlank(map.get("cid")) ? Long.valueOf(map.get("cid")) : 0L;
        int moreCount  = StringUtils.isNotBlank(map.get("morecount"))?Integer.valueOf(map.get("morecount")):0;
        String type = StringUtils.isNotBlank(map.get("type"))?map.get("type"):"";
        List<Integer> list = userLikeService.getTags(cid,type,moreCount);   
        return ConvertUtils.toJsonJk(list);
    }
    
    

}
