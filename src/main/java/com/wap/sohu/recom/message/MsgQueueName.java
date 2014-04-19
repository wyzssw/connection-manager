package com.wap.sohu.recom.message;

/**
 * 类MsgQueueName.java的实现描述：TODO 类实现描述
 * @author yeyanchao 2013-1-7 下午4:40:43
 */
public enum MsgQueueName {

    LATLNG("latlng"),CELLID("cellid");

    private String msgQueueName;

    private MsgQueueName(String msgQueue){
        this.msgQueueName = msgQueue;
    }


    /**
     * @return the msgQueue
     */
    public String getMsgQueueName() {
        return msgQueueName;
    }

}
