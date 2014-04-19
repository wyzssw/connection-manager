package com.wap.sohu.recom.model;

import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 新闻id与tagid的对应关系
 * @author hongfengwang 2012-9-3 下午12:01:26
 */
public class NewsTag {
private int id;
private int newsId; //新闻id
private int tid;//tag id
private double weight;//权重
private double norWeight;//归一化权重

/**
 * @return the norWeight
 */
public double getNorWeight() {
    return norWeight;
}


/**
 * @param norWeight the norWeight to set
 */
public void setNorWeight(double norWeight) {
    this.norWeight = norWeight;
}

private int deleteFlag;
private Date createTime;//记录插入表时间

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
 * @return the newsId
 */
@JSONField(name="newsId")
public int getNewsId() {
    return newsId;
}

/**
 * @param newsId the newsId to set
 */
public void setNewsId(int newsId) {
    this.newsId = newsId;
}

/**
 * @return the tid
 */
@JSONField(name="tid")
public int getTid() {
    return tid;
}

/**
 * @param tid the tid to set
 */
public void setTid(int tid) {
    this.tid = tid;
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

}
