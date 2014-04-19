package com.wap.sohu.recom.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.wap.sohu.recom.constants.CommonConstants;
import com.wap.sohu.recom.constants.EdbKeyConstants;
import com.wap.sohu.recom.constants.RedisKeyConstants;
import com.wap.sohu.recom.constants.TopNewsRedisKeyConstants;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.core.redis.lock.DistributedLock;
import com.wap.sohu.recom.elephantdb.connector.EdbTemplate;
import com.wap.sohu.recom.log.AccessTraceLogData;
import com.wap.sohu.recom.log.AccessTraceLogData.LogCategoryEnum;
import com.wap.sohu.recom.log.AccessTraceLogData.LogKeyEnum;
import com.wap.sohu.recom.log.StatisticLog;
import com.wap.sohu.recom.model.FilterBuild;
import com.wap.sohu.recom.service.impl.NewsUserService;
import com.wap.sohu.recom.service.impl.TopNewsService;
import com.wap.sohu.recom.util.bloom.BloomFilter;
import com.wap.sohu.recom.utils.ConvertUtils;
import com.wap.sohu.recom.utils.MapSortUtil;
import com.wap.sohu.recom.utils.TopNewsStrategy;

/**
 * 类TopNewsRecomService.java的实现描述：
 *
 * @author yeyanchao Jun 19, 2013 11:49:12 AM
 */
@Service
public class TopNewsRecomService {

    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateRecom;

    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateUser;

    @Autowired
    private NewsCacheService       newsCacheService;

    @Autowired
    private EdbTemplate            edbTemplate;

    @Autowired
    private NewsFilterService      newsFilterService;

    @Autowired
    private NewsUserService        newsUserService;

    @Autowired
    private TopNewsService         topNewsService;

    @Autowired
    private TopNewsBloomService    topNewsBloomService;

    @Autowired
    private DistributedLock        toutiaoLock;

    @Autowired
    private NewsTagService         newsTagService;

    @Autowired
    private WeiboNewsService       weiboNewsService;

    public static final int        WTAG_PARTITION = 1;
    public static final int        SHORT_PARTITION = 1;
    public static final int        LONG_PARTITION  = 1;
    public static final int        SUB_PARTITION   = 1;
    public static final int        HOT_PARTITION   = 1;
    public static final int        TOTAL_NUMBER    = 12;
    public static final long       FOUR_DAYS       = 4 * 24 * 60 * 60 * 1000;

    /**
     * recommend top news for user
     *
     * @param cid
     * @param pid
     * @param ispull
     * @return
     */
    public Map<Integer, String> recomTopNews(long cid, long pid, boolean ispull) {
        Map<Integer, String> map = new HashMap<Integer, String>();
        String lockKey = String.format(TopNewsRedisKeyConstants.TOUTIAO_USER_LOCK, cid);
        Map<String, BloomFilter> bMap = null;
        long expireTime = 0;
        boolean result = false;
        if ((expireTime = toutiaoLock.tryLock(lockKey)) != 0) {
            try {
                bMap = topNewsBloomService.getUserAllBloom(cid);
                map = getRecomData(cid, pid, ispull, bMap);
                result = topNewsBloomService.multiUpdate(cid, new ArrayList<Integer>(map.keySet()), bMap,null);
                map = result ? map : new HashMap<Integer, String>();
                // topNewsService.asyncSetExpire(cid);
            } finally {
                toutiaoLock.unlock(lockKey, expireTime);
                topNewsBloomService.removeDateListLocal();
                topNewsBloomService.removeSimpleDateFormatLocal();
            }
        }
        return map;
    }

