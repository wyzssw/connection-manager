/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wap.sohu.recom.cache.core.ICache;
import com.wap.sohu.recom.cache.manager.TopNewsCacheManager;
import com.wap.sohu.recom.constants.EdbKeyConstants;
import com.wap.sohu.recom.constants.TaoRecomRedisKeyConstants;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.elephantdb.connector.EdbTemplate;
import com.wap.sohu.recom.model.FilterBuild;
import com.wap.sohu.recom.service.impl.UserLikeService;
import com.wap.sohu.recom.util.bloom.BloomFilter;
import com.wap.sohu.recom.utils.ConvertUtils;
import com.wap.sohu.recom.utils.MapSortUtil;
import com.wap.sohu.recom.utils.TopNewsStrategy;

/**
 * 得到具体类别推荐
 *
 * @author hongfengwang 2013-8-9 上午11:06:39
 */
@Service
public class TaoNewsService {


    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateRecom;

    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateUser;

    @Autowired
    private MChannelNewsService    mChannelNewsService;

    @Autowired
    private NewsCacheService       newsCacheService;

    @Autowired
    private UserLikeService        userLikeService;

    @Autowired
    private EdbTemplate            edbTemplate;

    private final int              cycleCount = 3;
//    private final int              cycleSize  = 100;

    /**
     * @param cid
     * @param pid
     * @param filterBuild
     * @param i
     * @return
     */
    public List<Integer> getShortLikeNews(long cid, long pid, FilterBuild filterBuild, int count) {
        String key = String.format(TaoRecomRedisKeyConstants.USER_SHORT_LIKE_CAT, cid);
        List<Integer> resultList = new ArrayList<Integer>();
        List<String> shortLikeCat = queryShortCat(key);
        Random random = new Random();
        //TODO:上线后删除，这是为了测试阶段用户没有点击行为做的
        if (shortLikeCat == null || shortLikeCat.isEmpty()) {
            shortLikeCat.add(random.nextInt(300) + "");
        }
        if (shortLikeCat != null && !shortLikeCat.isEmpty()) {
            int index = TopNewsStrategy.queryShortCatIndex(shortLikeCat.size());
            String catId = shortLikeCat.get(index);
            for (int cycleIndex = 1, resultCount = 0; resultCount < count && cycleIndex <= cycleCount; cycleIndex++) {
                Set<Integer> catNewsCache = queryCatNews(Integer.parseInt(catId), 100 * (cycleIndex - 1),
                                                         100 * cycleIndex);
                for (Integer newsId : catNewsCache) {
                    if (filterBuild.checkNewsInvalid(newsId)) {
                        continue;
                    }
                    Set<Integer> catSet = newsCacheService.getCatIdsCacheForOther(newsId);
                    resultList.add(newsId);

                    // filter
                    filterBuild.addCatSet(catSet);
                    filterBuild.addNews(cid, newsId);
                    if (++resultCount >= count) {
                        break;
                    }
                }
            }
        }
        return resultList;
    }

    /**
     * @param cid
     * @param pid
     * @param filterBuild
     * @param i
     * @return
     */
    public List<Integer> getLongLikeNews(long cid, long pid, FilterBuild filterBuild, int recCount) {
        String jsonString = edbTemplate.getString(EdbKeyConstants.TAO_CAT_LONG, String.valueOf(cid));
        List<Integer> resultList = new ArrayList<Integer>();
        List<Integer> catList = getListFromEdb(jsonString);
        catList = (catList == null || catList.isEmpty()) ? userLikeService.getUserFirstLike(cid, pid) : catList;
        if (catList == null || catList.isEmpty()) {
            return resultList;
        }
        List<Integer> selectCats = selectCats(recCount, filterBuild, catList);
        // 从各个类别中得到推荐的新闻
        for (int catId : selectCats) {
            // query local cache
            Set<Integer> catNewsCache = queryCatNews(catId, 0, 100);
            for (Integer newsId : catNewsCache) {

                if (filterBuild.checkNewsInvalid(newsId)) {
                    continue;
                }
                resultList.add(newsId);
                filterBuild.addNews(cid, newsId);
                filterBuild.addCat(catId);
                break;
            }
        }
        return resultList;
    }

