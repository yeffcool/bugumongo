package com.bugull.mongo.cache;

import com.bugull.mongo.lucene.directory.IndexFile;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class IndexFileCache {
    
    private static IndexFileCache instance = new IndexFileCache();
    
    private Map<String, IndexFile> cache;
    
    private IndexFileCache(){
        cache = new ConcurrentHashMap<String, IndexFile>();
    }
    
    public static IndexFileCache getInstance(){
        return instance;
    }
    
    public IndexFile get(String dirname, String filename){
        IndexFile file = null;
        String name = dirname + ":" + filename;
        if(cache.containsKey(name)){
            file = cache.get(name);
        }else{
            file = new IndexFile(dirname, filename);
            cache.put(name, file);
        }
        return file;
    }
    
}
