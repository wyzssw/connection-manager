/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.wap.sohu.recom.cache.core.ICache;
import com.wap.sohu.recom.cache.manager.TopNewsCacheManager;
import com.wap.sohu.recom.constants.CommonConstants;
import com.wap.sohu.recom.constants.TopNewsRedisKeyConstants;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.dao.NewsCatDao;
import com.wap.sohu.recom.model.NewsContent;
import com.wap.sohu.recom.service.impl.MChannelNewsBloomService;
import com.wap.sohu.recom.service.impl.NewsUserService;
import com.wap.sohu.recom.util.bloom.BloomFilter;
import com.wap.sohu.recom.utils.ConvertUtils;
import com.wap.sohu.recom.utils.DateUtil;
import com.wap.sohu.recom.utils.TopNewsStrategy;

/**
 * 给要闻频道热门新闻的处理计算
 * @author hongfengwang 2013-7-15 下午04:20:25
 */
@Service
public class MChannelNewsService {

    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateRecom;

    @Autowired
    private NewsCacheService newsCacheService;
    @Autowired
    private MChannelNewsBloomService mChannelNewsBloomService;
    @Autowired
    private SubScriptionService subScriptionService;

    @Autowired
    private StringRedisTemplateExt stringRedisTemplateMChannel;

    @Autowired
    private NewsUserService newsUserService;

    @Autowired
    private NewsTagService newsTagService;

    @Autowired
    private NewsFilterService newsFilterService;

    @Autowired
    private PropertyProxy propertyProxy;

    @Autowired
    private NewsCatDao newsCatDao;

    private volatile boolean onStat = true;

    /**
     * 对应每个频道的开关
     */
    private volatile Map<Integer, Boolean> eachStatMap = new ConcurrentHashMap<Integer, Boolean>();

    private static final Long THREADHOLD_WEMEDIA=400L;
    private static final Long THREADHOLD_SUB=200L;
    private static final Long THREADHOLD_CHANNEL=800L;

    private static final Long THREADHOLD_FINANCE=0L;

    private static final Long THREADHOLD_IT  = 100L;

    private static final Long HTREADHOLD_CAR = 100L;

    private static final int cycleCount = 10;
    private static Set<Integer> subIds_6_11;
    private static Set<Integer> subIds_12_17;
    private static Set<Integer> subIds_18_23;
    private static Set<Integer> filterChannels;
    private static Set<Integer> filterChannelsWhole;
    private static Set<String>  filterChannelsKeywords;

    private static Set<Integer> financeChannelSubscriptions;

    private static Set<Integer> ITChannelSubscriptions;

    private volatile Set<String> sensitiveWords;
    private Set<String> itCatStrs;
    private Set<Integer> carSubIds;

