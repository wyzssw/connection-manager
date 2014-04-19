/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Tuple;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.wap.sohu.recom.cache.core.ICache;
import com.wap.sohu.recom.cache.manager.NewsCacheManager;
import com.wap.sohu.recom.cache.manager.TopNewsCacheManager;
import com.wap.sohu.recom.constants.CommonConstants;
import com.wap.sohu.recom.constants.EdbKeyConstants;
import com.wap.sohu.recom.constants.RedisKeyConstants;
import com.wap.sohu.recom.constants.TopNewsRedisKeyConstants;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.dao.NewsCatDao;
import com.wap.sohu.recom.dao.NewsContentDao;
import com.wap.sohu.recom.dao.NewsTagDao;
import com.wap.sohu.recom.elephantdb.connector.EdbTemplate;
import com.wap.sohu.recom.model.NewsContent;
import com.wap.sohu.recom.service.NewsCacheService;
import com.wap.sohu.recom.utils.ConvertUtils;

/**
 * 专门操作news cache服务的类，为了与其他bean分开，使用aysn标记
 *
 * @author hongfengwang 2012-9-13 下午04:15:25
 */
@Service("newsCacheService")
public class NewsCacheServiceImpl implements NewsCacheService {

    private static final Logger     LOGGER = Logger.getLogger(NewsCacheServiceImpl.class);

    @Autowired
    private StringRedisTemplateExt  shardedRedisTemplateRecom;

    @Autowired
    private RedisThreadLocalService redisThreadLocalService;

    @Autowired
    private NewsTagDao              newsTagDao;

    @Autowired
    private NewsCatDao              newsCatDao;

    @Autowired
    private NewsContentDao          newsContentDao;

    @Autowired
    private EdbTemplate             edbTemplate;

    @Override
    public Map<Integer, Double> getNewsSimCache(int newsId) {
        ICache<Integer, Map<Integer, Double>> iCache = NewsCacheManager.getNewsSimCache();
        Map<Integer, Double> map = null;
        if ((map = iCache.get(newsId)) == null) {
            map = new LinkedHashMap<Integer, Double>();
            String key = String.format(RedisKeyConstants.NEWS_SIM_NEWS, newsId);
            Set<Tuple> recomSet = redisThreadLocalService.getShardedJedisRecom().zRevRangeWithScores(key, 0, 130);
            for (Tuple tuple : recomSet) {
                if (tuple.getScore() > 0.2) {
                    Integer recNewsId = Integer.parseInt(tuple.getElement());
                    map.put(recNewsId, tuple.getScore());
                }
            }
            iCache.put(newsId, map);
        }
        return copyMap(map);
    }

    /**
     * 复制 Map对象
     * @param source
     * @return
     */
    public static <K, V> Map<K, V> copyMap(Map<K, V> source) {
        Map<K, V> des = new LinkedHashMap<K, V>();
        if (source != null && !source.isEmpty()) {
            des.putAll(source);
        }
        return des;
    }

    @Override
    public Set<String> getCatsCache(int newsId, boolean useThreadLocal) {
        ICache<Integer, Set<String>> iCache = NewsCacheManager.getNewsCatCache();
        Set<String> set = null;
        if ((set = iCache.get(newsId)) == null) {
            // 忽略并发影响
            set = new LinkedHashSet<String>();
            String key = String.format(RedisKeyConstants.NEWS_CAT_MATRIX, newsId);
            Set<String> retSet = null;
            if (useThreadLocal) {
                retSet = redisThreadLocalService.getShardedJedisRecom().zRevRange(key, 0, -1);
            } else {
                retSet = shardedRedisTemplateRecom.opsForZSet().reverseRange(key, 0, -1);
            }
            if (retSet == null || retSet.size() == 0) {
                return null;
            }
            set.addAll(retSet);
            iCache.put(newsId, set);
        }
        return set;
    }

