package com.wap.sohu.recom.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wap.sohu.mobilepaper.model.pic.GroupPicInfo;
import com.wap.sohu.recom.cache.core.ICache;
import com.wap.sohu.recom.cache.manager.GroupCacheManager;
import com.wap.sohu.recom.constants.ExpireTimeConstants;
import com.wap.sohu.recom.constants.RedisKeyConstants;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.dao.GroupPicDao;
import com.wap.sohu.recom.model.GroupPicInfoSerial;
import com.wap.sohu.recom.service.GroupPicService;
import com.wap.sohu.recom.utils.ConvertUtils;
import com.wap.sohu.recom.utils.StringExtUtils;

/**
 * Grouppic 信息相关服务
 *
 * @author hongfengwang 2012-7-24 下午03:18:15
 */
@Service("groupPicService")
public class GroupPicServiceImpl implements GroupPicService {

    private static final Logger    LOGGER           = Logger.getLogger(GroupPicServiceImpl.class);

    /** 热门推荐数量，每个分类取50个 */
    public static int              HOT_LIST_SIZE    = 50;
    /** 下一组组图推荐的数量 */
    public static final int        NEXT_SIZE        = 4;
    /** 最小推荐图片权重，小于该值则记录在不喜欢列表 */
    public static int              PIC_WEIGHT_LIMIT = 7;

    @Autowired
    private GroupPicDao            groupPicDao;

    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateRecom;

    @Autowired
    private StringRedisTemplateExt redisTemplateBase;

    private static final Calendar CALENDAR = Calendar.getInstance();





