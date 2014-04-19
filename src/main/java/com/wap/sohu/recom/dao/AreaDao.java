package com.wap.sohu.recom.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.wap.sohu.recom.dao.BaseJdbcSupport;
import com.wap.sohu.recom.model.Area;

@Repository("areaDao")
public class AreaDao extends BaseJdbcSupport {

    public List<Area> listArea() {
        return this.getJdbcTemplateMpaperCms2().query("select distinct code,area,province,city,gbcode,firstSpell from b_area where code>0 and gbcode is not null and status=0 order by firstSpell",
                                                      new dataMapper());
    }

    private static final class dataMapper implements RowMapper<Area> {

        public Area mapRow(ResultSet rs, int rowNum) throws SQLException {
            Area area = new Area();
            area.setCode(rs.getInt("code"));
            area.setArea(rs.getString("area"));
            area.setProvince(rs.getString("province"));
            area.setCity(rs.getString("city"));
            area.setGbcode(rs.getString("gbcode"));
            area.setFirstSpell(rs.getString("firstSpell"));
            return area;
        }
    }
}