    /**
     * 带redisThreadLocalService的只给新闻推荐用
     */
    @Override
    public Set<Integer> getCatIdsCache(int newsId) {
        ICache<Integer, Set<Integer>> iCache = NewsCacheManager.getNewsCatidCache();
        Set<Integer> set = null;
        if ((set = iCache.get(newsId)) == null) {
            // 忽略并发影响
            String key = String.format(TopNewsRedisKeyConstants.NEWS_CATID_MATRIX, newsId);
            Set<String> tmpSet = redisThreadLocalService.getShardedJedisRecom().zRevRange(key, 0, -1);
            if (tmpSet == null || tmpSet.size() == 0) {
                long sevenDaysTime = System.currentTimeMillis() - 86400 * 7 * 1000;
                set = newsTagDao.getNewsCatId(newsId, new Date(sevenDaysTime));
            } else {
                set = ConvertUtils.convert2intList(tmpSet);
            }
            set.addAll(set);
            iCache.put(newsId, set);
        }
        return set;
    }

    /**
     * 提供给其他接口来获取新闻对应的catId
     *
     * @param newsId
     * @return
     */
    @Override
    public Set<Integer> getCatIdsCacheForOther(int newsId) {
        ICache<Integer, Set<Integer>> iCache = NewsCacheManager.getNewsCatidCache();
        Set<Integer> set = null;
        if ((set = iCache.get(newsId)) == null) {
            // 忽略并发影响
            String key = String.format(TopNewsRedisKeyConstants.NEWS_CATID_MATRIX, newsId);
            Set<String> tmpSet = shardedRedisTemplateRecom.opsForZSet().reverseRange(key, 0, -1);
            if (tmpSet == null || tmpSet.size() == 0) {
                long sevenDaysTime = System.currentTimeMillis() - 86400 * 7 * 1000;
                set = newsTagDao.getNewsCatId(newsId, new Date(sevenDaysTime));
            } else {
                set = ConvertUtils.convert2intList(tmpSet);
            }
            set.addAll(set);
            iCache.put(newsId, set);
        }
        return set;
    }

    @Override
    public Map<Integer, Double> getTopTagsCache(int tagId, int count) {
        ICache<Integer, Map<Integer, Double>> iCache = NewsCacheManager.getTagTopCache();
        Map<Integer, Double> map = null;
        if ((map = iCache.get(tagId)) == null) {
            String key = String.format(RedisKeyConstants.TAG_TOP_TAGS, tagId);
            map = getRedisMap(key, count);
            iCache.put(tagId, map);
        }
        return map;
    }

    @Override
    public Integer getDupId(int newsId) {
        ICache<Integer, Integer> iCache = NewsCacheManager.getNewsDupCache();
        Integer dupId = null;
        if ((dupId = iCache.get(newsId)) == null) {
            String key = String.format(RedisKeyConstants.NEWS_DUP_ID, newsId);
            String idString = redisThreadLocalService.getShardedJedisRecom().get(key);
            if (StringUtils.isNotBlank(idString)) {
                dupId = Integer.valueOf(idString);
            } else {
                dupId = newsTagDao.getDupId(newsId);
            }
            iCache.put(newsId, dupId);
        }
        return dupId;
    }

    @Override
    public Set<Integer> getNoPubNews() {
        ICache<String, Set<Integer>> iCache = NewsCacheManager.getNotPubNews();
        if (iCache.get(CommonConstants.FILTER_PUB_KEY) == null) {
            return new HashSet<Integer>();
        }
        return iCache.get(CommonConstants.FILTER_PUB_KEY);
    }

    @Override
    public Set<Integer> getFilterTypeNews() {
        ICache<String, Set<Integer>> iCache = NewsCacheManager.getFilterTypeNews();
        if (iCache.get(CommonConstants.NEWS_FILTER_TYPE_KEY) == null) {
            return new HashSet<Integer>();
        }
        return iCache.get(CommonConstants.NEWS_FILTER_TYPE_KEY);
    }

