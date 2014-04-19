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
import com.wap.sohu.recom.dao.SubscriptionDao;
import com.wap.sohu.recom.service.SubscribeRecomService;
import com.wap.sohu.recom.service.impl.SubscribeRecomServiceImpl;
import com.wap.sohu.recom.utils.ConvertUtils;

/**
 * 类SubscribeRecomAction.java的实现描述：TODO 类实现描述
 *
 * @author yeyanchao 2012-10-17 下午5:09:40
 */
@Controller
@RequestMapping(value="/recom/subscribe")
public class SubscribeRecomAction extends ActionSupport {

    private static final Logger   LOGGER         = Logger.getLogger(SubscribeRecomAction.class);

    public static final String    counter        = SubscribeRecomAction.class.getSimpleName();

    private static final String   CID_KEY        = "cid";

    private static final String   MORECOUNT_KEY  = "morecount";

    private static final String   SUBID          = "subid";

    private static final String   MOREDETAIL_KEY = "detail";

    private static final String   RANDOM         = "random";

    /**
     * 不推荐刊物列表
     */
    private static final String   EXCLUDE        = "exclude";

    /**
     * '用户已经订阅刊物
     */
    private static final String   HAS_SUBS       = "hassub";

    private static final String   sperator       = ",";

    @Autowired
    private SubscribeRecomService subscribeRecomService;

    @Autowired
    private SubscriptionDao       subscriptionDao;


    @RequestMapping(value = { "/subscribeRecom.go" })
    public String action(HttpRequest request, HttpResponse resp) throws Exception {
        addCounter(counter);
        Map<String, String> map = packRequest(request);
        LOGGER.info(request.getUri().toString());
        map = ConvertUtils.lowerMapKey(map);
        long cid = StringUtils.isNotBlank(map.get(CID_KEY)) ? Long.valueOf(map.get(CID_KEY)) : 0L;
        int subId = StringUtils.isNotBlank(map.get(SUBID)) ? Integer.valueOf(map.get(SUBID)) : -1;
        int moreCount = StringUtils.isNotBlank(map.get(MORECOUNT_KEY)) ? Integer.valueOf(map.get(MORECOUNT_KEY)) : 4;
        moreCount = Math.min(moreCount, 40);
        int random = StringUtils.isNotBlank(map.get(RANDOM)) ? Integer.valueOf(map.get(RANDOM)) : 0;
        String[] excludes = StringUtils.isNotBlank(map.get(EXCLUDE)) ? StringUtils.split(map.get(EXCLUDE), sperator) : null;
        String[] hasSubs = StringUtils.isNotBlank(map.get(HAS_SUBS)) ? StringUtils.split(map.get(HAS_SUBS), sperator) : null;
        if (cid > 0) {
            List<Integer> list = null;

            if (subId > 0) {
                list = subscribeRecomService.getSubscriptionRecomInDesc(cid, subId, moreCount, excludes, hasSubs);
            } else {
                list = subscribeRecomService.getSubscriptionRecom(cid, moreCount, excludes, hasSubs);
            }

            boolean moreDetail = StringUtils.equals(map.get(MOREDETAIL_KEY), "true") ? true : false;
            if (moreDetail) {
                String result = actionForDetail(cid, list);
                resp.setHeader("Content-Type", "text/plain; charset=utf-8");
                return result;
            }
            if (list == null || list.size() < moreCount) {
                LOGGER.info("recom sub numbers : " + moreCount);
            }
            return ConvertUtils.toJsonJk(list);
        }
        LOGGER.info("cid is null !!");
        return "[]";
    }

    private String actionForDetail(long cid, List<Integer> list) {
        StringBuilder sb = new StringBuilder();
        Map<Integer, String> result = subscriptionDao.listSubscibeInfo(list);
        List<Integer> subList = subscriptionDao.queryUserSubscribeList(cid, 1);
        sb.append("user sub : ");
        if (subList != null) {
            Map<Integer, String> subMap = subscriptionDao.listSubscibeInfo(subList);
            sb.append("{");
            for (Integer subId : subList) {
                if (subMap.containsKey(subId)) {
                    sb.append(subId).append(":").append(subMap.get(subId));
                    sb.append("--").append(((SubscribeRecomServiceImpl)subscribeRecomService).querySubTypes(subId)).append(",");
                }
            }
            sb.append("}");
        }
        sb.append("\n\r");
        // 推荐结果
        sb.append("rec sub : ");
        if (list != null) {
            sb.append("{");
            int index = 0;
            for (Integer subId : list) {
                if (result.containsKey(subId)) {
                    index++;
                    sb.append(subId).append(":").append(result.get(subId));
                    sb.append("--").append(((SubscribeRecomServiceImpl)subscribeRecomService).querySubTypes(subId));
                    if (index%4==0) {
                        sb.append("@@@");
                    }else{
                        sb.append(",");
                    }
                }
            }
            sb.append("}").append("\n\r");
        }

        return sb.toString();
    }
}
