package com.wap.sohu.recom.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Tuple;
import scala.annotation.elidable;


import com.fasterxml.jackson.core.type.TypeReference;
import com.wap.sohu.mobilepaper.model.pic.GroupPicInfo;
import com.wap.sohu.recom.cache.core.ICache;
import com.wap.sohu.recom.cache.manager.GroupCacheManager;
import com.wap.sohu.recom.cache.manager.NewsCacheManager;
import com.wap.sohu.recom.constants.RedisKeyConstants;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.log.AccessTraceLogData;
import com.wap.sohu.recom.log.AccessTraceLogData.LogCategoryEnum;
import com.wap.sohu.recom.log.AccessTraceLogData.LogKeyEnum;
import com.wap.sohu.recom.log.StatisticLog;
import com.wap.sohu.recom.model.GroupCorrelationDo;
import com.wap.sohu.recom.service.GroupPicService;
import com.wap.sohu.recom.service.GroupRecomService;
import com.wap.sohu.recom.service.GroupUserService;
import com.wap.sohu.recom.utils.CommonUtils;
import com.wap.sohu.recom.utils.ConvertUtils;

/**
 * 类GroupRecomServiceImpl.java的实现描述：TODO 类实现描述
 *
 * @author hongfengwang 2012-7-27 下午04:39:20
 */
@Service("groupRecomService")
public class GroupRecomServiceImpl implements GroupRecomService {

    private static final Logger    LOGGER           = Logger.getLogger(GroupRecomServiceImpl.class);

    @Autowired
    private GroupUserService       groupUserService;

    @Autowired
    private GroupPicService        groupPicService;

    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateRecom;

    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateUser;

    /** 下一组组图推荐的数量 */
    public static final int        NEXT_SIZE        = 4;

    /** 最小推荐图片权重，小于该值则记录在不喜欢列表 */
    public static int              PIC_WEIGHT_LIMIT = 7;

    /**
     * 推荐服务接口：组图新闻 or 组图
     *
     * @param cid
     * @param gid
     * @param moreCount
     * @param picType
     * @return
     */
    public List<Integer> getRecomGroup(long cid, int newsId, int gid, int moreCount, int picType) {
        if (moreCount <= 0) {
            moreCount = NEXT_SIZE;
        }
        groupUserService.addHistory(cid, picType, gid);
        // 用户已看组图列表
        Set<Integer> filterSet = new HashSet<Integer>();
        filterSet.add(gid);
        filterSet.addAll(getUserHistorySet(cid));
        if (newsId > 0 && gid <= 0) {
            return getGroupNewsRecom(cid, newsId, moreCount, picType,filterSet);
        } else {
            return getGroupPicRecom(cid, gid, moreCount, picType,filterSet);
        }
    }

    /**
     * 组图新闻推荐
     *
     * @param cid
     * @param newsId
     * @param moreCount
     * @param picType
     * @return
     */
    private List<Integer> getGroupNewsRecom(long cid, int newsId, int moreCount, int picType,Set<Integer> filterSet) {
        AccessTraceLogData logData = new AccessTraceLogData(LogCategoryEnum.GroupRec);
        logData.add(LogKeyEnum.PicType, picType);
        logData.add(LogKeyEnum.Cid, cid);
        List<GroupPicInfo> resultList = new ArrayList<GroupPicInfo>();
        List<GroupPicInfo> groupPicList = new ArrayList<GroupPicInfo>();
        int gid = 0;
         // 来自组图新闻:关联newsId-groupId;
        logData.add(LogKeyEnum.NewsId, newsId);
        gid =  groupPicService.getNewsEmbedGid(newsId);
        // 组图新闻推荐--tag推荐
        if (gid!=0) {
            groupPicList = getRecommondPic(gid, picType, filterSet, moreCount);
        }
        Set<Integer> set = ConvertUtils.groupPicInfo2Set(groupPicList);
        filterSet.addAll(set);
        groupPicList.addAll(getSplitTagPic(newsId,filterSet,moreCount));
        
        resultList.addAll(groupPicList);
        logData.add(LogKeyEnum.GroupCorrelation, groupPicList.size());  
        int leftCount = 0;
        if ((leftCount=moreCount-groupPicList.size())>0) {
            for (int i = 0; i < groupPicList.size(); i++) {
                filterSet.add(groupPicList.get(i).getId());
            }
            // 组图新闻推荐 --itembase*/
            List<GroupPicInfo> groupNewsList = getItemBasedRecomPic(gid, picType,filterSet,leftCount);
            resultList.addAll(groupNewsList);
            logData.add(LogKeyEnum.NewsRec, groupNewsList.size());        
        }
        filterDupGroups(resultList);
        Set<Integer> idSet = GroupRecomUtils.getGroupPicIdSet(gid, resultList);
        List<GroupPicInfo> hotMoreList =getGroupFromHotList(moreCount-resultList.size(), picType,idSet);
        resultList.addAll(hotMoreList);
        // 多余组图剔除
        resultList = CommonUtils.getRandomList(resultList, moreCount);
        logData.add(LogKeyEnum.Total, resultList.size());
        StatisticLog.info(logData);
        if (resultList.isEmpty()) {
            LOGGER.error("recom group is null when cid=" + cid + " gid=" + gid);
            return null;
        }
        List<Integer> recomGidList = convert2IntList(resultList);
        return recomGidList;
    }