    /**
     * @param tagId
     * @param count
     * @return
     */
    private Map<Integer, Double> getRedisMap(String key, int count) {
        Map<Integer, Double> map = new LinkedHashMap<Integer, Double>();
        Set<TypedTuple<String>> set = shardedRedisTemplateRecom.opsForZSet().reverseRangeWithScores(key, 0, count);
        if (set == null || set.size() == 0) {
            return null;
        }
        for (TypedTuple<String> typedTuple : set) {
            map.put(Integer.parseInt(typedTuple.getValue()), typedTuple.getScore());
        }
        return map;
    }

    @Override
    public String getNewTitleCache(int newsId) {
        ICache<Integer, String> iCache = NewsCacheManager.getNewsTitleCahce();
        String title = null;
        if ((title = iCache.get(newsId)) == null) {
            String key = String.format(RedisKeyConstants.NEWS_TITLE_CACHE, newsId);
            title = shardedRedisTemplateRecom.opsForValue().get(key);
            if (StringUtils.isNotBlank(title)) {
                iCache.put(newsId, title);
            } else {
                NewsContent newsContent = newsTagDao.getOneNewsById(newsId);
                if (newsContent != null) {
                    title = newsContent.getTitle();
                    iCache.put(newsId, title);
                    if (newsContent.getNewsType() == 4) {
                        shardedRedisTemplateRecom.opsForValue().set(key, title, 15, TimeUnit.DAYS);
                    }
                }
            }
        }
        return title;
    }

    @Override
    public String getNewsContentCache(int newsId) {
        ICache<Integer, String> iCache = NewsCacheManager.getNewsContentCahce();
        String content = null;
        if ((content = iCache.get(newsId)) == null) {
            String key = String.format(RedisKeyConstants.NEWS_CONTENT_CACHE, newsId);
            content = shardedRedisTemplateRecom.opsForValue().get(key);
            if (StringUtils.isNotBlank(content)) {
                content = CommonConstants.patternNoChinese.matcher(content).replaceAll("");
                iCache.put(newsId, content);
            } else {
                NewsContent newsContent = newsTagDao.getOneNewsById(newsId);
                if (newsContent != null) {
                    content = newsContent.getContent();
                    content = StringUtils.substringBetween(content, "<content>", "</content>");
                    // 耗时破方法
                    content = StringEscapeUtils.unescapeHtml4(content);
                    // 有些转义了两次，so..
                    content = StringEscapeUtils.unescapeHtml4(content);
                    content = StringUtils.strip(content);
                    content = StringUtils.isNotBlank(content) ? ConvertUtils.delHTMLTag(content) : "";
                    content = CommonConstants.patternNoChinese.matcher(content).replaceAll("");
                    iCache.put(newsId, content);
                    if (newsContent.getNewsType() == 4) {
                        shardedRedisTemplateRecom.opsForValue().set(key, content, 7, TimeUnit.DAYS);
                    }
                }
            }
        }
        return content;
    }

    @Override
    public Map<Integer, Double> getNewsScore(Integer tid, double threadHold) {
        ICache<Integer, Map<Integer, Double>> iCache = NewsCacheManager.getNewsScore();
        Map<Integer, Double> map = null;
        if ((map = iCache.get(tid)) == null) {
            String jsonString = edbTemplate.getString(EdbKeyConstants.NEWS_SCORE, String.valueOf(tid));
            if (StringUtils.isBlank(jsonString)) {
                return null;
            }
            map = JSON.parseObject(jsonString, new TypeReference<LinkedHashMap<Integer, Double>>() {
            });
            Map<Integer, Double> map2 = new LinkedHashMap<Integer, Double>();
            for (Map.Entry<Integer, Double> item : map.entrySet()) {
                if (item.getValue() > threadHold) {
                    map2.put(item.getKey(), item.getValue());
                }
            }
            iCache.put(tid, map2);
        }
        return map;
    }

