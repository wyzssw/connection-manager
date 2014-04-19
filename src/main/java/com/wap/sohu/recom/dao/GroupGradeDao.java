/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.dao;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.wap.sohu.recom.dao.BaseJdbcSupport;


/**
 * 类GroupGradeDao.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2012-8-8 下午05:39:30
 */
@Repository
public class GroupGradeDao extends BaseJdbcSupport {
    private static Logger logger = Logger.getLogger(GroupGradeDao.class);
    
   
    
    private volatile AtomicInteger groupGrade = new AtomicInteger(3);
     
    /**
     * 两分钟刷新一次，与接口应用同步更新组图级别
     */
    @PostConstruct
    private void refreshGrade(){
        //两分钟刷新一次，与接口应用同步更新组图级别
        ScheduledExecutorService scheduledThread = Executors.newSingleThreadScheduledExecutor();
        scheduledThread.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {              
               try {
                   groupGrade.set(getGroupGradeFromDb());
               } catch (Throwable e) {
                  logger.error("groupgrade encounter an error", e);
               }
            }
        }, 0, 120, TimeUnit.SECONDS);
    }
    
    /**
     * 从数据库中获取最新组图级别
     * @return
     */
    public int getGroupGradeFromDb(){
        int i = 3;
        String sql = "select grade from p_group_grade_config limit 1";
        List<Integer> list = this.getJdbcTemplatePics().queryForList(sql, Integer.class);
        if (list != null && list.size() > 0) {
            i = list.get(0);
        }
        return i;
    }
    
    /**
     * 返回组图级别
     * @return
     */
    public int getGroupGrade(){
        return groupGrade.intValue();
    }
    
    

}
