package com.wap.sohu.recom.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class MapSortUtil {


	
	/** 根据MAP的VALUE，将MAP正排序，即VALUE越大位置越靠前，返回LIST为MAP的key
	 * @param <X> Map的key类型
	 * @param <T> Map的value类型
	 * @param map	待排序map
	 * @return 排好序的map的key的list
	 */
	public static <X extends Number, T extends Object> List<T> sortMapByValue (Map<T,X > map) {
		
		List<T> result = new ArrayList<T>();
		List<Map.Entry<T,X>> list = new ArrayList<Map.Entry<T,X>>();
		list.addAll(map.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<T, X>>() {

			@Override
			public int compare(Entry<T, X> o1,
					Entry<T, X> o2) {
				
				if (o1.getValue().doubleValue() <= o2.getValue().doubleValue())
					return 1;
				else
					return 0;
			}
			
		}
		);
		for (Map.Entry<T, X> entry : list) {
			result.add(entry.getKey());
		}
		return result;
	}

	public static <X extends Number> List<Integer> sortMapByValue2 (Map<Integer,X > map) {
		
		List<Integer> result = new ArrayList<Integer>();
		List<Map.Entry<Integer,X>> list = new ArrayList<Map.Entry<Integer,X>>();
		list.addAll(map.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<Integer, X>>() {

			@Override
			public int compare(Entry<Integer, X> o1,
					Entry<Integer, X> o2) {
				
				if (o1.getValue().doubleValue() <= o2.getValue().doubleValue())
					return 1;
				else
					return 0;
			}
			
		}
		);
		for (Map.Entry<Integer, X> entry : list) {
			result.add(entry.getKey());
		}
		return result;
	}
	
	

    public static <K extends Object> Map<K, Double> sumMap(Map<K, Double> map1, Map<K, Double> map2) {
        Map<K, Double> merged = new HashMap<K, Double>();
        merged.putAll(map1);
        for (Map.Entry<K, Double> entry : map2.entrySet()) {
            K key = entry.getKey();
            if (map1.containsKey(key)) {
                double sum = entry.getValue().doubleValue() + map1.get(key).doubleValue();
                merged.put(key, sum);
            } else {
                merged.put(key, entry.getValue());
            }
        }

        return merged;
    }
	
	
}
