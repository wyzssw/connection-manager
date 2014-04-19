package com.wap.sohu.recom.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.wap.sohu.recom.cache.core.ICache;
import com.wap.sohu.recom.cache.manager.NewsCacheManager;
import com.wap.sohu.recom.constants.CommonConstants;
import com.wap.sohu.recom.log.AccessTraceLogData;
import com.wap.sohu.recom.log.AccessTraceLogData.LogCategoryEnum;
import com.wap.sohu.recom.log.AccessTraceLogData.LogKeyEnum;
import com.wap.sohu.recom.log.StatisticLog;
import com.wap.sohu.recom.service.NewsCacheService;
import com.wap.sohu.recom.service.NewsDupService;
import com.wap.sohu.recom.service.NewsRecomService;
import com.wap.sohu.recom.service.NewsTagService;
import com.wap.sohu.recom.utils.DateThresholdUtils;
import com.wap.sohu.recom.utils.MapSortUtil;

/**
 * 新闻推荐的相关逻辑
 * @author hongfengwang 2012-9-4 下午02:49:25
 */
@Service("newsRecomService")
public class NewsRecomServiceImpl implements NewsRecomService {
    private static final Logger   LOGGER = Logger.getLogger(NewsRecomServiceImpl.class);
//    private static final Integer  MIN_COUNT = 2;
    private static final Integer  MIN_COUNT = 1;

    @Autowired
    private NewsTagService newsTagService;

    @Autowired
    private NewsCacheService newsCacheService;

    @Autowired
    private NewsUserService newsUserService;

    @Autowired
    private NewsDupService newsDupService;

    /**
     * 返回推荐列表
     */
    @Override
    public List<Integer> getRecomNews(long cid, int newsId, int count,int channelId) {
        int originNewsId = newsId;
        List<Integer> newsList = new ArrayList<Integer>();
        if (newsDupService.checkNewsId(newsId)) {
            return newsList;
        }
//        Set<Integer> set      = newsUserService.getNewsHistory(cid);
        Set<Integer> set      = new HashSet<Integer>();
        set.add(newsId);
        Integer      dupId    = newsCacheService.getDupId(newsId);
        if (dupId!=null&&dupId!=0) {
            set.add(dupId);
            //有相同分类，认定两个新闻完全相同
            if (newsTagService.hasCommonCat(newsId,dupId)) {
                newsId = dupId;
            }
        }
        Set<Integer> noPubNews = newsCacheService.getNoPubNews();
        set.addAll(noPubNews);
        //以下语句要在未来去除!!
//        set.addAll(filterTypeNews);
        //第一策略：得到推荐列表新闻
        List<Integer> retList = getRecomList(newsId, set);
        newsList.addAll(retList);
        set.addAll(newsList);
        //如果大于count，那么就去除里面不在同一个分类的新闻
        newsList = newsList.subList(0, newsList.size()>100?100:newsList.size());
        newsDupService.removeDupNews(newsList);
        newsList.removeAll(newsTagService.getNotInCatIds(newsId,newsList,count));
        newsList = newsList.subList(0, newsList.size()>count+10?count+10:newsList.size());
        newsDupService.filterDupNews(newsId,newsList);
        newsList = newsList.subList(0, newsList.size()>count?count:newsList.size());
        if (newsList.size()>=3) {
            newsDupService.filterDupNews(newsList.get(0), newsList);
            if (newsList.size()>=3) {
                newsDupService.filterDupNews(newsList.get(1), newsList);
            }
            if (newsList.size()>=3) {
                newsDupService.filterDupNews(newsList.get(2), newsList);
            }
        }

        //add advertise news
        addAdvertisement(newsId,newsList,count,set);


        if (newsList.size()<MIN_COUNT) {
            LOGGER.info("newsId=="+originNewsId+" recom news is empty and size = "+newsList.size());
            return  new ArrayList<Integer>();
        }
        return newsList;
    }





    /**
     * advertisement : replace the last news
     * @param newsId
     * @param newsList
     * @param set
     */
    private void addAdvertisement(int newsId, List<Integer> newsList, int count, Set<Integer> set){

        //time threshold:
        if(!DateThresholdUtils.isAdvertise()){
            return;
        }

        //news category filter:
        Set<String> catSet = newsCacheService.getCatsCache(newsId,true);
        if (catSet==null||catSet.size()==0) {
            return;
        }

        //IT catId: 10
        Integer ITCatId = 10;
        String IT = "IT";
        if(!catSet.contains(IT)){
            return;
        }

        //recommendation newsId:11273352
        Integer rNewsId = 11273352;
        if(set.contains(rNewsId)){
            return;
        }

        //
        if(newsList.contains(rNewsId)){
            return;
        }

        //replace the last news or add the trail
        if(newsList.size()>=count){
            newsList.set(count-1, rNewsId);
        }else if(newsList.size()<count){
            newsList.add(rNewsId);
        }

    }