    /**
     * @param cid
     * @param pid
     * @param ispull
     * @return
     */
    public Map<Integer, String> getRecomData(long cid, long pid, boolean ispull, Map<String, BloomFilter> bMap) {
        // filter information process
        Set<Integer> filterNews = new HashSet<Integer>();
        Set<Integer> editorSet = topNewsService.getEditedToutiaoNews();
        /** filter : user's click history */
        Set<Integer> clickHistory = newsUserService.getNewsHistory(cid);
        /** filter : editor not publish news */
        Set<Integer> noPubNews = newsCacheService.getNoPubNews();
        filterNews.addAll(editorSet);
        filterNews.addAll(clickHistory);
        filterNews.addAll(noPubNews);
        filterNews.addAll(newsCacheService.getDeletedNews());

        Map<Integer, String> resultMap = new LinkedHashMap<Integer, String>();
        Set<Integer> filterCats = new HashSet<Integer>();

        // weibo tag recommend
        FilterBuild filterBuild = new FilterBuild(filterNews, filterCats, bMap, topNewsBloomService);
        List<Integer> weiboResult = weiboNewsService.getWeiboTagNews(cid, pid, filterBuild, WTAG_PARTITION);
        resultMap.putAll(ConvertUtils.convertList2Map(weiboResult, CommonConstants.WEIBO_TAG_TYPE));

        recomByLocation(cid, filterNews, filterCats, resultMap, bMap);
        recomByShortCat(cid, filterNews, filterCats, resultMap, bMap);
        recomByLongCat(cid, filterNews, filterCats, resultMap, bMap);
        recomBySubCat(cid, filterNews, filterCats, resultMap, bMap);
        retrieveHotNews(cid, filterNews, filterCats, resultMap, bMap);
        // user history news:
        Map<Integer, String> historyNews = null;
        if (ispull) {
            // historyNews = queryUserHistoryNews(cid, TOTAL_NUMBER - resultMap.size());
            historyNews = new HashMap<Integer, String>();
        }
        // add history news to result map
        if (ispull) {
            resultMap.putAll(historyNews);
        }
        return resultMap;
    }

    private void recomByLocation(long cid, Set<Integer> filterNews, Set<Integer> filterCats,
                                 Map<Integer, String> resultMap, Map<String, BloomFilter> bMap) {
        if (cid > 0) {
            String key = String.format(RedisKeyConstants.USER_LOCATION_KEY, cid);
            String value = shardedRedisTemplateUser.opsForValue().get(key);
            if (StringUtils.isEmpty(value)) {
                return;
            }

            String localNewsKey = String.format(TopNewsRedisKeyConstants.LOCAL_NEWS_KEY, value);

            Set<String> newsIds = shardedRedisTemplateRecom.opsForZSet().reverseRange(localNewsKey, 0, -1);

            if (newsIds == null || newsIds.isEmpty()) return;
            for (String newsIdStr : newsIds) {
                Integer newsId = Integer.valueOf(newsIdStr);

                if (filterNews.contains(newsId) || resultMap.containsKey(newsId)
                    || topNewsBloomService.checkHasRecom(newsId, bMap)) {
                    continue;
                }

                Set<Integer> catSet = newsCacheService.getCatIdsCacheForOther(newsId);

                resultMap.put(newsId, CommonConstants.LOCAL_TYPE);
                topNewsBloomService.addUserBloom(cid, newsId, bMap);

                filterNews.add(newsId);
                filterCats.addAll(catSet);
                // local news break
                break;
            }
        }
    }