    /**
     * @param newsId
     * @param filterSet
     * @param moreCount
     * @return
     */
    @Override
    public List<GroupPicInfo> getSplitTagPic(int newsId, Set<Integer> filterSet, int moreCount) {
       List<Integer> gidList = getSplitGid(newsId,filterSet);
        //没办法，历史原因，返回的是grouppicinfo，所以要转换一次，不然代码改动太多
        List<GroupPicInfo> list = new ArrayList<GroupPicInfo>();
        for (Integer item : gidList) {
             GroupPicInfo groupPicInfo = new GroupPicInfo();
             groupPicInfo.setId(item);
             GroupPicInfo tempGroupPicInfo = groupPicService.getGroupPicInfo(item);
             if (tempGroupPicInfo==null) {
                 continue;
             }
             groupPicInfo.setTitle(tempGroupPicInfo.getTitle());
             list.add(groupPicInfo);
        }
        return list;
    }

    /**
     * @param newsId
     * @return
     */
    @Override
    public List<Integer> getSplitGid(int newsId,Set<Integer> filterSet) {
        ICache<Integer, Map<Integer, Double>> iCache = GroupCacheManager.getGroupNewsSimGroup();
        Map<Integer,Double> map = null;
        if ((map=iCache.get(newsId))==null) {
            map = new LinkedHashMap<Integer,Double>();
            String key = String.format(RedisKeyConstants.GROUPNEWS_SIM_GROUP, newsId);
            Set<TypedTuple<String>> recomSet =  shardedRedisTemplateRecom.opsForZSet().reverseRangeWithScores(key, 0, 15);
            for (TypedTuple<String> tuple : recomSet) {
                if (tuple.getScore()>0.2) {
                    Integer recGid = Integer.parseInt(tuple.getValue());
                    map.put(recGid, tuple.getScore());
                }
            }
            iCache.put(newsId, map);
        }
        List<Integer> retList = new ArrayList<Integer>();
        for (Map.Entry<Integer, Double> item : map.entrySet()) {
             if (!filterSet.contains(item.getKey())) {
                retList.add(item.getKey());
            }
        }
        return retList;
    }

    /**
     * @param resultList
     * @return
     */
    @Override
    public List<Integer> convert2IntList(List<GroupPicInfo> resultList) {
        List<Integer> recomGidList = new ArrayList<Integer>();
        for (GroupPicInfo groupPicInfo : resultList) {
            recomGidList.add(groupPicInfo.getId());
        }
        return recomGidList;
    }

    /**
     * @param moreCount
     * @param picType
     * @param logData
     * @param resultList
     * @param gid
     * @return
     */
    @Override
    public List<GroupPicInfo> getGroupFromHotList(int leftSize, int picType,  Set<Integer> idSet) {
            return nextFromHotList( picType,leftSize,idSet);
    }