    @SuppressWarnings("unused")
    @PostConstruct
    private void init(){
        String tmpsubIds6_11 = propertyProxy.getProperty("main_channel_news_recom_6-11");
        subIds_6_11   = new HashSet<Integer>(ConvertUtils.convertStringArray2IntegerList(StringUtils.split(tmpsubIds6_11, ",")));
        String tmpsubIds12_17 = propertyProxy.getProperty("main_channel_news_recom_12-17");
        subIds_12_17   =  new HashSet<Integer>(ConvertUtils.convertStringArray2IntegerList(StringUtils.split(tmpsubIds12_17, ",")));
        String tmpsubIds18_23 = propertyProxy.getProperty("main_channel_news_recom_18-23");
        subIds_18_23   =  new HashSet<Integer>(ConvertUtils.convertStringArray2IntegerList(StringUtils.split(tmpsubIds18_23, ",")));
        String filterChannelsStr = propertyProxy.getProperty("main_channel_filter_channels");
        filterChannels = new HashSet<Integer>(ConvertUtils.convertStringArray2IntegerList(StringUtils.split(filterChannelsStr, ",")));
        String wholeFilterChannelStr = propertyProxy.getProperty("main_channel_whole_filter_channels");
        filterChannelsWhole = new HashSet<Integer>(ConvertUtils.convertStringArray2IntegerList(StringUtils.split(wholeFilterChannelStr, ",")));
        String filterChannelsKeywordsStr =  propertyProxy.getProperty("main_channel_filter_keywords");
        filterChannelsKeywords = new HashSet<String>(Arrays.asList(StringUtils.split(filterChannelsKeywordsStr, ",")));

        String financeChannelSubscriptionsStr = propertyProxy.getProperty("finance_channel_subscriptions");
        financeChannelSubscriptions = new HashSet<Integer>(ConvertUtils.convertStringArray2IntegerList(StringUtils.split(financeChannelSubscriptionsStr,",")));

        String ITChannelSubscriptionsStr = propertyProxy.getProperty("IT_channel_subscriptions");
        ITChannelSubscriptions = new HashSet<Integer>(ConvertUtils.convertStringArray2IntegerList(StringUtils.split(ITChannelSubscriptionsStr,",")));

        sensitiveWords = newsCatDao.getSenitiveWords();
        String catIdStr = propertyProxy.getProperty("IT_catNames");
        itCatStrs = new HashSet<String>(Arrays.asList(StringUtils.split(catIdStr,",")));

        String catSubIdStr = propertyProxy.getProperty("car_channel_subIds");
        carSubIds = new HashSet<Integer>(ConvertUtils.convertStringArray2IntegerList(StringUtils.split(catSubIdStr,",")));
    }
    /**
     * @param newsId
     */
    public void updateForMainChannel(int newsId,long count) {
       NewsContent newsContent = newsCacheService.getNewsContent(newsId);
       if (filterMainChannel(count, newsContent)) {
            return ;
       }
       updateChannelNews(newsContent,count);
       updateWemediaNews(newsContent,count);
       updateSubNews(newsContent,count);

       /**
        *
        */
       updateFinanceChannel(newsContent,count);

//       updateITChannel(newsContent,count);

       updateCarChannel(newsContent,count);
    }
    /**
     * @param newsContent
     * @param count
     */
    private void updateCarChannel(NewsContent newsContent, long count) {
        Integer newsId = newsContent.getId();
        if (newsContent.getNTime()==null||DateUtil.getUnixTime(-12, TimeUnit.HOURS)>newsContent.getNTime().getTime()/1000) {
            return;
        }
        //process duplication news
        List<Integer> subIds = newsContent.getSubIds();
        if(subIds==null||subIds.isEmpty()|| Collections.disjoint(subIds, carSubIds)){
            return;
        }
        newsId = processDup(newsId,CommonConstants.CAR_CHANNEL_UPDATE_DAYS * 24);
        if (newsId==0) {
            return ;
        }
        //filter sensitive words:
        if(hasSensitiveWords(newsContent)){
            return;
        }
       // store newsid in CHANNEL_SEPARATE_ZSET
        long time = System.currentTimeMillis()/1000;
        String key = String.format(CommonConstants.CHANNEL_SEPARATE_ZSET, CommonConstants.CAR_CHANNEL_ID);
        if(count>=HTREADHOLD_CAR){
            Long rank = shardedRedisTemplateRecom.opsForZSet().rank(key, String.valueOf(newsId));
            if(rank==null){
                shardedRedisTemplateRecom.opsForZSet().add(key, String.valueOf(newsId),Double.valueOf(time));
            }
        }
    }
    /**
     * @param count
     * @param newsContent
     */
    private boolean filterMainChannel(long count, NewsContent newsContent) {
       if (newsContent==null||count==0L) {
            return true;
       }
       List<Integer> channels = newsContent.getChannelIds();
       String title = newsContent.getTitle();
       if (channels!=null&&!Collections.disjoint(channels, filterChannelsWhole)||StringUtils.isBlank(title)) {
            return true;
       }
       for (String key : filterChannelsKeywords) {
         if (title.contains(key)) {
            return true;
         }
       }
       return false;
    }

