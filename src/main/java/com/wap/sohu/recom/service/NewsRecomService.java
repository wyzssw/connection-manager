/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 类NewsRecomService.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2012-9-4 下午02:42:36
 */
public interface NewsRecomService {

    /**
     * 得到推荐的新闻
     * @param cid
     * @param newsId
     * @param count
     * @param channeId 频道id
     * 
     * @return
     */
    List<Integer> getRecomNews(long cid, int newsId, int count,int channelId);

    /**
     * 异步更新新闻推荐相关缓存 
     */
    void asyncUpdateCache(long cid,int newsId);

    /**
     * @param newsId
     * @param set
     * @return
     */
    List<Integer> getRecomList(int newsId, Set<Integer> set);

    /**
     * @param cid
     * @param newsId
     * @param count
     * @param channelId
     * @return
     */
    List<Integer> getRecomNewsPreview(long cid, int newsId, int count, int channelId,Map<Integer, String> map);

    /**
     * @param cid
     * @param newsId
     * @param list
     */
    void scribeLog(long cid, int newsId, List<Integer> list);

}
