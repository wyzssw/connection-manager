/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.utils;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.log4j.Logger;

/**
 * 类HttpClientUtils.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2013-7-1 下午04:39:45
 */
public class HttpClientUtils {
    
    private static final Logger LOGGER = Logger.getLogger(HttpClientUtils.class);
    
    public static String getContent(HttpClient httpClient,HttpUriRequest request){
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String content = "";
        try {
            content = httpClient.execute(request, responseHandler);
        } catch (ClientProtocolException e) {
            LOGGER.error(request.getURI().toString(), e);
            request.abort();
        } catch (IOException e) {
            LOGGER.error(request.getURI().toString(), e);
            request.abort();
        }
        return content;
    }

}
