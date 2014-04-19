package com.wap.sohu.recom.service.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.wap.sohu.recom.cache.core.ICache;
import com.wap.sohu.recom.cache.manager.TopNewsCacheManager;
import com.wap.sohu.recom.constants.CommonConstants;
import com.wap.sohu.recom.constants.EdbKeyConstants;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.elephantdb.connector.EdbTemplate;
import com.wap.sohu.recom.model.FilterBuild;
import com.wap.sohu.recom.service.MChannelNewsService;
import com.wap.sohu.recom.service.NewsCacheService;
import com.wap.sohu.recom.service.NewsFilterService;
import com.wap.sohu.recom.service.NewsTagService;
import com.wap.sohu.recom.service.impl.NewsUserService;
import com.wap.sohu.recom.utils.ConvertUtils;
import com.wap.sohu.recom.utils.LocalityFilterUtils;
import com.wap.sohu.recom.utils.MapSortUtil;
import com.wap.sohu.recom.utils.TopNewsStrategy;

/**
 * 类MainChannelDataService.java的实现描述：TODO 类实现描述
 *
 * @author yeyanchao Sep 11, 2013 3:12:39 PM
 */
@Service("mainChannelDataService")
public class MainChannelDataService implements ChannelNewsDataService {

    private static final int       cycleCount = 2;

    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateRecom;

    @Autowired
    private MChannelNewsService    mChannelNewsService;

    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateLbs;

    @Autowired
    private EdbTemplate            edbTemplate;

    @Autowired
    private NewsUserService        newsUserService;

    @Autowired
    private NewsCacheService       newsCacheService;

    @Autowired
    private NewsTagService         newsTagService;

    @Autowired
    private NewsFilterService      newsFilterService;

    @Autowired
    private LocalityFilterUtils    localityFilterUtils;

    @PostConstruct
    public void init() {
        ChannelDataServiceRegister.registerService(CommonConstants.MAIN_CHANNEL_ID, this);
        ChannelDataServiceRegister.setDefaultService(this);
    }

    @Override
    public Map<Integer, String> queryRecomData(long cid, long pid, int count, FilterBuild filterbuild) {
        Set<Integer> filterNews = filterbuild.getFilterNews();
        filterNews.addAll(mChannelNewsService.getMChannelNews());
        Map<Integer, String> resultMap = new LinkedHashMap<Integer, String>();
        /**
         * recom by location other than beijing:
         */
        Set<Integer> nolikeSet = mChannelNewsService.getUserNotLikeCat(cid, pid);
        if (localityFilterUtils.isNotInBeijing(cid)) {
            recomByLongCat(cid, pid, 1, resultMap, filterbuild);
            List<Integer> catIdList = recomShortCat(cid, pid, 1, resultMap, filterbuild, nolikeSet);
            // Set<Integer> likeSet = getLikeSet(catIdList);
            /**
             * hot news
             */
            for (int cycleIndex = 1, resultCount = 0, completed = 0; resultCount < count && cycleIndex <= cycleCount
                                                                     && completed == 0; cycleIndex++) {
                Set<Integer> set = getMChannelHotNews(CommonConstants.CHANNEL_RECOM_ZSET, 100 * (cycleIndex - 1),
                                                      100 * cycleIndex);
                if (set == null || set.isEmpty()) {
                    break;
                }
                for (Integer newsId : set) {
                    if (filterbuild.checkNewsInvalid(newsId)) {
                        continue;
                    }
                    // Set<Integer> catSet = newsCacheService.getCatIdsCacheForOther(newsId);
                    // if (likeSet.isEmpty() || catSet.isEmpty() || Collections.disjoint(catSet, likeSet)) {
                    // continue;
                    // }
                    resultMap.put(newsId, CommonConstants.HOT_TYPE);
                    filterbuild.addNews(cid, newsId);
                    resultCount++;
                    if (resultCount >= count) {
                        completed = 1;
                        break;
                    }
                }
            }
        }
        return resultMap;
    }

    /**
     * @param catIdList
     * @return
     */
    private Set<Integer> getLikeSet(List<Integer> catIdList) {
        Set<Integer> set = new HashSet<Integer>();
        if (catIdList == null || catIdList.isEmpty()) {
            return set;
        }
        for (Integer catId : catIdList) {
            set.add(catId);
            set.add(newsCacheService.getTopCatId(catId));
        }
        return set;
    }

