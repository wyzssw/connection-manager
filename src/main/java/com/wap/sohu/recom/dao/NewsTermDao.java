/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Repository;

import com.wap.sohu.recom.dao.BaseJdbcSupport;

/**
 * 期刊新闻
 * @author hongfengwang 2012-11-1 上午11:26:37
 */
@Repository
public class NewsTermDao  extends BaseJdbcSupport { 
    
    @Autowired
    private MessageSource messageSource;
 
    /**
     * 获取所有的date之后的新闻标签关系表
     * @param date
     * @return
     */
    public List<Integer> getTermNews(List<Integer> termIds){
        if (termIds==null||termIds.size()==0) {
            return new ArrayList<Integer>();
        }
        String filterTermIds = StringUtils.join(termIds.iterator(), ",");
        String sql = "SELECT DISTINCT newsId from p_term_news WHERE termId IN ("+filterTermIds+")"; 
        return  getJdbcTemplateMpaperCms2Slave().queryForList(sql, null, Integer.class);
    }
    
    
    public List<Integer> getNoPubTermIds(Date date){
        String  queryDate = DateFormatUtils.format(date, "yyMMddHHmmss");
        String  sql = "SELECT DISTINCT id FROM `p_term_info` WHERE lastEditTime>? AND termStatus<>4";
        return  getJdbcTemplateMpaperCms2Slave().queryForList(sql, new Object[]{queryDate}, Integer.class);
    }
    
    public List<Integer> getPubedTermId(Date date){
        String  queryDate = DateFormatUtils.format(date, "yyMMddHHmmss");
        String  sql = "SELECT DISTINCT id FROM `p_term_info` WHERE lastEditTime>? AND termStatus=4";
        return  getJdbcTemplateMpaperCms2Slave().queryForList(sql, new Object[]{queryDate}, Integer.class);
    }
    
    
     public List<Integer> getSkipTermNews(List<Integer> ids,Date date){
       String queryDate = DateFormatUtils.format(date, "yyMMddHHmmss");
       String sql2 = "SELECT id from p_news_content WHERE createTime>?  and newsType=12 ";
       List<Integer> filterNewsIds = getJdbcTemplateMpaperCms2Slave().queryForList(sql2,new Object[]{queryDate},Integer.class);
       return filterNewsIds;
     }
}
