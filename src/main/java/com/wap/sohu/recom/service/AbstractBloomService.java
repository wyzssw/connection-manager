/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import redis.clients.jedis.Jedis;

import com.google.common.io.ByteStreams;
import com.wap.sohu.recom.core.redis.ShardsSeesionCallback;
import com.wap.sohu.recom.core.redis.StringRedisTemplateExt;
import com.wap.sohu.recom.model.BloomKeyBuild;
import com.wap.sohu.recom.service.impl.MChannelNewsBloomService;
import com.wap.sohu.recom.util.bloom.BloomFilter;

/**
 * 类AbstractBloomService.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2013-8-8 上午10:16:10
 */
public abstract class AbstractBloomService {
    
    @Autowired
    private NewsTagService newsTagService;
    
    
    private static final Logger LOGGER  = Logger.getLogger(MChannelNewsBloomService.class);
    protected ThreadLocal<SimpleDateFormat> simpleDateFormatLocal = new ThreadLocal<SimpleDateFormat>();
    
    protected abstract List<String> getDateList();
    protected abstract void removeDateListLocal();
    protected abstract BloomFilter makeBloomFilter();
    /**
     * @param cid
     * @param list
     * @param bmap
     */
    protected abstract boolean multiUpdate(long cid, List<Integer> list,
                                         Map<String, BloomFilter> bMap,BloomKeyBuild bloomKeyBuild);
  
    protected SimpleDateFormat getSimpleDateFormat() {
        SimpleDateFormat simpleDateFormat = simpleDateFormatLocal.get();
        if (simpleDateFormat==null) {
            simpleDateFormat = new SimpleDateFormat("yyMMdd");
            simpleDateFormatLocal.set(simpleDateFormat);
        }
        return simpleDateFormat;
    }
    
    protected void removeSimpleDateFormatLocal(){
        simpleDateFormatLocal.remove();
    }

    
    public  Map<String, BloomFilter> getUserAllBloom(long cid,StringRedisTemplateExt stringRedisTemplateExt,final String shardsKey,final String key){
        List<String> dateList = getDateList();
        assert dateList!=null;
        final List<byte[]> rmList = new ArrayList<byte[]>();
        Map<String, BloomFilter> map = new HashMap<String, BloomFilter>();
        //从redis中读hash 字节到本地
        Map<byte[],byte[]> result = stringRedisTemplateExt.executeForShards(new ShardsSeesionCallback<Map<byte[],byte[]>>() {
            @Override
            public Map<byte[],byte[]> execute(Jedis jedis) throws DataAccessException {
               return  jedis.hgetAll(key.getBytes());
            }
        }, shardsKey);
        
        //构建bloomFilter
        for (Map.Entry<byte[],byte[]> entry : result.entrySet()) {
             String field = new String(entry.getKey());
             if (!dateList.contains(field)) {
                rmList.add(entry.getKey());
                continue;
            }
           BloomFilter bloomFilter = new BloomFilter();
            try {
                bloomFilter.readFields(ByteStreams.newDataInput(entry.getValue()));
            } catch (IOException e) {
                LOGGER.error("bloomfilter read error,data is destroyed,which cid is" +cid+" and fiele is "+field);
                throw new RuntimeException("bloomfilter read error,data is destroyed,which cid is" +cid+" and fiele is "+field, e);
            }
           map.put(field, bloomFilter);
        }
        
        //删除用户过期历史记录
        stringRedisTemplateExt.executeForShards(new ShardsSeesionCallback<Long>() {
            @Override
            public Long execute(Jedis jedis) throws DataAccessException {
                Long count = 0L;
                for (byte[] bs : rmList) {
                    count += jedis.hdel(key.getBytes(), bs);
                }
                return count;
            }
            
        }, shardsKey);
        return map;
    }
    

    public boolean checkHasRecom(Integer newsId, Map<String, BloomFilter> map){
            List<String> dateList = getDateList();
            assert dateList!=null;
            Long time = newsTagService.getNewsTime(newsId);
            //每次都创建一次SimpleDateFormat性能太差
//            String field = DateFormatUtils.format(1000*time, "yyMMdd");
            String field = getSimpleDateFormat().format(new Date(time*1000));
            if (!dateList.contains(field)) {
                LOGGER.warn(field+" newsId is "+newsId);
                return true;
            }
            BloomFilter bloomFilter = null;
            if ((bloomFilter=map.get(field))!=null) {
                return bloomFilter.membershipTest(String.valueOf(newsId).getBytes());
            }
            return false;
    }
    
    /**
     * 
     * @param newsId
     * @return true表示过期，false表示没有过期
     */
    public boolean checkNewsTime(Integer newsId){
          return false;
    }

    public  void addUserBloom(long cid, Integer newsId, Map<String, BloomFilter> map){
        Long time = newsTagService.getNewsTime(newsId);
        String field = DateFormatUtils.format(1000*time, "yyMMdd");
        BloomFilter bloomFilter = null;
        if ((bloomFilter=map.get(field))!=null) {
            if (!bloomFilter.membershipTest(String.valueOf(newsId).getBytes())) {
                 bloomFilter.add(String.valueOf(newsId).getBytes());
            }
        }else {
            bloomFilter = makeBloomFilter();
            bloomFilter.add(String.valueOf(newsId).getBytes());
            map.put(field, bloomFilter);
        }
    }
    



}
