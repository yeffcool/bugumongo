package com.bugull.mongo.cache;

import com.bugull.mongo.lucene.BuguIndex;
import com.bugull.mongo.lucene.directory.DirectoryFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class IndexWriterCache {
    
    private final static Logger logger = Logger.getLogger(IndexWriterCache.class);
    
    private static IndexWriterCache instance;
    
    private Map<String, IndexWriter> cache;
    private Map<String, Long> lastChange;
    
    private IndexWriterCache(){
        cache = new ConcurrentHashMap<String, IndexWriter>();
        lastChange = new ConcurrentHashMap<String, Long>();
    }
    
    public static IndexWriterCache getInstance(){
        if(instance == null){
            instance = new IndexWriterCache();
        }
        return instance;
    }
    
    public IndexWriter get(String name){
        IndexWriter writer = null;
        if(cache.containsKey(name)){
            writer = cache.get(name);
        }else{
            BuguIndex index = BuguIndex.getInstance();
            Directory dir = DirectoryFactory.create(index.getDirectoryType(), index.getDirectoryPath(), name);
            IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_32, index.getAnalyzer());
            try{
                writer = new IndexWriter(dir, conf);
            }catch(Exception e){
                logger.error(e.getMessage());
            }
            cache.put(name, writer);
        }
        return writer;
    }
    
    public Long getLastChange(String name){
        if(lastChange.containsKey(name)){
            return lastChange.get(name);
        }else{
            return 0L;
        }
    }
    
    public void putLastChange(String name, Long time){
        lastChange.put(name, time);
    }
    
}
