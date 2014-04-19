///*
// * Copyright 2012 sohu.com All right reserved. This software is the
// * confidential and proprietary information of sohu.com ("Confidential
// * Information"). You shall not disclose such Confidential Information and shall
// * use it only in accordance with the terms of the license agreement you entered
// * into with sohu.com.
// */
//package com.wap.sohu.recom.cache.manager;
//
//import java.util.Map;
//import java.util.Set;
//
//import com.wap.sohu.recom.cache.core.AbstractCacheManager;
//import com.wap.sohu.recom.cache.core.ICache;
//
///**
// * tag-group,news的对应关系
// * @author hongfengwang 2012-11-19 下午03:06:02
// */
//public class TagItemCacheManager {
//   
//    private static final ICache<Integer,Map<Integer,Double>>           TAG_NEWS_MATRIX     =  AbstractCacheManager.getInstance().getCache("user_tag_news_matrix",100000,60*60*24);
//    
//    private static final ICache<Integer,Set<Integer>>           TAG_GROUP_MATRIX     =  AbstractCacheManager.getInstance().getCache("user_tag_group_matrix",100000,60*60*24);
//    
//    public static  final  ICache<Integer,Map<Integer,Double>>           getTagNewsMatrix(){
//        return TAG_NEWS_MATRIX;
//    }
//    
//    public static  final  ICache<Integer,Set<Integer>>           getTagGroupMatrix(){
//        return TAG_GROUP_MATRIX;
//    }
//}
