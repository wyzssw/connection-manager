package com.wap.sohu.recom.service;

import java.util.List;
import java.util.Set;

import com.wap.sohu.mobilepaper.model.pic.GroupPicInfo;

/**
 * 类GroupRecomService.java的实现描述：TODO 类实现描述
 * 
 * @author hongfengwang 2012-7-27 下午03:43:12
 */
public interface GroupRecomService {

    /**
     * 得到该组图的相关推荐组图
     * 
     * @param cid
     * @param newsId
     * @param newsId
     * @param gid
     * @param moreCount
     * @return
     */
    public List<Integer> getRecomGroup(long cid, int newsId, int gid, int moreCount, int picType);

    /**
     * @param cid
     * @param picType
     * @return
     */
    Set<Integer> getUserHistorySet(long cid);

    /**
     * @param gid
     * @param picType
     * @param filterSet
     * @param recommondCount
     * @return
     */
    List<GroupPicInfo> getRecommondPic(int gid, int picType, Set<Integer> filterSet, int recommondCount);

    /**
     * @param resultList
     * @return
     */
    List<Integer> convert2IntList(List<GroupPicInfo> resultList);

    /**
     * @param newsId
     * @param filterSet
     * @param moreCount
     * @return
     */
    List<GroupPicInfo> getSplitTagPic(int newsId, Set<Integer> filterSet, int moreCount);

    /**
     * @param leftSize
     * @param picType
     * @param currentList
     * @param gid
     * @return
     */
    List<GroupPicInfo> getGroupFromHotList(int leftSize, int picType, Set<Integer> set);

    /**
     * @param newsId
     * @return
     */
    List<Integer> getSplitGid(int newsId,Set<Integer> set);

}
