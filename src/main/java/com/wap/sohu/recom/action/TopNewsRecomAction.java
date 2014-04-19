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
import com.wap.sohu.recom.constants.CommonConstants;
import com.wap.sohu.recom.service.TopNewsRecomService;
import com.wap.sohu.recom.utils.ConvertUtils;

/**
 * 类TopNewsRecomAction.java的实现描述：TODO 类实现描述
 *
 * @author yeyanchao Jun 19, 2013 4:39:12 PM
 */
@Controller
@RequestMapping(value = "/recom/news")
public class TopNewsRecomAction extends ActionSupport {

    @SuppressWarnings("unused")
    private static final Logger LOGGER     = Logger.getLogger(TopNewsRecomAction.class);

    public static final String  counter    = TopNewsRecomAction.class.getSimpleName();

    private static final String CID_KEY    = "cid";

    private static final String PASSID_KEY = "passportid";

    @SuppressWarnings("unused")
    private static final String PULL_KEY   = "pull";

    @Autowired
    private TopNewsRecomService topNewsRecomService;

    @SuppressWarnings({ "unchecked", "unused" })
    @RequestMapping(value = { "/topnews.go" })
    public String action(HttpRequest request, HttpResponse resp) throws Exception {
        addCounter(counter);
        Map<String, String> map = packRequest(request);
        map = ConvertUtils.lowerMapKey(map);
        long cid = StringUtils.isNotBlank(map.get(CID_KEY)) ? Long.valueOf(map.get(CID_KEY)) : 0L;
        long passportid = StringUtils.isNotBlank(map.get(PASSID_KEY)) ? Long.valueOf(map.get(PASSID_KEY)) : 0L;
//        boolean ispull = StringUtils.equals("1", map.get(PULL_KEY)) ? true : false;
        boolean ispull = false;
        if (cid > 0) {
            Map<Integer, String> resultMap = topNewsRecomService.recomTopNews(cid, passportid, ispull);
            if (resultMap != null && !resultMap.isEmpty()) {
                topNewsRecomService.scribeLog(cid, resultMap);
                return result(resultMap);
            }
        }
        return "[]";
    }

    private static String result(Map<Integer, String> resultMap) {
        StringBuilder result = new StringBuilder();
        result.append("[");
        int size = resultMap.size();
        int count = 0;
        for (Map.Entry<Integer, String> entry : resultMap.entrySet()) {
            result.append("{").append("\"newsid\":").append(entry.getKey()).append(",");
            if (StringUtils.equals(entry.getValue(), CommonConstants.HISTORY_TYPE)) {
                result.append("\"newly\":").append("\"").append("false").append("\"}");
            } else {
                result.append("\"type\":").append("\"").append(entry.getValue()).append("\",");
                result.append("\"newly\":").append("\"").append("true").append("\"}");
            }
            count++;
            if (count < size) {
                result.append(",");
            }
        }
        result.append("]");

        return result.toString();
    }

}
