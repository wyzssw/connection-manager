package com.wap.sohu.recom.constants;

import java.util.concurrent.TimeUnit;

/**
 * 类MemcachedKeyConstants.java的实现描述：Memcached缓存key
 *
 * @author yeyanchao 2012-12-27 下午2:42:48
 */
public enum MemcachedKeys {
    /**
     * 摇一摇已出现刊物 key: rec_sub_show_cid expire: 3600s
     */
    RecomSubList("rec_sub_show_%d", 60 * 60),

    /**
     * 摇一摇 用户喜欢渠道刊物推荐列表
     */
    UserChannelSubList("user_cn_sub_%d", 25 * 60 * 60);

    // key name
    private String   key;
    // expire unit
    private TimeUnit expireUnit;
    // expire value
    private int      expireTime;

    private MemcachedKeys(String key, TimeUnit expireUnit, int expireTime){
        this.key = key;
        this.expireTime = expireTime;
    }

    private MemcachedKeys(String key, int expireTime){
        this(key, TimeUnit.SECONDS, expireTime);
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @return the expireUnit
     */
    public TimeUnit getExpireUnit() {
        return expireUnit;
    }

    /**
     * @return the expireTime
     */
    public int getExpireTime() {
        return expireTime;
    }

}