    /**
     * @param cid
     * @param filterNews
     * @param filterCats
     * @param resultMap
     */
    private void retrieveHotNews(long cid, Set<Integer> filterNews, Set<Integer> filterCats,
                                 Map<Integer, String> resultMap, Map<String, BloomFilter> bMap) {
        Set<String> catNewsSet = shardedRedisTemplateRecom.opsForZSet().reverseRange(TopNewsRedisKeyConstants.HOT_NEW_ZSET,
                                                                                     0, 40 * HOT_PARTITION);
        if (catNewsSet != null && !catNewsSet.isEmpty()) {

            int count = 0;
            for (String tuple : catNewsSet) {
                if (count >= HOT_PARTITION) {
                    return;
                }
                Integer newsId = Integer.parseInt(tuple);
                if (filterNews.contains(newsId) || resultMap.containsKey(newsId)
                    || topNewsBloomService.checkHasRecom(newsId, bMap)) {
                    continue;
                }
                Set<Integer> catSet = newsCacheService.getCatIdsCacheForOther(newsId);
                boolean flag = false;
                if (catSet != null && !catSet.isEmpty()) {
                    for (Integer newsCat : catSet) {
                        if (filterCats.contains(newsCat)) {
                            flag = true;
                            break;
                        }
                    }
                    // contain category
                    if (flag) continue;
                }

                resultMap.put(newsId, CommonConstants.HOT_TYPE);
                topNewsBloomService.addUserBloom(cid, newsId, bMap);

                filterNews.add(newsId);
                //
                filterCats.addAll(catSet);
                count++;
            }
        }

    }

    /**
     * recommend news by user's long like cat
     *
     * @param cid
     * @param filterNews
     * @param filterCats
     * @param resultMap
     */
    private void recomByLongCat(long cid, Set<Integer> filterNews, Set<Integer> filterCats,
                                Map<Integer, String> resultMap, Map<String, BloomFilter> bMap) {
        String jsonString = edbTemplate.getString(EdbKeyConstants.CAT_SHORT, String.valueOf(cid));
        if (StringUtils.isNotBlank(jsonString)) {
            Map<Integer, Double> longLikeCat = JSON.parseObject(jsonString,
                                                                new com.alibaba.fastjson.TypeReference<LinkedHashMap<Integer, Double>>() {
                                                                });

            if (longLikeCat != null && !longLikeCat.isEmpty()) {
                List<Integer> longLikeList = MapSortUtil.sortMapByValue(longLikeCat);
                int number = longLikeList.size();
                List<Integer> selectCats = new ArrayList<Integer>();
                if (number < LONG_PARTITION) {
                    if (!filterCats.contains(longLikeList.get(0))) {
                        selectCats.add(longLikeList.get(0));
                    }
                } else {
                    Random random = new Random();
                    for (int count = 0; count < LONG_PARTITION; count++) {
                        int pos = random.nextInt(number);
                        number--;
                        Integer catId = longLikeList.get(pos);
                        if (!filterCats.contains(catId)) {
                            selectCats.add(longLikeList.get(pos));
                        }
                        longLikeList.remove(catId);
                    }
                }
                for (int catId : selectCats) {
                    // query local cache
                    List<Integer> catNewsCache = newsTagService.queryCatNews(catId);
                    // recom news
                    for (Integer newsId : catNewsCache) {
                        if (filterNews.contains(newsId) || resultMap.containsKey(newsId)
                            || newsFilterService.checkFilter(newsId) || topNewsBloomService.checkHasRecom(newsId, bMap)) {
                            continue;
                        }
                        resultMap.put(newsId, CommonConstants.RECOM_TYPE_LONG);
                        topNewsBloomService.addUserBloom(cid, newsId, bMap);

                        filterNews.add(newsId);
                        // add cat filter
                        filterCats.add(catId);
                        break;
                    }
                }
            }
        }

    }

