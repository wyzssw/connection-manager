package com.wap.sohu.recom.cache.core;

import java.util.List;

/**
 * 
 * @author ruikunh
 * 2010-12-07
 * @param <K>
 * @param <V>
 */
public interface ICache<K, V> {

	public void put(K k, V v);
	
	public V get(K k);
	
	public void remove(K k);
	
	public void removeAll();
	
	public List<K> getKeys();
	
}