    @PostConstruct
    public void init() {
        ScheduledExecutorService scheduledThread = Executors.newSingleThreadScheduledExecutor();
        scheduledThread.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                // 定时更新本地热门组图缓存
                loadHotList();
            }
        }, 0, 600, TimeUnit.SECONDS);
    }

    public void loadHotList() {
        // 查询图组类型列表
        List<Integer> typeList = groupPicDao.listGroupPicCategory();
        for (int groupPicType : typeList) {
            try {
                listGroupListHot(groupPicType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从推荐后台取出一定数量的推荐，用户推荐队列为空时使用 [效率：高]
     *
     * @param picType
     * @return
     */
    public List<GroupPicInfo> listGroupListHot(int picType) {
        List<GroupPicInfo> list = listGroupListHotNoRefresh(picType, 50);
        return list;
    }

    /**
     * @param valueOf
     * @return
     */
    @Override
    public GroupPicInfo getGroupPicInfo(Integer gid) {
        Set<Integer> delCache = getDelGroups();
        if (delCache.contains(gid)) {
            return null;
        }
        String key = String.format(RedisKeyConstants.KEY_GROUP_PIC_INFO, gid);
        GroupPicInfoSerial groupPicInfoSerial = null;
        GroupPicInfo groupPicInfo =null;
        ICache<Integer, GroupPicInfoSerial> iCache = GroupCacheManager.getGroupInfoCache();
        if ((groupPicInfoSerial=iCache.get(gid))==null) {
            if (shardedRedisTemplateRecom.hasKey(key)) {
                String json = shardedRedisTemplateRecom.opsForValue().get(key);
                if (StringUtils.isBlank(json) || json.equals("null")) {
                    return null;
                }
                groupPicInfoSerial = ConvertUtils.fromJsonJk(json, GroupPicInfoSerial.class);
                iCache.put(gid, groupPicInfoSerial);
            } else {
                groupPicInfo = this.groupPicDao.findGroupPicInfo(gid);
                if (groupPicInfo==null) {
                    this.shardedRedisTemplateRecom.opsForValue().set(key, "null",3*ExpireTimeConstants.MEMCACHED_ONE_HOUR, TimeUnit.SECONDS);
                    return null;
                }
                groupPicInfoSerial = new GroupPicInfoSerial(groupPicInfo);
                this.shardedRedisTemplateRecom.opsForValue().set(key, ConvertUtils.toJsonJk(groupPicInfoSerial),
                                                         3*ExpireTimeConstants.MEMCACHED_ONE_HOUR, TimeUnit.SECONDS);
                iCache.put(gid, groupPicInfoSerial);
                return groupPicInfo;
            }
        }
        groupPicInfo = new GroupPicInfo();
        groupPicInfo.setId(groupPicInfoSerial.getId());
        groupPicInfo.setPic(groupPicInfoSerial.getPic());
        groupPicInfo.setPicCount(groupPicInfoSerial.getPicCount());
        groupPicInfo.setPicType(groupPicInfoSerial.getPicType());
        groupPicInfo.setRecommendDate(groupPicInfoSerial.getRecommendDate());
        groupPicInfo.setTitle(groupPicInfoSerial.getTitle());
        groupPicInfo.setWeigth(groupPicInfoSerial.getWeigth());
        return groupPicInfo;
    }

    /**
     * @return
     */
    public Set<Integer> getDelGroups() {
        Set<Integer> delCache = GroupCacheManager.getGroupDelCache().get(RedisKeyConstants.GROUP_DEL_SET);
        if (null == delCache) {
                delCache = ConvertUtils.convert2intList(redisTemplateBase.opsForSet().members(RedisKeyConstants.GROUP_DEL_SET));
                GroupCacheManager.getGroupDelCache().put(RedisKeyConstants.GROUP_DEL_SET, delCache);
        }
        return delCache;
    }

    
    
    /**
     * @param gid
     * @return
     */
    @Deprecated
    @Override
    public List<Long> getGroupTagSet(int gid) {
        //从喜欢新闻组图的用户set缓存中读取数据
        String key = String.format(RedisKeyConstants.KEY_GROUP_TQG_SET, gid);
        List<Long> likeSet = null;
        if (shardedRedisTemplateRecom.getExpire(key) != -1) {
            String json = shardedRedisTemplateRecom.opsForValue().get(key);
            if (StringUtils.isBlank(json) || json.equals("null")) {
                return null;
            }
            return ConvertUtils.fromJsonJkGeneric(json, new TypeReference<List<Long>>() {
            });
        } else {
            likeSet = groupPicDao.getGroupTagSet(gid);
            shardedRedisTemplateRecom.opsForValue().set(key, ConvertUtils.toJsonJk(likeSet),
                                                ExpireTimeConstants.MEMCACHED_ONE_HOUR, TimeUnit.SECONDS);
        }
        return likeSet;
    }

    /**
     * @param nextSize
     * @return
     */
    @Override
    public List<Integer> getRdmPicTypeList(int size) {
        // 随机生成组图类型列表 (可以重复出现)
        ICache<String, List<Integer>> iCache = GroupCacheManager.getGroupAllCatCache();
        List<Integer> picTypeList = null;
        if ((picTypeList=iCache.get("all_group_category"))==null) {
            picTypeList = groupPicDao.listGroupPicCategory();
            iCache.put("all_group_category", picTypeList);
        }
        int picTypeCount = picTypeList.size();
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < size; i++) {
            double rdm = Math.random();
            int index = (int) (rdm * picTypeCount);
            list.add(picTypeList.get(index));
        }
        return list;
    }

    /**
     * @param picType
     * @param leftSize
     * @return
     */
    @Override
    public List<GroupPicInfo> listGroupListHotNoRefresh(int picType, int moreCount) {
        // 使用本地热门推荐流程
        String key = String.format(RedisKeyConstants.KEY_HOT_LIST, picType);
        List<GroupPicInfo> list = GroupCacheManager.getHotGroupCache().get(key);
        boolean needUpdateCache = false;
        // 如果缓存中没有，从推荐redis中取一次数据
        if (list == null || list.size() < moreCount) {
            needUpdateCache = true;
            String json = redisTemplateBase.opsForValue().get(key);
            if (StringUtils.isNotBlank(json) && !json.equals("null")) {
                list = ConvertUtils.fromJsonJkGeneric(json, new TypeReference<List<GroupPicInfo>>() {
                });
            }
        }
        // 如果还没有数据，查一次数据库
        if (list == null || list.size() < moreCount) {
            list = groupPicDao.getHotList(picType);
        }
        if (needUpdateCache && list != null && list.size() > 0) {
            GroupCacheManager.getHotGroupCache().put(key, list);
        }
        return list;
    }

    @Override
    public Integer getCategoryIdByGid(int gid) {
        Integer picType = GroupCacheManager.getGroupCatCache().get(gid);
        if (picType == null) {
            picType = groupPicDao.getCategory(gid);
            GroupCacheManager.getGroupCatCache().put(gid, picType);
        }
        return picType;
    }

    /**
     * 将删除的组图放到redis中
     */
    @Override
    public void setDelGroupToRedis(List<Long> list) {
        redisTemplateBase.opsForBatch().add(RedisKeyConstants.GROUP_DEL_SET, list);
        //凌晨3点过期，这样也不会碰到编辑删除，每天编辑删除20-30套组图
        int thisTime = CALENDAR.get(Calendar.HOUR_OF_DAY);
        redisTemplateBase.expire(RedisKeyConstants.GROUP_DEL_SET,(24-thisTime+3)*60*60, TimeUnit.SECONDS);
        //        
//        for (Long long1 : list) {
//            redisTemplateBase.opsForZSet().add(RedisKeyConstants.GROUP_DEL_SORTEDSET,String.valueOf(long1),new Date().getTime()/1000);
//        }
//        Long max = DateUtils.addDays(new Date(), -10).getTime()/1000;
//        redisTemplateBase.opsForZSet().removeRangeByScore(RedisKeyConstants.GROUP_DEL_SORTEDSET, 0, max);
    }

    @Override
    public Integer getNewsEmbedGid(int newsId){
        String key = String.format(RedisKeyConstants.NEWSID_GROUPID_KEY, newsId);
        String value = shardedRedisTemplateRecom.opsForValue().get(key);
        //redis中没有值的value是 没有相关组图的新闻组图，所以没有加到redis
        if (StringUtils.isBlank(value)) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    @Override
    public void filterDupGroup(GroupPicInfo group,List<GroupPicInfo> groupList) {
        if (groupList.size() == 0) {
            return;
        }
        String title = group.getTitle();
        if (StringUtils.isBlank(group.getTitle())) {
            return;
        }
        int id = group.getId();
        Iterator<GroupPicInfo> iterator = groupList.iterator();
        while (iterator.hasNext()) {
            GroupPicInfo groupPicInfo = iterator.next();
            if (id == groupPicInfo.getId()) {
                continue;
            }
            String titleItem = groupPicInfo.getTitle();
            int interSize = StringExtUtils.getInterSize(title, titleItem, 14);
            if ((double) interSize / title.length() > 0.75) {
                iterator.remove();
                continue;
            }
        }
    }

}
