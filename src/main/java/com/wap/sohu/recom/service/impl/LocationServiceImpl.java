package com.wap.sohu.recom.service.impl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.wap.sohu.recom.constants.ExpireTimeConstants;
import com.wap.sohu.recom.constants.RedisKeyConstants;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.dao.AreaDao;
import com.wap.sohu.recom.dao.CellTowerDao;
import com.wap.sohu.recom.dao.LatLngDao;
import com.wap.sohu.recom.log.AccessTraceLogData;
import com.wap.sohu.recom.log.AccessTraceLogData.LogCategoryEnum;
import com.wap.sohu.recom.log.AccessTraceLogData.LogKeyEnum;
import com.wap.sohu.recom.log.StatisticLog;
import com.wap.sohu.recom.message.LatLngMsg;
import com.wap.sohu.recom.message.MsgPublisherManager;
import com.wap.sohu.recom.message.MsgQueueName;
import com.wap.sohu.recom.model.Area;
import com.wap.sohu.recom.service.LocationService;
import com.wap.sohu.recom.utils.ConvertUtils;
import com.wap.sohu.recom.utils.LocationUtils;

/**
 * 类LocationServiceImpl.java的实现描述：地理位置定位服务
 *
 * @author yeyanchao 2012-9-25 下午3:02:30
 */
@Repository("locationService")
public class LocationServiceImpl implements LocationService {

    private static final Logger    LOGGER     = Logger.getLogger(LocationServiceImpl.class);

    @Autowired
    private CellTowerDao           cellTowerDao;

    @Autowired
    private LatLngDao              latLngDao;

    @Autowired
    private AreaDao                areaDao;

    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateUser;

    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateRecom;

    @Autowired
    private StringRedisTemplateExt redisTemplateBase;

    @Autowired
    private MsgPublisherManager    msgPublisherManager;

    /**
     * gbcode to city
     */
    private Map<String, String>    gbcodeCity = new HashMap<String, String>();

    /**
     * cit to gbcode
     */
    private Map<String, String>    cityGbcode = new HashMap<String, String>();

    @PostConstruct
    public void init() {
        List<Area> listArea = areaDao.listArea();
        for (Area area : listArea) {
            gbcodeCity.put(area.getGbcode(), area.getCity());
            cityGbcode.put(area.getCity(), area.getGbcode());
        }
    }

    @Override
    public String locateByCellTower(long cid, int lac, int cellId) {
        String gbcode = NULL_GBCODE;
        if (lac >= 0 && cellId >= 0) {
            String key = String.format(RedisKeyConstants.CELL_TOWER_KEY, lac, cellId);
            if (redisTemplateBase.hasKey(key) && redisTemplateBase.getExpire(key) > -1) {
                gbcode = redisTemplateBase.opsForValue().get(key);
            } else {
                String city = LocationUtils.formatCityName(cellTowerDao.getCityName(lac, cellId));
                if (StringUtils.isNotEmpty(city)) {
                    gbcode = cityGbcode.get(city);
                    if (gbcode!=null) {
                        redisTemplateBase.opsForValue().set(key, gbcode, ExpireTimeConstants.Location_EXP_TIME,
                                                            TimeUnit.SECONDS);
                    }else{
                        gbcode = NULL_GBCODE;
                    }
                } else {
                    // 无法定位到时缓存无效值(gbcode="-1")
                    redisTemplateBase.opsForValue().set(key, gbcode, ExpireTimeConstants.LOC_NO_EXP_TIME,
                                                        TimeUnit.SECONDS);
                    // 发送消息到后台服务
                    // msgPublisherManager.publishMsg(MsgQueueName.CELLID,
                    // ConvertUtils.toJsonJk(new CellIdMsg(cid, cellId, lac)));
                }
            }
        }
        // 跟新用户所在城市
        refreshUserLocate(cid, gbcode);

        return gbcode;
    }

    @Override
    public String locateByLatLng(long cid, double lat, double lng) {
        String gbcode = NULL_GBCODE;
        if (LocationUtils.validateLat(lat) && LocationUtils.validateLng(lng)) {
            String latText = LocationUtils.formatLatLng(lat);
            String lngText = LocationUtils.formatLatLng(lng);
            String key = String.format(RedisKeyConstants.LAT_LNG_KEY, latText, lngText);
            if (redisTemplateBase.hasKey(key) && redisTemplateBase.getExpire(key) > -1) {
                gbcode = redisTemplateBase.opsForValue().get(key);
            } else {
                String city = LocationUtils.formatCityName(latLngDao.getCityName(Double.valueOf(latText),
                                                                                 Double.valueOf(lngText)));
                if (StringUtils.isNotEmpty(city)) {
                    gbcode = cityGbcode.get(city);
                    if (gbcode!=null) {
                        redisTemplateBase.opsForValue().set(key, gbcode, ExpireTimeConstants.Location_EXP_TIME,
                                                            TimeUnit.SECONDS);
                    }else{
                        gbcode = NULL_GBCODE;
                    }
                } else {
                    // 无法定位到时缓存无效值(gbcode="-1")
                    redisTemplateBase.opsForValue().set(key, gbcode, ExpireTimeConstants.GPS_NO_EXP_TIME,
                                                        TimeUnit.SECONDS);
                    // 发送消息后台定位服务
                    msgPublisherManager.publishMsg(MsgQueueName.LATLNG,
                                                   ConvertUtils.toJsonJk(new LatLngMsg(cid, lat, lng)));
                }
            }
        }

        /**
         * statitics location
         */
        if(!StringUtils.equalsIgnoreCase(gbcode, NULL_GBCODE)){
            AccessTraceLogData logData = new AccessTraceLogData(LogCategoryEnum.LocationInfo);
            logData.add(LogKeyEnum.Cid, cid);
            logData.add(LogKeyEnum.TimeStamp,Calendar.getInstance().getTimeInMillis());
            logData.add(LogKeyEnum.CITYGBCODE, gbcode);
            logData.add(LogKeyEnum.LOCATIONTYPE, "gps");
            StatisticLog.info(logData);
        }

        // 跟新用户所在城市
        refreshUserLocate(cid, gbcode);

        return gbcode;
    }

    // 更新用户所在城市
    private void refreshUserLocate(long cid, String gbcode) {
        if (cid > 0 && StringUtils.isNotBlank(gbcode) && StringUtils.isNumeric(gbcode)
            && !StringUtils.equalsIgnoreCase(gbcode, NULL_GBCODE)) {
            String key = String.format(RedisKeyConstants.USER_LOCATION_KEY, cid);
            shardedRedisTemplateUser.opsForValue().set(key, gbcode);
        }
    }

    @Override
    public String locateByCid(long cid) {
        String gbcode = NULL_GBCODE;
        if (cid > 0) {
            String key = String.format(RedisKeyConstants.USER_LOCATION_KEY, cid);
            String value = shardedRedisTemplateUser.opsForValue().get(key);
            if (StringUtils.isNotEmpty(value)) {
                gbcode = value;
            }
        }
        return gbcode;
    }
}
