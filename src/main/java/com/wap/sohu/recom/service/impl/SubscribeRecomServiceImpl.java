package com.wap.sohu.recom.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.wap.sohu.recom.cache.manager.SubscribeManager;
import com.wap.sohu.recom.constants.EdbKeyConstants;
import com.wap.sohu.recom.constants.ExpireTimeConstants;
import com.wap.sohu.recom.constants.MemcachedKeys;
import com.wap.sohu.recom.constants.RedisKeyConstants;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.dao.SubscriptionDao;
import com.wap.sohu.recom.elephantdb.connector.EdbTemplate;
import com.wap.sohu.recom.model.SubscriptionDo;
import com.wap.sohu.recom.model.SubscriptionType;
import com.wap.sohu.recom.service.SubscribeRecomService;
import com.wap.sohu.recom.utils.ConvertUtils;
import com.wap.sohu.recom.utils.MapSortUtil;
import com.wap.sohu.recom.utils.ThreadLocalUtils;

/**
 * 类SubscriptionRecomServiceImpl.java的实现描述：刊物推荐服务
 *
 * @author yeyanchao 2012-10-17 下午5:18:35
 */
@Component
public class SubscribeRecomServiceImpl implements SubscribeRecomService {

    private static final Logger    LOGGER           = Logger.getLogger(SubscribeRecomServiceImpl.class);

    /**
     * 最大调用次数
     */
    private static final int       maxTimes         = 2;

    /**
     * 每屏刊物数目
     */
    private static final int       SUBSIZEPERSCREEN = 4;

    /**
     * 最大推荐种子
     */
    private static final int       MAXSEED          = 10;

    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateRecom;

    @Autowired
    private StringRedisTemplateExt redisTemplateRecom;

    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateUser;

    @Autowired
    private StringRedisTemplateExt redisTemplateBase;

    @Autowired
    private SubscriptionDao        subscriptionDao;

    @Autowired
    private MemcachedClient        memcachedClient;

    @Autowired
    private EdbTemplate            edbTemplate;

    /**
     * @param excludes 包含用户已经订阅刊物信息
     */
    @Override
    public List<Integer> getSubscriptionRecomInDesc(long cid, int subId, int moreCount, String[] excludes,
                                                    String[] hasSubs) {
        List<Integer> result = new ArrayList<Integer>();

        if (moreCount <= 0) {
            return result;
        }
        Set<Integer> excludeSub = new HashSet<Integer>();

        List<Integer> subList = queryUserSubList(cid);
        excludeSub.addAll(subList);

        excludeSub.add(subId);

        // 添加过滤列表
        if (excludes != null && excludes.length > 0) {
            for (String id : excludes) {
                excludeSub.add(Integer.valueOf(id));
            }
        }

        //query sub types
        List<Integer> subTypes = querySubTypes(subId);

        if (cid > 0) {

            List<SubscriptionDo> recomList = copyOnWriteSubs(queryRecomSub(subId, moreCount * 10));
            if (recomList != null && !recomList.isEmpty()) {
                for (Iterator<SubscriptionDo> iterator = recomList.iterator(); iterator.hasNext();) {
                    SubscriptionDo subscriptionDo = iterator.next();
                    if (excludeSub.contains(subscriptionDo.getSubId())) {
                        continue;
                    }
                    //query recommand sub types
                    List<Integer> recSubTypes = querySubTypes(subscriptionDo.getSubId());
                    //filter sub type
                    for(Integer recType : recSubTypes){
                        if(subTypes.contains(recType)){
                            result.add(subscriptionDo.getSubId());
                            break;
                        }
                    }
                }
            }

            if (result.size() < moreCount) {
                List<SubscriptionDo> locSubs = copyOnWriteSubs(queryUserLocSubList(cid));

                if (locSubs != null && !locSubs.isEmpty()) {
                    for (Iterator<SubscriptionDo> iterator = locSubs.iterator(); iterator.hasNext();) {
                        SubscriptionDo subscriptionDo = iterator.next();
                        if (excludeSub.contains(subscriptionDo.getSubId())) {
                            continue;
                        }
                        if (result.contains(subscriptionDo.getSubId())) {
                            continue;
                        }
                        //query loc sub types
                        List<Integer> locSubTypes = querySubTypes(subscriptionDo.getSubId());
                        //filter sub type
                        for(Integer locType : locSubTypes){
                            if(subTypes.contains(locType)){
                                result.add(subscriptionDo.getSubId());
                                break;
                            }
                        }
                    }
                }
            } else {
                result = result.subList(0, moreCount);
            }
        }

        // 推荐热门
        recomHotSubsByRandom(moreCount, result, excludeSub);

        return result;
    }

