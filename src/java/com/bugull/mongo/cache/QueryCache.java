package com.bugull.mongo.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class QueryCache {
    
    private static QueryCache instance = new QueryCache();
    
    private Map<String, List> cache;
    private Map<String, Long> times;
    
    private QueryCache(){
        cache = new ConcurrentHashMap<String, List>();
        times = new ConcurrentHashMap<String, Long>();
    }
    
    public static QueryCache getInstance(){
        return instance;
    }
    
    public boolean contains(Class<?> clazz, String key){
        key = clazz.getName() + "-" + key;
        return cache.containsKey(key);
    }
    
    public List get(Class<?> clazz, String key){
        key = clazz.getName() + "-" + key;
        return cache.get(key);
    }
    
    public void put(Class<?> clazz, String key, List value){
        key = clazz.getName() + "-" + key;
        cache.put(key, value);
        times.put(key, System.currentTimeMillis());
    }
    
    public void remove(String key){
        cache.remove(key);
        times.remove(key);
    }
    
    public Map<String, Long> getTimes(){
        return times;
    }
    
}
