///*
// * Copyright 2012 sohu.com All right reserved. This software is the
// * confidential and proprietary information of sohu.com ("Confidential
// * Information"). You shall not disclose such Confidential Information and shall
// * use it only in accordance with the terms of the license agreement you entered
// * into with sohu.com.
// */
//package com.wap.sohu.recom.cache.job;
//
//import java.util.Date;
//import java.util.HashSet;
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.Set;
//
//import org.apache.commons.lang3.time.DateUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import com.wap.sohu.mobilepaper.model.pic.GroupPic;
//import com.wap.sohu.recom.cache.core.ICache;
//import com.wap.sohu.recom.cache.manager.GroupCacheManager;
//import com.wap.sohu.recom.cache.manager.NewsCacheManager;
//import com.wap.sohu.recom.constants.CommonConstants;
//import com.wap.sohu.recom.dao.GroupPicDao;
//import com.wap.sohu.recom.dao.NewsTagDao;
//import com.wap.sohu.recom.model.NewsContent;
//
///**
// * 用户喜好tag 缓存任务 不更新了
// * @author hongfengwang 2012-11-19 下午03:24:28
// */
//@Service
//public class UserTagLikeCacheJob {
//    
//    @Autowired
//    private NewsTagDao newsTagDao;
//    
//    @Autowired
//    private GroupPicDao groupPicDao;
//    
//    //缓存24小时的新闻
//    @SuppressWarnings("unused")
//    @Scheduled(fixedDelay=3600000)
//    private void loadNews(){
//        ICache<String, Set<Integer>>    iCache = NewsCacheManager.getLastHourNews();
//        Date date = DateUtils.addDays(new Date(), -1);
//        Set<Integer> set = new LinkedHashSet<Integer>();
//        List<NewsContent> list  = newsTagDao.getAllNews(date);
//        if (list==null||list.isEmpty()) {
//            return;
//        }
//        for (NewsContent newsContent : list) {
//             set.add(newsContent.getId());
//        }
//        iCache.put(CommonConstants.NEWS_LAST_CACHE,set);
//    }
//    
//    
//  //缓存24小时的组图
//    @SuppressWarnings("unused")
//    @Scheduled(fixedDelay=3600000)
//    private void loadGroup(){
//        //load 最近30天被删除组图的缓存
//        ICache<String,Set<Integer>> iCache2 = GroupCacheManager.getGroupDelThoseDays();
//        Date date2 = DateUtils.addDays(new Date(), -30);
//        List<Integer> list2= groupPicDao.getDelGroup(date2);
//        Set<Integer> set2 = new HashSet<Integer>(list2);
//        iCache2.put(CommonConstants.GROUP_DEL_THOSE_DAYS, set2);
//        
//        //load最近1天的所有组图缓存
//        ICache<String, Set<Integer>>    iCache = GroupCacheManager.getLastHourGroup();
//        Date date = DateUtils.addDays(new Date(), -1);
//        Set<Integer> set = new LinkedHashSet<Integer>();
//        List<GroupPic> list  = groupPicDao.getGroupByDate(date);
//        if (list==null||list.isEmpty()) {
//            return;
//        }
//        for (GroupPic group : list) {
//             set.add(group.getGid());
//        }
//        iCache.put(CommonConstants.GROUP_LAST_CACHE,set);
//       
//    }
//
//}
