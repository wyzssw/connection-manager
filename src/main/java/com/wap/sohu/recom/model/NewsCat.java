/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.model;

import java.util.Date;

/**
 * 类NewsCat.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2013-6-9 下午02:25:59
 */
public class NewsCat{
    
    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
    
    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * @return the newsid
     */
    public int getNewsid() {
        return newsid;
    }
    
    /**
     * @param newsid the newsid to set
     */
    public void setNewsid(int newsid) {
        this.newsid = newsid;
    }
    
    /**
     * @return the catid
     */
    public int getCatid() {
        return catid;
    }
    
    /**
     * @param catid the catid to set
     */
    public void setCatid(int catid) {
        this.catid = catid;
    }
    
    /**
     * @return the weight
     */
    public double getWeight() {
        return weight;
    }
    
    /**
     * @param weight the weight to set
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }
    
    /**
     * @return the createTime
     */
    public Date getCreateTime() {
        return createTime;
    }
    
    /**
     * @param createTime the createTime to set
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    
    /**
     * @return the dup_flag
     */
    public int getDup_flag() {
        return dup_flag;
    }
    
    /**
     * @param dup_flag the dup_flag to set
     */
    public void setDup_flag(int dup_flag) {
        this.dup_flag = dup_flag;
    }
    private int id;
    private int newsid; //新闻id
    private int catid;//cat id
    private double weight;//权重
    private Date createTime;//创建时间
    private int   dup_flag;
   

}
