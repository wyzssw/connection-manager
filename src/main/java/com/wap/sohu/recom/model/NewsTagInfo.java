package com.wap.sohu.recom.model;

import java.util.Date;

/**
 * 代表新闻tag的model 
 * @author hongfengwang 2012-9-3 上午11:39:33
 */
public class NewsTagInfo {

    private int   id;
    private String tagName;
    private Date   createTime;
    private int    deleteFlag;

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
     * @return the tagName
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * @param tagName the tagName to set
     */
    public void setTagName(String tagName) {
        this.tagName = tagName;
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
    public int getDeleteFlag() {
        return deleteFlag;
    }

    /**
     * @param deleteFlag the deleteFlag to set
     */
    public void setDeleteFlag(int deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

}
