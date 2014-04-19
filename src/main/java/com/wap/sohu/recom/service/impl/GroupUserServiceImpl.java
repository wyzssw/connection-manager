package com.wap.sohu.recom.service.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wap.sohu.mobilepaper.model.pic.GroupPicInfo;
import com.wap.sohu.mobilepaper.model.pic.ScanHistory;
import com.wap.sohu.recom.constants.ExpireTimeConstants;
import com.wap.sohu.recom.constants.RedisKeyConstants;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.service.GroupUserService;
import com.wap.sohu.recom.utils.ConvertUtils;


/**
 * 用户关联组图逻辑
 * @author hongfengwang 2012-7-27 下午03:54:31
 */
@Service("groupUserService")
public class GroupUserServiceImpl implements GroupUserService {


    
    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateUser;


    /** 用户组图推荐最小数量 */
    public static final int USER_GROUP_PIC_MIN_SIZE = 5;

    /** 用户组图推荐最大数量 */
    public static int USER_GROUP_PIC_MAX_SIZE = 15;


    @Override
    public void addHistory(long cid, int picType, int gid) {
        if (cid <= 0 || gid <= 0|| picType <= 0) {
            return;
        }
        String key = String.format(RedisKeyConstants.KEY_USER_HISTORY, cid);
        long count = shardedRedisTemplateUser.opsForList().size(key);
        if (count>100) {
            for (int i = 0; i < count-50; i++) {
                shardedRedisTemplateUser.opsForList().rightPop(key);
            }
        }
        shardedRedisTemplateUser.opsForList().leftPush(key, String.valueOf(gid));
        shardedRedisTemplateUser.expire(key, 10, TimeUnit.DAYS);

    }


    @Override
    public void addUnlike(long cid, int picType, List<GroupPicInfo> gidList) {
      String key = String.format(RedisKeyConstants.KEY_USER_UNLIKE, cid, picType);
      String  json  = shardedRedisTemplateUser.opsForValue().get(key);
      json = StringUtils.isBlank(json)?"null":json;
      Set<ScanHistory> set = ConvertUtils.fromJsonJkGeneric(json, new TypeReference<Set<ScanHistory>>() {});
      if (set == null) {
          set = new HashSet<ScanHistory>();
      }
      for (GroupPicInfo pic : gidList) {
          ScanHistory history = new ScanHistory();
          history.setGid(pic.getId());
          history.setScanDate(new Date());
          set.add(history);
      }
      shardedRedisTemplateUser.opsForValue().set(key, ConvertUtils.toJsonJk(set),
                                            ExpireTimeConstants.USER_UNLIKE_EXP_TIME, TimeUnit.SECONDS);
    }


}
