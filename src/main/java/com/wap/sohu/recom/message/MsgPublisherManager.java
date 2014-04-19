package com.wap.sohu.recom.message;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * 消息队列管理类，消息队列都注册在这里
 *
 * @author hongfengwang 2012-7-25 下午03:13:33
 */
public class MsgPublisherManager {

    private static final Logger           LOGGER     = Logger.getLogger(MsgPublisherManager.class);

    private String                        host       = "";
    private int                           port       = 5672;
    private Set<MsgQueueName>             queueNameSet;

    private Connection                    connection = null;
    private volatile Map<MsgQueueName, Channel> channelMap = new ConcurrentHashMap<MsgQueueName, Channel>();
    private final ReentrantLock           lock       = new ReentrantLock(true);

    /**
     * 只在初始化和连接重连时使用
     */
    private void initMsgQueue() {
        lock.lock();
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            connection = factory.newConnection();
            for (MsgQueueName msgQueueName : queueNameSet) {
                Channel channel = connection.createChannel();
                channel.queueDeclare(msgQueueName.getMsgQueueName(), false, false, false, null);
                channelMap.put(msgQueueName, channel);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            lock.unlock();
        }

    }

    public void publishMsg(MsgQueueName queueName, String message) {
        if (channelMap.get(queueName) == null) {
            return;
        }
        Channel channel = channelMap.get(queueName);
        if (channel == null || !channel.isOpen()) {
            initMsgQueue();
        }
        try {
            channel.basicPublish("", queueName.getMsgQueueName(), null, message.getBytes());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the queueNameSet
     */
    public Set<MsgQueueName> getQueueNameSet() {
        return queueNameSet;
    }

    /**
     * @param queueNameSet the queueNameSet to set
     */
    public void setQueueNameSet(Set<MsgQueueName> queueNameSet) {
        this.queueNameSet = queueNameSet;
    }

}
