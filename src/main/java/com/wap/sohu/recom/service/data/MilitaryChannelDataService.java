package com.wap.sohu.recom.service.data;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wap.sohu.recom.cache.core.ICache;
import com.wap.sohu.recom.cache.manager.TopNewsCacheManager;
import com.wap.sohu.recom.constants.CommonConstants;
import com.wap.sohu.recom.constants.TopNewsRedisKeyConstants;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.model.FilterBuild;
import com.wap.sohu.recom.utils.ConvertUtils;
import com.wap.sohu.recom.utils.LocalityFilterUtils;

/**
 * 类MilitaryChannelDataService.java的实现描述：TODO 类实现描述
 *
 * @author yeyanchao Sep 23, 2013 11:31:09 AM
 */
@Service("militaryChannelDataService")
public class MilitaryChannelDataService implements ChannelNewsDataService {

    private static final int       cycleCount = 2;

    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateRecom;

    public static final String     key        = String.format(CommonConstants.CHANNEL_SEPARATE_ZSET,
                                                              CommonConstants.MILITARY_CHANNEL_ID);

    @Autowired
    private LocalityFilterUtils    localityFilterUtils;

    @PostConstruct
    public void init() {
        ChannelDataServiceRegister.registerService(CommonConstants.MILITARY_CHANNEL_ID, this);
    }

    @Override
    public Map<Integer, String> queryRecomData(long cid, long pid, int count, FilterBuild filterbuild) {
        Map<Integer, String> resultMap = new LinkedHashMap<Integer, String>();
        if (localityFilterUtils.isNotInBeijing(cid)) {
            // filter editor IT channel news
            filterbuild.getFilterNews().addAll(getMilitaryEditorNews());
            count = 3;
            for (int cycleIndex = 1, resultCount = 0, completed = 0; resultCount < count && cycleIndex <= cycleCount
                    && completed == 0; cycleIndex++) {
                Set<Integer> set = getTVData(key, (cycleIndex - 1) * 100, cycleIndex * 100);
                if (set == null || set.isEmpty()) {
                    break;
                }
                for (Integer newsId : set) {
                    if (filterbuild.checkNewsInvalid(newsId)) {
                        continue;
                    }
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

    private Set<Integer> getTVData(String key, int start, int count) {
        ICache<String, Set<Integer>> iCache = TopNewsCacheManager.getMilitaryChannelNews();
        Set<Integer> resultSet = null;
        Set<String> set = null;
        if ((resultSet = iCache.get(key)) == null) {
            resultSet = (resultSet == null ? new LinkedHashSet<Integer>() : resultSet);
            set = shardedRedisTemplateRecom.opsForZSet().reverseRange(key, 0, 1000);
            if (set == null || set.isEmpty()) {
                return null;
            }
            Set<Integer> moreSet = ConvertUtils.convert2intList(set);
            resultSet.addAll(moreSet);
            iCache.put(key, resultSet);
        }
        return resultSet;
        // if (resultSet.size()<start) {
        // return null;
        // }
        // List<Integer> list = new ArrayList<Integer>(resultSet);
        // return new LinkedHashSet<Integer>(list.subList(start, count>resultSet.size()?resultSet.size():count));
    }

    private Set<Integer> getMilitaryEditorNews() {
        ICache<String, Set<Integer>> iCache = TopNewsCacheManager.getMilitaryEditorChannelNews();
        Set<Integer> set = Collections.emptySet();
        if ((set = iCache.get(TopNewsRedisKeyConstants.EDITED_MILITARYCHANNEL_NEWS)) == null) {
            Set<String> setTmp = shardedRedisTemplateRecom.opsForZSet().range(TopNewsRedisKeyConstants.EDITED_MILITARYCHANNEL_NEWS,
                                                                              0, -1);
            set = ConvertUtils.convert2intList(setTmp);
            iCache.put(TopNewsRedisKeyConstants.EDITED_MILITARYCHANNEL_NEWS, set);
        }
        return set;
    }
}
