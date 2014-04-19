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
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.wap.sohu.recom.model.TermInfo;

/**
 * 类TermInfoDao.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2013-1-9 上午10:24:31
 */
@Repository
public class TermInfoDao extends BaseJdbcSupport{
    
    /**
     * 获取所有的date之后的新闻标签关系表
     * @param date
     * @return
     */
    public TermInfo getTermInfo(int termId){
        String sql = "select * from p_term_info where id=?"; 
        List<TermInfo> list = getJdbcTemplateMpaperCms2Slave().query(sql, new Object[]{termId}, new RowMapper<TermInfo>(){

            @Override
            public TermInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                   TermInfo termInfo = new TermInfo();
                   termInfo.setCreateTime(rs.getTimestamp("createTime"));
                   termInfo.setPublishTime(rs.getTimestamp("publishTime"));
                   termInfo.setPublicId(rs.getInt("publicId"));
                   termInfo.setPubTermName(rs.getString("pubTermName"));
                   termInfo.setId(rs.getInt("id"));
                   return termInfo;
            }
            
        });
        if (list==null||list.size()==0) {
            return null;
        }
        return list.get(0);
    }
    
}
