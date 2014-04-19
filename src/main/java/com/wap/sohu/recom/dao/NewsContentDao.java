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

import com.wap.sohu.recom.model.NewsContent;

/**
 * 类NewsContentDao.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2013-6-21 下午05:07:12
 */
@Repository
public class NewsContentDao extends BaseJdbcSupport{
    
    public NewsContent getNewsContent(int newsId){
          String sql = "select * from p_news_content where id=? and is_delete=0 limit 1";
          List<NewsContent> list = getJdbcTemplateMpaperCms2().query(sql, new Object[]{newsId},  new RowMapper<NewsContent>(){

            @Override
            public NewsContent mapRow(ResultSet rs, int rowNum) throws SQLException {
                NewsContent newsContent = new NewsContent();
                newsContent.setId(rs.getInt("id"));
                newsContent.setTitle(rs.getString("title"));
                newsContent.setDeleteFlag(rs.getInt("is_delete"));
                newsContent.setCreateTime(rs.getTimestamp("createTime"));
                newsContent.setNewsType(rs.getInt("newsType"));
                newsContent.setPubId(rs.getInt("pubId"));
                newsContent.setProductId(rs.getInt("productId"));
                newsContent.setFetchRuleId(rs.getInt("fetchRuleId"));
                newsContent.setSnapShot(rs.getString("SNAPSHOT"));
                return newsContent;
            }
            
        });
          if (list!=null&&list.size()>0) {
            return list.get(0);
        }
          return null;
    }
    
    
    public List<Integer> getChannelIds(int newsId){
//        String  sql        = "select channelId from p_channel_news where news_content_id=? and channelId not in (1,25,27)";
        String  sql        = "select channelId from p_channel_news where news_content_id=?";
        List<Integer> channelIds  =  getJdbcTemplateMpaperCms2Slave().queryForList(sql, Integer.class, new Object[]{newsId});
        return  channelIds;
    }
    
    public List<Integer> getSubIds(int pubId){
        String sql = "select subscriptionId from p_subscription_bind  where  bindId=? ";
        List<Integer> subIds  =  getJdbcTemplateMpaperCms2Slave().queryForList(sql, Integer.class, new Object[]{pubId});
        return  subIds;
    }
    
    public Integer getPubIds(int newsId){
        String sql = "select ownerId from p_news_owner  where  newsId=? and ( ownerType = 1 or ownerType = 2)";
        List<Integer> list = getJdbcTemplateMpaperCms2Slave().queryForList(sql,Integer.class, new Object[]{newsId});
        if (list!=null&&!list.isEmpty()) {
            return list.get(0);
        }
        return -1;
    }
 }
