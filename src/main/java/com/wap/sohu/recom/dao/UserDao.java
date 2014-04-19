/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

/**
 * 类UserDao.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2013-7-1 下午05:32:59
 */
@Repository
public class UserDao extends BaseJdbcSupport{
    
    private static String tableName = "r_u_following";

    private Integer shards;
    
    
    public List<Long> getFriends(long pid,List<Long> list){
        List<Long> retList = new ArrayList<Long>();
        if (list.isEmpty()) {
            return retList;
        }
        String instr = StringUtils.join(list, ",");
        String sql = "SELECT sid FROM  "+getTable(pid)+" WHERE pid=? and sid in ("+instr+")" ;
        retList= getJdbcTemplateSmcSociality().queryForList(sql, new Object[]{pid}, Long.class);
        return retList;
    }
    
    /**
     * @param pid
     * @param recompid
     * @return
     */
    public boolean checkFriend(long pid, Long recompid) {
        String sql = "SELECT count(*) FROM  "+getTable(pid)+" WHERE pid=? and sid=?" ;
        int    count = getJdbcTemplateSmcSociality().queryForInt(sql,new Object[]{pid,recompid});
        return count>0?true:false;
    }

    public String getTable(long id) {
        return tableName + "_" + (id % shards);
    }
    
    /**
     * @param shards the shards to set
     */
    public void setShards(Integer shards) {
        this.shards = shards;
    }

}
