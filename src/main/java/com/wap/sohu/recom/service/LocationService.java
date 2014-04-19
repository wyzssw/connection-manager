package com.wap.sohu.recom.service;

/**
 * 类LocationService.java的实现描述：定位服务
 * 
 * @author yeyanchao 2012-9-25 下午2:58:20
 */
public interface LocationService {

    public static final String NULL_GBCODE = "-1";

    /**
     * 基站位置定位
     * @param cid TODO
     * @param lac
     * @param cellId
     * 
     * @return 返回 城市gbcode
     */
    public String locateByCellTower(long cid, int lac, int cellId);

    /**
     * 根据经纬度定位
     * @param cid TODO
     * @param lat
     * @param lng
     * 
     * @return 返回 城市gbcode
     */
    public String locateByLatLng(long cid, double lat, double lng);

    /**
     * 根据用户历史数据定位
     * 
     * @param cid
     * @return 返回 城市gbcode
     */
    public String locateByCid(long cid);
}