    /**
     * 反正推荐列表
     */
    @Override
    public List<Integer> getRecomNewsPreview(long cid, int newsId, int count,int channelId,Map<Integer, String> map) {
        //贯穿始终的推荐列表
        List<Integer> newsList = new ArrayList<Integer>();
        Set<Integer> set   = newsUserService.getNewsHistory(cid);
        set.add(newsId);
        Integer      dupId = newsCacheService.getDupId(newsId);
        if (dupId!=null) {
            set.add(dupId);
        }
        //第一策略：得到推荐列表新闻
        newsList.addAll(getRecomList(newsId, set));

        putMap(map, newsList, "打分推荐");
        set.addAll(newsList);

        //如果大于count，那么就去除里面不在同一个分类的新闻
        newsList = newsList.subList(0, newsList.size()>100?100:newsList.size());
        //第二策略：再不足就从第一个tag开始，取相邻tag，这个出新闻的概率不大
//      newsList.addAll(newsTagService.getRecomNidsBySim(newsId,set,count,newsList));
//
//      putMap(map, newsList, "tag相似推荐");
        //筛选策略：根据分类策略过滤掉非当前新闻所在分类的新闻
        newsList.removeAll(newsTagService.getNotInCatIds(newsId,newsList,count));

//      processCatPreview(newsId, count, map, newsList, set);

        newsList = newsList.subList(0, newsList.size()>count+10?count+10:newsList.size());
        newsDupService.filterDupNews(newsId,newsList);
        newsList = newsList.subList(0, newsList.size()>count?count:newsList.size());
        if (newsList.size()>=3) {
            newsDupService.filterDupNews(newsList.get(0), newsList);
            if (newsList.size()>=3) {
                newsDupService.filterDupNews(newsList.get(1), newsList);
            }
            if (newsList.size()>=3) {
                newsDupService.filterDupNews(newsList.get(2), newsList);
            }
        }
        if (newsList.size()<count) {
            LOGGER.info("newsId=="+newsId+" recom news is empty and size = "+newsList.size());
            return new ArrayList<Integer>();
        }
        return newsList;
    }



    private void putMap(Map<Integer,String> map,List<Integer> newsList,String flag){
        for (Integer integer : newsList) {
             if (!map.containsKey(integer)) {
                 map.put(integer, flag);
            }
        }
    }


    /**
     * 将后台任务计算的相似度列表拿出来
     * @param newsId
     * @param set
     * @return
     */
    @Override
    public List<Integer> getRecomList(int newsId, Set<Integer> set) {
        Map<Integer, Double> map  = newsCacheService.getNewsSimCache(newsId);
        Set<Integer> deletedNews  = newsCacheService.getDeletedNews();
        Map<Integer, Long> timeMapping = newsTagService.getNewsTimeMap(new ArrayList<Integer>(map.keySet()));
        ICache<String, Set<Integer>> iCache = NewsCacheManager.getEditedNewsCache();
        processTimeWeight(map,timeMapping);
        processEditedWeight(map,iCache.get(CommonConstants.EDITED_NEWS_KEY));
        processDeletedNews(map,deletedNews);
        List<Integer> list = MapSortUtil.sortMapByValue(map);
        List<Integer> recomList = new ArrayList<Integer>();
        for (Integer id : list) {
             if (!set.contains(id)) {
                 recomList.add(id);
            }
        }
        return recomList;
    }

    /**
     * @param map
     * @param deletedNews
     */
    private void processDeletedNews(Map<Integer, Double> map, Set<Integer> deletedNews) {
        if (deletedNews==null||map == null) {
            return ;
        }
        map.keySet().removeAll(deletedNews);
    }




    /**
     * 加上时间权重，今天新闻加0.1，昨天新闻0.06，前天新闻加0.03，其余时间不加
     * @param map
     * @param timeMapping
     */
    private void processTimeWeight(Map<Integer, Double> map, Map<Integer, Long> timeMapping) {
        Long nowTime = new Date().getTime()/1000;
        Set<Integer> rmSet = new HashSet<Integer>();
        for (Map.Entry<Integer, Double> item : map.entrySet()) {
             Long itemTime = 0L;
             if ((itemTime=timeMapping.get(item.getKey()))!=null) {
                 Long diffTime = nowTime-itemTime;
                 if (diffTime< TimeUnit.DAYS.toSeconds(1)) {
                    item.setValue(item.getValue()+0.1);
                    continue;
                 }
                 if (diffTime< TimeUnit.DAYS.toSeconds(2)) {
                    item.setValue(item.getValue()+0.06);
                    continue;
                 }
                 if (diffTime< TimeUnit.DAYS.toSeconds(3)) {
                     item.setValue(item.getValue()+0.03);
                     continue;
                 }
                 if(diffTime>TimeUnit.DAYS.toSeconds(CommonConstants.UPDATE_DAYS)){
                    rmSet.add(item.getKey());
                }
            }else {
                rmSet.add(item.getKey());
            }
        }
        map.keySet().removeAll(rmSet);
    }


    /**
     * 如果是期刊新闻或者频道新闻则加0.1
     * @param map
     * @param set
     */
    private void processEditedWeight(Map<Integer, Double> map, Set<Integer> set) {
        if (set==null) {
            return ;
        }
        for (Map.Entry<Integer, Double> item : map.entrySet()) {
            if (set.contains(item.getKey())) {
                item.setValue(item.getValue()+0.15);
            }
        }
    }


    /**
     * 异步更新，用户历史记录
     */
    @Async
    @Override
    public void asyncUpdateCache(long cid,int newsId){
        try {
            newsUserService.setHistory(cid,newsId);
            //TODO 更新本地缓存
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(),e);
        }
    }



    @Override
    public void scribeLog(long cid, int newsId, List<Integer> list) {
        AccessTraceLogData logData = new AccessTraceLogData(LogCategoryEnum.NewsRec);
        logData.add(LogKeyEnum.Cid, cid);
        logData.add(LogKeyEnum.NewsId, newsId);
        String ids = StringUtils.replaceEach(list.toString(), new String[]{"[","]"," "}, new String[]{"","",""});
        logData.add(LogKeyEnum.RecNewsIds,ids);
        logData.add(LogKeyEnum.RecCount, list.size());
//        logData.add(LogKeyEnum.IP, AccessTraceLogData.getLocalHost());
//        logData.add(LogKeyEnum.TimeStamp, System.currentTimeMillis());
        StatisticLog.info(logData);
    }



}
