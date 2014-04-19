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

import com.wap.sohu.recom.model.NewsContent;


/**
 * 类NewsCacheService.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2012-9-13 下午04:14:25
 */
public interface NewsCacheService {

    /**
     * @param newsId
     * @return
     */
    Map<Integer,Double> getNewsSimCache(int newsId);

    /**
     * 得到该新闻下面的所有分类
     * @param newsId
     * @return
     */
    Set<String> getCatsCache(int newsId,boolean useThreadLocal);

    /**获得tag 相似度列表
     * @param tagId
     * @return
     */
    Map<Integer, Double> getTopTagsCache(int tagId,int count);
    

    /**
     * 获得重复对象id
     * @param newsId
     * @return
     */
    Integer getDupId(int newsId);


    /**
     * @return
     */
    Set<Integer> getNoPubNews();

    /**
     * @param newsId
     * @return
     */
    String getNewTitleCache(int newsId);

    /**
     * @param newsId
     * @return
     */
    String getNewsContentCache(int newsId);

    /**
     * @return
     */
    Set<Integer> getFilterTypeNews();

    /**
     * @param tag
     * @return
     */
    Map<Integer, Double> getNewsScore(Integer tag,double threadHold);

    /**
     * @param newsId
     * @return
     */
    Set<Integer> getCatIdsCache(int newsId);

    /**
     * @param newsId
     * @return
     */
    Integer getNewsPubId(Integer newsId);

    /**
     * @param pubId
     * @return
     */
    List<Integer> getCertainPubNews(Integer pubId);

    /**
     * @param newsId
     * @return
     */
    NewsContent getNewsContent(int newsId);

    /**
     * @return
     */
    Set<Integer> getDeletedNews();

    /**
     * @param key
     * @param count
     * @return
     */
    Set<Integer> getMChannelHotNews(String key,int start,  int count);

    /**
     * @param newsId
     * @return
     */
    Set<Integer> getCatIdsCacheForOther(int newsId);

    Integer getTopCatId(int catId);

}
