/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.stereotype.Service;

import com.wap.sohu.recom.model.NewsContent;
import com.wap.sohu.recom.utils.ConvertUtils;

/**
 * 类TaoNewsService.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2013-8-7 下午12:10:50
 */
@Service
public class NewsFilterService {
    
    @Autowired
    private PropertyProxy propertyProxy;
    
    @Autowired
    private NewsCacheService newsCacheService;
    
    @Autowired
    private NewsTagService newsTagService;
    
    /**带有广告、汽车、小说前缀的正则 */ 
    private static Set<Pattern> filterWordsPatternSet =  new HashSet<Pattern>();
    /** 要过滤的产品id   */
    private static Set<Integer> filterProductIdsSet  = new HashSet<Integer>();
    /** 要过滤的pubId 如糗百、汽车 */
    private static Set<Integer> filterPubIdsSet       = new HashSet<Integer>();
    /** 要过滤的fetchRuleId 如汽车的*/
    private static Set<Integer> filterFetchRuleIdsSet =  new HashSet<Integer>();
    /** 要过滤的channel id  */
    private static Set<Integer> filterChannelIdsSet   =  new HashSet<Integer>();
    
    @PostConstruct
    private void getFilter(){
        String[]    filterProductIds =  propertyProxy.getProperty("cat_news_filter_productids").split(",");
        String[]    filterPubIds =  propertyProxy.getProperty("cat_news_filter_pubids").split(",");
        String[]    filterFetchRuleIds =  propertyProxy.getProperty("cat_news_filter_fetchRuleIds").split(",");
        String[]    filterChannelIds =  propertyProxy.getProperty("cat_news_filter_channelIds").split(",");
        filterProductIdsSet.addAll(ConvertUtils.convertStringArray2IntegerList(filterProductIds));
        filterPubIdsSet.addAll(ConvertUtils.convertStringArray2IntegerList(filterPubIds));
        filterFetchRuleIdsSet.addAll(ConvertUtils.convertStringArray2IntegerList(filterFetchRuleIds));
        filterChannelIdsSet.addAll(ConvertUtils.convertStringArray2IntegerList(filterChannelIds));
        String[]    filterWords =   propertyProxy.getProperty("cat_news_filter_words").split(",");
        for (String string : filterWords) {
            filterWordsPatternSet.add(Pattern.compile(string));
        }
    }

    /**
     * 过滤一般新闻，返回true过滤掉
     * @param newsId
     */
    public boolean filterCommonNews(int newsId,boolean isgroupnews) {
        NewsContent newsContent = newsCacheService.getNewsContent(newsId);
        if (newsContent==null) {
            return true;
        }
        String title = newsCacheService.getNewTitleCache(newsId);
        String content = newsCacheService.getNewsContentCache(newsId);
        if (StringUtils.isBlank(title)||title.length()<9) {
            return true;
        }
        if (!isgroupnews&&(StringUtils.isBlank(content)||content.length()<10)) {
            return true;
        }
        for (Pattern pattern : filterWordsPatternSet) {
            if (pattern.matcher(title).find()&&!title.contains("(图)")&&!title.contains("(组图)")&&!title.contains("（图）")&&!title.contains("（组图）")){
                return true;
            }
        }
        Integer productId = newsContent.getProductId()==null?-1:newsContent.getProductId();
        Integer pubId = newsContent.getPubId()==null?-1:newsContent.getPubId();
        Integer fetchRuleId = newsContent.getFetchRuleId()==null?-1:newsContent.getFetchRuleId();
        if (filterProductIdsSet.contains(productId)||filterPubIdsSet.contains(pubId)||filterFetchRuleIdsSet.contains(fetchRuleId)) {
            return true;
        }
        return false;
    }

    /**
     * 过滤频道新闻
     * @param newsId
     * @return
     */
    public boolean filterChannelNews(Integer newsId){
        NewsContent newsContent = newsCacheService.getNewsContent(newsId);
        if (newsContent==null) {
            return true;
        }
        List<Integer> channels  = newsContent.getChannelIds()==null?new ArrayList<Integer>():newsContent.getChannelIds();
        if (!Collections.disjoint(channels, filterChannelIdsSet)) {
            return true;
        }
        return false;
    }
    
    /**
     * 过滤分类新闻
     * @param newsId
     * @param isgroupnews
     * @return
     */
    public boolean filterCatNews(int newsId,boolean isgroupnews){
        NewsContent newsContent = newsCacheService.getNewsContent(newsId);
        if (newsContent==null) {
            return true;
        }
        Integer pubId = newsContent.getPubId()==null?-1:newsContent.getPubId();
        List<Integer> channels  = newsContent.getChannelIds()==null?new ArrayList<Integer>():newsContent.getChannelIds();
        if (filterPubIdsSet.contains(pubId)||!Collections.disjoint(channels, filterChannelIdsSet)) {
            return true;
        }
        String title = newsCacheService.getNewTitleCache(newsId);
        String content = newsCacheService.getNewsContentCache(newsId);
        if (StringUtils.isBlank(title)||title.length()<9) {
            return true;
        }
        if (!isgroupnews&&(StringUtils.isBlank(content)||content.length()<10)) {
            return true;
        }
        return false;
    }
    
    /**
     * 
     * @param newsId
     * @param expireTime 单位秒
     * @return
     */
    public boolean filterCatNewsByTime(int newsId,long timeout,TimeUnit timeUnit){
        Long time = newsTagService.getNewsTime(newsId);
        //超过1天前的就不更新了
        if (System.currentTimeMillis()/1000-time> TimeoutUtils.toSeconds(timeout, timeUnit)) {
            return true; 
        }
        return false;
    }
    
    /**
     * @param newsId
     * @return
     */
    public boolean checkFilter(Integer newsId) {
        Long time = newsTagService.getNewsTime(newsId);
        long dayBefore = System.currentTimeMillis() / 1000 - DateUtils.MILLIS_PER_DAY / 1000 * 1;
        if (time < dayBefore) {
            String title = newsCacheService.getNewTitleCache(newsId);
            if (StringUtils.isBlank(title) || title.contains("先知道") || title.contains("神吐槽")) {
                return true;
            }
        }
        return false;

    }
    
    
    public boolean checkShenTuCao(Integer newsId) {
      String title = newsCacheService.getNewTitleCache(newsId);
      if (StringUtils.isBlank(title) || title.contains("先知道") || title.contains("神吐槽")) {
           return true;
      }
      if (title.length()<10||title.length()>25) {
          return true;
      }
      return false;

    }
    
    
    public boolean checkCatFilter(Set<Integer> catFilter,int newsId,boolean addCatSet){
        Set<Integer> catSet = newsCacheService.getCatIdsCacheForOther(newsId);
        if (catSet != null && !catSet.isEmpty()) {
            for (Integer newsCat : catSet) {
                if (catFilter.contains(newsCat)) {
                    return true;
                }
            }
        }
        if (addCatSet) {
            catFilter.addAll(catSet);
        }        
        return false;
    }
    
    
}
