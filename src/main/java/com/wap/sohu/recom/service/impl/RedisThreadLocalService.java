/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.util.Hashing;

import com.wap.sohu.recom.core.redis.ShardedJedisConnectionFactory;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.core.redis.StringShardedJedisConnection;
import com.wap.sohu.recom.model.ShardedJedisWrapper;

/**
 * 类RedisThreadLocalService.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2012-12-25 下午02:06:59
 */
@Component
public class RedisThreadLocalService implements InitializingBean{
    private final static Map<Thread, ShardedJedisWrapper> mapUser = new ConcurrentHashMap<Thread, ShardedJedisWrapper>(50); 
    private final static Map<Thread, ShardedJedisWrapper> mapRecom = new ConcurrentHashMap<Thread, ShardedJedisWrapper>(50); 
    
    private final static CopyOnWriteArraySet<StringShardedJedisConnection> brokenClientSet = new CopyOnWriteArraySet<StringShardedJedisConnection>();
    
    private static final Integer liveTime = 300000;
    private static final Integer additionMaxTime = 120000;
    
    private static final Logger LOGGER = Logger.getLogger(RedisThreadLocalService.class);
    
    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateUser;
    
    @Autowired
    private StringRedisTemplateExt shardedRedisTemplateRecom;

    ThreadLocal<ShardedJedisWrapper> threadLocalUser = new ThreadLocal<ShardedJedisWrapper>();
    ThreadLocal<ShardedJedisWrapper> threadLocalRecom = new ThreadLocal<ShardedJedisWrapper>();
    
    private   List<JedisShardInfo> shardsUser = null;
    private   List<JedisShardInfo> shardsRecom = null;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        ShardedJedisConnectionFactory redisConnectionUser = (ShardedJedisConnectionFactory) shardedRedisTemplateUser.getConnectionFactory();
        shardsUser =  redisConnectionUser.getShards();
        ShardedJedisConnectionFactory redisConnectionRecom = (ShardedJedisConnectionFactory) shardedRedisTemplateRecom.getConnectionFactory();
        shardsRecom =  redisConnectionRecom.getShards();
    }
    
    @SuppressWarnings("unused")
    @PostConstruct
    private void init(){
        ScheduledExecutorService scheduledThread = Executors.newSingleThreadScheduledExecutor();
        scheduledThread.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    cleanBrokenClient();
                    expungeNouseJedis(mapUser,shardsUser);
                    expungeNouseJedis(mapRecom,shardsRecom);
                } catch (Exception e) {
                   LOGGER.error(e.getMessage(),e);
                }
           }
        }, 0, 90, TimeUnit.SECONDS);
    }
    
    /**
     * 清理过期与坏掉的连接，有间隔时间，避免与业务线程冲突
     */
    private void cleanBrokenClient(){
        Iterator<StringShardedJedisConnection> it = brokenClientSet.iterator();
        while (it.hasNext()) {
            StringShardedJedisConnection stringShardedJedisConnection = (StringShardedJedisConnection) it.next();
            //清理掉
            stringShardedJedisConnection.disconnectIgnoreEx();
            brokenClientSet.remove(stringShardedJedisConnection);
        }
    }
    
    /**
     * 及时关闭死掉线程所用连接，将过期连接放入brokenclientset，下次关闭并删掉
     * @param map
     * @param shards
     */
    private void expungeNouseJedis(Map<Thread, ShardedJedisWrapper> map, List<JedisShardInfo> shards) {
        
        for (Map.Entry<Thread, ShardedJedisWrapper> item : map.entrySet()) {            
            if (!item.getKey().isAlive()) {
                 item.getValue().getStringShardedJedisConnection().disconnectIgnoreEx();
                 item.setValue(new ShardedJedisWrapper(null, -1L));
            //没到10分钟就提前解决掉
            }else if (item.getValue().getTimeStamp()<System.currentTimeMillis()-(liveTime+RandomUtils.nextInt(additionMaxTime))) {
                 StringShardedJedisConnection shardedJedisConnection = item.getValue().getStringShardedJedisConnection();
                 item.getValue().setStringShardedJedisConnection(new StringShardedJedisConnection(new ShardedJedis(shards, Hashing.MURMUR_HASH),null));   
                 item.getValue().setTimeStamp(System.currentTimeMillis());
                 brokenClientSet.add(shardedJedisConnection);
            }
        }
        Iterator<Map.Entry<Thread, ShardedJedisWrapper>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Thread, ShardedJedisWrapper> entry = (Map.Entry<Thread, ShardedJedisWrapper>) iterator.next();
            if (entry.getValue().getTimeStamp() == -1) {
                iterator.remove();
            }
        }
    }
    
    /**
     * 拿到后马上用
     * @return
     */
    public StringShardedJedisConnection getShardedJedisUser(){
        return getShardedConnection("user");
    }
    
    /**
     * 拿到后马上用
     * @return
     */
    public StringShardedJedisConnection getShardedJedisRecom(){
        return getShardedConnection("recom");
    }
    
    private StringShardedJedisConnection getShardedConnection(String type){ 
        if (StringUtils.isBlank(type)) {
            return null;
        }
        ShardedJedisWrapper shardedJedisWrapper = null;
        boolean broken = false;
        if (type.equals("user")) {
            if ((shardedJedisWrapper=threadLocalUser.get())==null||(broken=shardedJedisWrapper.getStringShardedJedisConnection().isBroken())) {
                if (broken) {
                    brokenClientSet.add(shardedJedisWrapper.getStringShardedJedisConnection());
                }
                ShardedJedis shardedJedis = new ShardedJedis(shardsUser, Hashing.MURMUR_HASH);
                shardedJedisWrapper = new ShardedJedisWrapper(new StringShardedJedisConnection(shardedJedis, null), System.currentTimeMillis());
                //注册到map中，及时关闭连接
                mapUser.put(Thread.currentThread(),shardedJedisWrapper);
                threadLocalUser.set(shardedJedisWrapper);
            }
            return shardedJedisWrapper.getStringShardedJedisConnection();
        }
        else {
            if ((shardedJedisWrapper=threadLocalRecom.get())==null||(broken=shardedJedisWrapper.getStringShardedJedisConnection().isBroken())) {
                if (broken) {
                    brokenClientSet.add(shardedJedisWrapper.getStringShardedJedisConnection());
                }
                ShardedJedis shardedJedis = new ShardedJedis(shardsRecom, Hashing.MURMUR_HASH);
                shardedJedisWrapper = new ShardedJedisWrapper(new StringShardedJedisConnection(shardedJedis, null), System.currentTimeMillis());
                mapRecom.put(Thread.currentThread(),shardedJedisWrapper);
                threadLocalRecom.set(shardedJedisWrapper);
            }
            return shardedJedisWrapper.getStringShardedJedisConnection();
        }
    }
    

    
}