    /**
     * @param newsId
     */
    private void updateSubNews(NewsContent newsContent,long count) {
        List<Integer> list = newsContent.getSubIds();
        Integer newsId = newsContent.getId();
        if (list == null || list.isEmpty() || newsContent.getNTime() == null||count<THREADHOLD_SUB
            || DateUtil.getUnixTime(-CommonConstants.SUBID_UPDATE_HOURS, TimeUnit.HOURS) > newsContent.getNTime().getTime() / 1000) {
            return;
        }
        String key6_11  = TopNewsRedisKeyConstants.SUB_NEWS_HOT_6_11;
        String key12_17 = TopNewsRedisKeyConstants.SUB_NEWS_HOT_12_17;
        String key18_23 = TopNewsRedisKeyConstants.SUB_NEWS_HOT_18_23;
        long time = System.currentTimeMillis()/1000;
        newsId = processDup(newsId,CommonConstants.SUBID_UPDATE_HOURS);
        if (newsId==0) {
            return ;
        }
        if (!Collections.disjoint(newsContent.getSubIds(), subIds_6_11)) {
            Long rank = shardedRedisTemplateRecom.opsForZSet().rank(key6_11, String.valueOf(newsId));
            if (rank==null) {
                shardedRedisTemplateRecom.opsForZSet().add(key6_11, String.valueOf(newsId),Double.valueOf(time));
            }
        }
        if (!Collections.disjoint(newsContent.getSubIds(), subIds_12_17)) {
            Long rank = shardedRedisTemplateRecom.opsForZSet().rank(key12_17, String.valueOf(newsId));
            if (rank==null) {
                shardedRedisTemplateRecom.opsForZSet().add(key12_17, String.valueOf(newsId),Double.valueOf(time));
            }
        }
        if (!Collections.disjoint(newsContent.getSubIds(), subIds_18_23)) {
            Long rank = shardedRedisTemplateRecom.opsForZSet().rank(key18_23, String.valueOf(newsId));
            if (rank==null) {
                shardedRedisTemplateRecom.opsForZSet().add(key18_23, String.valueOf(newsId),Double.valueOf(time));
            }
        }
    }

    /**
     * @param newsId
     */
    private void updateWemediaNews(NewsContent newsContent,long count) {
        List<Integer> list = newsContent.getSubIds();
        Integer newsId = newsContent.getId();
        Set<Integer> set = subScriptionService.getWeMediaSubIds();
        if (list == null || list.isEmpty() || set == null || set.isEmpty()
            || Collections.disjoint(set, list)
            || newsContent.getNTime()==null
            ||DateUtil.getUnixTime(-CommonConstants.WEMEDIA_UPDATE_DAYS, TimeUnit.DAYS) - newsContent.getNTime().getTime() / 1000>0) {
              return;
        }
        long time = 0L,currentTime=System.currentTimeMillis()/1000;
        if (DateUtil.getUnixTime(-1, TimeUnit.DAYS) - newsContent.getNTime().getTime() / 1000>0) {
            time =  DateUtil.getUnixTime(-5, TimeUnit.HOURS);
        }else {
            time = currentTime;
        }
        newsId = processDup(newsId,CommonConstants.WEMEDIA_UPDATE_DAYS);
        if (newsId==0) {
            return ;
        }
        String key = TopNewsRedisKeyConstants.WEMEDIA_NEWS_HOT;
        if (count>THREADHOLD_WEMEDIA) {
            Long rank = shardedRedisTemplateRecom.opsForZSet().rank(key, String.valueOf(newsId));
            if (rank==null) {
                shardedRedisTemplateRecom.opsForZSet().add(key, String.valueOf(newsId),Double.valueOf(time));
            }
        }
    }

    /**
     * @param newsId
     * @return
     */
    private Integer processDup(Integer newsId,int hours) {
        NewsContent newsContent;
        Integer dupId =   newsCacheService.getDupId(newsId);
        newsId = (dupId==null||dupId==0)?newsId:dupId;
        if (dupId!=null&&dupId!=0) {
            newsContent = newsCacheService.getNewsContent(newsId);
            if (newsContent==null) {
                return 0;
            }
            if (DateUtil.getUnixTime(-hours, TimeUnit.HOURS) > newsContent.getNTime().getTime() / 1000) {
                return 0;
            }
        }
        return newsId;
    }

