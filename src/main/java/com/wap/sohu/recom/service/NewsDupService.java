/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service;

import java.util.List;

/**
 * 专门过滤重复新闻的
 * @author hongfengwang 2012-11-8 上午11:40:39
 */
public interface NewsDupService {

    /**
     * @param newsId
     * @param targetList
     */
    void filterDupNews(Integer newsId,List<Integer> targetList);

    /**
     * @param newsId
     * @return
     */
    boolean isLowQualityNews(int newsId);

    /**
     * @param newsId
     * @param targetList
     */
    void filterDupGroupNews(Integer newsId, List<Integer> targetList,int index);

    /**
     * @param newsList
     */
    void removeDupNews(List<Integer> newsList);

    /**
     * @param newsId
     * @return
     */
    boolean checkNewsId(int newsId);

}
