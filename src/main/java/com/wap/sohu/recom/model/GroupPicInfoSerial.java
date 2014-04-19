/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.model;

import java.io.Serializable;
import java.util.Date;

import com.wap.sohu.mobilepaper.model.pic.GroupPicInfo;

/**
 * 类GroupPicInfoSerial.java的实现描述：TODO 类实现描述
 * 
 * @author hongfengwang 2013-1-5 下午03:08:30
 */
public class GroupPicInfoSerial implements Serializable{

    private static final long serialVersionUID = -6298897222279285807L;
    
    public GroupPicInfoSerial(GroupPicInfo groupPicInfo){
        this.id=groupPicInfo.getId();
        this.pic=groupPicInfo.getPic();
        this.picCount=groupPicInfo.getPicCount();
        this.picType=groupPicInfo.getPicType();
        this.recommendDate=groupPicInfo.getRecommendDate();
        this.title  =groupPicInfo.getTitle();
        this.weigth =groupPicInfo.getWeigth();
    }
    
    public GroupPicInfoSerial(){
        id = -1;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public int getWeigth() {
        return weigth;
    }

    public void setWeigth(int weigth) {
        this.weigth = weigth;
    }

    public int getPicCount() {
        return picCount;
    }

    public void setPicCount(int picCount) {
        this.picCount = picCount;
    }

    public Date getRecommendDate() {
        return recommendDate;
    }

    public void setRecommendDate(Date recommendDate) {
        this.recommendDate = recommendDate;
    }

    public int getPicType() {
        return picType;
    }

    public void setPicType(int picType) {
        this.picType = picType;
    }

    public int hashCode() {
        return id;
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj instanceof GroupPicInfo) {
            GroupPicInfo other = (GroupPicInfo) obj;
            return id==other.getId();
        }
        return false;
    }

    public String toString() {
        return (new StringBuilder("GroupPicInfo [id=")).append(id).append(", title=").append(title).append(", pic=").append(pic).append(", weigth=").append(weigth).append(", picCount=").append(picCount).append(", recommendDate=").append(recommendDate).append("]").toString();
    }

    private int    id;
    private String title;
    private String pic;
    private int    weigth;
    private int    picCount;
    private Date   recommendDate;
    private int    picType;

}