    /**
     * @param newsId
     */
    private void updateChannelNews(NewsContent newsContent,long count) {
        List<Integer> list = newsContent.getChannelIds();
        Integer newsId = newsContent.getId();
        if (list==null||list.isEmpty()||newsContent.getNTime()==null||DateUtil.getUnixTime(-CommonConstants.YAOWENCHANNEL_UPDATE_DAYS, TimeUnit.DAYS)>newsContent.getNTime().getTime()/1000) {
              return;
        }
        List<Integer> channels = newsContent.getChannelIds();
        if (channels==null||!Collections.disjoint(channels, filterChannels)) {
            return;
        }
        long time = System.currentTimeMillis()/1000;
        String key = TopNewsRedisKeyConstants.CHANNEL_NEWS_HOT;
        newsId = processDup(newsId,CommonConstants.MCHANNEL_UPDATE_DAYS * 24);
        if (newsId==0) {
            return ;
        }
        if (count>THREADHOLD_CHANNEL) {
            Long rank = shardedRedisTemplateRecom.opsForZSet().rank(key, String.valueOf(newsContent.getId()));
            if (rank==null) {
                shardedRedisTemplateRecom.opsForZSet().add(key, String.valueOf(newsId),Double.valueOf(time));
            }
        }
    }

    /**
     * finance news
     * @param newsContent
     * @param count
     */
    private void updateFinanceChannel(NewsContent newsContent, long count){
        Integer newsId = newsContent.getId();
        if (newsContent.getNTime()==null||DateUtil.getUnixTime(-12, TimeUnit.HOURS)>newsContent.getNTime().getTime()/1000) {
            return;
        }


        /**
         * process duplication news
         */


        List<Integer> subIds = newsContent.getSubIds();
        if(subIds==null||subIds.isEmpty()|| Collections.disjoint(subIds, financeChannelSubscriptions)){
            return;
        }

        newsId = processDup(newsId,CommonConstants.FINANCE_CHANNEL_UPDATE_DAYS * 24);
        if (newsId==0) {
            return ;
        }

        /**
         * filter sensitive words:
         */
        if(hasSensitiveWords(newsContent)){
            return;
        }


        /**
         * store newsid in CHANNEL_SEPARATE_ZSET
         */
        long time = System.currentTimeMillis()/1000;
        String key = String.format(CommonConstants.CHANNEL_SEPARATE_ZSET, CommonConstants.FINANCE_CHANNEL_ID);
        if(count>=THREADHOLD_FINANCE){
            Long rank = shardedRedisTemplateRecom.opsForZSet().rank(key, String.valueOf(newsId));
            if(rank==null){
                shardedRedisTemplateRecom.opsForZSet().add(key, String.valueOf(newsId),Double.valueOf(time));
            }
        }
    }



//    private void updateITChannel(NewsContent newsContent, long count){
//        Integer newsId = newsContent.getId();
//        if (newsContent.getNTime()==null||DateUtil.getUnixTime(-12, TimeUnit.HOURS)>newsContent.getNTime().getTime()/1000) {
//            return;
//        }
//
//        Set<String> catSet = newsCacheService.getCatsCache(newsId, false);
//        boolean isContain = false;
//        if (catSet!=null&&!catSet.isEmpty()) {
//            for (String catId : catSet) {
//                 if (itCatStrs.contains(catId)) {
//                     isContain =true;
//                }
//            }
//        }
//        if (!isContain) {
//            return;
//        }
//        /**
//         * process duplication news
//         */
//
//
//        List<Integer> subIds = newsContent.getSubIds();
//        if(subIds==null||subIds.isEmpty()|| Collections.disjoint(subIds, ITChannelSubscriptions)){
//            return;
//        }
//
//        newsId = processDup(newsId,CommonConstants.IT_CHANNEL_UPDATE_DAYS * 24);
//        if (newsId==0) {
//            return ;
//        }
//
//        /**
//         * filter sensitive words:
//         */
//        if(hasSensitiveWords(newsContent)){
//            return;
//        }
//
//
//        /**
//         * store newsid in CHANNEL_SEPARATE_ZSET
//         */
//        long time = System.currentTimeMillis()/1000;
//        String key = String.format(CommonConstants.CHANNEL_SEPARATE_ZSET, CommonConstants.IT_CHANNEL_ID);
//        if(count>=THREADHOLD_IT){
//            Long rank = shardedRedisTemplateRecom.opsForZSet().rank(key, String.valueOf(newsId));
//            if(rank==null){
//                shardedRedisTemplateRecom.opsForZSet().add(key, String.valueOf(newsId),Double.valueOf(time));
//            }
//        }
//    }

