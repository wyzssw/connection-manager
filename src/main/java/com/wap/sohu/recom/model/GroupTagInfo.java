package com.wap.sohu.recom.model;

/**
 * 类TagInfo.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2012-7-3 上午11:08:19
 */
public class GroupTagInfo {
    
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
    private int id;
    private String tagName;
}
