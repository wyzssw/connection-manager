package com.wap.sohu.recom.cache.manager;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.wap.sohu.recom.cache.core.AbstractCacheManager;
import com.wap.sohu.recom.cache.core.ICache;
import com.wap.sohu.recom.constants.TopNewsRedisKeyConstants;
import com.wap.sohu.recom.service.data.FinanceChannelNewsDataService;
import com.wap.sohu.recom.service.data.HouseChannelDataService;
import com.wap.sohu.recom.service.data.ITChannelNewsDataService;
import com.wap.sohu.recom.service.data.InternationalChannelDataService;
import com.wap.sohu.recom.service.data.MilitaryChannelDataService;
import com.wap.sohu.recom.service.data.SportsChannelDataService;
import com.wap.sohu.recom.service.data.VideoChannelDataService;

/**
 * 类TopNewsCacheManager.java的实现描述：TODO 类实现描述
 *
 * @author yeyanchao Jun 19, 2013 2:57:45 PM
 */
public class TopNewsCacheManager {

    /**
     * category top news cache
     */
    private static final ICache<Integer, List<Integer>> CAT_TOPNEWS_CACHE     = AbstractCacheManager.getInstance().getCache("catTopNews",
                                                                                                                            5000,
                                                                                                                            5 * 60);

    private static final ICache<Integer, List<Integer>> CAT_SELECTEDNEWS_CACHE     = AbstractCacheManager.getInstance().getCache("catSeletedNews",
                                                                                                                            500,
                                                                                                                            5 * 60);

    private static final ICache<Integer, Set<Integer>> CAT_TAONEWS_CACHE     = AbstractCacheManager.getInstance().getCache("catTaoNews",
                                                                                                                            5000,
                                                                                                                            8 * 60);
    private static final ICache<String, Set<Integer>>   MAIN_CHANNEL_HOT_NEWS = AbstractCacheManager.getInstance().getCache("main_channel_hot_news",
                                                                                                                            200,
                                                                                                                            8 * 60);
    private static final ICache<String, Set<Integer>>   EDITED_TOUTIAO_NEWS   = AbstractCacheManager.getInstance().getCache("editedToutiaoNews",
                                                                                                                            4,
                                                                                                                            5 * 60);

    private static final ICache<String, Set<Integer>>   MAIN_CHANNEL_EDITED_NEWS   = AbstractCacheManager.getInstance().getCache("main_channel_edited_news",
                                                                                                                            4,
                                                                                                                            5 * 60);


    private static final ICache<String, Set<Integer>>   FINANCE_CHANNEL_NEWS   = AbstractCacheManager.getInstance().getCache(FinanceChannelNewsDataService.key,
                                                                                                                                 200,
                                                                                                                                 5 * 60);

    private static final ICache<String, Set<Integer>>   IT_CHANNEL_NEWS   = AbstractCacheManager.getInstance().getCache(ITChannelNewsDataService.key,
                                                                                                                             200,
                                                                                                                             20 * 60);

    private static final ICache<String, Set<Integer>>   IT_ZIMEITI_CHANNEL_NEWS   = AbstractCacheManager.getInstance().getCache(ITChannelNewsDataService.zimeitiKey,
                                                                                                                        200,
                                                                                                                        5 * 60);

    private static final ICache<String, Set<Integer>>   FINANCE_EDITOR_CHANNEL_NEWS   = AbstractCacheManager.getInstance().getCache(TopNewsRedisKeyConstants.EDITED_FINANCECHANNEL_NEWS,
                                                                                                                             200,
                                                                                                                             5 * 60);

    private static final ICache<String, Set<Integer>>   IT_EDITOR_CHANNEL_NEWS   = AbstractCacheManager.getInstance().getCache(TopNewsRedisKeyConstants.EDITED_ITCHANNEL_NEWS,
                                                                                                                                    200,
                                                                                                                                    5 * 60);

