/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service;

import java.util.Map;

/**
 * 调用微博远程服务得到tag列表
 * @author hongfengwang 2012-9-13 下午03:16:25
 */
public interface NewsRemoteService {

    /**
     * 远程过程调用获得tag
     * @param title
     * @param content
     * @return
     */
    Map<String, Double> getRmiTags(String title,String content);
    
    /**
     * 远程过程调用获得分类
     * @param title
     * @param content
     * @return
     */
    Map<String, Double> getRmiCat(String title,String content);
}
