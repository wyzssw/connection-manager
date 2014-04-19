/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wap.sohu.recom.dao.UserDao;
import com.wap.sohu.recom.utils.HttpClientUtils;

/**
 * 类UserService.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2013-7-1 下午05:20:15
 */
@Service
public class UserService {
    private static final String RESPONSE_TAG        = "response";
    private static final String DOCS_TAG            = "docs";
    private static final String PASSPORT_ID_TAG     = "passport_id";
    private static final String NUMFOUND_TAG        = "numFound";
    private static final String CHANNELS_TAG        = "channels";
    private String[]  host ;
    private Integer port ;

    @Autowired
    private UserDao userDao;
    
    
    @Autowired
    private PropertyProxy propertyProxy;
    

    @Autowired
    private HttpClient httpClient;
    
    @SuppressWarnings("unused")
    @PostConstruct
    private void init(){
        host = StringUtils.split(propertyProxy.getProperty("user_recom_search_host"),",");
        port = Integer.valueOf(propertyProxy.getProperty("user_recom_search_port"));
    }
  

    
    /**
     * @param pid
     * @return
     */
    public String getUserLike(long pid) {
        URIBuilder uriBuilder = getBaseURIBuilder();
        uriBuilder.setPath("/solr/csv/select");
        uriBuilder.addParameter("q", "passport_id:"+String.valueOf(pid));
        uriBuilder.setParameter("sort", "indextime desc");
        HttpGet httpGet = new HttpGet(uriBuilder.toString());
        String json = HttpClientUtils.getContent(httpClient,httpGet);
        String channels = parseJsonKey(json,CHANNELS_TAG);
        if (StringUtils.isBlank(channels)) {
            return "";
        }
        String[] likes = StringUtils.split(channels, ",");
        return likes[0];
    }
    
    /**
     * @param like
     * @return
     */
    public List<Long> getRecomList(String like,int hasrecom,int pgnum) {
        URIBuilder uriBuilder = getBaseURIBuilder();
        uriBuilder.addParameter("q", StringUtils.isNotBlank(like)?"channels:" + like:"*:*");
        uriBuilder.addParameter("fl", "passport_id").addParameter("start", String.valueOf(hasrecom)).addParameter("rows",
                                                                                                                  String.valueOf(pgnum));
        HttpGet httpGet = new HttpGet(uriBuilder.toString());
        String json = HttpClientUtils.getContent(httpClient,httpGet);
        List<Long> recomList = parseJsonList(json);
        return recomList;
    }

    /**
     * @return
     */
    private URIBuilder getBaseURIBuilder() {
        Integer index = new Random().nextInt(2);
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setHost(host[index]).setPort(port).setScheme(HttpHost.DEFAULT_SCHEME_NAME).setPath("/solr/comment/select").addParameter("sort",
                                                                                                                                    "comment_count desc").addParameter("wt",
                                                                                                                                                                       "json");
        return uriBuilder;
    }
    
    /**
     * @param json
     * @return
     */
    private List<Long> parseJsonList(String json) {
        List<Long> list = new ArrayList<Long>();
        JSONObject jsonObject =  JSON.parseObject(json);
        JSONObject responseObject = jsonObject.getJSONObject(RESPONSE_TAG);
        JSONArray jsonArray   = responseObject.getJSONArray(DOCS_TAG);
        for (Object object : jsonArray) {
            Long   pid = (Long)((JSONObject)object).getLongValue(PASSPORT_ID_TAG);
            list.add(pid);
        }
        return list;
    }
    
    private String parseJsonKey(String json,String key){
        if (StringUtils.isBlank(json)) {
            return "";
        }
        JSONObject jsonObject =  JSON.parseObject(json);
        JSONObject responseJson = jsonObject.getJSONObject(RESPONSE_TAG);
        String num = responseJson.getString(NUMFOUND_TAG);
        if ("0".equals(num)) {
            return "";
        }    
        JSONArray jsonArray   = responseJson.getJSONArray(DOCS_TAG);
        JSONObject single     = (JSONObject)jsonArray.get(0);
        String     channels   = single.getString(key);
        if (StringUtils.isBlank(channels)||"NULL".equals(channels)) {
            return "";
        }
        return channels;
    }
    

    /**过滤recomList中已经成为当前pid好友的pid
     * @param pid
     * @param recomList
     * 注意：recomList的个数不要太多
     */
    public List<Long> getFriendsInList(long pid, List<Long> recomList) {
        return  userDao.getFriends(pid, recomList);
    }

    
    /**
     * @param pid
     * @param recompid
     * @return
     */
    public boolean checkFriend(long pid, Long recompid) {
        return userDao.checkFriend(pid,recompid);
    }
    
    
}