    @Override
    public List<Integer> getSubscriptionRecom(long cid, int moreCount, String[] excludes, String[] hasSubs) {
        List<Integer> result = null;
        for (int times = 0; times < maxTimes; times++) {
            result = getSubscriptionRecomByRandom(cid, moreCount, excludes, hasSubs, times);
            if (result != null && !result.isEmpty()) {
                return result;
            }
            // clear memcached
            clearRecomSubShowList(cid);
            // clear user subscrition list
            clearUserSubList(cid);
        }
        return result;
    }

    @Override
    public List<Integer> getSubscriptionRecomByRandom(long cid, int moreCount, String[] excludes, String[] hasSubs,
                                                      int times) {
        /**
         * 最大调用次数
         */
        List<Integer> result = new ArrayList<Integer>();

        if (times >= maxTimes) {
            return result;
        }

        if (moreCount <= 0) {
            return result;
        }

        Set<Integer> excludeSub = new HashSet<Integer>();

        // 获取已经展示列表
        Set<Integer> showSubSet = queryRecomSubShowList(cid);
        excludeSub.addAll(showSubSet);

        // 用户订阅
        List<Integer> subList = queryUserSubList(cid);
        excludeSub.addAll(subList);

        // 添加接口过滤列表
        if (excludes != null && excludes.length > 0) {
            for (String id : excludes) {
                excludeSub.add(Integer.valueOf(id));
            }
        }

        // 分段划分推荐刊物类别：
        int sectionMode = moreCount % SUBSIZEPERSCREEN;
        int sections = sectionMode == 0 ? moreCount / SUBSIZEPERSCREEN : moreCount / SUBSIZEPERSCREEN + 1;
        Map<Integer, List<Integer>> resultMap = new HashMap<Integer, List<Integer>>();
        Map<Integer, Set<Integer>> typesMap = new HashMap<Integer, Set<Integer>>();
        // 每段包含刊物数目
        Map<Integer, Integer> sectionCounts = new HashMap<Integer, Integer>();
        for (int i = 0; i < sections; i++) {
            resultMap.put(i, new ArrayList<Integer>());
            typesMap.put(i, new HashSet<Integer>());
            // 最后一屏订阅推荐数量
            if (i == (sections - 1) && sectionMode != 0) {
                sectionCounts.put(i, sectionMode);
            } else {
                sectionCounts.put(i, SUBSIZEPERSCREEN);
            }
        }

        // 运营推荐
        recomOperationSubsByRandom(excludeSub, typesMap, sectionCounts, resultMap);
        LOGGER.info("after operation :" + resultMap);

        if (cid > 0) {

            // 地理位置推荐
            recomLocSubsByRandom(cid, excludeSub, typesMap, sectionCounts, resultMap);

            LOGGER.info("after recomLoc:" + cid + resultMap.toString());

            // 用户喜好频道推荐
            recomSubsByUserChannelLike(cid, excludeSub, typesMap, sectionCounts, resultMap);

            // 相关推荐
            recSubs(subList, excludeSub, typesMap, sectionCounts, resultMap);

            LOGGER.info("after recomRandom:" + cid + result.toString());

        }

        // 推荐热门
        recomHotSubs(moreCount, excludeSub, typesMap, sectionCounts, resultMap);

        LOGGER.info("after recomHot:" + cid + resultMap.toString());

        // 生成最终推荐结果
        for (int sectionId = 0; sectionId < resultMap.size(); sectionId++) {
            result.addAll(resultMap.get(sectionId));
        }

        if (result != null && result.size() > moreCount) {
            result = result.subList(0, moreCount);
        }
        // 存储展示列表
        putRecomSubShowList(cid, result);

        return result;
    }

