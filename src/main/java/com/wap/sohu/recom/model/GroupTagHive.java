package com.wap.sohu.recom.model;

import com.alibaba.fastjson.annotation.JSONField;

public class GroupTagHive {
   
    private int id;
	private int gid;
	private int tagId;
	public void setTagId(int tagId) {
		this.tagId = tagId;
	}
	
	@JSONField(name="tid")
	public int getTagId() {
		return tagId;
	}
	public void setGid(int gid) {
		this.gid = gid;
	}
	@JSONField(name="gid")
	public int getGid() {
		return gid;
	}
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
}
