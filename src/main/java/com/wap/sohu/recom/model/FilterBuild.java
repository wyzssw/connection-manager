/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.model;

import java.util.Map;
import java.util.Set;

import com.wap.sohu.recom.service.AbstractBloomService;
import com.wap.sohu.recom.util.bloom.BloomFilter;

/**
 * 类FilterBuild.java的实现描述：TODO 类实现描述
 *
 * @author hongfengwang 2013-8-8 下午04:16:27
 */
public class FilterBuild {

    private Set<Integer>             filterNews;
    private Set<Integer>             filterCats;
    private Map<String, BloomFilter> bMap;
    private AbstractBloomService     bloomService;

    /**
     *
     */
    public FilterBuild(Set<Integer> filterNews, Set<Integer> filterCats, Map<String, BloomFilter> bMap,
                       AbstractBloomService bloomService){
        this.filterCats = filterCats;
        this.filterNews = filterNews;
        this.bMap = bMap;
        this.bloomService = bloomService;
    }

    /**
     * check newsid existence
     *
     * @param newsId
     * @return
     */
    public boolean checkNewsInvalid(Integer newsId) {
        if (filterNews.contains(newsId) || bloomService.checkHasRecom(newsId, bMap)||bloomService.checkNewsTime(newsId)) {
            return true;
        }
        return false;
    }

    /**
     * add newsid to filter and bloom filter
     * @param cid
     * @param newsId
     */
    public void addNews(long cid, Integer newsId) {
        filterNews.add(newsId);
        bloomService.addUserBloom(cid, newsId, bMap);
    }


    /**
     * check category id exist in category set
     * @param catId
     * @return
     */
    public boolean checkCatExist(Integer catId){
        return filterCats.contains(catId);
    }
    /**
     * add catid set to filter category set
     * @param catSet
     */
    public void addCatSet(Set<Integer> catSet){
        filterCats.addAll(catSet);
    }

    /**
     * add catid to filter category set
     * @param catId
     */
    public void addCat(Integer catId){
        filterCats.add(catId);
    }

    /**
     * @return the filterNews
     */
    public Set<Integer> getFilterNews() {
        return filterNews;
    }

    /**
     * @return the filterCats
     */
    public Set<Integer> getFilterCats() {
        return filterCats;
    }

    /**
     * @return the bMap
     */
    public Map<String, BloomFilter> getbMap() {
        return bMap;
    }

}