    @Override
    public Integer getNewsPubId(Integer newsId) {
        ICache<Integer, Integer> iCache = NewsCacheManager.getNewsPubIdCache();
        Integer pubId = null;
        String key = String.format(RedisKeyConstants.NEWS_PUBID_CACHE, newsId);
        if ((pubId = iCache.get(newsId)) == null) {
            String str = shardedRedisTemplateRecom.opsForValue().get(key);
            if (StringUtils.isNotBlank(str)) {
                pubId = Integer.valueOf(str);
                iCache.put(newsId, pubId);
            } else {
                pubId = newsTagDao.getPubId(newsId);
                if (pubId != null) {
                    shardedRedisTemplateRecom.opsForValue().set(key, String.valueOf(pubId), 1, TimeUnit.DAYS);
                    iCache.put(newsId, pubId);
                } else {
                    shardedRedisTemplateRecom.opsForValue().set(key, "0", 6, TimeUnit.HOURS);
                    pubId = 0;
                    iCache.put(newsId, pubId);
                }
            }
        }
        return pubId;
    }

    @Override
    public List<Integer> getCertainPubNews(Integer pubId) {
        List<Integer> newsList = null;
        ICache<Integer, List<Integer>> iCache = NewsCacheManager.getPubIdNewsList();
        String key = String.format(RedisKeyConstants.PUBID_NEWS_LIST, pubId);
        if ((newsList = iCache.get(pubId)) == null) {
            List<String> tempList = shardedRedisTemplateRecom.opsForList().range(key, 0, -1);
            newsList = new ArrayList<Integer>();
            if (tempList != null && tempList.size() != 0) {
                for (String string : tempList) {
                    newsList.add(Integer.valueOf(string));
                }
                iCache.put(pubId, newsList);
            } else {
                long beforeTime = System.currentTimeMillis() - 86400 * 1000 * CommonConstants.GROUP_UPDATE_DAYS;
                newsList = newsTagDao.getPubIdNewsList(pubId, new Date(beforeTime));
                shardedRedisTemplateRecom.opsForBatch().leftPushForInt(key, newsList);
                shardedRedisTemplateRecom.expire(key, 2, TimeUnit.DAYS);
                iCache.put(pubId, newsList);
            }
        }
        return newsList;
    }

    @Override
    public NewsContent getNewsContent(int newsId) {
        NewsContent newsContent = null;
        ICache<Integer, NewsContent> iCache = NewsCacheManager.getNewsContentMore();
        String key = String.format(TopNewsRedisKeyConstants.NEWS_ATTRIBUTE_CACHE, newsId);
        if ((newsContent = iCache.get(newsId)) == null) {
            String json = shardedRedisTemplateRecom.opsForValue().get(key);
            if (StringUtils.isBlank(json) || json.equals("null") || !StringUtils.containsIgnoreCase(json, "snapshot")) {
                newsContent = getWholeNewsContent(newsId);
                if (newsContent == null) {
                    NewsContent newsContent2 = new NewsContent();
                    newsContent2.setId(newsId);
                    iCache.put(newsId, newsContent2);
                    return null;
                }
                iCache.put(newsId, newsContent);
                shardedRedisTemplateRecom.opsForValue().set(key, JSON.toJSONString(newsContent),
                                                            CommonConstants.UPDATE_DAYS, TimeUnit.DAYS);
            } else {
                newsContent = JSON.parseObject(json, new TypeReference<NewsContent>() {
                });
                iCache.put(newsId, newsContent);
            }
        }
        return newsContent;
    }

    @Override
    public Set<Integer> getDeletedNews() {
        ICache<String, Set<Integer>> iCache = NewsCacheManager.getNewsDeletedNews();
        if (iCache.get(CommonConstants.NEWS_DELETED_CACHE) == null) {
            iCache.put(CommonConstants.NEWS_DELETED_CACHE, new HashSet<Integer>());
        }
        return iCache.get(CommonConstants.NEWS_DELETED_CACHE);
    }

