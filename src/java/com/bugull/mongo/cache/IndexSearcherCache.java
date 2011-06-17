package com.bugull.mongo.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class IndexSearcherCache {
    
    private final static Logger logger = Logger.getLogger(IndexSearcherCache.class);
    
    private static IndexSearcherCache instance;
    
    private Map<String, IndexSearcher> cache;
    private Map<String, Long> lastOpen;
    private Map<String, Boolean> openning;
    
    private IndexSearcherCache(){
        cache = new ConcurrentHashMap<String, IndexSearcher>();
        lastOpen = new ConcurrentHashMap<String, Long>();
        openning = new ConcurrentHashMap<String, Boolean>();
    }
    
    public static IndexSearcherCache getInstance(){
        if(instance == null){
            instance = new IndexSearcherCache();
        }
        return instance;
    }
    
    public IndexSearcher get(String name){
        IndexSearcher searcher = null;
        if(cache.containsKey(name)){
            searcher = cache.get(name);
        }else{
            IndexWriter writer = IndexWriterCache.getInstance().get(name);
            IndexReader reader = null;
            try{
                reader = IndexReader.open(writer, true);
            }catch(Exception e){
                logger.error(e.getMessage());
            }
            searcher = new IndexSearcher(reader);
            cache.put(name, searcher);
        }
        return searcher;
    }
    
    public Map<String, IndexSearcher> getAll(){
        return cache;
    }
    
    public void put(String name, IndexSearcher searcher){
        cache.put(name, searcher);
    }
    
    public Long getLastOpen(String name){
        if(lastOpen.containsKey(name)){
            return lastOpen.get(name);
        }else{
            return 0L;
        }
    }
    
    public void putLastOpen(String name, Long time){
        lastOpen.put(name, time);
    }
    
    public Boolean isOpenning(String name){
        if(openning.containsKey(name)){
            return openning.get(name);
        }else{
            return Boolean.FALSE;
        }
    }
    
    public void putOpenning(String name, Boolean value){
        openning.put(name, value);
    }
    
}
