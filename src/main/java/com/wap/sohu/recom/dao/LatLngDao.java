package com.wap.sohu.recom.dao;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import com.wap.sohu.recom.dao.BaseJdbcSupport;
import com.wap.sohu.recom.utils.LocationUtils;

/**
 * 类LatLngDao.java的实现描述：查询经纬度数据
 * 
 * @author yeyanchao 2012-9-28 下午3:58:15
 */
@Repository("latLngDao")
public class LatLngDao extends BaseJdbcSupport {

    public String getCityName(double lat, double lng) {
        String sql = "select city from lat_lng_info where lat = ? and lng = ?";
        List<String> city = this.getJdbcTemplateMpaperNewSlave().queryForList(sql,
                                                                              new Object[] {
                                                                                      Double.valueOf(LocationUtils.formatLatLng(lat)),
                                                                                      Double.valueOf(LocationUtils.formatLatLng(lng)) },
                                                                              String.class);
        if (city != null && !city.isEmpty() && !StringUtils.equals("null", city.get(0))) {
            return city.get(0);
        }
        return null;
    }
}
