/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.model;


/**
 * 类BloomKeyBuild.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2013-9-12 下午03:06:57
 */
public class BloomKeyBuild {

    private String newsZsetKey;
    private String bloomHashKey;
    private String lockKey;
    private String shardKey;
    
    public BloomKeyBuild(String newsZsetKey,String bloomHashKey,String lockKey,String shardKey){
        this.newsZsetKey = newsZsetKey;
        this.bloomHashKey = bloomHashKey;
        this.lockKey = lockKey;
        this.shardKey = shardKey;
    }
    
    public BloomKeyBuild(){
        
    }
    
    /**
     * @return the newsZsetKey
     */
    public String getNewsZsetKey() {
        return newsZsetKey;
    }

    
    /**
     * @return the bloomHashKey
     */
    public String getBloomHashKey() {
        return bloomHashKey;
    }

    
    /**
     * @return the lockKey
     */
    public String getLockKey() {
        return lockKey;
    }

    
    /**
     * @return the shardKey
     */
    public String getShardKey() {
        return shardKey;
    }
    

}