    /**
     * 运营刊物推荐: 推荐数目：
     *
     * @param excludeSub
     * @param typesMap
     * @param sectionCounts
     * @param resultMap
     */
    private void recomOperationSubsByRandom(Set<Integer> excludeSub, Map<Integer, Set<Integer>> typesMap,
                                            Map<Integer, Integer> sectionCounts, Map<Integer, List<Integer>> resultMap) {
        List<Integer> operationSubs = copyOnWriteSubs(queryOperationSubs());
        if (operationSubs == null || operationSubs.isEmpty()) {
            return;
        }
        Random random = ThreadLocalUtils.getLocalRandom();
        int sections = resultMap.size();
        for (int sectionId = 0; sectionId < sections;) {
            if (operationSubs.isEmpty()) {
                break;
            }
            int index = random.nextInt(operationSubs.size());
            Integer subId = operationSubs.remove(index);
            if (excludeSub.contains(subId)) {
                continue;
            }
            // 每段分类添加
            List<Integer> subTypes = querySubTypes(subId);
            Set<Integer> typeSet = typesMap.get(sectionId);
            if (isSubTypeOverlap(subTypes, typeSet)) {
                continue;
            }
            // 分段还可以推荐刊物
            int leftCount = sectionCounts.get(sectionId);
            if (leftCount > 0) {
                sectionCounts.put(sectionId, leftCount - 1);
                // 添加分段订阅类型
                if (subTypes != null) {
                    typeSet.addAll(subTypes);
                }
                // 收集结果
                excludeSub.add(subId);
                resultMap.get(sectionId).add(subId);
            }
            sectionId++;
        }
    }

    /**
     * 相关刊物推荐
     *
     * @param excludeSub
     * @param typesMap
     * @param sectionCounts
     * @param resultMap
     * @param localSubList
     */
    private void recSubs(List<Integer> subList, Set<Integer> excludeSub, Map<Integer, Set<Integer>> typesMap,
                         Map<Integer, Integer> sectionCounts, Map<Integer, List<Integer>> resultMap) {
        if (subList == null || subList.isEmpty()) {
            return;
        }

        // 订阅推荐种子
        int recSeeds = Math.min(subList.size(), MAXSEED);

        Map<Integer, Double> recSubScore = new HashMap<Integer, Double>();
        for (int i = 0; i < recSeeds; i++) {
            List<SubscriptionDo> recSubList = copyOnWriteSubs(queryRecomSub(subList.get(i), MAXSEED * 5));
            if (recSubList == null || recSubList.isEmpty()) {
                continue;
            }
            for (SubscriptionDo subscriptionDo : recSubList) {
                if (excludeSub.contains(subscriptionDo.getSubId())) {
                    continue;
                }
                // 累加记分
                if (recSubScore.containsKey(subscriptionDo.getSubId())) {
                    double scoreSum = recSubScore.get(subscriptionDo.getSubId());
                    scoreSum += subscriptionDo.getScore();
                    recSubScore.put(subscriptionDo.getSubId(), scoreSum);
                } else {
                    recSubScore.put(subscriptionDo.getSubId(), subscriptionDo.getScore());
                }
            }
        }

        // 排序推荐列表
        List<Integer> sortSubList = MapSortUtil.sortMapByValue(recSubScore);
        for (int sectionId = 0; sectionId < resultMap.size(); sectionId++) {
            int sectionCount = sectionCounts.get(sectionId);
            if (sectionCount <= 0) {
                continue;
            } else {
                for (Iterator<Integer> iterator = sortSubList.iterator(); iterator.hasNext();) {
                    if (sectionCount <= 0) {
                        break;
                    }
                    Integer subId = iterator.next();
                    Set<Integer> typeSet = typesMap.get(sectionId);
                    List<Integer> subTypes = querySubTypes(subId);
                    if (isSubTypeOverlap(subTypes, typeSet)) {
                        continue;
                    }
                    // 添加分段订阅类型
                    if (subTypes != null) {
                        typeSet.addAll(subTypes);
                    }
                    // 添加结果
                    sectionCount--;
                    sectionCounts.put(sectionId, sectionCount);
                    resultMap.get(sectionId).add(subId);
                    excludeSub.add(subId);
                    iterator.remove();
                }
            }
        }
    }

