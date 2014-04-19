/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.wap.sohu.recom.constants.CommonConstants;
import com.wap.sohu.recom.constants.TopNewsRedisKeyConstants;
import com.wap.sohu.recom.core.redis.ShardsSeesionCallback;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.model.BloomKeyBuild;
import com.wap.sohu.recom.service.AbstractBloomService;
import com.wap.sohu.recom.util.bloom.BloomFilter;
import com.wap.sohu.recom.utils.DateUtil;

/**
 * 和TopNewsBloomService.java大部分一样的，以后再抽取公共的 
 * @author hongfengwang 2013-7-10 下午06:09:55
 */
@Service
public class MChannelNewsBloomService extends AbstractBloomService{
    
    private int vectorSize=10000, nbHash=4, hashType=1;
    @Autowired
    private StringRedisTemplateExt stringRedisTemplateMChannel;
    
    
    private ThreadLocal<List<String>> dateListLocal = new ThreadLocal<List<String>>();
    
    private static final Logger LOGGER  = Logger.getLogger(MChannelNewsBloomService.class);
    
    
    /**
     * @param cid
     * @return
     */
    public Map<String, BloomFilter> getUserAllBloom(long cid,int channelId) {
        final String shardsKey = String.format(TopNewsRedisKeyConstants.MCHANNEL_USER, cid);
        if (channelId>1) {
            final String key = String.format(TopNewsRedisKeyConstants.CHANNEL_OTHER_USER_HASH, cid,channelId);
            return  super.getUserAllBloom(cid, stringRedisTemplateMChannel, shardsKey, key);
        }else {
            final String key = String.format(TopNewsRedisKeyConstants.MCHANNEL_USER_HASH, cid);
            return  super.getUserAllBloom(cid, stringRedisTemplateMChannel, shardsKey, key);
        }
        
    }
    
    /**
     * @return
     */
    private List<String> getOriginalDateList() {
        List<String> list = new ArrayList<String>();
        String field1 = DateFormatUtils.format(DateUtil.getUnixTime(0, TimeUnit.DAYS)*1000, "yyMMdd");
        String field2 = DateFormatUtils.format(DateUtil.getUnixTime(-1, TimeUnit.DAYS)*1000, "yyMMdd");
        String field3 = DateFormatUtils.format(DateUtil.getUnixTime(-2, TimeUnit.DAYS)*1000, "yyMMdd");
        list.add(field1);
        list.add(field2);
        list.add(field3);
        return list;
    }
    
    public BloomKeyBuild getBloomKeyBuild(long cid, int channelId) {
        if (channelId > 1) {
            String newsZsetKey = String.format(TopNewsRedisKeyConstants.CHANNEL_OTHER_USER_ZSET, cid, channelId);
            String bloomHashKey = String.format(TopNewsRedisKeyConstants.CHANNEL_OTHER_USER_HASH, cid, channelId);
            String lockKey = String.format(TopNewsRedisKeyConstants.MCHANNEL_USER_LOCK, cid, channelId);
            String shardKey = String.format(TopNewsRedisKeyConstants.MCHANNEL_USER, cid);
            return new BloomKeyBuild(newsZsetKey, bloomHashKey, lockKey, shardKey);
        } else {
            String newsZsetKey = String.format(TopNewsRedisKeyConstants.MCHANNEL_USER_ZSET, cid);
            String bloomHashKey = String.format(TopNewsRedisKeyConstants.MCHANNEL_USER_HASH, cid);
            String lockKey = String.format(TopNewsRedisKeyConstants.MCHANNEL_USER_LOCK, cid);
            String shardKey = String.format(TopNewsRedisKeyConstants.MCHANNEL_USER, cid);
            return new BloomKeyBuild(newsZsetKey, bloomHashKey, lockKey, shardKey);
        }
    }
    
    
    /**
     * @param cid
     * @param key
     * @param map
     */
    @Override
    public boolean multiUpdate(final long cid,final List<Integer> list,final Map<String, BloomFilter> bMap,final BloomKeyBuild bloomKeyBuild) {
       
       //精确到毫秒！
       final Double expireTime = Double.valueOf(DateUtil.getUnixTime(-CommonConstants.MCHANNEL_UPDATE_DAYS+1, TimeUnit.DAYS))*1000;
       final Integer keyTimeout = Long.valueOf(TimeUnit.DAYS.toSeconds(CommonConstants.MCHANNEL_UPDATE_DAYS+1)).intValue();
       final long time = System.currentTimeMillis();
       boolean result =   stringRedisTemplateMChannel.executeForShards(new ShardsSeesionCallback<Boolean>() {
        @Override
        public Boolean execute(Jedis jedis) throws DataAccessException {
            jedis.watch(bloomKeyBuild.getNewsZsetKey(),bloomKeyBuild.getBloomHashKey());
            jedis.multi();
            Transaction transaction = new Transaction(jedis.getClient());
            transaction.zremrangeByScore(bloomKeyBuild.getNewsZsetKey(), 0, expireTime);
            Double incrment = 0D;
            for (Integer newsId: list) {
                incrment = incrment +0.01;
                transaction.zadd(bloomKeyBuild.getNewsZsetKey(), time+incrment,String.valueOf(newsId));
            }
            for (Map.Entry<String, BloomFilter> entry : bMap.entrySet()) {
                ByteArrayDataOutput byteArrayDataOutput =  ByteStreams.newDataOutput();
                 try {
                    entry.getValue().write(byteArrayDataOutput);
                } catch (IOException e) {
                    LOGGER.error("write error which cid is "+cid);
                    transaction.discard();
                    return false;
                }
                transaction.hset(bloomKeyBuild.getBloomHashKey().getBytes(), entry.getKey().getBytes(),byteArrayDataOutput.toByteArray());
            }
            transaction.expire(bloomKeyBuild.getNewsZsetKey(),keyTimeout);
            transaction.expire(bloomKeyBuild.getBloomHashKey(),keyTimeout);
            transaction.expire(bloomKeyBuild.getLockKey(),keyTimeout);
            Object object = transaction.exec();
            return object==null?false:true;
        }
      }, bloomKeyBuild.getShardKey());
      return result;
    }
    
    @Override
    public void removeDateListLocal(){
        dateListLocal.remove();
    }

    
    @Override
    public BloomFilter makeBloomFilter() {
        return  new BloomFilter(vectorSize,nbHash,hashType);
    }


    @Override
    protected List<String> getDateList() {
        List<String> dateList = dateListLocal.get();
        if (dateList==null) {
            dateList = getOriginalDateList();
            dateListLocal.set(dateList);
        }
        return dateList;
    }
 

}