    /**
     * 组图推荐
     *
     * @param cid
     * @param gid
     * @param moreCount
     * @param picType
     * @return
     */
    private List<Integer> getGroupPicRecom(long cid, int gid, int moreCount, int picType,Set<Integer> filterSet) {
        AccessTraceLogData logData = new AccessTraceLogData(LogCategoryEnum.GroupRec);
        logData.add(LogKeyEnum.PicType, picType);
        logData.add(LogKeyEnum.Cid, cid);
        List<GroupPicInfo> resultList = new ArrayList<GroupPicInfo>();       
        int halfCount =   moreCount / 2 + moreCount % 2;
        if (gid > 0) {
            logData.add(LogKeyEnum.Gid, gid);
            // 获取当前Tag相似组图列表
            List<GroupPicInfo> groupPicList = this.getRecommondPic(gid, picType, filterSet, moreCount);
            resultList.addAll(groupPicList);
            // 当前展示4/6张组图推荐:展示组图数目 Tag相似组图列表 与 Itembased相似列表对半
            int tagGroupRecSize =  groupPicList.size();
            int leftCount = tagGroupRecSize > halfCount ? moreCount - halfCount : moreCount - tagGroupRecSize;
            // Tag 相似组图列表中 必然推荐 结果
            int recommendCount = tagGroupRecSize > halfCount ? halfCount : tagGroupRecSize;
            for (int i = 0; i < recommendCount; i++) {
                filterSet.add(groupPicList.get(i).getId());
            }
            // 获取ItemBased相似图片列表
            List<GroupPicInfo> itemBasedGroupList = getItemBasedRecomPic(gid, picType, filterSet,
                                                                         leftCount);
            resultList.addAll(itemBasedGroupList);
            filterSet.addAll(convert2IntList(itemBasedGroupList));
            logData.add(LogKeyEnum.ItemBased, itemBasedGroupList.size());
            // 删除多余推荐组图index
            rmSurplusTagGroup(moreCount, resultList);
            // collect tag recommend 推荐列表
            logData.add(LogKeyEnum.GroupCorrelation, resultList.size() - itemBasedGroupList.size());
        }
        List<GroupPicInfo> hotRecList = new ArrayList<GroupPicInfo>();
        if (resultList.size()<moreCount) {
            Set<Integer> idSet = GroupRecomUtils.getGroupPicIdSet(gid, resultList);
            hotRecList = getGroupFromHotList(moreCount-resultList.size(), picType, idSet);
            resultList.addAll(hotRecList);
        }
        logData.add(LogKeyEnum.HotRec, hotRecList.size());
        // 多余组图剔除
        resultList = CommonUtils.getRandomList(resultList, moreCount);
        logData.add(LogKeyEnum.Total, resultList.size());
        StatisticLog.info(logData);
        List<Integer> recomGidList = convert2IntList(resultList);
        return recomGidList;
    }

    /**
     * 删除多余组图
     * @param moreCount
     * @param resultList
     * @param itemBasedGroupList
     */
    private void rmSurplusTagGroup(int moreCount, List<GroupPicInfo> resultList) {
        int removeIndex = resultList.size();
        // 删除多余组图:tag推荐列表获取数据 > halfCount时，删除多余的Taglist数据
        if (resultList.size() > moreCount) {
            int removeNum = resultList.size() - moreCount;
            for (int i = removeNum; i > 0; i--) {
                resultList.remove(removeIndex - removeNum);
            }
        }
    }

    /**
     * ItemBased相关推荐列表:保证返回数据最多leftCount个
     *
     * @param gid
     * @param picType TODO
     * @param hasRecommond
     * @param historySet
     * @param leftCount
     * @return
     */
    private List<GroupPicInfo> getItemBasedRecomPic(int gid, int picType,Set<Integer> filterSet, int leftCount) {
        List<GroupPicInfo> itemBasedGroupList = new ArrayList<GroupPicInfo>();
        String key = String.format(RedisKeyConstants.KEY_ITEMBASED_RECOMMEND_LIST, gid);
        long len = shardedRedisTemplateRecom.opsForList().size(key);
        List<String> itemBasedContents = shardedRedisTemplateRecom.opsForList().range(key, -1 * leftCount * 10, len);
        // 获取Itembased推荐list
        List<Integer> itemBasedGidList = this.convert2GroupIdList(itemBasedContents);
        itemBasedGidList.removeAll(filterSet);
        // 获取推荐内容
        itemBasedGroupList.addAll(filterRecommond(gid, picType, itemBasedGidList, leftCount));
        // 如果获取到的组图为空或数量不足，则以已经浏览过的组图个数加10为下标查询一次；
        if ((itemBasedGroupList.isEmpty() || itemBasedGroupList.size() < leftCount) && len > leftCount * 10
            && filterSet.size()>1) {
            itemBasedContents = shardedRedisTemplateRecom.opsForList().range(key,
                                                                      -1
                                                                              * (leftCount * 10 + filterSet.size()-1),
                                                                      -1 * (leftCount * 10 + 1));
            itemBasedGidList = this.convert2GroupIdList(itemBasedContents);
            itemBasedGidList.removeAll(filterSet);
            itemBasedGroupList.addAll(filterRecommond(gid, picType, itemBasedGidList,leftCount - itemBasedGroupList.size()));
        }
        return itemBasedGroupList;
    }