    /**
     * 热门刊物推荐
     *
     * @param moreCount
     * @param excludeSub
     * @param typesMap
     * @param sectionCounts
     * @param resultMap
     */
    private void recomHotSubs(int moreCount, Set<Integer> excludeSub, Map<Integer, Set<Integer>> typesMap,
                              Map<Integer, Integer> sectionCounts, Map<Integer, List<Integer>> resultMap) {
        List<Integer> hotSubs = null;
        boolean noClassify = false;
        int totalCount = 0;
        for (Iterator<Integer> countIter = sectionCounts.values().iterator(); countIter.hasNext();) {
            Integer count = countIter.next();
            totalCount += count;
        }

        for (int sectionId = 0; sectionId < resultMap.size(); sectionId++) {
            int sectionCount = sectionCounts.get(sectionId);
            if (sectionCount <= 0) {
                continue;
            } else {
                if (hotSubs == null) {
                    hotSubs = new ArrayList<Integer>();
                    List<SubscriptionDo> hotSubscriptions = queryHotSubList();
                    for (SubscriptionDo subscriptionDo : hotSubscriptions) {
                        if (excludeSub.contains(subscriptionDo.getSubId())) {
                            continue;
                        }
                        hotSubs.add(subscriptionDo.getSubId());
                    }
                    // 热门数据少于 需要推荐数目 2倍时，不过来订阅类型
                    if (hotSubs.size() < totalCount * 2) {
                        noClassify = true;
                    }
                }
                // 添加结果
                for (Iterator<Integer> iterator = hotSubs.iterator(); iterator.hasNext();) {
                    if (sectionCount <= 0) {
                        break;
                    }
                    Integer subId = iterator.next();
                    Set<Integer> typeSet = typesMap.get(sectionId);
                    List<Integer> subTypes = querySubTypes(subId);
                    if (!noClassify && isSubTypeOverlap(subTypes, typeSet)) {
                        continue;
                    }
                    // 添加分段订阅类型
                    if (subTypes != null) {
                        typeSet.addAll(subTypes);
                    }
                    // 添加结果
                    sectionCount--;
                    sectionCounts.put(sectionId, sectionCount);
                    resultMap.get(sectionId).add(subId);
                    excludeSub.add(subId);
                    iterator.remove();
                }
            }
        }
    }

    private void recomHotSubsByRandom(int moreCount, List<Integer> result, Set<Integer> excludeSub) {
        if (result.size() < moreCount) {
            List<SubscriptionDo> hotSubList = copyOnWriteSubs(queryHotSubList());
            if (hotSubList != null && !hotSubList.isEmpty()) {
                Random random = ThreadLocalUtils.getLocalRandom();
                for (int i = 0; i < moreCount;) {
                    if (result.size() >= moreCount) {
                        break;
                    }
                    if (hotSubList.isEmpty()) {
                        break;
                    }
                    int index = random.nextInt(hotSubList.size());
                    SubscriptionDo subscriptionDo = hotSubList.get(index);
                    //
                    hotSubList.remove(index);
                    if (result.contains(subscriptionDo.getSubId())) {
                        continue;
                    }
                    if (excludeSub.contains(subscriptionDo.getSubId())) {
                        continue;
                    }
                    result.add(subscriptionDo.getSubId());
                    i++;
                }
            }
        }
    }

    /**
     * 随机用户地方刊物
     *
     * @param cid
     * @param excludeSub
     * @param typesMap
     * @param sectionCounts
     * @param resultMap
     */
    private void recomLocSubsByRandom(long cid, Set<Integer> excludeSub, Map<Integer, Set<Integer>> typesMap,
                                      Map<Integer, Integer> sectionCounts, Map<Integer, List<Integer>> resultMap) {
        List<SubscriptionDo> locSubs = copyOnWriteSubs(queryUserLocSubList(cid));
        if (locSubs == null || locSubs.isEmpty()) {
            return;
        }

        int sections = resultMap.size();

        Random random = ThreadLocalUtils.getLocalRandom();
        for (int sectionId = 0; sectionId < sections;) {
            if (locSubs.isEmpty()) {
                break;
            }
            int index = random.nextInt(locSubs.size());
            SubscriptionDo subscriptionDo = locSubs.remove(index);

            Integer locsubId = subscriptionDo.getSubId();
            if (excludeSub.contains(locsubId)) {
                continue;
            }
            List<Integer> subTypes = querySubTypes(locsubId);
            Set<Integer> typeSet = typesMap.get(sectionId);
            if (isSubTypeOverlap(subTypes, typeSet)) {
                continue;
            }

            // 分段刊物推荐数据
            int leftCount = sectionCounts.get(sectionId);
            if (leftCount > 0) {
                sectionCounts.put(sectionId, leftCount - 1);
                // 添加类型
                if (subTypes != null) {
                    typeSet.addAll(subTypes);
                }

                // 添加到 已推荐数据
                resultMap.get(sectionId).add(locsubId);
                excludeSub.add(locsubId);
            }
            sectionId++;
        }

    }

