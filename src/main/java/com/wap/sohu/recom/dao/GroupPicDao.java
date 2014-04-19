package com.wap.sohu.recom.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import com.wap.sohu.mobilepaper.model.pic.GroupInfo;
import com.wap.sohu.mobilepaper.model.pic.GroupPic;
import com.wap.sohu.mobilepaper.model.pic.GroupPicInfo;
import com.wap.sohu.recom.dao.BaseJdbcSupport;
import com.wap.sohu.recom.model.GroupTagHive;
import com.wap.sohu.recom.model.GroupTagInfo;

@Repository
public class GroupPicDao extends BaseJdbcSupport {

    @Autowired
    private MessageSource       messageSource;

    @Autowired
    private GroupGradeDao       groupGradeDao;

    private static final Logger LOGGER = Logger.getLogger(GroupPicDao.class);

    private static class GroupInner {

        private String name;
        private int    categoryId;

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return the categoryId
         */
        public int getCategoryId() {
            return categoryId;
        }

        /**
         * @param categoryId the categoryId to set
         */
        public void setCategoryId(int categoryId) {
            this.categoryId = categoryId;
        }

    }

    /**
     * 查询组图简要信息，包括title，第一张图片等
     * 
     * @param gid
     * @return
     */
    public GroupPicInfo findGroupPicInfo(int gid) {
        // String group_max_grade = messageSource.getMessage("group_max_grade", null, Locale.getDefault());
        int group_max_grade = groupGradeDao.getGroupGrade();
        // 加安全级别过滤
        String sql1 = "select name,category_id from p_grouppic_group where id=? and recommendation>3 and delete_flag!=1 and group_grade<="
                      + group_max_grade;
        String sql2 = "select name from p_grouppic_pics  where group_id=? and delete_flag=0 limit 1";
        // LogWriter.printlog("DaoLog:GroupPicDao:findGroupPicInfo(int gid):" + gid);
        @SuppressWarnings("unchecked")
        List<GroupInner> groupInnerList = this.getJdbcTemplatePics().query(sql1, new Object[] { gid }, new RowMapper() {

            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                GroupInner info = new GroupInner();
                info.setName(rs.getString("name"));
                info.setCategoryId(rs.getInt("category_id"));
                return info;
            }

        });
        List<String> picNameList = this.getJdbcTemplatePics().queryForList(sql2, new Object[] { gid }, String.class);

