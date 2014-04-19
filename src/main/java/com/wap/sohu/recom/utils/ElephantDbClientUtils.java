/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.wap.sohu.recom.utils;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.apache.thrift7.protocol.TBinaryProtocol;
import org.apache.thrift7.transport.TFramedTransport;
import org.apache.thrift7.transport.TSocket;
import org.apache.thrift7.transport.TTransportException;

import com.wap.sohu.recom.elephantdb.ElephantDB;

/**
 * Edb client
 * User: qinqd
 * Date: 11-7-15
 * Time: 下午2:04
 * To change this template use File | Settings | File Templates.
 */
/**
 * 做测试用
 *
 * @author hongfengwang 2012-11-23 下午04:49:06
 */
public class ElephantDbClientUtils {

    private String            hostname;
    private String            host = "10.13.80.148";
    private int               port = 3578;

    private ElephantDB.Client client;
    private TFramedTransport  transport;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void initScribe() {
        try {
            synchronized (this) {
                // Thrift boilerplate code
                TSocket sock = new TSocket(new Socket(host, port));
                transport = new TFramedTransport(sock);
                TBinaryProtocol protocol = new TBinaryProtocol(transport, false, false);
                client = new ElephantDB.Client(protocol, protocol);
            }
        } catch (TTransportException e) {
            e.printStackTrace();
            System.err.println(e);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.err.println(e);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e);
        }
    }

    /*
     * Appends a log message to Scribe
     */

    public String get(String key,String key2) {
        connect();
        try {
            // System.out.println(client.getDomains());
            byte[] b = client.get(key2, ByteBuffer.wrap(key.getBytes())).get_data();
            if (b==null) {
                System.out.println(key2 +" is empty");
            }
//            byte[] c = client.get("long_news", ByteBuffer.wrap(key2.getBytes())).get_data();
//            System.out.println(client.getInputProtocol().getTransport().isOpen());
            // System.out.println("==\t    " + new String(b));
            return new String(b);
        } catch (TTransportException e) {
            transport.close();
        } catch (Exception e) {
            System.err.println(e);
        }
        return null;
    }

    // hadoop-0.20 fs -put my-edb-domain/1326308250568.version data/my-edb-domain/
    // hadoop-0.20 fs -put my-edb-domain/1326308250568 data/my-edb-domain/

    /*
     * Connect to scribe if not open, reconnect if failed.
     */
    public void connect() {
        if (transport != null && transport.isOpen()) return;

        if (transport != null && transport.isOpen() == false) {
            transport.close();

        }
        initScribe();
    }

    public void close() {
        if (transport != null && transport.isOpen()) {
            transport.close();
        }
    }

    public static void main(String[] args) {
//        EDBClient c = new EDBClient();
//        c.get("5677265789300772895");
        ElephantDbClientUtils eClientUtils = new ElephantDbClientUtils();

//        System.out.println(eClientUtils.get("5759194532658810903","gateway"));
      System.out.println(eClientUtils.get("24866851","gateway"));

//        System.out.println(eClientUtils.get("24866851","cat_sub"));
//        System.out.println(eClientUtils.get("5688548379416596503","cat_sub"));
//        System.out.println(eClientUtils.get("40601","sim_news"));
//        System.out.println(eClientUtils.get("45572","short_news"));
//        System.out.println(eClientUtils.get("59124","short_news"));
//        Map<Integer, Double> map = JSON.parseObject(eClientUtils.get("40601","sim_group"), new TypeReference<LinkedHashMap<Integer, Double>>(){});
//        System.out.println(map);
    }
}
