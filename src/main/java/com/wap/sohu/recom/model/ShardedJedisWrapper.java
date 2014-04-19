/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.model;

import com.wap.sohu.recom.core.redis.StringShardedJedisConnection;

/**
 * 类JedisWapper.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2012-12-28 下午04:47:29
 */
public class ShardedJedisWrapper {
  
    private volatile StringShardedJedisConnection stringShardedJedisConnection;
    
   

    //创建时间
    private volatile Long  timeStamp;
    

    public ShardedJedisWrapper(StringShardedJedisConnection stringShardedJedisConnection,Long timeStamp){
        this.stringShardedJedisConnection=stringShardedJedisConnection;
        this.timeStamp=timeStamp;
    }
    

    
    /**
     * @return the timeStamp
     */
    public Long getTimeStamp() {
        return timeStamp;
    }

    
    /**
     * @param timeStamp the timeStamp to set
     */
    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
    
    
    /**
     * @return the stringShardedJedisConnection
     */
    public StringShardedJedisConnection getStringShardedJedisConnection() {
        return stringShardedJedisConnection;
    }

    
    /**
     * @param stringShardedJedisConnection the stringShardedJedisConnection to set
     */
    public void setStringShardedJedisConnection(StringShardedJedisConnection stringShardedJedisConnection) {
        this.stringShardedJedisConnection = stringShardedJedisConnection;
    }

}
