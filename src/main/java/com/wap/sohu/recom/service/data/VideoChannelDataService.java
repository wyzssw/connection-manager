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
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.model.FilterBuild;
import com.wap.sohu.recom.utils.ConvertUtils;

@Service("videoChannelDataService")
public class VideoChannelDataService implements ChannelNewsDataService {


    private static final int cycleCount = 2;

    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateRecom;

    public static final String key = String.format(CommonConstants.CHANNEL_SEPARATE_ZSET, CommonConstants.VIDEO_CHANNEL_ID);

    public static final String editorNewsKey = String.format(CommonConstants.EDITOR_CHANNEL_NEWS, CommonConstants.VIDEO_CHANNEL_ID);

    @PostConstruct
    public void init(){
        ChannelDataServiceRegister.registerService( CommonConstants.VIDEO_CHANNEL_ID, this);
    }

    @Override
    public Map<Integer, String> queryRecomData(long cid, long pid, int count, FilterBuild filterbuild) {
        //filter  editor IT channel news
        filterbuild.getFilterNews().addAll(getEditorNews());
        count = 2;
        Map<Integer,String> resultMap = new LinkedHashMap<Integer,String>();
        for (int cycleIndex = 1, resultCount = 0,completed=0; resultCount < count && cycleIndex <= cycleCount && completed==0; cycleIndex++) {
            Set<Integer> set = getRecommendData(key,(cycleIndex-1)*100,cycleIndex*100);
            if (set==null||set.isEmpty()) {
                break;
            }
            for (Integer newsId : set) {
                if (filterbuild.checkNewsInvalid(newsId)) {
                    continue;
                }
                resultMap.put(newsId, CommonConstants.HOT_TYPE);
                filterbuild.addNews(cid, newsId);
                resultCount++;
                if (resultCount>=count) {
                    completed = 1;
                    break;
                }
            }
        }
        return resultMap;
    }

    private Set<Integer> getRecommendData(String key, int start,int count){
        ICache<String, Set<Integer>> iCache =  TopNewsCacheManager.getVedioChannelNews();
        Set<Integer> resultSet = null;
        Set<String> set=null;
        if ((resultSet=iCache.get(key))==null) {
           resultSet = (resultSet==null?new LinkedHashSet<Integer>():resultSet);
           set= shardedRedisTemplateRecom.opsForZSet().reverseRange(key,0,3000);
            if (set == null||set.isEmpty()) {
                return null;
            }
           Set<Integer> moreSet = ConvertUtils.convert2intList(set);
           resultSet.addAll(moreSet);
           iCache.put(key, resultSet);
        }
        return resultSet;
    }

    private Set<Integer> getEditorNews(){
        ICache<String, Set<Integer>> iCache = TopNewsCacheManager.getVideoEditorChannelNews();
        Set<Integer> set = Collections.emptySet();
        if ((set=iCache.get(editorNewsKey))==null) {
             Set<String> setTmp = shardedRedisTemplateRecom.opsForZSet().range(editorNewsKey, 0, -1);
             set = ConvertUtils.convert2intList(setTmp);
             iCache.put(editorNewsKey,set);
        }
        return set;
    }

}
