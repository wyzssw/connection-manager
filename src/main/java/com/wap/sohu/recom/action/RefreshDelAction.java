package com.wap.sohu.recom.action;


import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.sohu.smc.core.annotation.RequestMapping;
import com.sohu.smc.core.http.Action;
import com.sohu.smc.core.server.ActionSupport;
import com.wap.sohu.recom.service.GroupPicService;
import com.wap.sohu.recom.urlmapping.UriMapping;
import com.wap.sohu.recom.utils.RequestUtil;


/**
 * 最近时间段内编辑删除的组图缓存在redis中
 * @author hongfengwang
 */
@Controller
@RequestMapping(value="/recom")
public class RefreshDelAction extends ActionSupport {

    public static final String counter = RefreshDelAction.class.getSimpleName();


    private static final Logger LOGGER = Logger.getLogger(RefreshDelAction.class);

    @Autowired
    private GroupPicService groupPicService;

    @RequestMapping(value="delgroup.go")
    public String action(HttpRequest request, HttpResponse response) throws Exception {
        addCounter(counter);
        LOGGER.info(request.getUri().toString());
        Map<String, String> map = packRequest(request);
        List<Long> list = RequestUtil.getRequestList(map.get("gids"));
        groupPicService.setDelGroupToRedis(list);
        return "Refresh sucess";
    }

}
