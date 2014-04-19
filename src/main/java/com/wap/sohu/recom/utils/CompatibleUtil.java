package com.wap.sohu.recom.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 类CompatibleUtil.java的实现描述：兼容新的相关推荐 与 频道推荐报文
 *
 * @author yeyanchao 2013-10-24 上午11:06:20
 */
public class CompatibleUtil {

    public static String toCompatibleJson(Map<Integer, String> resultMap, String type) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"status\":\"C00000\"").append(",");
        sb.append("\"msg\":\"success\"").append(",");
        sb.append("\"token\":\"asdg\"").append(",");
        // long ts = System.currentTimeMillis() / 1000;
        // sb.append("\"ctx\":\"engine:old,ts:").append(ts).append("\",");
        // TODO
        // sb.append("\"tracker\":\"engine:old,type:").append(type).append("\",");
        if (resultMap == null || resultMap.isEmpty()) {
            sb.append("\"count\":" + 0).append(",");
            sb.append("\"data\":[]");
        } else {
            sb.append("\"count\":" + resultMap.size()).append(",");
            sb.append("\"data\":").append("[");
            int last = resultMap.size() - 1;
            int iterator = 0;
            for (Integer newsId : resultMap.keySet()) {
                sb.append("{").append("\"nid\":").append(newsId).append(",");
                if (resultMap.get(newsId).startsWith("r")) {
                    sb.append("\"t\":").append("\"r\"");
                } else {
                    sb.append("\"t\":").append("\"h\"");
                }
                sb.append("}");
                if (iterator < last) {
                    sb.append(",");
                }
                iterator++;
            }
            sb.append("]");
        }
        sb.append("}");
        return sb.toString();
    }

    public static String toCompatibleJson(List<Integer> resultList, String type) {
        /**
         * return map : relevance news for type r
         */
        Map<Integer, String> resultMap = ConvertUtils.convertList2Map(resultList, "r");
        return toCompatibleJson(resultMap, type);
    }

    public static void main(String[] args) {
        Map<Integer, String> resultMap = new LinkedHashMap<Integer, String>();
        resultMap.put(1234, "rlong");
        resultMap.put(123434, "rshort");
        resultMap.put(12345, "h");

        System.out.println(toCompatibleJson(resultMap, "recom"));

        System.out.println(toCompatibleJson(Arrays.asList(1234, 1242352, 123132), "relevance"));

        System.out.println(toCompatibleJson(Collections.<Integer>emptyList(), "relevance"));


//         DiscoveryClientUtil client = DiscoveryClientUtil.getInstance("/smc/services");
//         client.rebuildConnection("/smc/services");
//         System.out.println(client.getService("gateway"));

    }

}
