package com.wap.sohu.recom.service.data;

import java.util.HashMap;
import java.util.Map;

/**
 * 类AbstractChannelNewsData.java的实现描述：TODO 类实现描述
 * @author yeyanchao Sep 11, 2013 2:36:42 PM
 */
public class ChannelDataServiceRegister {

    private static Map<Integer,ChannelNewsDataService> registerMap = new HashMap<Integer,ChannelNewsDataService>();

    private static ChannelNewsDataService defaultService;

    public static void setRegisterMap(Map<Integer,ChannelNewsDataService> map){
        registerMap = map;
    }

    public static void registerService(Integer key,ChannelNewsDataService channelDataServiceRegister){
        registerMap.put(key, channelDataServiceRegister);
    }

    public static void setDefaultService(ChannelNewsDataService service){
        defaultService = service;
    }

    public static ChannelNewsDataService getChannelNewsDataService(Integer channelId){
        if(registerMap.containsKey(channelId)){
            return registerMap.get(channelId);
        }
        return null;
    }
}