    private boolean hasSensitiveWords(NewsContent newsContent){
        String content = newsContent.getContent();
        if (StringUtils.isNotBlank(content)) {
            content = StringUtils.substringBetween(content, "<content>", "</content>");
            // 耗时破方法
            content = StringEscapeUtils.unescapeHtml4(content);
            // 有些转义了两次，so..
            content = StringEscapeUtils.unescapeHtml4(content);
            content = StringUtils.strip(content);
            content = StringUtils.isNotBlank(content) ? ConvertUtils.delHTMLTag(content) : "";
            newsContent.setContent(content);
        }
        String title = newsContent.getTitle();
        boolean isContain = false;
        for (String word : sensitiveWords) {
            if (title.contains(word) || content.contains(word)) {
                isContain = true;
                break;
            }
        }
        return isContain;
    }

    /**
     * @param cid
     * @param filterNews
     * @param filterCats
     * @param resultList
     * @param bMap
     */
    public List<Integer> getChannelNews(long cid, Set<Integer> filterNews, Set<Integer> filterCats,Map<String, BloomFilter> bMap,int count) {
        List<Integer> resultList = new ArrayList<Integer>();
        for (int cycleIndex = 1, resultCount = 0; resultCount < count && cycleIndex <= cycleCount; cycleIndex++) {
            Set<Integer> set = newsCacheService.getMChannelHotNews(CommonConstants.CHANNEL_NEWS_HOT,100*(cycleIndex-1), 100 * cycleIndex);
            if (set==null||set.isEmpty()) {
                break;
            }
            for (Integer newsId : set) {
                if (filterNews.contains(newsId) || mChannelNewsBloomService.checkHasRecom(newsId, bMap)) {
                    continue;
                }
                resultList.add(newsId);
                mChannelNewsBloomService.addUserBloom(cid, newsId, bMap);
                filterNews.add(newsId);
                resultCount++;
                break;
            }
        }
        return resultList;
    }

    /**
     * @param cid
     * @param filterNews
     * @param filterCats
     * @param resultList
     * @param bMap
     */
    public List<Integer> getCommonChannelNews(long cid, Set<Integer> filterNews, Set<Integer> filterCats,Map<String, BloomFilter> bMap,int count) {
        List<Integer> resultList = new ArrayList<Integer>();
        for (int cycleIndex = 1, resultCount = 0,completed=0; resultCount < count && cycleIndex <= cycleCount && completed==0; cycleIndex++) {
            Set<Integer> set = newsCacheService.getMChannelHotNews(CommonConstants.CHANNEL_RECOM_ZSET,100*(cycleIndex-1), 100 * cycleIndex);
            if (set==null||set.isEmpty()) {
                break;
            }
            for (Integer newsId : set) {
                if (filterNews.contains(newsId) || mChannelNewsBloomService.checkHasRecom(newsId, bMap)) {
                    continue;
                }
                resultList.add(newsId);
                mChannelNewsBloomService.addUserBloom(cid, newsId, bMap);
                filterNews.add(newsId);
                resultCount++;
                if (resultCount>=count) {
                    completed = 1;
                    break;
                }
            }
        }
        return resultList;
    }


