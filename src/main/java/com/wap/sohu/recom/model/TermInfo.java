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
 * 刊物
 * @author hongfengwang 2013-1-8 下午06:02:00
 */
public class TermInfo {
  
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
     * @return the pubTermName
     */
    public String getPubTermName() {
        return pubTermName;
    }
    
    /**
     * @param pubTermName the pubTermName to set
     */
    public void setPubTermName(String pubTermName) {
        this.pubTermName = pubTermName;
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
     * @return the publishTime
     */
    public Date getPublishTime() {
        return publishTime;
    }
    
    /**
     * @param publishTime the publishTime to set
     */
    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }
    
    /**
     * @return the publishId
     */
    public int getPublicId() {
        return publicId;
    }
    
    /**
     * @param publishId the publishId to set
     */
    public void setPublicId(int publicId) {
        this.publicId = publicId;
    }
    
    private int    id;
    private String pubTermName;
    private Date   createTime;
    private Date   publishTime;
    private int    publicId;
}
