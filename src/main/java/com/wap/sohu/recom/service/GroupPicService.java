package com.wap.sohu.recom.service;

import java.util.List;

import com.wap.sohu.mobilepaper.model.pic.GroupPicInfo;

/**
 * 类GroupPicService.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2012-7-24 下午03:17:42
 */
public interface GroupPicService {
     /**
     * 通过gid得到gid对应的categoryId
     * 先从缓存中取，缓存中没有从数据库中取   
     * @param gid
     * @return
     */
    public Integer getCategoryIdByGid(int gid);

    /**
     * @param gid
     * @return
     */
    GroupPicInfo getGroupPicInfo(Integer gid);

    /**
     * @param gid
     * @return
     */
    List<Long> getGroupTagSet(int gid);

    /**
     * @param picType
     * @param moreCount
     * @return
     */
    List<GroupPicInfo> listGroupListHotNoRefresh(int picType, int moreCount);

    /**
     * @param size
     * @return
     */
    List<Integer> getRdmPicTypeList(int size);

    /**
     * 将删除的组图放到redis中
     * @param list
     */
    void setDelGroupToRedis(List<Long> list);

    /**
     * @param newsId
     * @return
     */
    Integer getNewsEmbedGid(int newsId);
    
    void filterDupGroup(GroupPicInfo group,List<GroupPicInfo> groupList);
    
}