    private Map<Integer, Double> recomByLongCat(long cid, long pid, int count, Map<Integer, String> resultMap,
                                                FilterBuild filterbuild) {
        String jsonString = edbTemplate.getString(EdbKeyConstants.CAT_SHORT, String.valueOf(cid));
        if (StringUtils.isNotBlank(jsonString)) {
            Map<Integer, Double> longLikeCat = JSON.parseObject(jsonString,
                                                                new com.alibaba.fastjson.TypeReference<LinkedHashMap<Integer, Double>>() {
                                                                });
            if (longLikeCat != null && !longLikeCat.isEmpty()) {
                List<Integer> longLikeList = MapSortUtil.sortMapByValue(longLikeCat);
                int index = TopNewsStrategy.queryShortCatIndex(longLikeCat.size());
                Integer catId = longLikeList.get(index);
                List<Integer> catNewsCache = newsTagService.querySelectedCatNews(catId);
                int longSize = 0;
                for (Integer newsId : catNewsCache) {
                    if (filterbuild.checkNewsInvalid(newsId) || newsFilterService.checkShenTuCao(newsId)) {
                        continue;
                    }
                    resultMap.put(newsId, CommonConstants.RECOM_TYPE_LONG);
                    filterbuild.addNews(cid, newsId);
                    longSize++;
                    if (longSize >= count) {
                        break;
                    }
                }
            }
            return longLikeCat;
        } else {
            return null;
        }
    }

    private List<Integer> recomShortCat(long cid, long pid, int count, Map<Integer, String> resultMap,
                                        FilterBuild filterbuild, Set<Integer> shortNoLikeSet) {
        List<Integer> shortLikeCat = newsUserService.queryShortCatId(cid, 0L);
        List<Integer> retList = new ArrayList<Integer>();
        if (shortLikeCat != null && !shortLikeCat.isEmpty()) {
            int index = TopNewsStrategy.queryShortCatIndex(shortLikeCat.size());
            Integer catInt = shortLikeCat.get(index);
            // if (shortNoLikeSet!=null&&shortNoLikeSet.contains(catInt)) {
            // return shortLikeCat;
            // }
            retList.add(catInt);
            mChannelNewsService.setUserNotLikeCat(retList, cid, pid);
            List<Integer> catNewsCache = newsTagService.querySelectedCatNews(catInt);
            int shortSize = 0;
            for (Integer newsId : catNewsCache) {
                if (filterbuild.checkNewsInvalid(newsId) || newsFilterService.checkShenTuCao(newsId)) {
                    continue;
                }
                resultMap.put(newsId, CommonConstants.RECOM_TYPE_SHORT);
                filterbuild.addNews(cid, newsId);
                shortSize++;
                if (shortSize >= count) {
                    break;
                }
            }
        }
        return shortLikeCat;
    }

    // private Set<Integer> getMChannelHotNews(String key, int start, int count) {
    // ICache<String, Set<Integer>> iCache = TopNewsCacheManager.getMChannelHotNews();
    // Set<Integer> resultSet = null;
    // Set<String> set = null;
    // if ((resultSet = iCache.get(key)) == null || resultSet.size() < count) {
    // resultSet = (resultSet == null ? new LinkedHashSet<Integer>() : resultSet);
    // set = shardedRedisTemplateRecom.opsForZSet().reverseRange(key, resultSet.size(), count - 1);
    // if (set == null||set.isEmpty()) {
    // return null;
    // }
    // Set<Integer> moreSet = ConvertUtils.convert2intList(set);
    // resultSet.addAll(moreSet);
    // iCache.put(key, resultSet);
    // }
    // if (resultSet.size() < start) {
    // return null;
    // }
    // List<Integer> list = new ArrayList<Integer>(resultSet);
    // return new LinkedHashSet<Integer>(list.subList(start, count > resultSet.size() ? resultSet.size() : count));
    // }

    private Set<Integer> getMChannelHotNews(String key, int start, int count) {
        ICache<String, Set<Integer>> iCache = TopNewsCacheManager.getMChannelHotNews();
        Set<Integer> resultSet = null;
        Set<String> set = null;
        if ((resultSet = iCache.get(key)) == null) {
            resultSet = new LinkedHashSet<Integer>();
            set = shardedRedisTemplateRecom.opsForZSet().reverseRange(key, 0, 1000);
            if (set == null || set.isEmpty()) {
                return null;
            }
            Set<Integer> moreSet = ConvertUtils.convert2intList(set);
            resultSet.addAll(moreSet);
            iCache.put(key, resultSet);
        }
        return resultSet;
        // if (resultSet.size() < start) {
        // return null;
        // }
        // List<Integer> list = new ArrayList<Integer>(resultSet);
        // return new LinkedHashSet<Integer>(list.subList(start, count > resultSet.size() ? resultSet.size() : count));
    }

}
