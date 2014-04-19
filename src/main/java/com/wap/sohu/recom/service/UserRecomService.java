/*
 * Copyright 2013 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 类UserRecomService.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2013-7-1 下午03:21:19
 */
@Service
public class UserRecomService {
    
    
    
    @Autowired
    private UserService userService;
   
    /**
     * @param pid passport_id
     * @param pgsize 第几页
     * @param pgnum  一页显示多少个
     * @return
     */
    public List<Long> getRecomUser(long pid, int pgnum, int pgsize) {
        if (pgnum<1) {
            return null;
        }
        int hasRecom = (pgnum-1)*pgsize;
        String like = userService.getUserLike(pid);
        List<Long> recomList = userService.getRecomList(like,hasRecom,pgsize);
        List<Long> rmList = userService.getFriendsInList(pid,recomList);
        recomList.removeAll(rmList);
        return recomList;
    }
}
