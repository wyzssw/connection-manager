package com.wap.sohu.recom.utils;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wap.sohu.recom.constants.RedisKeyConstants;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.service.PropertyProxy;

/**
 * 类LocalityFilterUtils.java的实现描述：TODO 类实现描述
 * @author yeyanchao Oct 11, 2013 1:52:49 PM
 */
@Service
public class LocalityFilterUtils {
    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateLbs;

    @Autowired
    private PropertyProxy propertyProxy;

    private Set<Long> cidSet = new HashSet<Long>();

    @PostConstruct
    public void init() {
        String cidStr =  propertyProxy.getProperty("main_cid_set");
        String[] cids  = StringUtils.split(cidStr, ",");
        for (String cid : cids) {
             cidSet.add(Long.valueOf(cid));
        }
    }

    public boolean isNotInBeijing(Long cid){
        if(cidSet.contains(cid)){
            return true;// not filter these users
        }
        if(cid>0){
            String key = String.format(RedisKeyConstants.USER_LBS_LOCATION_KEY, cid);
            String value = shardedRedisTemplateLbs.opsForValue().get(key);
            // not in beijing user
            if(StringUtils.isNotEmpty(value) && !StringUtils.equals("1100", value)){
                return true;
            }
        }
        return false;
    }

}
