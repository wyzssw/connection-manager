/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.dao;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.stereotype.Repository;

import com.wap.sohu.recom.dao.BaseJdbcSupport;

/**
 * 类NewsChannelDao.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2012-11-8 下午08:59:47
 */
@Repository
public class NewsChannelDao extends BaseJdbcSupport{
    /**
     * 获取所有的date之后的新闻标签关系表
     * @param date
     * @return
     */
    public List<Integer> getChannelNewsIds(Date date){
        String  queryDate = DateFormatUtils.format(date, "yyMMddHHmmss");
        String sql = "SELECT DISTINCT news_content_id FROM `p_channel_news` WHERE create_time>? and newsType=3"; 
        return getJdbcTemplateMpaperCms2Slave().queryForList(sql, Integer.class,new Object[]{queryDate});
    }
    
    
    /**
     * 获取所有的date之后的新闻标签关系表
     * @param date
     * @return
     */
    public List<Integer> getFilterNewsIds(Date date){
        String  queryDate = DateFormatUtils.format(date, "yyMMddHHmmss");
        String sql = "SELECT id FROM `p_news_content` WHERE createTime>? AND newsType=12 AND is_delete=0"; 
        return getJdbcTemplateMpaperCms2Slave().queryForList(sql, Integer.class,new Object[]{queryDate});
    }
    
    public List<Integer> getSkipNews(Date date){
        String  queryDate = DateFormatUtils.format(date, "yyMMddHHmmss");
        String sql = "SELECT id FROM `p_news_content` WHERE createTime>? AND newsType=12 AND is_delete=0"; 
        return getJdbcTemplateMpaperCms2Slave().queryForList(sql, Integer.class,new Object[]{queryDate});
    }
    
    public String getUserLike(long cid,long pid){
        String sql = "select channelIds from tn_sub_channel where clientId =? limit 1";
        if (pid!=0) {
            sql = "select channelIds from tn_sub_channel where pid =? limit 1";
        }
        List<String> result =  getJdbcTemplateMpaperNewSlave().queryForList(sql, new Object[]{cid}, String.class);
        if (result!=null&&!result.isEmpty()) {
            return result.get(0);
        }
        return "";
    }

}
