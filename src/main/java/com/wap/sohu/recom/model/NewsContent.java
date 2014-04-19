/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 新闻内容model 
 * @author hongfengwang 2012-9-3 下午04:58:32
 */
public class NewsContent implements Serializable{
    
    /**
     * 
     */
    private static final long serialVersionUID = 5411467185994899853L;
    private Integer id ;
    private String title;
    private String content;
    private Date   createTime;
    private Integer    deleteFlag; 
    private Integer    newsType;
    private Integer pubId;
    private Integer productId;
    private Integer fetchRuleId;
    private List<Integer> subIds;
    private List<Integer> channelIds;
    private String    snapShot;
    private Date      nTime;  
    
    /**
     * @return the newsType
     */
    public Integer getNewsType() {
        return newsType;
    }

    
    /**
     * @param newsType the newsType to set
     */
    public void setNewsType(Integer newsType) {
        this.newsType = newsType;
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }
    
    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }
    
    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }
    
    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
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
     * @return the deleteFlag
     */
    public Integer getDeleteFlag() {
        return deleteFlag;
    }
    
    /**
     * @param deleteFlag the deleteFlag to set
     */
    public void setDeleteFlag(Integer deleteFlag) {
        this.deleteFlag = deleteFlag;
    }
  
    /**
     * @return the pubId
     */
    public Integer getPubId() {
        return pubId;
    }


    
    /**
     * @param pubId the pubId to set
     */
    public void setPubId(Integer pubId) {
        this.pubId = pubId;
    }


    
    /**
     * @return the productId
     */
    public Integer getProductId() {
        return productId;
    }


    
    /**
     * @param productId the productId to set
     */
    public void setProductId(Integer productId) {
        this.productId = productId;
    }


    
    /**
     * @return the fetchRuleId
     */
    public Integer getFetchRuleId() {
        return fetchRuleId;
    }


    
    /**
     * @param fetchRuleId the fetchRuleId to set
     */
    public void setFetchRuleId(Integer fetchRuleId) {
        this.fetchRuleId = fetchRuleId;
    }
    
    /**
     * @return the subIds
     */
    public List<Integer> getSubIds() {
        return subIds;
    }


    
    /**
     * @param subIds the subIds to set
     */
    public void setSubIds(List<Integer> subIds) {
        this.subIds = subIds;
    }


    
    /**
     * @return the channelIds
     */
    public List<Integer> getChannelIds() {
        return channelIds;
    }


    
    /**
     * @param channelIds the channelIds to set
     */
    public void setChannelIds(List<Integer> channelIds) {
        this.channelIds = channelIds;
    }


    
    /**
     * @return the snapShot
     */
    public String getSnapShot() {
        return snapShot;
    }


    
    /**
     * @param snapShot the snapShot to set
     */
    public void setSnapShot(String snapShot) {
        this.snapShot = snapShot;
    }


    
    /**
     * @return the nTime
     */
    public Date getNTime() {
        return nTime;
    }


    
    /**
     * @param nTime the nTime to set
     */
    public void setNTime(Date nTime) {
        this.nTime = nTime;
    }


    


}
