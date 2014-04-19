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
 * 通过newsId查tag，传递tag到列内聚模型的服务
 * @author hongfengwang 2012-9-4 下午03:15:16
 */
public interface NewsTagService {

   

    /**
     * 获得不在分类的新闻列表
     * @param newsId
     * @param set
     * @param size
     * @param count
     * @return
     */
    List<Integer> getNotInCatIds(int newsId, List<Integer> newsList, int count);


    /**
     * 将CMS过来的删除新闻放到redis中
     * @param list
     */
    void setDelNewsToRedis(List<Integer> list);

    /**
     * 得到删除新闻列表
     * @return
     */
    Set<Integer> getDelNews();

    /**
     * @param newsList
     * @return
     */
    Map<Integer, Long> getNewsTimeMap(List<Integer> newsList);


    /**
     * @param newsId
     * @return 新闻创建时间，单位秒
     */
    Long getNewsTime(Integer newsId);


    /**
     * @param newsId
     * @param dupId
     * @return
     */
    boolean hasCommonCat(int newsId, Integer dupId);


    /**
     * @param catId
     * @return
     */
    List<Integer> queryCatNews(Integer catId);

    /**
     * 返回精编分类新闻
     * @param catId
     * @return
     */
    List<Integer> querySelectedCatNews(Integer catId);
  

}
