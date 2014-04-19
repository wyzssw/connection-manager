package com.wap.sohu.recom.constants;

/**
 * 类RedisKeyConstants.java的实现描述：TODO 类实现描述
 *
 * @author hongfengwang 2012-7-27 下午02:12:57
 */
public class RedisKeyConstants {


    /** 标签相似组图推荐缓存key：新列表 */
    public static final String KEY_THIRD_RECOMMEND_LIST_NEW = "new_group_correlation_%d";
    /** itembased 组图key */
    public static final String KEY_ITEMBASED_RECOMMEND_LIST = "group_sim_list_%d";

    /** newsId-groupId 关联key */
    public static final String NEWSID_GROUPID_KEY           = "newsId_groupId_%d";

    /** 用户浏览历史缓存key,set存储 */
    public static final String KEY_USER_HISTORY             = "uh_%d";

    /** 热门推荐缓存key */
    public static final String KEY_HOT_LIST                 = "hot_group_%d";

    /** KEY: 组图标签的IDset */
    public static final String KEY_GROUP_TQG_SET            = "grouptagset_%d";
    /** 用户不喜欢组图缓存key */
    public static final String KEY_USER_UNLIKE              = "unlike_%d_%d";
    /** KEY: 图组对象（GroupPicInfo）缓存； */
    public static final String KEY_GROUP_PIC_INFO           = "grouppicinfo_%d";

    /** 编辑在最近时间段内删除的组图id列表 */
    public static final String GROUP_DEL_SET                = "group_del_set";

    /** 新闻tag 矩阵key %d代表newsid */
    public static final String NEWS_TAG_MATRIX              = "news_tag_matrix_%d";
    /** tag新闻 矩阵key %d代表tagid */
    public static final String TAG_NEWS_MATRIX              = "tag_news_matrix_%d";

    /** 新闻分类 矩阵key %d代表newsId */
    public static final String NEWS_CAT_MATRIX              = "news_cat_matrix_%d";
    /** 分类新闻 矩阵key %s代表cat */
    public static final String CAT_NEWS_MATRIX              = "cat_news_matrix_%s";

    /** 新闻与时间的对应关系 */
    public static final String NEWS_TIME_MAPPING            = "news_time_%d";


    /** 用户看过的新闻历史记录 */
    public static final String NEWS_USER_HISTORY            = "news_user_history_%d";

    /** tag相关性的tag */
    public static final String TAG_TOP_TAGS                 = "tag_top_tags_%d";

    /** tag重合度的新闻列表 */
    public static final String NEWS_SIM_NEWS                = "news_sim_news_%d";

    /** 每个分类下的新闻点击数量 */
    public static final String CAT_NEWS_COUNT               = "cat_news_count_%s";

    /** 新闻重复对象id */
    public static final String NEWS_DUP_ID                  = "news_dup_id_%d";
    /**新闻标题缓存 */
    public static final String NEWS_TITLE_CACHE              = "news_title_cache_%d";
    /**新闻内容缓存 */
    public static final String NEWS_CONTENT_CACHE            = "news_content_cache_%d";

    /** 要删除的新闻列表 */
    public static final String NEWS_DEL_ZSET                = "news_del_zset";

    /**
     * 用户基站定位缓存: cell_tower_$lac_$cellid
     */
    public static final String CELL_TOWER_KEY               = "cell_tower_%d_%d";

    /**
     * 经纬度数据
     */
    public static final String LAT_LNG_KEY                  = "lat_lng_%s_%s";

    /**
     * 用户所在城市
     */
    public static final String USER_LOCATION_KEY            = "c_loc_%d";

    /**
     * 用户所在城市
     */
    public static final String USER_LBS_LOCATION_KEY            = "lbs_loc_%d";

    /**
     * 用户默认订阅
     */
    public static final String DEFAULT_SUB_KEY              = "default_sub";

    /**
     * 用户已订阅刊物
     */
    public static final String USER_SUBSCRIPTION_KEY        = "c_subscribe_%d";

    /**
     * 刊物推荐列表
     */
    public static final String REC_SUB_KEYS                 = "rec_sub_%d";

    /** 地方刊物推荐 */
    public static final String LOC_SUB_KEYS                 = "loc_sub_%s";

    /** 刊物热门列表key **/
    public static final String HOT_SUB_KEYS                 = "hot_sub";

    /** 频道刊物对应    */
    public static final String CHANNEL_SUB_KEYS                     = "cl_sub_%d";

    /**猜你喜欢展示组图历史记录 */
    public static final String USER_LIKE_HISTORY_GROUP      = "user_like_history_group_%d";

    /**猜你喜欢展示新闻历史记录 */
    public static final String USER_LIKE_HISTORY_NEWS       = "user_like_history_news_%d";

    /** 通过分词生成的  组图新闻关联组图  */
    public static final String GROUPNEWS_SIM_GROUP           = "groupnews_sim_group_%d";

    /** 通过分词生成的  组图新闻关联组图 新闻 */
    public static final String GROUPNEWS_SIM_NEWS            = "groupnews_sim_news_%d";

    /** 新闻与pubid的对应关系  */
    public static final String NEWS_PUBID_CACHE              = "news_pubid_cache_%d";

    /**刊物下新闻list  */
    public static final String PUBID_NEWS_LIST               = "pubid_news_list_%d";

}