    public NewsContent getWholeNewsContent(Integer newsId) {
        NewsContent newsContent = newsContentDao.getNewsContent(newsId);
        if (newsContent == null) {
            return null;
        }
        List<Integer> channelIds = newsContentDao.getChannelIds(newsId);
        newsContent.setChannelIds(channelIds);
        newsContent.setPubId(newsContentDao.getPubIds(newsId));
        List<Integer> subIds = null;
        if (newsContent.getPubId() != null && newsContent.getPubId() != -1) {
            subIds = newsContentDao.getSubIds(newsContent.getPubId());
            newsContent.setSubIds(subIds);
        }
        newsContent.setContent(newsTagDao.getNewsText(newsContent.getId()));
        processNTime(newsContent);
        return newsContent;
    }

    /**
     * 返回redis key下面的count个value值 虽然命名是hotnews，但是可以用到commonNews上
     *
     * @param key
     * @param count
     * @return linkedhashset
     */
    @Override
    public Set<Integer> getMChannelHotNews(String key, int start, int count) {
        ICache<String, Set<Integer>> iCache = TopNewsCacheManager.getMChannelHotNews();
        Set<Integer> resultSet = null;
        Set<String> set = null;
        if ((resultSet = iCache.get(key)) == null || resultSet.size() < count) {
            resultSet = (resultSet == null ? new LinkedHashSet<Integer>() : resultSet);
            set = shardedRedisTemplateRecom.opsForZSet().reverseRange(key, resultSet.size(), count - 1);
            if (set == null) {
                return null;
            }
            Set<Integer> moreSet = ConvertUtils.convert2intList(set);
            resultSet.addAll(moreSet);
            iCache.put(key, resultSet);
        }
        if (resultSet.size() < start) {
            return null;
        }
        List<Integer> list = new ArrayList<Integer>(resultSet);
        return new LinkedHashSet<Integer>(list.subList(start, count > resultSet.size() ? resultSet.size() : count));
    }

    public void processNTime(NewsContent newsContent) {
        if (StringUtils.isNotBlank(newsContent.getSnapShot())) {
            String snapShot = newsContent.getSnapShot();
            JSONObject jsonObject = JSON.parseObject(snapShot);
            String time = jsonObject.getString("ntime");
            if (StringUtils.isNotBlank(time)) {
                try {
                    Date _date = DateUtils.parseDate(time.trim(), "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH:mm:ss",
                                                     "yyyy-MM-dd HH", "yyyy-MM-dd");
                    Long _dateValueLong = Long.valueOf(DateFormatUtils.format(_date, "yyyyMMdd"));
                    Long today = Long.valueOf(DateFormatUtils.format(new Date(), "yyyyMMdd"));
                    if (_dateValueLong > today) {
                        LOGGER.info(time + "newscontent id is " + newsContent.getId() + "snapshot is"
                                    + newsContent.getSnapShot());
                        newsContent.setNTime(newsContent.getCreateTime());
                    } else {
                        newsContent.setNTime(_date);
                    }
                } catch (ParseException e) {
                    // 解析出错，就扔到7天前去
                    LOGGER.info(time + " parseException encountered " + newsContent.getId() + "snapshot is"
                                + newsContent.getSnapShot());
                    newsContent.setNTime(DateUtils.addDays(new Date(), -CommonConstants.UPDATE_DAYS));
                }
            } else {
                LOGGER.info(" ntime is empty " + newsContent.getSnapShot());
                newsContent.setNTime(newsContent.getCreateTime());
            }
        }
    }

    @Override
    public Integer getTopCatId(int catId) {
        ICache<Integer, Integer> iCache = TopNewsCacheManager.getCatTopCatId();
        Integer topId = null;
        if ((topId = iCache.get(catId)) == null) {
            topId = newsCatDao.getTopId(catId);
            iCache.put(catId, topId);
            return topId;
        }
        return topId;
    }

}