    /**
     * 获取推荐Tag相似组图列表：返回结果最多为recommondCount个
     *
     * @param gid
     * @param picType TODO
     * @param historySet
     * @param hasRecommond
     * @param recommondCount
     * @return
     */
    @Override
    public List<GroupPicInfo> getRecommondPic(int gid, int picType, Set<Integer> filterSet, int recommondCount) {
        List<GroupPicInfo> group = new ArrayList<GroupPicInfo>();
        String key = String.format(RedisKeyConstants.KEY_THIRD_RECOMMEND_LIST_NEW, gid);
        long jlen = shardedRedisTemplateRecom.opsForList().size(key);
        List<String> groupTagList = shardedRedisTemplateRecom.opsForList().range(key, -1 * (recommondCount * 10), jlen);
        // 获取Gid列表
        List<Integer> groupList = convert2GroupIdList(groupTagList);
        if (groupList != null && !groupList.isEmpty()) {
            groupList.removeAll(filterSet);
            group.addAll(filterRecommond(gid, picType, groupList, recommondCount));
        }
        // 如果获取到的组图为空，则以已经浏览过的组图个数加10为下标查询一次；
        // redis lrange 返回[start,end];第二次获取 lrange [end+1,new_end]
        if ((group.isEmpty() || group.size() < recommondCount) && jlen > (recommondCount * 10) && filterSet.size() > 1) {
            groupTagList = shardedRedisTemplateRecom.opsForList().range(key,
                                                                        (filterSet.size() - 1 + (recommondCount * 10))
                                                                                * -1, -1 * (1 + recommondCount * 10));
            groupList = convert2GroupIdList(groupTagList);
            groupList.removeAll(filterSet);
            group.addAll(this.filterRecommond(gid, picType, groupList, recommondCount - group.size()));
        }
        return group;
    }

    /**
     * 查询用户浏览记录列表
     *
     * @param cid
     * @param picType
     * @return
     */
    @Override
    public Set<Integer> getUserHistorySet(long cid) {
        String Hiskey = String.format(RedisKeyConstants.KEY_USER_HISTORY, cid);
        List<String> historyList = shardedRedisTemplateUser.opsForList().range(Hiskey, 0, -1);
        Set<String> historySet = new HashSet<String>(historyList);
        Set<Integer> historyGidSet = new HashSet<Integer>();
        if (historyGidSet != null) {
            for (String gid : historySet) {
                historyGidSet.add(Integer.parseInt(gid));
            }
        }
        return historyGidSet;
    }

    /**
     * 反序列化Tag相似度组图列表，并返回gid list
     *
     * @param groupCorList
     * @return
     */
    private List<Integer> convert2GroupIdList(List<String> groupCorList) {
        List<Integer> groupList = new ArrayList<Integer>();
        if (groupCorList != null && !groupCorList.isEmpty()) {
            List<GroupCorrelationDo> groupCorListValue = ConvertUtils.fromJsonJkGeneric(groupCorList.toString(),
                                                                                        new TypeReference<List<GroupCorrelationDo>>() {
                                                                                        });
            if (groupCorListValue != null && !groupCorListValue.isEmpty()) {
                for (GroupCorrelationDo groupCorrelationDo : groupCorListValue) {
                    groupList.add(groupCorrelationDo.getGid());
                }
            }
        }
        return groupList;
    }


