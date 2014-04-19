package com.wap.sohu.recom.utils;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * 类ThreadLocalUtils.java的实现描述：线程局部变量工具类
 * @author yeyanchao 2013-1-16 上午11:33:18
 */
public class ThreadLocalUtils {

    private static final Set<ThreadLocal<?>> threadLocalIndexes = new HashSet<ThreadLocal<?>>();


    private static final ThreadLocal<Random> randomLocal = new ThreadLocal<Random>(){

        @Override
        protected Random initialValue() {
            return new Random();
        }

    };

    static{
        threadLocalIndexes.add(randomLocal);
    }

    public static Random getLocalRandom(){
        return randomLocal.get();
    }

    public static void removeLocalRandom() {
        randomLocal.remove();
    }

    /**
     *  清理当前线程所有local缓存
     */
    public static void removeAllLocal(){
        for (ThreadLocal<?> local : threadLocalIndexes) {
            local.remove();
        }
    }

}