    /**
     * @param cid
     * @param filterNews
     * @param filterCats
     * @param resultList
     * @param bMap
     */
    public List<Integer> getWemediaNews(long cid, Set<Integer> filterNews, Set<Integer> filterCats,Map<String, BloomFilter> bMap,int count) {
        List<Integer> resultList = new ArrayList<Integer>();
        for (int cycleIndex = 1, resultCount = 0; resultCount < count && cycleIndex <= cycleCount; cycleIndex++) {
            Set<Integer> set = newsCacheService.getMChannelHotNews(CommonConstants.WEMEDIA_NEWS_HOT,100*(cycleIndex-1), 100 * cycleIndex);
            if (set==null||set.isEmpty()) {
                break;
            }
            for (Integer newsId : set) {
                if (filterNews.contains(newsId) || mChannelNewsBloomService.checkHasRecom(newsId, bMap)) {
                    continue;
                }
                resultList.add(newsId);
                mChannelNewsBloomService.addUserBloom(cid, newsId, bMap);
                filterNews.add(newsId);
                resultCount++;
                break;
            }
        }
        return resultList;
    }
    /**
     * @param cid
     * @param filterNews
     * @param filterCats
     * @param resultList
     * @param bMap
     */
    public List<Integer> getSubNews(long cid, Set<Integer> filterNews, Set<Integer> filterCats,   Map<String, BloomFilter> bMap,int count) {
        List<Integer> resultList = new ArrayList<Integer>();
        int curHours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String key = "";
        if (curHours <= 11 && curHours >= 6) {
            key = TopNewsRedisKeyConstants.SUB_NEWS_HOT_6_11;
        }else if ( curHours >= 12 && curHours <= 17) {
            key = TopNewsRedisKeyConstants.SUB_NEWS_HOT_12_17;
        }else if(curHours>=18 && curHours<=23){
            key = TopNewsRedisKeyConstants.SUB_NEWS_HOT_18_23;
        }
        if (StringUtils.isBlank(key)) {
            return resultList;
        }
        for (int cycleIndex = 1, resultCount = 0; resultCount < count && cycleIndex <= cycleCount; cycleIndex++) {
            Set<Integer> set = newsCacheService.getMChannelHotNews(key,100*(cycleIndex-1), 100 * cycleIndex);
            if (set==null||set.isEmpty()) {
                break;
            }
            for (Integer newsId : set) {
                if (filterNews.contains(newsId) || mChannelNewsBloomService.checkHasRecom(newsId, bMap)) {
                    continue;
                }
                resultList.add(newsId);
                mChannelNewsBloomService.addUserBloom(cid, newsId, bMap);
                filterNews.add(newsId);
//                resultCount++;
//                break;
                if (++resultCount>=count) {
                    break;
                }
            }
        }
        return resultList;

    }


    public Set<Integer> getMChannelNews(){
        ICache<String, Set<Integer>> iCache = TopNewsCacheManager.getMainChannelEditedNews();
        Set<Integer> set = null;
        if ((set=iCache.get(CommonConstants.EDITED_MCHANNEL_NEWS))==null) {
             Set<String> setTmp = shardedRedisTemplateRecom.opsForZSet().range(TopNewsRedisKeyConstants.EDITED_MCHANNEL_NEWS, 0, -1);
             set = ConvertUtils.convert2intList(setTmp);
             iCache.put(CommonConstants.EDITED_MCHANNEL_NEWS,set);
        }
        return set;
    }
    /**
     * @param cid
     * @param filterNews
     * @param filterCats
     * @param bMap
     * @param i
     * @return
     */
    public List<Integer> getShortLikeNews(long cid, Set<Integer> filterNews, Set<Integer> filterCats,
                                          Map<String, BloomFilter> bMap, int count) {
        List<Integer> resultList = new ArrayList<Integer>();
        List<String> shortLikeCat = newsUserService.queryShortCat(cid,0L);
        if (shortLikeCat == null || shortLikeCat.isEmpty()) {
            return resultList;
        }
        int index = TopNewsStrategy.queryShortCatIndex(shortLikeCat.size());
        String catId = shortLikeCat.get(index);
        List<Integer> catNewsCache = newsTagService.queryCatNews(Integer.parseInt(catId));
        int recCount=0;
        for (Integer newsId : catNewsCache) {
            if (filterNews.contains(newsId)|| newsFilterService.checkFilter(newsId)
                || mChannelNewsBloomService.checkHasRecom(newsId, bMap)) {
                continue;
            }
            filterNews.add(newsId);
            resultList.add(newsId);
            mChannelNewsBloomService.addUserBloom(cid, newsId, bMap);
            if (++recCount>=count) {
                break;
            }
        }
       return resultList;
    }


