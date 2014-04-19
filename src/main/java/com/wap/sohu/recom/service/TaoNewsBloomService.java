/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service;

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
import com.wap.sohu.recom.constants.TaoRecomRedisKeyConstants;
import com.wap.sohu.recom.core.redis.ShardsSeesionCallback;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.model.BloomKeyBuild;
import com.wap.sohu.recom.util.bloom.BloomFilter;
import com.wap.sohu.recom.utils.DateUtil;

/**
 * 类TaoNewsBloomService.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2013-8-8 上午10:11:31
 */
@Service
public class TaoNewsBloomService extends AbstractBloomService{
    
    private static final Logger LOGGER  = Logger.getLogger(TaoNewsBloomService.class);
    private int vectorSize=40000, nbHash=4, hashType=1;
    
    @Autowired
    private StringRedisTemplateExt stringRedisTemplateToutiao;
    
    @Autowired
    private NewsFilterService newsFilterService;
    
    
    private ThreadLocal<List<String>> dateListLocal = new ThreadLocal<List<String>>();
    
    
    
    public  Map<String, BloomFilter> getUserAllBloom(long cid){
        final String shardsKey = String.format(TaoRecomRedisKeyConstants.TAO_USER, cid);
        final String key = String.format(TaoRecomRedisKeyConstants.TAO_USER_HASH, cid);
        return  super.getUserAllBloom(cid, stringRedisTemplateToutiao, shardsKey, key);
    }

    @Override
    public boolean multiUpdate(final long cid, final List<Integer> list,final Map<String, BloomFilter> bMap,BloomKeyBuild bloomKeyBuild) {
        final String userKey = String.format(TaoRecomRedisKeyConstants.TAO_USER_ZSET, cid);
        final String blKey   = String.format(TaoRecomRedisKeyConstants.TAO_USER_HASH, cid);
        final String lockKey   = String.format(TaoRecomRedisKeyConstants.TAO_USER_LOCK, cid);
        String key = String.format(TaoRecomRedisKeyConstants.TAO_USER, cid);
        //精确到毫秒！
        final Double expireTime = Double.valueOf(DateUtil.getUnixTime(-CommonConstants.TAONEWS_UPDATE_DAYS+1, TimeUnit.DAYS))*1000;
        final Integer keyTimeout = Long.valueOf(TimeUnit.DAYS.toSeconds(CommonConstants.TAONEWS_UPDATE_DAYS+1)).intValue();
        final long time = System.currentTimeMillis();
        boolean result =   stringRedisTemplateToutiao.executeForShards(new ShardsSeesionCallback<Boolean>() {
         @Override
         public Boolean execute(Jedis jedis) throws DataAccessException {
             jedis.watch(userKey,blKey);
             jedis.multi();
             Transaction transaction = new Transaction(jedis.getClient());
             transaction.zremrangeByScore(userKey, 0, expireTime);
             Double incrment = 0D;
             for (Integer newsId: list) {
                 incrment = incrment +0.01;
                 transaction.zadd(userKey, time+incrment,String.valueOf(newsId));
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
                 transaction.hset(blKey.getBytes(), entry.getKey().getBytes(),byteArrayDataOutput.toByteArray());
             }
             transaction.expire(userKey,keyTimeout);
             transaction.expire(blKey,keyTimeout);
             transaction.expire(lockKey,keyTimeout);
             Object object = transaction.exec();
             return object==null?false:true;
         }
       }, key);
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
    
    
    /**
     * 
     * @param newsId
     * @return true表示过期，false表示没有过期
     */
    public boolean checkNewsTime(Integer newsId){
           return newsFilterService.filterCatNewsByTime(newsId, 12, TimeUnit.HOURS);
    }


}