    /**
     * @param newsId
     * @return
     */
    // private boolean checkFilter(Integer newsId) {
    // Long time = newsTagService.getNewsTime(newsId);
    // long dayBefore = System.currentTimeMillis() / 1000 - DateUtils.MILLIS_PER_DAY / 1000 * 1;
    // if (time < dayBefore) {
    // String title = newsCacheService.getNewTitleCache(newsId);
    // if (StringUtils.isBlank(title) || title.contains("先知道") || title.contains("神吐槽")) {
    // return true;
    // }
    // }
    // return false;
    //
    // }
    
   

    private List<String> queryShortCat(String key) {
        List<String> shortLikeCat = shardedRedisTemplateUser.opsForList().range(key, 0, -1);
        return shortLikeCat;
    }

    /**
     * 获取start开始下一100个或者不够100个有多少返回多少 query category news
     *
     * @param catId
     * @return
     */
    private Set<Integer> queryCatNews(Integer catId, int start, int count) {
        ICache<Integer, Set<Integer>> iCache = TopNewsCacheManager.getTaoNewsCache();
        Set<Integer> retLinkedHashSet = new LinkedHashSet<Integer>();
        Set<Integer> resultSet = iCache.get(catId);
        int index = 0;
        if (resultSet == null || resultSet.isEmpty() || resultSet.size() < start) {
            resultSet = (resultSet == null ? new LinkedHashSet<Integer>() : resultSet);
            index = resultSet.size();
            String queryKey = String.format(TaoRecomRedisKeyConstants.CAT_NEWS_MATRIX_TAO, catId);
            // 获取下一100个catId下的新闻
            Set<String> moreStrSet = shardedRedisTemplateRecom.opsForZSet().reverseRange(queryKey,
                                                                                         resultSet.size() + 1, count);
            if (moreStrSet == null || moreStrSet.isEmpty()) {
                return retLinkedHashSet;
            }
            Set<Integer> moreSet = ConvertUtils.convert2intList(moreStrSet);
            resultSet.addAll(moreSet);
            iCache.put(catId, resultSet);
            TopNewsCacheManager.getTaoNewsCache().put(catId, resultSet);
        }
        List<Integer> list = new ArrayList<Integer>(resultSet);
        return new LinkedHashSet<Integer>(list.subList(start > resultSet.size() ? index : start,
                                                       count > resultSet.size() ? resultSet.size() : count));
    }

    /**
     * 按照时间段划分的sub 新闻
     *
     * @param cid
     * @param pid
     * @param filterBuild
     * @param i
     * @return
     */
    public List<Integer> getHotNews(long cid, long pid, FilterBuild filterBuild, int count) {
        Set<Integer> filterCats = filterBuild.getFilterCats();
        Set<Integer> filterNews = filterBuild.getFilterNews();
        Map<String, BloomFilter> bMap = filterBuild.getbMap();
        // filtercat就不过滤分类了，subId出来的新闻比较少
        List<Integer> list = mChannelNewsService.getSubNews(cid, filterNews, filterCats, bMap, count);
        return list;
    }

    public List<Integer> getListFromEdb(String json) {
        if (StringUtils.isBlank(json) || json.equals("null")) {
            return null;
        }
        Map<Integer, Double> longLikeCat = JSON.parseObject(json, new TypeReference<LinkedHashMap<Integer, Double>>() {
        });
        if (longLikeCat != null && !longLikeCat.isEmpty()) {
            return MapSortUtil.sortMapByValue(longLikeCat);
        }
        return null;
    }

    /**
     * 筛选类别
     *
     * @param recCount
     * @param filterCats
     * @param catList
     * @return
     */
    private List<Integer> selectCats(int recCount, FilterBuild filterBuild, List<Integer> catList) {
        int number = catList.size();
        List<Integer> selectCats = new ArrayList<Integer>();
        if (number < recCount) {
            for (Integer catId : catList) {
                if (!filterBuild.checkCatExist(catId)) {
                    selectCats.add(catId);
                }
            }
        } else {
            Random random = new Random();
            for (int count = 0; count < recCount; count++) {
                int pos = random.nextInt(number);
                number--;
                Integer catId = catList.get(pos);
                if (!filterBuild.checkCatExist(catId)) {
                    selectCats.add(catList.get(pos));
                }
                catList.remove(catId);
            }
        }
        return selectCats;
    }

}