        if (groupInnerList != null && groupInnerList.size() > 0 && picNameList != null && picNameList.size() > 0) {
            GroupPicInfo gp = new GroupPicInfo();
            gp.setId(gid);
            gp.setTitle(groupInnerList.get(0).getName());
            gp.setPicType(groupInnerList.get(0).getCategoryId());
            gp.setPic(StringUtils.trim(picNameList.get(0)));

            return gp;
        }
        return null;
    }

    /**
     * 根据gid获取组图类别
     * 
     * @param gid
     * @return
     */
    public int getCategory(int gid) {
        String sql = "select category_id from p_grouppic_group where id=" + gid;
        try {
            // 如果sql查询的结果为空或 大于1, 将报错. (queryForInt()方法应用于返回结果有且只有一条的情况. 例如count(*)等.
            // int cateId = this.getPicsJdbcTemplate().queryForInt(sql, gid);
            List<Integer> list = this.getJdbcTemplatePics().queryForList(sql, Integer.class);
            if (list != null && list.size() > 0) {
                return list.get(0);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return 0;
    }

    public List<Integer> listGroupPicCategory() {
        String sql = "select id from p_grouppic_category";
        List<Integer> list = this.getJdbcTemplatePics().queryForList(sql, Integer.class);
        return list;
    }

    public List<GroupPicInfo> getHotList(int groupPicType) {
        // String group_max_grade = messageSource.getMessage("group_max_grade", null, Locale.getDefault());
        // 加安全级别控制
        int group_max_grade = groupGradeDao.getGroupGrade();
        String sql1 = "select id, name from p_grouppic_group where category_id=? and group_grade<=" + group_max_grade
                      + " and recommendation>=4 order by id desc limit 50";
        String sql2 = "select name from p_grouppic_pics where group_id=? and delete_flag=0 order by position limit 1";
        String sql3 = "select count(1) from p_grouppic_pics where group_id=? and delete_flag=0";
        List<GroupPicInfo> list = this.getJdbcTemplatePics().query(sql1, new Object[] { groupPicType },
                                                                   new GroupPicInfoMapper());

        Iterator<GroupPicInfo> itor = list.iterator();
        while (itor.hasNext()) {
            GroupPicInfo gp = itor.next();
            try {
                List<String> picNameList = getJdbcTemplatePics().queryForList(sql2, new Object[] { gp.getId() },
                                                                              String.class);
                if (picNameList == null || picNameList.size() == 0) {
                    itor.remove();
                    continue;
                }
                gp.setPic(StringUtils.trim(picNameList.get(0)));
                int picCount = this.getJdbcTemplatePics().queryForInt(sql3, new Object[] { gp.getId() });
                gp.setPicCount(picCount);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return list;
    }

    /**
     * 查询某个组图的图片列表
     * 
     * @param gid
     * @return
     */
    public List<GroupPic> listGroupPics(int gid) {
        String sql = "select * from p_grouppic_pics where group_id=? and delete_flag=0 order by position";
        return getJdbcTemplatePics().query(sql, new Object[] { gid }, new GroupPicMapper());
    }

    /**
     * 查询某个组图的图片url列表
     * 
     * @param gid
     * @return
     */
    public List<String> listGroupPicUrls(int gid) {
        String sql = "select name from p_grouppic_pics where group_id=? and delete_flag=0 order by position";
        return getJdbcTemplatePics().queryForList(sql, new Object[] { gid }, String.class);
    }

    class GroupPicInfoMapper implements RowMapper<GroupPicInfo> {

        @Override
        public GroupPicInfo mapRow(ResultSet rs, int arg1) throws SQLException {
            GroupPicInfo gp = new GroupPicInfo();
            gp.setId(rs.getInt("id"));
            gp.setTitle(rs.getString("name"));
            return gp;
        }

    }

    class GroupPicMapper implements RowMapper<GroupPic> {

        @Override
        public GroupPic mapRow(ResultSet rs, int arg1) throws SQLException {
            GroupPic gp = new GroupPic();
            gp.setGid(rs.getInt("group_id"));
            gp.setUrl(StringUtils.trim(rs.getString("name")));
            gp.setDesc(rs.getString("description"));
            gp.setWidth(rs.getInt("width"));
            gp.setHeight(rs.getInt("height"));
            gp.setCutImgUrl(StringUtils.trim(rs.getString("cutImg")));
            return gp;
        }

    }

  private static  class GroupInfoMapper implements RowMapper<GroupInfo> {

        @Override
        public GroupInfo mapRow(ResultSet rs, int arg1) throws SQLException {
            GroupInfo group = new GroupInfo();
            group.setGid(rs.getInt(1));
            group.setCategory(rs.getInt(2));
            group.setTitle(rs.getString(3));
            group.setModifyTime(rs.getDate(4));
            group.setNewsId(rs.getInt(5));
            group.setShowType(rs.getInt("show_type"));
            group.setNid(rs.getLong("nid"));
            group.setCommentPurview(rs.getInt("commentPurview"));
            group.setIsDelete(rs.getInt("delete_flag"));
            group.setGrade(rs.getInt("group_grade"));
            return group;
        }

    }

    /**
     * 根据组图ID获取该组图的标签IDSet
     * 
     * @param gid
     * @return
     */
    public List<Long> getGroupTagSet(int gid) {
        String sql = "select tag_id from p_group_tag where gid=? and delete_flag=0 order by tag_index";
        SqlRowSet rs = this.getJdbcTemplatePics().queryForRowSet(sql, new Object[] { gid });
        List<Long> tagList = new ArrayList<Long>();
        while (rs.next()) {
            tagList.add(rs.getLong("tag_id"));
        }
        return tagList;
    }

    /**
     * 查询组组安全级别设置
     * 
     * @return
     */
    public int getGroupSaveGrade() {
        int i = 2; // 默认级别为2
        String sql = "select grade from p_group_grade_config limit 1";
        List<Integer> list = this.getJdbcTemplatePics().queryForList(sql, Integer.class);
        if (list != null && list.size() > 0) {
            i = list.get(0);
        }
        // LogWriter.printlog("DaoLog:GroupPicDao:getGroupSaveGrade():" + i);
        return i;
    }

    /**
     * @param gid
     * @return
     */
    public GroupInfo findGroupInfo(int gid) {
        String sql = "select id, category_id, name, modify_time,news_id, show_type, nid, commentPurview, delete_flag, group_grade from p_grouppic_group where id=?";
        // LogWriter.printlog("DaoLog:GroupPicDao:findGroupInfo(int gid):" + gid);
        List<GroupInfo> list = this.getJdbcTemplatePics().query(sql, new Object[] { gid }, new GroupInfoMapper());
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * @param date
     * @return
     */
    public List<GroupTagHive> getAllGroupTagHive(Date date) {
        String sql = "select * from p_group_tag_hive where createTime >?" ;
        String queryDate = DateFormatUtils.format(date, "yyMMddHHmmss");
        List<GroupTagHive> list = this.getJdbcTemplateMpaperCms2().query(sql,new Object[]{queryDate}, new RowMapper<GroupTagHive>() {
            @Override
            public GroupTagHive mapRow(ResultSet rs, int rowNum) throws SQLException {
                GroupTagHive groupTagHive = new GroupTagHive();
                groupTagHive.setId(rs.getInt("id"));
                groupTagHive.setGid(rs.getInt("gid"));
                groupTagHive.setTagId(rs.getInt("tag_id"));
                return null;
            }
        });        
        return list;
    }

    public List<GroupPic> getGroupByDate(Date date){
        int group_max_grade = groupGradeDao.getGroupGrade();
        String sql = "select id from p_grouppic_group where group_grade<=" + group_max_grade + " and recommendation>=4 and create_time>?" ;
        String queryDate = DateFormatUtils.format(date, "yyMMddHHmmss");
        List<GroupPic> list = this.getJdbcTemplatePics().query(sql,new Object[]{queryDate}, new RowMapper<GroupPic>() {
            @Override
            public GroupPic mapRow(ResultSet rs, int rowNum) throws SQLException {
                GroupPic groupPic = new GroupPic();
                groupPic.setGid(rs.getInt("id"));
                return groupPic;
            }
        });        
        return list;
    }
    
    public List<Integer> getDelGroup(Date date){
        String queryDate = DateFormatUtils.format(date, "yyMMddHHmmss");
        String sql = "SELECT id FROM `p_grouppic_group` WHERE create_time>? and delete_flag=1";
        List<Integer> list = this.getJdbcTemplatePics().queryForList(sql, Integer.class, new Object[]{queryDate});
        return list;
    }
}