    /**
     * @param gid
     * @param picType
     * @param moreCount
     * @param list
     * @return
     */
    private List<GroupPicInfo> nextFromHotList(int picType,int leftSize,Set<Integer> set) {
        List<GroupPicInfo> hotMoreList = new ArrayList<GroupPicInfo>();
        Random rdm = new Random();
        // 取随机列表的最大循环数量
        final int max_loop = 20;
        int loop_count = 0;
        Set<Integer> idSet = set;
        // 如果明确知道当前组图类型
        if (picType > 0) {
            List<GroupPicInfo> hotList = groupPicService.listGroupListHotNoRefresh(picType, leftSize);
//            idSet = GroupRecomUtils.getGroupPicIdSet(gid, currentList);
            for (int i = 0; i < leftSize; i++) {
                if (++loop_count > max_loop) {
                    break;
                }
                if (i >= hotList.size()) {
                    break;
                }
                GroupPicInfo gp = GroupRecomUtils.getRandomGroupPic(idSet, hotList, rdm);
                // 如果当前取到的数量等于hotList大小，则推出循环
                if (gp != null && groupPicService.getGroupPicInfo(gp.getId()) != null) {
                    hotMoreList.add(gp);
                } else {
                    i--;
                }
            }
        } else {// 如果当前不知道是哪个类型，就随机4个类型，每个类型下取一个
            // List<Integer> picTypeList = groupPicService.getRdmPicTypeList(NEXT_SIZE);
            // 如果当前不知道是哪个类型，就随机leftSize个类型，每个类型下取一个
            List<Integer> picTypeList = groupPicService.getRdmPicTypeList(leftSize);
            for (int i = 0; i < leftSize; i++) {
                if (++loop_count > max_loop) {
                    break;
                }
                int type = picTypeList.get(i);
                List<GroupPicInfo> hotList = groupPicService.listGroupListHotNoRefresh(type, leftSize);
                GroupPicInfo gp = GroupRecomUtils.getRandomGroupPic(idSet, hotList, rdm);
                if (gp != null && groupPicService.getGroupPicInfo(gp.getId()) != null) {
                    hotMoreList.add(gp);
                } else {
                    i--;
                }
            }
        }

        return hotMoreList;
    }
    
    



    /**
     * 返回GroupPicInfo 最多 recommondCount个
     *
     * @param gid
     * @param picType TODO
     * @param groupTagList
     * @param historySet
     * @param hasRecommond
     * @param recommondCount
     * @return
     */
    private List<GroupPicInfo> filterRecommond(int gid, int picType, List<Integer> groupTagList, int recommondCount) {
        //TODO:pictype的判定到后台任务中去做
        List<GroupPicInfo> groups = new ArrayList<GroupPicInfo>();
        if (groupTagList.isEmpty()) {
            return groups;
        }
        Integer firstGid = 0;
        int count = groupTagList.size() > recommondCount ? recommondCount : groupTagList.size();
        for (int i = 1; i <= count; i++) {
            int index = groupTagList.size() - i;
            if (index < 0) break;
            firstGid = groupTagList.get(index);
            GroupPicInfo group = groupPicService.getGroupPicInfo(firstGid);
            if (group != null && group.getId() > 0) {
                // 过滤 组图为同一类型：兼容当前redis中group.picType=0数据
                if (group.getPicType() > 0) {
                    if (group.getPicType() == picType) {
                        groups.add(group);
                    } else {
                        count++;
                    }
                } else {
                    groups.add(group);
                }
            } else {
                count++;
            }
        }

        return groups;
    }

    public void filterDupGroups(List<GroupPicInfo> groupList){
        if (groupList.size()>=3) {
            groupPicService.filterDupGroup(groupList.get(0), groupList);
            if (groupList.size()>=3) {
                groupPicService.filterDupGroup(groupList.get(1), groupList);
            }
            if (groupList.size()>=3) {
                groupPicService.filterDupGroup(groupList.get(2), groupList);
            }
        }
    }
    
    
    private static final class GroupRecomUtils {


        /**
         * @param idMap
         * @param hotList
         * @param rdm
         * @return
         */
        private static final GroupPicInfo getRandomGroupPic(Set<Integer> idSet, List<GroupPicInfo> hotList, Random rdm) {
            if (hotList == null || hotList.size() == 0) {
                return null;
            }
            // TODO 需优化--尽可能少的做随机
            // 随机产生推荐
            int index = rdm.nextInt(hotList.size());
            GroupPicInfo gp = hotList.get(index);
            if (idSet == null) {
                return gp;
            }
            if (!idSet.contains(gp.getId())) {
                idSet.add(gp.getId());
                return gp;
            }
            return null;
        }
        
        /**
         * @param gid
         * @param currentList
         * @return
         */
        private static final Set<Integer> getGroupPicIdSet(int gid, List<GroupPicInfo> list) {
            Set<Integer> idSet = new HashSet<Integer>();
            idSet.add(gid);
            if (list != null) {
                for (GroupPicInfo gp : list) {
                    idSet.add(gp.getId());
                }
            }
            return idSet;
        }


    }

}