    private static final ICache<String, Set<Integer>>   ENJOY_CHANNEL_NEWS  = AbstractCacheManager.getInstance().getCache("channel_separate_zset_2",4,5*60);

    private static final ICache<String, Set<Integer>>   ENJOY_EDITOR_CHANNEL_NEWS = AbstractCacheManager.getInstance().getCache("enjoy_editor_channel_news", 4, 5*60);

    private static final ICache<String, Set<Integer>>   CAR_CHANNEL_NEWS   = AbstractCacheManager.getInstance().getCache("channel_separate_zset_11",2,15*60);

    private static final ICache<String, Set<Integer>>   CAR_EDITOR_CHANNEL_NEWS   = AbstractCacheManager.getInstance().getCache("car_editor_channel_news",2,5*60);

    private static final ICache<String, Set<Integer>>   TV_CHANNEL_NEWS   = AbstractCacheManager.getInstance().getCache("channel_separate_zset_30",2,15*60);
    private static final ICache<String, Set<Integer>>   TV_EDITOR_CHANNEL_NEWS   = AbstractCacheManager.getInstance().getCache("tv_editor_channel_news",2,5*60);

    private static final ICache<String, Set<Integer>>   MILITARY_CHANNEL_NEWS   = AbstractCacheManager.getInstance().getCache(MilitaryChannelDataService.key,50,5*60);
    private static final ICache<String, Set<Integer>>   MILITARY_EDITOR_CHANNEL_NEWS   = AbstractCacheManager.getInstance().getCache(TopNewsRedisKeyConstants.EDITED_MILITARYCHANNEL_NEWS,50,5*60);

    private static final ICache<Integer, Integer>       CAT_TOP_CATID  = AbstractCacheManager.getInstance().getCache("cat_top_catid",2000,60*60*12);

    private static final ICache<String, Set<Integer>>   INTERNATIONAL_CHANNEL_NEWS   = AbstractCacheManager.getInstance().getCache(InternationalChannelDataService.key,50,5*60);
    private static final ICache<String, Set<Integer>>   INTERNATIONAL_EDITOR_CHANNEL_NEWS   = AbstractCacheManager.getInstance().getCache(InternationalChannelDataService.editorNewsKey,50,5*60);

    private static final ICache<String, Set<Integer>>   VIDEO_CHANNEL_NEWS   = AbstractCacheManager.getInstance().getCache(VideoChannelDataService.key,50,5*60);
    private static final ICache<String, Set<Integer>>   VIDEO_EDITOR_CHANNEL_NEWS   = AbstractCacheManager.getInstance().getCache(VideoChannelDataService.editorNewsKey,50,5*60);

    private static final ICache<String, Set<Integer>>   SPORTS_CHANNEL_NEWS   = AbstractCacheManager.getInstance().getCache(SportsChannelDataService.key,50,5*60);
    private static final ICache<String, Set<Integer>>   SPORTS_EDITOR_CHANNEL_NEWS   = AbstractCacheManager.getInstance().getCache(SportsChannelDataService.editorNewsKey,50,5*60);

    private static final ICache<String, Set<Integer>>   HOUSE_CHANNEL_NEWS   = AbstractCacheManager.getInstance().getCache(HouseChannelDataService.key,50,5*60);
    private static final ICache<String, Set<Integer>>   HOUSE_EDITOR_CHANNEL_NEWS   = AbstractCacheManager.getInstance().getCache(HouseChannelDataService.editorNewsKey,50,5*60);


    public static ICache<Integer, Integer> getCatTopCatId(){
           return CAT_TOP_CATID;
    }

    /**
     * @return the militaryChannelNews
     */
    public static ICache<String, Set<Integer>> getMilitaryChannelNews() {
        return MILITARY_CHANNEL_NEWS;
    }