    /**
     * @param key
     * @return
     */
    public boolean check(String key) {
        String datePrefix = DateFormatUtils.format(new Date(), "yyyyMMdd");
        String md5 = DigestUtils.md5Hex(datePrefix+"sohunews");
        if (md5.equals(key)) {
            return true;
        }
        return false;
    }


    /**
     * @param open
     */
    public void setOnOff(String open) {
        if (open.equals("on")) {
            stringRedisTemplateMChannel.opsForValue().set(TopNewsRedisKeyConstants.MCHANNEL_NEWS_RECOM_STAT, "on");
            setOnStat(true);
        }else if (open.equals("off")) {
            stringRedisTemplateMChannel.opsForValue().set(TopNewsRedisKeyConstants.MCHANNEL_NEWS_RECOM_STAT, "off");
            setOnStat(false);
        }
    }

    public boolean getOnStat(){
        return onStat;
    }

    public void   setOnStat(boolean value){
        onStat = value;
    }


    public void copyEachStat(Map<Integer, Boolean> map ){
        eachStatMap = map;
    }

    public void setEachStat(Integer channelId,String value){
        if (value.equals("on")) {
            stringRedisTemplateMChannel.opsForHash().put(TopNewsRedisKeyConstants.EACH_CHANNEL_RECOM_STAT_HASH, channelId+"", value);
            eachStatMap.put(channelId, true);
        }else if (value.equals("off")) {
            stringRedisTemplateMChannel.opsForHash().put(TopNewsRedisKeyConstants.EACH_CHANNEL_RECOM_STAT_HASH, channelId+"", value);
            eachStatMap.put(channelId, false);
        }
    }

    public boolean getEachStat(int channelId){
        Boolean statBoolean = eachStatMap.get(channelId);
        if (statBoolean==null) {
            return true;
        }
        return statBoolean;
    }

    /**
     * @param catIdList
     * @param cid
     * @param pid
     */
    @Async
    public void setUserNotLikeCat(List<Integer> catIdList, long cid, long pid) {
        String key = String.format(TopNewsRedisKeyConstants.MCHANNEL_USER_NOT_LIKE_HASH,cid);
        if (catIdList==null||catIdList.isEmpty()) {
            return;
        }
        for (Integer catId : catIdList) {
            stringRedisTemplateMChannel.opsForZSet().incrementScore(key, String.valueOf(catId), 1.0);
        }
        Date date = DateUtil.getToday();
        Date expireDate = DateUtils.addHours(date, 24);
        stringRedisTemplateMChannel.expireAt(key,expireDate);
    }


    public Set<Integer> getUserNotLikeCat(long cid,long pid){
       String key = String.format(TopNewsRedisKeyConstants.MCHANNEL_USER_NOT_LIKE_HASH,cid);
       Set<String> notLikeCatSet =  stringRedisTemplateMChannel.opsForZSet().rangeByScore(key, 3.0D,1000D);
       if(notLikeCatSet==null){
           return null;
       }
      return  ConvertUtils.convert2intList(notLikeCatSet);
    }

    public void removeNotLike(long cid, int newsId,int channelId, String type) {
      if (channelId!=1||StringUtils.isBlank(type)) {
            return;
      }
      Set<Integer> catIdSet =   newsCacheService.getCatIdsCacheForOther(newsId);
      String key = String.format(TopNewsRedisKeyConstants.MCHANNEL_USER_NOT_LIKE_HASH,cid);
      if (catIdSet!=null&&!catIdSet.isEmpty()) {
          for (Integer catId : catIdSet) {
              stringRedisTemplateMChannel.opsForZSet().remove(key,String.valueOf(catId));
        }
      }
    }
}
