package com.wap.sohu.recom.cache.core;

import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/**
 * 
 * @author ruikunh
 * 2010-12-07
 * @param <K>
 * @param <V>
 */
public class ClientCache<K, V> implements ICache<K, V> {

	Cache cache;
	
	public ClientCache(Cache cache) {
		this.cache = cache;
	}

	@Override
	public V get(K k) {
		Element el = cache.get(k);
		if(el!=null){
			return (V) el.getValue();
		}
		return null;
	}

	@Override
	public void put(K k, V v) {
		cache.put(new Element(k, v));
	}

	@Override
	public void remove(K k) {
		cache.remove(k);
	}

	@Override
	public void removeAll() {
		cache.removeAll();
	}

	@Override
	public List<K> getKeys() {
		return cache.getKeys();
	}

}