    /**
     * @return the militaryEditorChannelNews
     */
    public static ICache<String, Set<Integer>> getMilitaryEditorChannelNews() {
        return MILITARY_EDITOR_CHANNEL_NEWS;
    }
    /**
     * @return
     */
    public static final ICache<String, Set<Integer>> getTVEditorChannelNews() {
       return TV_EDITOR_CHANNEL_NEWS;
    }
    /**
     * @return
     */
    public static final ICache<String, Set<Integer>> getCarEditorChannelNews() {
       return CAR_EDITOR_CHANNEL_NEWS;
    }

    public static final ICache<String, Set<Integer>>    getTVChannelNews(){
        return TV_CHANNEL_NEWS;
    }

    public static final ICache<String, Set<Integer>>    getCarChannelNews(){
        return CAR_CHANNEL_NEWS;
    }

    public static final ICache<String, Set<Integer>>    getEnjoyEditorChannelNews(){
        return ENJOY_EDITOR_CHANNEL_NEWS;
    }

    public static final ICache<String, Set<Integer>>    getFinanceEditorChannelNews(){
        return FINANCE_EDITOR_CHANNEL_NEWS;
    }

    public static final ICache<String, Set<Integer>>    getITEditorChannelNews(){
        return IT_EDITOR_CHANNEL_NEWS;
    }

    public static final ICache<String, Set<Integer>>    getInternationalEditorChannelNews(){
        return INTERNATIONAL_EDITOR_CHANNEL_NEWS;
    }

    public static final ICache<String, Set<Integer>>    getVideoEditorChannelNews(){
        return VIDEO_EDITOR_CHANNEL_NEWS;
    }

    public static final ICache<String, Set<Integer>>    getSportsEditorChannelNews(){
        return SPORTS_EDITOR_CHANNEL_NEWS;
    }

    public static final ICache<String, Set<Integer>>    getHouseEditorChannelNews(){
        return HOUSE_EDITOR_CHANNEL_NEWS;
    }


    public static final ICache<String, Set<Integer>> getEnjoyChannelNews(){
        return ENJOY_CHANNEL_NEWS;
    }

    public static final ICache<String, Set<Integer>> getMainChannelEditedNews(){
        return MAIN_CHANNEL_EDITED_NEWS;
    }

    public static final ICache<String,Set<Integer>>  getEditedToutiaoNews(){
         return EDITED_TOUTIAO_NEWS;
    }

    public static final ICache<String,Set<Integer>> getMChannelHotNews(){
        return MAIN_CHANNEL_HOT_NEWS;
    }

    public static ICache<Integer, List<Integer>> getCatTopNewsCache() {
        return CAT_TOPNEWS_CACHE;
    }

    public static ICache<Integer, List<Integer>> getCatSelectedNewsCache(){
        return CAT_SELECTEDNEWS_CACHE;
    }

    public static ICache<Integer, Set<Integer>> getTaoNewsCache() {
        return CAT_TAONEWS_CACHE;
    }

    public static final ICache<String, Set<Integer>> getFinanceChannelNews(){
        return FINANCE_CHANNEL_NEWS;
    }

    public static final ICache<String, Set<Integer>> getITChannelNews(String key){
        if(StringUtils.equalsIgnoreCase(key, ITChannelNewsDataService.key)){
            return IT_CHANNEL_NEWS;
        } else if(StringUtils.equalsIgnoreCase(key, ITChannelNewsDataService.zimeitiKey)){
            return IT_ZIMEITI_CHANNEL_NEWS;
        }
        return null;
    }

    public static final ICache<String, Set<Integer>> getInternationalChannelNews(){
        return INTERNATIONAL_CHANNEL_NEWS;
    }

    public static final ICache<String, Set<Integer>> getVedioChannelNews(){
        return VIDEO_CHANNEL_NEWS;
    }

    public static final ICache<String, Set<Integer>> getSportsChannelNews(){
        return SPORTS_CHANNEL_NEWS;
    }

    public static final ICache<String, Set<Integer>> getHouseChannelNews(){
        return HOUSE_CHANNEL_NEWS;
    }




}