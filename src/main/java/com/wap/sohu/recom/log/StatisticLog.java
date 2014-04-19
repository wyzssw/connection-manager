/**
 *
 */
package com.wap.sohu.recom.log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.wap.sohu.recom.log.AccessTraceLogData.LogCategoryEnum;
import com.wap.sohu.recom.log.AccessTraceLogData.LogKeyEnum;

/**
 * @author yeyanchao
 */
public class StatisticLog {

    public static boolean  isEnabled               = false;

    public static String   IS_STATISTIC_LOG_ENABLE = "statisticlog.isEnable";

    public static AsyncLog asyScribe               = new AsyncLog("localhost");

    static {
        String enabled = System.getProperty(IS_STATISTIC_LOG_ENABLE);
        if (StringUtils.isNotBlank(enabled)) {
            if (StringUtils.equalsIgnoreCase("true", StringUtils.trim(enabled))) {
                isEnabled = true;
            }
        }
    }

    public static void info(AccessTraceLogData logData) {
        if (!isEnabled) {
            return;
        }
        String msg = JSON.toJSONString(logData.getMessage());
        sendLog(logData.getCategory(), msg);
    }

    /**
     * 发送业务日志到Scribe服务器
     *
     * @param category
     * @param jso
     */
    private static void sendLog(LogCategoryEnum category, String message) {
        if (isEnabled) {
            asyScribe.log(category.getCategory(), message);
        }
    }

    static int threads = 130;

    /**
     * @param args
     */
    public static void main(String[] args) {

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            executor.execute(new Worker());
        }
        executor.shutdown();
    }
    private static class Worker implements Runnable {

        @Override
        public void run() {
            while (true) {
                info(getLogData());
                try {
                    Thread.sleep(8);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private static AccessTraceLogData getLogData() {
        AccessTraceLogData logData = new AccessTraceLogData(LogCategoryEnum.GroupRec);
        logData.add(LogKeyEnum.UserRec, 1);
        logData.add(LogKeyEnum.HotRec, 0);
        logData.add(LogKeyEnum.TagRec, 1);
        logData.add(LogKeyEnum.GroupCorrelation, 1);
        logData.add(LogKeyEnum.Total, 0);
        return logData;
    }
    

}