    /**
     * 根据用户喜欢频道推荐
     *
     * @param cid
     * @param excludeSub
     * @param typesMap
     * @param sectionCounts
     * @param resultMap
     */
    private void recomSubsByUserChannelLike(long cid, Set<Integer> excludeSub, Map<Integer, Set<Integer>> typesMap,
                                            Map<Integer, Integer> sectionCounts, Map<Integer, List<Integer>> resultMap) {

        List<Integer> channelRecResult = queryChannelRecSubList(cid);
        // 推荐刊物
        int sectionSize = sectionCounts.size();
        int sectionId = 0;
        for (Integer subId : channelRecResult) {
            // 已完成本次推荐
            if (sectionId >= sectionSize) {
                break;
            }

            // 过滤已推荐刊物
            if (excludeSub.contains(subId)) {
                continue;
            }

            // 过滤同类型刊物
            List<Integer> subTypes = querySubTypes(subId);
            Set<Integer> typeSet = typesMap.get(sectionId);
            if (isSubTypeOverlap(subTypes, typeSet)) {
                continue;
            }

            // 添加推荐刊物
            int leftCount = sectionCounts.get(sectionId);
            if (leftCount > 0) {
                sectionCounts.put(sectionId, leftCount - 1);
                // 添加类型
                if (subTypes != null) {
                    typeSet.addAll(subTypes);
                }

                // 添加到 已推荐数据
                resultMap.get(sectionId).add(subId);
                excludeSub.add(subId);
            }
            sectionId++;
        }
    }

    /**
     * 根据用户ID 获取用户喜欢频道推荐刊物
     * @param cid
     * @return
     */
    private List<Integer> queryChannelRecSubList(long cid) {

        if (cid <= 0) {
            return Collections.emptyList();
        }
        //读取缓存结果
        String userKey = String.format(MemcachedKeys.UserChannelSubList.getKey(), cid);
        String cache = (String) memcachedClient.get(userKey);
        List<Integer> channelRecResult = Collections.emptyList();

        if (StringUtils.isNotBlank(cache)) {
            if (StringUtils.equalsIgnoreCase("-1", cache)) {
                return Collections.emptyList();
            } else {
                channelRecResult = ConvertUtils.fromJsonJkGeneric(cache, new TypeReference<ArrayList<Integer>>() {
                });
                return channelRecResult;
            }
        }

        //缓存失效 or 无缓存
        String jsonString = edbTemplate.getString(EdbKeyConstants.USER_CHANNEL, String.valueOf(cid));
        if (StringUtils.isNotBlank(jsonString)) {
            Map<Integer, Double> userChannelLike = JSON.parseObject(jsonString, new com.alibaba.fastjson.TypeReference<LinkedHashMap<Integer, Double>>(){});

            Map<Integer, Double> channelRecMap = new HashMap<Integer, Double>();
            // 频道喜好 计算推荐刊物权重(Sub, score)
            if (userChannelLike != null && !userChannelLike.isEmpty()) {
                for (Integer channelId : userChannelLike.keySet()) {
                    String channelKey = String.format(RedisKeyConstants.CHANNEL_SUB_KEYS, channelId);
                    List<String> subList = redisTemplateRecom.opsForList().range(channelKey, 0, -1);
                    if (subList != null && !subList.isEmpty()) {
                        for (String subId : subList) {
                            Integer subIdValue = Integer.valueOf(subId);
                            if (channelRecMap.containsKey(subIdValue)) {
                                Double value = userChannelLike.get(channelId) + channelRecMap.get(subIdValue);
                                channelRecMap.put(subIdValue, value);
                            } else {
                                channelRecMap.put(subIdValue, userChannelLike.get(channelId));
                            }
                        }
                    }
                }
            }

            // 排序推荐刊物
            channelRecResult = MapSortUtil.sortMapByValue(channelRecMap);

        }
        // 缓存用户喜欢频道推荐结果
        if (channelRecResult != null && !channelRecResult.isEmpty()) {
            String value = ConvertUtils.toJsonJk(channelRecResult);
            memcachedClient.set(userKey, MemcachedKeys.UserChannelSubList.getExpireTime(), value);
        } else {
            memcachedClient.set(userKey, MemcachedKeys.UserChannelSubList.getExpireTime(), "-1");
        }
        return channelRecResult;
    }

