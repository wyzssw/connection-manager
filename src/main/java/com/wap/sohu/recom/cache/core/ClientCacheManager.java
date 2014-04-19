package com.wap.sohu.recom.cache.core;

import net.sf.ehcache.Cache;

/**
 * 
 * @author ruikunh 2010-12-07
 */
@SuppressWarnings("unchecked")
public class ClientCacheManager extends AbstractCacheManager {

	private static ClientCacheManager instance = new ClientCacheManager();

	public static ClientCacheManager getInstance() {
		return instance;
	}

	@Override
	public ICache getCache(String cacheName) {
		return getCache(cacheName, 200, 10 * 60);
	}

	@Override
	public ICache getCache(String cacheName, int maxSize, long leftTime) {
		ICache myCache = cacheMap.get(cacheName);
		if (myCache == null) {
			Cache cache = manager.getCache(cacheName);
			if (cache == null) {
				cache = new Cache(cacheName, maxSize, false, false, leftTime, leftTime);
				manager.addCache(cache);
			}
			myCache = new ClientCache(cache);
			cacheMap.put(cacheName, myCache);
		}
		return myCache;
	}

	/**
	 * net.sf.ehcache.Cache.Cache(String name, int maxElementsInMemory, boolean
	 * overflowToDisk, boolean eternal, long timeToLiveSeconds, long
	 * timeToIdleSeconds)
	 * 
	 * 
	 * 这两个参数很容易误解，看文档根本没用，我仔细分析了ehcache的代码。结论如下：
	 * 1、timeToLiveSeconds的定义是：以创建时间为基准开始计算的超时时长；
	 * 2、timeToIdleSeconds的定义是：在创建时间和最近访问时间中取出离现在最近的时间作为基准计算的超时时长；
	 * 3、如果仅设置了timeToLiveSeconds，则该对象的超时时间=创建时间+timeToLiveSeconds，假设为A；
	 * 4、如果没设置timeToLiveSeconds
	 * ，则该对象的超时时间=min(创建时间，最近访问时间)+timeToIdleSeconds，假设为B；
	 * 5、如果两者都设置了，则取出A、B最少的值，即min(A,B)，表示只要有一个超时成立即算超时。
	 * 为了更好理解，可直接查看代码。摘自：net.sf.ehcache.Element.java(版本1.2.4)：
	 */

}
