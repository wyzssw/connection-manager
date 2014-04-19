package com.wap.sohu.recom.service;

import java.util.List;

import com.wap.sohu.mobilepaper.model.pic.GroupPicInfo;

/**
 * 类GroupUserService.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2012-7-27 下午03:43:36
 */
public interface GroupUserService {
    
    /**
     * 将用户所浏览过的组图历史放到redis里面
     */
    public void addHistory(long cid, int picType, int gid);
    
    /**
     * 将用户不喜欢的组图放入redis中
     * @param cid
     * @param picType
     * @param gidList
     */
    public void addUnlike(long cid, int picType, List<GroupPicInfo> gidList);


}
