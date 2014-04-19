package com.wap.sohu.recom.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wap.sohu.mobilepaper.model.pic.GroupPicInfo;

/**
 * 类ConvertUtils.java的实现描述：TODO 类实现描述
 *
 * @author hongfengwang 2012-7-25 下午01:57:10
 */
public class ConvertUtils {

    private static final Logger             LOGGER                = Logger.getLogger(ConvertUtils.class);

    private static final ObjectMapper       writeMapper           = new ObjectMapper();

    private static final int                QUEUE_SIZE            = 500;

    private static final AtomicInteger      counter               = new AtomicInteger();

    
    /**
     * jackson deserailized Mapper 缓冲 balance 队列
     */
    private static final List<ObjectMapper> readMapperBalanceList = new ArrayList<ObjectMapper>(QUEUE_SIZE);
    static {
        for (int i = 0; i < QUEUE_SIZE; i++) {
            readMapperBalanceList.add(new ObjectMapper());
        }
    }

    private static ObjectMapper rollMapper() {
        int index = counter.incrementAndGet() % QUEUE_SIZE;
        return readMapperBalanceList.get(index);
    }

    /**
     * bean filed中要生成对应的setter与getter方法
     *
     * @param object
     * @return
     */
    public static String toJsonJk(Object object) {
        String json = null;
        try {
            json = writeMapper.writeValueAsString(object);
        } catch (JsonGenerationException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (JsonMappingException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
        }
        return json;
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromJsonJkGeneric(String content, TypeReference<?> valueTypeRef) {
        Object result = null;
        ObjectMapper mapper = rollMapper();
        try {
            if (mapper != null) {
                result = mapper.readValue(content, valueTypeRef);
            }
        } catch (JsonParseException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (JsonMappingException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return (T) result;
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromJsonJk(String content, Class<T> valueType) {
        Object result = null;
        ObjectMapper mapper = rollMapper();
        try {
            if (mapper != null) {
                result = mapper.readValue(content, valueType);
            }
        } catch (JsonParseException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (JsonMappingException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return (T) result;
    }

    public static <T> Map<String, T> lowerMapKey(Map<String, T> map) {
        if (map == null || map.size() == 0) {
            return map;
        }
        Map<String, T> mapWrapper = new HashMap<String, T>();
        for (Map.Entry<String, T> entry : map.entrySet()) {
            mapWrapper.put(entry.getKey().toString().toLowerCase(), entry.getValue());
        }
        return mapWrapper;
    }

    public static Set<Integer> convert2intList(Set<String> set) {
        if (set == null) {
            return null;
        }
        Set<Integer> finalSet = new LinkedHashSet<Integer>();
        for (String str : set) {
            finalSet.add(Integer.parseInt(str));
        }
        return finalSet;
    }
    
    public static List<Integer> convertStringArray2IntegerList(String[] array){
        List<Integer> list = new ArrayList<Integer>();
        if (array==null) {
             return list;
        }
        for (String string : array) {
             list.add(Integer.parseInt(string));
        }   
        return list;
    }

    public static Map<Integer, Double> convertObjectMap(Map<Object, Object> map) {
        Map<Integer, Double> finalMap = new HashMap<Integer, Double>();
        if (map != null) {
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                String key = entry.getKey().toString();
                String value = entry.getValue().toString();
                finalMap.put(Integer.parseInt(key), Double.parseDouble(value));
            }
        }
        return finalMap;
    }

    public static Map<String, Double> convert2StrDoubleMap(Map<Object, Object> map) {
        Map<String, Double> finalMap = new HashMap<String, Double>();
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            finalMap.put(key, Double.parseDouble(value));
        }
        return finalMap;
    }

    /**
     * @param set
     * @return
     */
    public static Map<Integer, Double> convert2LinkedHashMap(Set<TypedTuple<String>> set) {
        if (set == null) {
            return null;
        }
        Map<Integer, Double> map = new LinkedHashMap<Integer, Double>();
        for (TypedTuple<String> typedTuple : set) {
            map.put(Integer.parseInt(typedTuple.getValue()), typedTuple.getScore());
        }
        return map;
    }

    
    public static Set<Integer> groupPicInfo2Set(List<GroupPicInfo> list){
        if (list == null) {
            list = new ArrayList<GroupPicInfo>();
        }
        Set<Integer> set = new HashSet<Integer>();
        for (GroupPicInfo groupPicInfo : list) {
            set.add(groupPicInfo.getId());
        }
        return set;
    }
    
    /**
     * @param set
     * @return
     */
    public static Map<String, Double> convert2LinkedHashMapStr(Set<TypedTuple<String>> set) {
        if (set == null) {
            return null;
        }
        Map<String, Double> map = new LinkedHashMap<String, Double>();
        for (TypedTuple<String> typedTuple : set) {
            map.put(typedTuple.getValue(), typedTuple.getScore());
        }
        return map;
    }

    public static String delHTMLTag(String htmlStr) {
        String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式
        String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式
        String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式
        String regEx_color = "\\&\\#\\d+;"; // 定义颜色标签

        Pattern p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        htmlStr = m_script.replaceAll(""); // 过滤script标签

        Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll(""); // 过滤style标签

        Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll(""); // 过滤html标签

        Pattern p_color = Pattern.compile(regEx_color, Pattern.CASE_INSENSITIVE);
        Matcher m_color = p_color.matcher(htmlStr);
        htmlStr = m_color.replaceAll(""); // 过滤html标签

        return htmlStr.trim(); // 返回文本字符串
    }
    
    public static Map<Integer, String> convertList2Map(List<Integer> list,String value){
        Map<Integer, String> map = new LinkedHashMap<Integer, String>();
        if (list==null||list.isEmpty()) {
            return map;
        }
        for (Integer integer : list) {
             map.put(integer, value);
        }
        return map;
           
    }
   
  
    
    public static <K,V> void   putList2Map(Map<K, V> map,List<K> list,V value){
        if (map==null||list==null) {
            return;
        }
        for (K k : list) {
            map.put(k, value);
        }
    }
    
  
}
