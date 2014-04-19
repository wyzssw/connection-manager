package com.wap.sohu.recom.dao;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.wap.sohu.recom.dao.BaseJdbcSupport;

/**
 * 类CellTowerDao.java的实现描述：基站信息查询
 * 
 * @author yeyanchao 2012-9-25 下午3:22:53
 */
@Repository("cellTowerDao")
public class CellTowerDao extends BaseJdbcSupport {

    private static Logger logger = Logger.getLogger(CellTowerDao.class);

    /**
     * 查询城市名
     * 
     * @param lac
     * @param cellId
     * @return
     */
    public String getCityName(int lac, int cellId) {
        if (lac < 0 || cellId < 0) {
            return null;
        }
        String sql = "select city from cell_tower_info where lac = ? and cellid = ?";

        List<String> city = this.getJdbcTemplateMpaperNewSlave().queryForList(sql, new Object[] { lac, cellId },
                                                                              String.class);

        if (city != null && !city.isEmpty() && !StringUtils.equals("null", city.get(0))) {
            return city.get(0);
        }
        return null;
    }
}
