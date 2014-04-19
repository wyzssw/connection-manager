package com.wap.sohu.recom.cache.manager;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wap.sohu.mobilepaper.model.pic.GroupPicInfo;
import com.wap.sohu.recom.cache.core.AbstractCacheManager;
import com.wap.sohu.recom.cache.core.ICache;
import com.wap.sohu.recom.model.GroupPicInfoSerial;

/**
 * 类GroupCacheManager.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2012-7-27 下午03:06:57
 */
public class GroupCacheManager {
    /** 组图category本地缓存 */
    private static final ICache<Integer, Integer> GROUPPIC_CAT_CACHE = AbstractCacheManager
            .getInstance().getCache("groupPicCatCache", 2000, 60 * 60);   

    /** 热门推荐本地缓存 */
    private static final ICache<String, List<GroupPicInfo>> HOT_GROUPPIC_CACHE = AbstractCacheManager
            .getInstance().getCache("hotGroupPicCache", 200, 60 * 60);
    
    /**本地缓存redis中存放的删除的组图，只存5秒钟，防止同一请求 请求多次该key对应的信息  */
    private static final ICache<String, Set<Integer>> GROUP_DEL_SET = AbstractCacheManager.getInstance().getCache("group_del_set", 5000, 5);
    
    /**最近几个小时的组图  */
    private static final ICache<String, Set<Integer>> LAST_HOUR_GROUP = AbstractCacheManager.getInstance().getCache("group_last_hour", 5000, 60*60);
    
    /**组图分数缓存 */
    private static final ICache<Integer, Map<Integer, Double>> GROUP_SCORE_CACHE = AbstractCacheManager.getInstance().getCache("group_score_cache",10000,60*60);
    
    private static final ICache<Integer,List<Integer>>         GROUP_SIM_CACHE    = AbstractCacheManager.getInstance().getCache("group_sim_cache",30000,60*60*3);
    
    /**本地组图缓存 */
    private static final ICache<Integer, GroupPicInfoSerial>   GROUP_INFO_CACHE   = AbstractCacheManager.getInstance().getCache("group_info_cache",30000,60*60*6); 
    
    private static final ICache<String, Set<Integer>>   GROUP_DEL_THOSE_DAYS = AbstractCacheManager.getInstance().getCache("group_del_those_days", 5000,60*60);
    
    /**组图类别缓存 */ 
    private static final ICache<String, List<Integer>> GROUP_CATEGORY_CACHE  = AbstractCacheManager.getInstance().getCache("group_category_cache",50,60*60*12);
    
    
    private static final ICache<Integer, Map<Integer, Double>> GROUPNEWS_SIM_GROUP =   AbstractCacheManager.getInstance().getCache("groupnews_sim_group",2000,60*5);
    
    
    private static final ICache<Integer, Map<Integer, Double>>  GROUPNEWS_SIM_NEWS =    AbstractCacheManager.getInstance().getCache("groupnews_sim_news",3000,60*5);
    
    public static ICache<Integer, Map<Integer, Double>>  getGroupNewsSimNews(){
        return GROUPNEWS_SIM_NEWS;
    }
    
    public static ICache<Integer, Map<Integer, Double>> getGroupNewsSimGroup(){
        return GROUPNEWS_SIM_GROUP;
    }
    
    public static ICache<String, List<Integer>>  getGroupAllCatCache(){
        return GROUP_CATEGORY_CACHE;
    }
    
    public static ICache<String, Set<Integer>>  getGroupDelThoseDays(){
        return GROUP_DEL_THOSE_DAYS;
    }
    
    public static ICache<Integer, GroupPicInfoSerial> getGroupInfoCache(){
        return GROUP_INFO_CACHE;
    }
    
    
    public static ICache<Integer, Map<Integer, Double>> getGroupScore(){
        return GROUP_SCORE_CACHE;
    }
    
    
    public static ICache<String, Set<Integer>> getLastHourGroup(){
        return LAST_HOUR_GROUP;
    }
    
    
    public static ICache<String, List<GroupPicInfo>> getHotGroupCache(){
         return HOT_GROUPPIC_CACHE;
    }
    
    public static ICache<Integer, Integer> getGroupCatCache(){
        return GROUPPIC_CAT_CACHE;
    }
    
    public static ICache<String, Set<Integer>> getGroupDelCache(){
        return GROUP_DEL_SET;
    }


    /**
     * @return
     */
    public static ICache<Integer, List<Integer>> getSimGroup() {
        return GROUP_SIM_CACHE;
    }
} 
