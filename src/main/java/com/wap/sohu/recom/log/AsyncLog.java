package com.wap.sohu.recom.log;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.scribe.LogEntry;
import org.json.JSONObject;

import com.twitter.common.logging.ScribeLog;


/**
 * 类AsyncLog.java的实现描述：异步日志发送
 *
 * @author yeyanchao 2012-8-22 下午7:32:14
 */
public class AsyncLog {

    private int                maxThreads      = 20;

    private int                blockQueueSize  = 5000;

    ExecutorService            executorService = Executors.newFixedThreadPool(maxThreads);

    private String             url             = null;

    private int                port            = 1464;

    private BlockingQueue<Log> blockingQueue   = null;

    private static class Log {

        String     scribe_category;
        String     message;

        public Log(String scribe_category, String message){
            super();
            this.scribe_category = scribe_category;
            this.message = message;
        }

    }

    public AsyncLog(int maxThreads, int blockQueueSize, String url){
        this.maxThreads = maxThreads;
        this.blockQueueSize = blockQueueSize;
        this.url = url;
        blockingQueue = new LinkedBlockingQueue<Log>(blockQueueSize);
        start();
    }

    public AsyncLog(int maxThreads, int blockQueueSize){
        this(maxThreads, blockQueueSize, "localhost");
    }

    public AsyncLog(String url){
        this(20, 5000, url);
    }

    private void start() {
        for (int i = 0; i < maxThreads; i++) {
            executorService.execute(new Worker());
        }
        executorService.shutdown();
    }

    public void log(String scribe_category, String message) {
        blockingQueue.offer(new Log(scribe_category, message));
    }

    private class Worker implements Runnable {

        private final ScribeLog scribe = new ScribeLog((Arrays.asList(new InetSocketAddress(url, port))));

        @Override
        public void run() {
            try {
                while (true) {
                    Log log = blockingQueue.take();
                    if (log != null) {
                        // 发送日志
                        scribe.log(new LogEntry(log.scribe_category, log.message+"\n"));
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                // 线程退出后，重启一个新线程
                executorService.execute(new Worker());
            }
        }
    }

}
