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
import com.wap.sohu.recom.service.GroupRecomService;
import com.wap.sohu.recom.service.impl.TopNewsService;
import com.wap.sohu.recom.utils.ConvertUtils;

/**
 * 类RecomAction.java的实现描述：TODO 类实现描述
 *
 * @author hongfengwang 2012-7-20 下午03:52:59
 */
@Controller
@RequestMapping(value="/recom/group")
public class RecomAction extends ActionSupport {

    public static final String counter = RecomAction.class.getSimpleName();

    private static final Logger LOGGER = Logger.getLogger(RecomAction.class);

    @Autowired
    private GroupRecomService   groupRecomService;
    
    @Autowired
    private TopNewsService topNewsService;

    @RequestMapping(value = { "/correlation.go" })
    public String action(HttpRequest request, HttpResponse response) throws Exception {
        addCounter(counter);
        Map<String, String> map = packRequest(request);
        LOGGER.info(request.getUri().toString());
        map = ConvertUtils.lowerMapKey(map);
        long cid = StringUtils.isNotBlank(map.get("cid")) ? Long.valueOf(map.get("cid")) : 0L;
        int newsId = StringUtils.isNotBlank(map.get("newsid")) ? Integer.valueOf(map.get("newsid")) : 0;
        int gid = StringUtils.isNotBlank(map.get("gid")) ? Integer.valueOf(map.get("gid")) : 0;
        int moreCount = StringUtils.isNotBlank(map.get("morecount")) ? Integer.valueOf(map.get("morecount")) : 0;
        int picType = StringUtils.isNotBlank(map.get("pictype")) ? Integer.valueOf(map.get("pictype")) : 0;
        List<Integer> list = groupRecomService.getRecomGroup(cid, newsId, gid, moreCount, picType);
        if (newsId!=0) {
            topNewsService.processTopGroupNews(cid, newsId);
        }
        return ConvertUtils.toJsonJk(list);
    }

}
