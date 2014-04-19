package com.wap.sohu.recom.service.data;


import java.util.Map;

import com.wap.sohu.recom.model.FilterBuild;

/**
 * 类ChannelNewsDataService.java的实现描述 : query data
 * @author yeyanchao Sep 11, 2013 2:06:07 PM
 */
public interface ChannelNewsDataService {

    public Map<Integer,String> queryRecomData(long cid,long pid,int count, FilterBuild filterbuild);

}
