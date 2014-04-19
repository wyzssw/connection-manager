/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wap.sohu.recom.model.NewsContent;
import com.wap.sohu.recom.service.NewsCacheService;
import com.wap.sohu.recom.service.NewsDupService;
import com.wap.sohu.recom.service.PropertyProxy;
import com.wap.sohu.recom.utils.ConvertUtils;
import com.wap.sohu.recom.utils.StringExtUtils;


/**
 * 过滤新闻重复的服务类
 * @author hongfengwang 2012-11-8 上午11:41:16
 */
@Service("newsDupService")
public class NewsDupServiceImpl implements NewsDupService {
    
    @Autowired
    private NewsCacheService newsCacheService;
    
    @Autowired
    private PropertyProxy propertyProxy;
    
    private Set<Integer> filterProductIds = new HashSet<Integer>();
    
    @PostConstruct
    private void init(){
        String productIds = propertyProxy.getProperty("news_filter_news_productIds");
        String[] pids =  StringUtils.split(productIds, ",");
        filterProductIds.addAll(ConvertUtils.convertStringArray2IntegerList(pids));
    }
    
    /**
     * 从缓存中筛选，有重复返回1，非重复返回0
     */
      @Override
      public void filterDupNews(Integer newsId,List<Integer> targetList) {
          if (targetList.size()==0) {
            return ;
          }         
          String title   = newsCacheService.getNewTitleCache(newsId);
          String content = newsCacheService.getNewsContentCache(newsId);
          content = StringUtils.strip(content);
          if (StringUtils.isBlank(title)||StringUtils.isBlank(content)) {
               targetList.clear();
               return;  
          }
          Integer length = content.length();
          Set<Integer> rmSet  = new HashSet<Integer>();
          for (Integer id : targetList) {
               if (newsId.equals(id)) {
                  continue;
               }
               String titleItem = newsCacheService.getNewTitleCache(id);
               if (StringUtils.isBlank(titleItem)||titleItem.length()<10) {
                   rmSet.add(id);
                   continue;
               }
               int    interSize = StringExtUtils.getInterSize(title, titleItem, 5);
               if ((double)interSize/title.length()>0.69) {
                  rmSet.add(id);
                  continue;
               }             
              String hisContent = newsCacheService.getNewsContentCache(id);
              hisContent = StringUtils.strip(hisContent);
              if (hisContent.length()<100) {
                rmSet.add(id);
                continue;
              }
              if (StringUtils.isBlank(hisContent) || (hisContent.length() / (double) length) < 0.5
                  || (hisContent.length() / (double) length) > 2
                  ) {
                  continue;
              }
              if (hisContent.length()<50||length<50) {
                  continue;
              }
              if (StringUtils.contains(hisContent, StringUtils.substring(content, 15,25))||
                      StringUtils.contains(hisContent, StringUtils.substring(content, 21,35))||
                      StringUtils.contains(hisContent, StringUtils.substring(content, length-10,length-1))) {
                      rmSet.add(id);
              }
          }
          targetList.removeAll(rmSet);
      }
      
      
      
      
      @Override
      public void filterDupGroupNews(Integer newsId,List<Integer> targetList,int index) {
          if (targetList.size()==0) {
            return ;
          }         
          String title   = newsCacheService.getNewTitleCache(newsId);
          String content = newsCacheService.getNewsContentCache(newsId);
          content = StringUtils.strip(content);
          if (StringUtils.isBlank(title)) {
               targetList.clear();
               return;  
          }
          Integer length = content.length();
          Set<Integer> rmSet  = new HashSet<Integer>();
          List<Integer> tmpList = targetList.subList(index, targetList.size());
          for (Integer id : tmpList) {
               if (newsId.equals(id)) {
                  continue;
               }
               String titleItem = newsCacheService.getNewTitleCache(id);
               if (StringUtils.isBlank(titleItem)) {
                   rmSet.add(id);
                   continue;
               }
               int    interSize = StringExtUtils.getInterSize(title, titleItem, 14);
               if ((double)interSize/title.length()>0.65) {
                  rmSet.add(id);
                  continue;
               }             
              String hisContent = newsCacheService.getNewsContentCache(id);
              hisContent = StringUtils.strip(hisContent);
              if (StringUtils.isBlank(hisContent)||length==0 || (hisContent.length() / (double) length) < 0.5
                  || (hisContent.length() / (double) length) > 2
                  ) {
                  continue;
              }
              if (hisContent.length()<20||length<20) {
                  continue;
              }
              if (StringUtils.contains(hisContent, StringUtils.substring(content, 1,10))||
                  StringUtils.contains(hisContent, StringUtils.substring(content, 6,15))||
                  StringUtils.contains(hisContent, StringUtils.substring(content, length-10,length-2))) {
                  rmSet.add(id);
              }
          }
          targetList.removeAll(rmSet);
      }
      
      
      
     @Override
     public boolean isLowQualityNews(int newsId){
         String titleItem = newsCacheService.getNewTitleCache(newsId);
         if (StringUtils.isBlank(titleItem)||titleItem.length()<10) {
             return true;
         }
         String hisContent = newsCacheService.getNewsContentCache(newsId);
         hisContent = StringUtils.strip(hisContent);
         if (hisContent.length()<100) {
             return true;
         }
         return false;
     }




    @Override
    public void removeDupNews(List<Integer> newsList) {
        Iterator<Integer> iterator = newsList.iterator();
        while (iterator.hasNext()) {
            Integer newsId = (Integer) iterator.next();
            Integer dupId = newsCacheService.getDupId(newsId);
            if (dupId!=null&&dupId>0) {
                iterator.remove();
            }
            
        }
        
    }

    /**
     * @param newsId
     * @return
     */
    @Override
    public boolean checkNewsId(int newsId) {
        NewsContent newsContent = newsCacheService.getNewsContent(newsId);
        if (newsContent==null) {
            return true;
        }
        if (newsContent.getNewsType()!=null&&newsContent.getNewsType()==12) {
            return true;
        }
        if (newsContent.getProductId()!=null && filterProductIds.contains(newsContent.getProductId())) {
            return true;
        }
        return false;
    }

}
