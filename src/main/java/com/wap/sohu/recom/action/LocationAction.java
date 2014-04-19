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
import com.wap.sohu.recom.service.LocationService;
import com.wap.sohu.recom.service.TopNewsRecomService;
import com.wap.sohu.recom.utils.ConvertUtils;
import com.wap.sohu.recom.utils.LocationUtils;

/**
 * 类LocationAction.java的实现描述：用户定位服务
 *
 * @author yeyanchao 2012-9-25 下午2:43:27
 */
@Controller
@RequestMapping(value = "/recom/location")
public class LocationAction extends ActionSupport {

    public static final String  counter = LocationAction.class.getSimpleName();

    private static final Logger LOGGER  = Logger.getLogger(LocationAction.class);

    @Autowired
    private LocationService     locationService;

    @Autowired
    private TopNewsRecomService topNewsRecomService;

    @RequestMapping(value = { "/queryLocation.go" })
    public String action(HttpRequest request, HttpResponse response) throws Exception {
        addCounter(counter);
        Map<String, String> map = packRequest(request);
        LOGGER.info(request.getUri().toString());
        map = ConvertUtils.lowerMapKey(map);
        long cid = StringUtils.isNotBlank(map.get("cid")) ? Long.valueOf(map.get("cid")) : -1;
        // 定位服务类型
        // process Windowsphone问题：
        double lat = Double.NEGATIVE_INFINITY, lng = Double.NEGATIVE_INFINITY;

        if (StringUtils.isNotBlank(map.get("lat"))) {
            lat = Double.valueOf(formatLatLngData(map.get("lat")));
        }

        if (StringUtils.isNotBlank(map.get("lng"))) {
            lng = Double.valueOf(formatLatLngData(map.get("lng")));
        }

        /**
         * 优先经纬度定位
         */
        if (LocationUtils.validateLat(lat) && LocationUtils.validateLng(lng)) {
            String gbcode = locationService.locateByLatLng(cid, lat, lng);
            return gbcode;
        }

        /**
         * 其次基站定位
         */
        int lac = StringUtils.isNotBlank(map.get("lac")) ? Integer.valueOf(map.get("lac")) : -1;
        int cellId = StringUtils.isNotBlank(map.get("cellid")) ? Integer.valueOf(map.get("cellid")) : -1;
        if (lac >= 0 && cellId >= 0) {
            String gbcode = locationService.locateByCellTower(cid, lac, cellId);
            return gbcode;
        }

        /**
         * 用户历史定位记录
         */
        if (cid > 0) {
            String gbcode = locationService.locateByCid(cid);
            return gbcode;
        }

        return LocationService.NULL_GBCODE;
    }

    /**
     * 处理WindowsPhone数据问题
     *
     * @param origin
     * @return
     */
    private String formatLatLngData(String origin) {
        origin = StringUtils.contains(origin, ",") ? StringUtils.replace(origin, ",", ".") : origin;
        return origin;
    }

    @RequestMapping(value = { "/news.go" })
    public String news(HttpRequest request, HttpResponse response) throws Exception {
        Map<String, String> map = packRequest(request);
        map = ConvertUtils.lowerMapKey(map);
        int page = StringUtils.isNotBlank(map.get("page")) ? Integer.valueOf(map.get("page")) : -1;
        if (page <= 0) page = 1;

        List<String> ids = topNewsRecomService.getLocalNews(page);

        if(ids==null||ids.isEmpty()) return "";

        return ids.toString();
    }
}
