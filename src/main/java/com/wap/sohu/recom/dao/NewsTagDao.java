package com.wap.sohu.recom.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.wap.sohu.recom.dao.BaseJdbcSupport;
import com.wap.sohu.recom.model.NewsContent;
import com.wap.sohu.recom.model.NewsTag;
import com.wap.sohu.recom.model.NewsTagInfo;

/**
 * 新闻标签相关数据库访问层
 * @author hongfengwang 2012-9-3 下午12:07:36
 */
@Repository
public class NewsTagDao extends BaseJdbcSupport {
    
    @Autowired
    private MessageSource messageSource;

    /**
     * 获取所有的date之后的新闻标签关系表
     * @param date
     * @return
     */
    public List<NewsTag> getAllNewsTags(Date date){
        String queryDate = DateFormatUtils.format(date, "yyMMdd");
        String sql = "select * from p_news_tag where createTime >? and delete_falg=0"; 
        return  getJdbcTemplateMpaperCms2Slave().query(sql, new Object[]{queryDate}, new RowMapper<NewsTag>(){

            @Override
            public NewsTag mapRow(ResultSet rs, int rowNum) throws SQLException {
                NewsTag newsTag = new NewsTag();
                newsTag.setId(rs.getInt("id"));
                newsTag.setNewsId(rs.getInt("newid"));
                newsTag.setTid(rs.getInt("tid"));
                newsTag.setWeight(rs.getDouble("weight"));
                newsTag.setNorWeight(rs.getDouble("normal_weight"));
                newsTag.setDeleteFlag(rs.getInt("delete_flag"));
                newsTag.setCreateTime(rs.getTimestamp("createTime"));
                return newsTag;
            }
            
        });
    }
    
    
//    /**
//     * 获取所有的date之后的新闻标签关系表
//     * @param date
//     * @return
//     */
//    public List<NewsTag> getAllNewsTagsHive(Date date){
//        String queryDate = DateFormatUtils.format(date, "yyMMdd");
//        String sql = "select * from p_news_tag_hive where createTime >? and delete_flag=0 and normal_weight>0.1"; 
//        return  getJdbcTemplateMpaperCms2().query(sql, new Object[]{queryDate}, new RowMapper<NewsTag>(){
//
//            @Override
//            public NewsTag mapRow(ResultSet rs, int rowNum) throws SQLException {
//                NewsTag newsTag = new NewsTag();
//                newsTag.setId(rs.getInt("id"));
//                newsTag.setNewsId(rs.getInt("newid"));
//                newsTag.setTid(rs.getInt("tid"));
//                newsTag.setWeight(rs.getDouble("weight"));
//                newsTag.setNorWeight(rs.getDouble("normal_weight"));
//                newsTag.setDeleteFlag(rs.getInt("delete_flag"));
//                newsTag.setCreateTime(rs.getTimestamp("createTime"));
//                return newsTag;
//            }
//            
//        });
//    }
    
    
    /**
     * 得到新闻tag表具体tag的是否存在
     * @param tag
     * @return
     */
    public NewsTagInfo getTag(String tag){
        String sql = "select * from p_newstag_info where tag_name=?";
        List<NewsTagInfo>  list  = getJdbcTemplateMpaperCms2Slave().query(sql, new Object[]{tag}, new RowMapper<NewsTagInfo>() {

            @Override
            public NewsTagInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
               NewsTagInfo newsTagInfo = new NewsTagInfo();
               newsTagInfo.setCreateTime(rs.getTimestamp("createTime"));
               newsTagInfo.setDeleteFlag(rs.getInt("delete_flag"));
               newsTagInfo.setId(rs.getInt("id"));
               newsTagInfo.setTagName(rs.getString("tag_name"));
               return newsTagInfo;
            }
        });
        if (list==null||list.size()==0) {
            return null;
        }
        return list.get(0);
    }
    
    /**
     * 将新tag添加到tag表里
     * @param tag
     */
    public boolean addTag(String tag){
        String sql = "insert into p_newstag_info(tag_name,createTime,delete_flag) values(?,?,?)";
        int rows =  getJdbcTemplateMpaperCms2().update(sql,new Object[]{tag,new Timestamp(System.currentTimeMillis()),0});
        return rows>0;
    }
    
    
   /**
    * 获得newsTag对象
    * @param tag
    * @param newsId
    * @return
    */
   public NewsTag getNewsTag(String tag,int newsId){
//       NewsTagInfo newsTagInfo = new NewsTagInfo();
//       newsTagInfo = getTag(tag);
       NewsTagInfo newsTagInfo = getTag(tag);
       if (newsTagInfo==null) {
           addTag(tag);
           return null;
       }
       String sql = "select * from p_news_tag where newid=? and tid =?";
       List<NewsTag>  list  = getJdbcTemplateMpaperCms2Slave().query(sql, new Object[]{newsId,newsTagInfo.getId()}, new RowMapper<NewsTag>() {

           @Override
           public NewsTag mapRow(ResultSet rs, int rowNum) throws SQLException {
               NewsTag newsTag = new NewsTag();
               newsTag.setCreateTime(rs.getTimestamp("createTime"));
               newsTag.setDeleteFlag(rs.getInt("delete_flag"));
               newsTag.setId(rs.getInt("id"));
               newsTag.setNewsId(rs.getInt("newid"));
               newsTag.setTid(rs.getInt("tid"));
               newsTag.setWeight(rs.getDouble("weight"));
               newsTag.setNorWeight(rs.getDouble("normal_weight"));
               return newsTag;
           }
       });
       if (list==null||list.size()==0) {
           return null;
       }
       return list.get(0);
   }

    /**
     * 将newsTag对象数据添加到数据库中
     * @param newsId
     * @param key
     * @param value
     */
    public boolean addNewsTag(int newsId, String key, Double value) {
        NewsTagInfo newsTagInfo = getTag(key);
        if (newsTagInfo == null) {
            return false;
        }
        String sql = "insert into p_news_tag(newid,tid,weight,delete_flag,createTime) values(?,?,?,?)";
        int rows = getJdbcTemplateMpaperCms2().update(sql, new Object[]{newsId,newsTagInfo.getId(),0,new Timestamp(System.currentTimeMillis())});
        return (rows>0);        

    }
    
    /**
     * 获得新闻的建立时间
     * @param newsId
     * @return
     */
    public Long getNewsTime(int newsId){
        String sql = "SELECT createTime FROM `p_news_content` WHERE id=? and is_delete=0";
        List<Date> list = getJdbcTemplateMpaperCms2Slave().queryForList(sql, new Object[]{newsId}, Date.class);
        if (list==null||list.size()==0||list.get(0)==null) {
            return null;
        }
        return list.get(0).getTime()/1000;
    }
    
    public NewsContent getNewsById(int newsId,Date date){
        String queryDate = DateFormatUtils.format(date, "yyMMddHHmmss");
        List<NewsContent> list = getNewsListById(newsId);
        if (list!=null&&list.size()>0) {
            NewsContent retNews =  list.get(0);
            String filterPubIds ="0";
            filterPubIds =  messageSource.getMessage("news_filter_news_pub_ids", null, Locale.getDefault());
            String sql =  "SELECT id from p_term_info WHERE lastEditTime>? and publicId in ("+filterPubIds+")";
            List<Integer> retList  = getJdbcTemplateMpaperCms2Slave().queryForList(sql,new Object[]{queryDate},Integer.class);
            String filterTermIds = StringUtils.join(retList.iterator(),",");
            String sql2 = "SELECT newsId FROM `p_term_news` WHERE termId IN ("+filterTermIds+")";
            List<Integer> filterNewsIds = getJdbcTemplateMpaperCms2Slave().queryForList(sql2,Integer.class);
            if (filterNewsIds!=null&&filterNewsIds.contains(newsId)) {
                return null;
            }
            return retNews;
        }
        return null;
    }

    /**
     * @param newsId
     * @return
     */
    private List<NewsContent> getNewsListById(int newsId) {
        String sql = "SELECT * FROM `p_news_content` WHERE id=? and is_delete=0";
        List<NewsContent> list= getJdbcTemplateMpaperCms2Slave().query(sql, new Object[]{newsId}, new RowMapper<NewsContent>(){

            @Override
            public NewsContent mapRow(ResultSet rs, int rowNum) throws SQLException {
                NewsContent newsContent = new NewsContent();
                newsContent.setId(rs.getInt("id"));
                newsContent.setTitle(rs.getString("title"));
//                newsContent.setContent(rs.getString("content"));
                newsContent.setDeleteFlag(rs.getInt("is_delete"));
                newsContent.setCreateTime(rs.getTimestamp("createTime"));
                newsContent.setNewsType(rs.getInt("newsType"));
                return newsContent;
            }
            
        });
        if (list==null||list.isEmpty()) {
            return new ArrayList<NewsContent>();
        }
        for (NewsContent newsContent : list) {
             newsContent.setContent(getNewsText(newsContent.getId()));
        }
        return list;
    }
    
    public NewsContent getOneNewsById(int newsId){
        List<NewsContent> list = getNewsListById(newsId);
        if (list!=null&&list.size()>0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 获得newsType
     * @param newsId
     * @return
     */
    public Integer getNewsType(int newsId) {
        String sql = "SELECT newsType FROM p_news_content WHERE id =? and is_delete=0"; 
        List<Integer> retTypeList =  getJdbcTemplateMpaperCms2Slave().queryForList(sql, new Object[]{newsId}, Integer.class);
        if (retTypeList==null||retTypeList.size()==0) {
            return 0;
        }
        return retTypeList.get(0);
    }
    
    public Set<Integer> getNewsCatId(int newsId,Date date){
        String queryDate = DateFormatUtils.format(date, "yyMMddHHmmss");
        String sql = "select catid from p_news_cat where newsid=? and createTime>?";
        List<Integer> list=   getJdbcTemplateMpaperCms2Slave().queryForList(sql, new Object[]{newsId,queryDate}, Integer.class);
        return new HashSet<Integer>(list);
    }


    /**
     * @param newsId
     * @return
     */
    public Integer getPubId(Integer newsId) {
        String sql = "SELECT pubId FROM p_news_content WHERE id =? and is_delete=0 limit 1"; 
        List<Integer> retTypeList =  getJdbcTemplateMpaperCms2Slave().queryForList(sql, new Object[]{newsId}, Integer.class);
        if (retTypeList==null||retTypeList.size()==0) {
            return 0;
        }
        return retTypeList.get(0);
    }
    
    public Integer getDupId(Integer newsId){
        String sql = "SELECT dup_flag FROM p_news_tag where newid=? ";
        List<Integer> dupList = getJdbcTemplateMpaperCms2Slave().queryForList(sql, new Object[]{newsId}, Integer.class);
        if (dupList!=null&&dupList.size()>0) {
            return dupList.get(0);
        }
        return 0;
    }
    


    /**
     * @param pubId
     * @return
     */
    public List<Integer> getPubIdNewsList(Integer pubId,Date date) {
        String queryDate = DateFormatUtils.format(date, "yyMMddHHmmss");
        String sql = "select id from p_news_content where createTime>? and pubId=? and is_delete=0 limit 500";
        List<Integer> list = getJdbcTemplateMpaperCms2Slave().queryForList(sql,new Object[]{queryDate,pubId}, Integer.class);
        return list;
    }
    
    public String getNewsText(Integer id){
        String sql = "SELECT content FROM p_news WHERE id=?";
        List<String> list  = getJdbcTemplateMpaperCms2Slave().queryForList(sql,new Object[]{id}, String.class);
        if (list!=null&&list.size()>0) {
            return list.get(0);
        }
        return "";
    }
    
    private static final class  Pnews implements RowMapper<NewsContent>{
        @Override
        public NewsContent mapRow(ResultSet rs, int rowNum) throws SQLException {
            NewsContent newsContent = new NewsContent();
            newsContent.setId(rs.getInt("id"));
            newsContent.setContent(rs.getString("content"));
            return newsContent;
        }
    };
 
}