    private void recomBySubCat(long cid, Set<Integer> filterNews, Set<Integer> filterCats,
                               Map<Integer, String> resultMap, Map<String, BloomFilter> bMap) {
        String jsonString = edbTemplate.getString(EdbKeyConstants.CAT_SUB, String.valueOf(cid));
        if (StringUtils.isNotBlank(jsonString)) {
            Map<Integer, Double> longLikeCat = JSON.parseObject(jsonString,
                                                                new com.alibaba.fastjson.TypeReference<LinkedHashMap<Integer, Double>>() {
                                                                });

            if (longLikeCat != null && !longLikeCat.isEmpty()) {
                List<Integer> subCatLikeList = MapSortUtil.sortMapByValue(longLikeCat);
                int number = subCatLikeList.size();
                List<Integer> selectCats = new ArrayList<Integer>();
                if (number < LONG_PARTITION) {
                    if (!filterCats.contains(subCatLikeList.get(0))) {
                        selectCats.add(subCatLikeList.get(0));
                    }
                } else {
                    Random random = new Random();
                    for (int count = 0; count < LONG_PARTITION; count++) {
                        int pos = random.nextInt(number);
                        number--;
                        Integer catId = subCatLikeList.get(pos);
                        if (!filterCats.contains(catId)) {
                            selectCats.add(subCatLikeList.get(pos));
                        }
                        subCatLikeList.remove(catId);
                    }
                }
                for (int catId : selectCats) {
                    // query local cache
                    List<Integer> catNewsCache = newsTagService.queryCatNews(catId);
                    // recom news
                    for (Integer newsId : catNewsCache) {
                        if (filterNews.contains(newsId) || resultMap.containsKey(newsId)
                            || newsFilterService.checkFilter(newsId) || topNewsBloomService.checkHasRecom(newsId, bMap)) {
                            continue;
                        }
                        resultMap.put(newsId, CommonConstants.RECOM_TYPE_SUB);
                        topNewsBloomService.addUserBloom(cid, newsId, bMap);

                        filterNews.add(newsId);
                        // add cat filter
                        filterCats.add(catId);
                        break;
                    }
                }
            }
        }

    }

    /**
     * recommend news by user's short like cat
     *
     * @param cid
     * @param filterNews
     * @param filterCats
     * @param resultMap
     */
    private void recomByShortCat(long cid, Set<Integer> filterNews, Set<Integer> filterCats,
                                 Map<Integer, String> resultMap, Map<String, BloomFilter> bMap) {
        List<String> shortLikeCat = newsUserService.queryShortCat(cid, 0L);
        if (shortLikeCat != null && !shortLikeCat.isEmpty()) {
            int index = TopNewsStrategy.queryShortCatIndex(shortLikeCat.size());
            String catId = shortLikeCat.get(index);
            List<Integer> catNewsCache = newsTagService.queryCatNews(Integer.parseInt(catId));
            for (Integer newsId : catNewsCache) {
                if (filterNews.contains(newsId) || resultMap.containsKey(newsId)
                    || newsFilterService.checkFilter(newsId) || topNewsBloomService.checkHasRecom(newsId, bMap)
                    || newsFilterService.checkCatFilter(filterCats, newsId, true)) {
                    continue;
                }
                resultMap.put(newsId, CommonConstants.RECOM_TYPE_SHORT);
                topNewsBloomService.addUserBloom(cid, newsId, bMap);
                filterNews.add(newsId);
                break;
            }
        }
    }

    public void scribeLog(long cid, Map<Integer, String> resultMap) {
        AccessTraceLogData logData = new AccessTraceLogData(LogCategoryEnum.topNewsRec);
        logData.add(LogKeyEnum.Cid, cid);
        logData.add(LogKeyEnum.RecCount, resultMap.keySet().size());
        logData.add(LogKeyEnum.RecNewsIds, resultMap.toString());
        logData.add(LogKeyEnum.TimeStamp, System.currentTimeMillis());
        StatisticLog.info(logData);
    }

    public List<String> getLocalNews(int page) {
        String key = String.format(TopNewsRedisKeyConstants.LOCAL_NEWS_KEY, "1100");
        List<String> result = new ArrayList<String>();
        Long count = shardedRedisTemplateRecom.opsForZSet().size(key);
        result.add(count + "");
        if (count <= 0) return null;
        if (5 * (page - 1) > count) {
            page = 1;
        }
        Set<String> newsIds = shardedRedisTemplateRecom.opsForZSet().reverseRange(key, 5 * (page - 1), 5 * page - 1);
        if (newsIds != null && !newsIds.isEmpty()) {
            result.addAll(newsIds);
        }
        return result;
    }

}
