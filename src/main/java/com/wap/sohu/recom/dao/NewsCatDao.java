/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.wap.sohu.recom.model.NewsCat;

/**
 * 类NewsCatDao.java的实现描述：TODO 类实现描述
 * @author hongfengwang 2013-6-28 下午07:31:42
 */
@Repository("newsCatDao")
public class NewsCatDao extends BaseJdbcSupport{
    public List<NewsCat> getNewsCat(int newsId){
        String sql = "SELECT * FROM p_news_cat where newsid=? order by weight desc";
        return getJdbcTemplateMpaperCms2Slave().query(sql,new Object[]{newsId}, new RowMapper<NewsCat>() {

            @Override
            public NewsCat mapRow(ResultSet rs, int rowNum) throws SQLException {
                NewsCat newsCat = new NewsCat();
                newsCat.setCatid(rs.getInt("catid"));
                newsCat.setCreateTime(rs.getTime("createTime"));
                newsCat.setDup_flag(rs.getInt("dup_flag"));
                newsCat.setId(rs.getInt("id"));
                newsCat.setNewsid(rs.getInt("newsid"));
                newsCat.setWeight(rs.getDouble("weight"));
                return newsCat;
            }

        });
    }

    public Set<String> getSenitiveWords() {
        String sql = "SELECT word FROM `p_senitive_words`";
        List<String> list = getJdbcTemplateMpaperCms2Slave().queryForList(sql, String.class);
        return new HashSet<String>(list);
    }

    public Integer getTopId(int catId){
        String sql = "SELECT toplevel FROM `p_newscat_info` WHERE id=?";
        List<Integer> list = getJdbcTemplateMpaperCms2Slave().queryForList(sql, Integer.class, new Object[]{catId});
        if (list!=null&&!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }
}