    /**
     * 判断刊物与当前段数据是否属于同类型
     *
     * @param subId
     * @param types
     * @return
     */
    private boolean isSubTypeOverlap(List<Integer> subTypes, Set<Integer> typeSet) {
        if (subTypes == null || subTypes.isEmpty()) {
            return false;
        }

        for (Integer subType : subTypes) {
            if (typeSet.contains(subType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * shallow copy 刊物推荐列表
     *
     * @param source
     * @return
     */
    private <T> List<T> copyOnWriteSubs(List<T> source) {
        if (source == null || source.isEmpty()) {
            return source;
        }
        List<T> result = new ArrayList<T>();
        result.addAll(source);
        return result;
    }

    private List<SubscriptionDo> queryRecomSub(Integer subId, int size) {
        List<SubscriptionDo> subscriptionDos = SubscribeManager.getRecomSub().get(subId);
        if (subscriptionDos != null) {
            return subscriptionDos;
        }
        String subKey = String.format(RedisKeyConstants.REC_SUB_KEYS, subId);
        List<String> recSubList = redisTemplateRecom.opsForList().range(subKey, 0, size);
        if (recSubList != null && !recSubList.isEmpty()) {
            subscriptionDos = ConvertUtils.fromJsonJkGeneric(recSubList.toString(),
                                                             new TypeReference<List<SubscriptionDo>>() {
                                                             });
            SubscribeManager.getRecomSub().put(subId, subscriptionDos);
            // copy cache list to result data : should not modify list
            return subscriptionDos;
        } else {
            SubscribeManager.getRecomSub().put(subId, Collections.<SubscriptionDo> emptyList());
            return null;
        }
    }

    /**
     * @param cid
     * @return
     */
    private List<Integer> queryUserSubList(long cid) {
        if (cid <= 0) {
            return Collections.<Integer> emptyList();
        }
        List<Integer> subList = SubscribeManager.getUserSub().get(cid);
        if (subList != null) {
            return subList;
        }

        String userKey = String.format(RedisKeyConstants.USER_SUBSCRIPTION_KEY, cid);
        // 过滤default
        if (redisTemplateBase.getExpire(userKey) > 0) {
            List<String> subListValue = redisTemplateBase.opsForList().range(userKey, 0, -1);
            if (subListValue != null && !subListValue.isEmpty()) {
                subList = ConvertUtils.fromJsonJkGeneric(subListValue.toString(), new TypeReference<List<Integer>>() {
                });
            }
            SubscribeManager.getUserSub().put(cid, subList);
            return subList;
        } else {
            int productId = 1;
            subList = subscriptionDao.queryUserSubscribeList(cid, productId);
            List<String> subListValue = new ArrayList<String>();

            if (subList != null && !subList.isEmpty()) {
                // 过滤无效刊物
                Map<Integer, String> subscriptionInfoMap = subscriptionDao.listSubscibeInfo(subList);
                if (subscriptionInfoMap != null && !subscriptionInfoMap.isEmpty()) {
                    for (Iterator<Integer> iterator = subList.iterator(); iterator.hasNext();) {
                        Integer subId = iterator.next();
                        if (subscriptionInfoMap.containsKey(subId)) {
                            subListValue.add(subId.toString());
                        } else {
                            // 过滤无效刊物
                            iterator.remove();
                        }
                    }
                }
            }

            // 存储 用户订阅列表
            if (subListValue != null && !subListValue.isEmpty()) {
                redisTemplateBase.opsForBatch().rightPush(userKey, subListValue);
                SubscribeManager.getUserSub().put(cid, subList);
            } else {
                redisTemplateBase.opsForList().rightPush(userKey, "-1");
                SubscribeManager.getUserSub().put(cid, Collections.<Integer> emptyList());
            }
            redisTemplateBase.expire(userKey, ExpireTimeConstants.USER_SUB_EXP_TIME, TimeUnit.SECONDS);
        }
        return subList;
    }

    private List<Integer> queryDefaultSubList() {
        List<Integer> defaultList = SubscribeManager.getDefaultSub().get(RedisKeyConstants.DEFAULT_SUB_KEY);

        if (defaultList != null) {
            return defaultList;
        }

        if (redisTemplateBase.getExpire(RedisKeyConstants.DEFAULT_SUB_KEY) > 0) {
            List<String> defaultValueList = redisTemplateBase.opsForList().range(RedisKeyConstants.DEFAULT_SUB_KEY, 0,
                                                                                 -1);
            if (defaultValueList != null && !defaultValueList.isEmpty()) {
                defaultList = ConvertUtils.fromJsonJkGeneric(defaultValueList.toString(),
                                                             new TypeReference<List<Integer>>() {
                                                             });
            }
            SubscribeManager.getDefaultSub().put(RedisKeyConstants.DEFAULT_SUB_KEY, defaultList);
            return defaultList;
        } else {
            defaultList = subscriptionDao.queryDefaultSubscriptionList();
            List<String> defaultValueList = new ArrayList<String>();
            if (defaultList != null && !defaultList.isEmpty()) {
                for (Integer subId : defaultList) {
                    defaultValueList.add(subId.toString());
                }
                redisTemplateBase.opsForBatch().rightPush(RedisKeyConstants.DEFAULT_SUB_KEY, defaultValueList);
                SubscribeManager.getDefaultSub().put(RedisKeyConstants.DEFAULT_SUB_KEY, defaultList);
            } else {// 防止频繁查询数据库
                redisTemplateBase.opsForList().rightPush(RedisKeyConstants.DEFAULT_SUB_KEY, "-1");
                SubscribeManager.getDefaultSub().put(RedisKeyConstants.DEFAULT_SUB_KEY,
                                                     Collections.<Integer> emptyList());
            }
            redisTemplateBase.expire(RedisKeyConstants.DEFAULT_SUB_KEY, ExpireTimeConstants.DEFAULT_SUB_EXP_TIME,
                                     TimeUnit.SECONDS);
        }
        return defaultList;
    }

    private List<SubscriptionDo> queryUserLocSubList(long cid) {
        String userLoc = String.format(RedisKeyConstants.USER_LOCATION_KEY, cid);
        if (shardedRedisTemplateUser.hasKey(userLoc)) {
            String cityCode = shardedRedisTemplateUser.opsForValue().get(userLoc);
            if (StringUtils.isNumeric(cityCode) && cityCode.length() > 2) {
                String area = StringUtils.substring(cityCode, 0, 2);
                return queryLocationSub(area);
            }
        }
        return null;
    }

    private List<SubscriptionDo> queryLocationSub(String area) {
        // 本地缓存数据
        List<SubscriptionDo> subscriptionDos = SubscribeManager.getLocationSub().get(area);
        // copy local cache to return;
        if (subscriptionDos != null) {
            return subscriptionDos;
        }

        String locSubKey = String.format(RedisKeyConstants.LOC_SUB_KEYS, area);
        List<String> locSubs = redisTemplateRecom.opsForList().range(locSubKey, 0, -1);
        if (locSubs != null && !locSubs.isEmpty()) {
            subscriptionDos = ConvertUtils.fromJsonJkGeneric(locSubs.toString(),
                                                             new TypeReference<List<SubscriptionDo>>() {
                                                             });
            SubscribeManager.getLocationSub().put(area, subscriptionDos);
            return subscriptionDos;
        } else {
            SubscribeManager.getLocationSub().put(area, Collections.<SubscriptionDo> emptyList());
            return null;
        }
    }

    private List<SubscriptionDo> queryHotSubList() {
        List<SubscriptionDo> subscriptionDos = SubscribeManager.getHotSub().get(RedisKeyConstants.HOT_SUB_KEYS);

        if (subscriptionDos != null) {
            return subscriptionDos;
        }

        List<String> hotSubList = redisTemplateRecom.opsForList().range(RedisKeyConstants.HOT_SUB_KEYS, 0, -1);
        if (hotSubList != null && !hotSubList.isEmpty()) {
            subscriptionDos = ConvertUtils.fromJsonJkGeneric(hotSubList.toString(),
                                                             new TypeReference<List<SubscriptionDo>>() {
                                                             });
            SubscribeManager.getHotSub().put(RedisKeyConstants.HOT_SUB_KEYS, subscriptionDos);
            return subscriptionDos;
        } else {
            SubscribeManager.getHotSub().put(RedisKeyConstants.HOT_SUB_KEYS, Collections.<SubscriptionDo> emptyList());
        }
        return null;
    }

    /**
     * 查选热门刊物
     *
     * @return
     */
    private List<Integer> queryOperationSubs() {
        List<Integer> subIds = SubscribeManager.getOperationSubs().get("opSub");
        if (subIds != null) {
            return subIds;
        }

        // 查询数据库
        synchronized (SubscribeManager.getOperationSubs()) {
            subIds = SubscribeManager.getOperationSubs().get("opSub");
            if (subIds != null) {
                return subIds;
            }
            subIds = subscriptionDao.listOperationSubs();
            if (subIds != null && !subIds.isEmpty()) {
                SubscribeManager.getOperationSubs().put("opSub", subIds);
                return subIds;
            } else {
                SubscribeManager.getOperationSubs().put("opSub", Collections.<Integer> emptyList());
            }
        }
        return Collections.<Integer> emptyList();
    }

    /**
     * 查询刊物类型
     *
     * @param subId
     * @return
     */
    public List<Integer> querySubTypes(Integer subId) {
        List<Integer> typeIds = SubscribeManager.getSubTypes().get(subId);
        if (typeIds != null) {
            return typeIds;
        }

        //
        synchronized (SubscribeManager.getSubTypes()) {
            typeIds = SubscribeManager.getSubTypes().get(subId);
            if (typeIds != null) {
                return typeIds;
            }
            List<SubscriptionType> subTypes = subscriptionDao.listSubscriptionType(subId);
            if (subTypes == null || subTypes.isEmpty()) {
                SubscribeManager.getSubTypes().put(subId, Collections.<Integer> emptyList());
                return Collections.<Integer> emptyList();
            } else {
                typeIds = new ArrayList<Integer>();
                for (SubscriptionType subscriptionType : subTypes) {
                    typeIds.add(subscriptionType.getTypeId());
                }
                SubscribeManager.getSubTypes().put(subId, typeIds);
                return typeIds;
            }
        }
    }

    /**
     * 查选摇一摇已经出现刊物列表
     *
     * @param cid
     * @return
     */
    private Set<Integer> queryRecomSubShowList(long cid) {
        String key = String.format(MemcachedKeys.RecomSubList.getKey(), cid);
        String value = (String) memcachedClient.get(key);
        if (value == null || value.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Integer> showSubList = ConvertUtils.fromJsonJkGeneric(value, new TypeReference<Set<Integer>>() {
        });
        return showSubList;
    }

    /**
     * 存储已经出现刊物列表
     *
     * @param cid
     */
    private void putRecomSubShowList(long cid, List<Integer> recomList) {
        if (recomList == null || recomList.isEmpty()) {
            return;
        }
        String key = String.format(MemcachedKeys.RecomSubList.getKey(), cid);
        String value = (String) memcachedClient.get(key);
        Set<Integer> recomSet = new HashSet<Integer>(recomList);
        if (value != null && !value.isEmpty()) {
            Set<Integer> showSubSet = ConvertUtils.fromJsonJkGeneric(value, new TypeReference<Set<Integer>>() {
            });
            recomSet.addAll(showSubSet);
        }
        value = recomSet.toString();
        memcachedClient.set(key, MemcachedKeys.RecomSubList.getExpireTime(), value);
    }

    /**
     * 清空缓存
     *
     * @param cid
     */
    private void clearRecomSubShowList(long cid) {
        String key = String.format(MemcachedKeys.RecomSubList.getKey(), cid);
        memcachedClient.delete(key);
    }

    /**
     * 清空用户已订阅缓存
     *
     * @param cid
     */
    private void clearUserSubList(long cid) {
        SubscribeManager.getUserSub().remove(cid);
        String userKey = String.format(RedisKeyConstants.USER_SUBSCRIPTION_KEY, cid);
        redisTemplateBase.delete(userKey);
    }

}
